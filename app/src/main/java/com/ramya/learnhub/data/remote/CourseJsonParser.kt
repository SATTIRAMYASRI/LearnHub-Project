package com.ramya.learnhub.data.remote

import com.google.gson.JsonElement
import com.google.gson.JsonParser

class MalformedDataException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Parses the courses payload defensively: bad entries are skipped, a bad document throws. */
object CourseJsonParser {

    const val MAX_LESSONS = 999

    fun parse(json: String): List<RemoteCourse> {
        val root = try {
            JsonParser.parseString(json)
        } catch (e: Exception) {
            throw MalformedDataException("Courses payload is not valid JSON", e)
        }
        if (!root.isJsonArray) throw MalformedDataException("Courses payload must be a JSON array")
        return root.asJsonArray.mapNotNull { parseCourse(it) }.distinctBy { it.id }
    }

    private fun parseCourse(element: JsonElement): RemoteCourse? = runCatching {
        val obj = element.asJsonObject
        val id = obj.get("id").asLong
        val title = obj.get("title").asString.trim()
        val lessons = obj.get("lessons").asInt
        if (title.isEmpty() || lessons !in 0..MAX_LESSONS) return null
        RemoteCourse(
            id = id,
            title = title,
            instructor = obj.get("instructor")?.takeIf { !it.isJsonNull }?.asString?.trim().orEmpty(),
            progress = (obj.get("progress")?.takeIf { !it.isJsonNull }?.asInt ?: 0).coerceIn(0, 100),
            lessons = lessons,
            lessonTitles = obj.get("lessonTitles")?.takeIf { it.isJsonArray }
                ?.asJsonArray?.mapNotNull { runCatching { it.asString }.getOrNull() }
                .orEmpty()
        )
    }.getOrNull()
}
