package com.ramya.learnhub.data.repository

import com.ramya.learnhub.data.local.CourseDao
import com.ramya.learnhub.data.local.CourseEntity
import com.ramya.learnhub.data.local.LessonEntity
import com.ramya.learnhub.data.local.TransactionRunner
import com.ramya.learnhub.data.local.toDomain
import com.ramya.learnhub.data.remote.CourseRemoteDataSource
import com.ramya.learnhub.data.remote.RemoteCourse
import com.ramya.learnhub.domain.Course
import com.ramya.learnhub.domain.CourseDetails
import com.ramya.learnhub.domain.ProgressCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt

/** Room is the single source of truth; the remote source only refreshes it. */
class CourseRepository(
    private val dao: CourseDao,
    private val remote: CourseRemoteDataSource,
    private val transactions: TransactionRunner
) {

    fun observeCourses(): Flow<List<Course>> =
        dao.observeCourses().map { list -> list.map { it.toDomain() } }

    fun observeCourseDetails(courseId: Long): Flow<CourseDetails?> =
        combine(dao.observeCourse(courseId), dao.observeLessons(courseId)) { course, lessons ->
            course?.let { CourseDetails(it.toDomain(), lessons.map { l -> l.toDomain() }) }
        }

    suspend fun hasCachedCourses(): Boolean = dao.courseCount() > 0

    /** Fetches remote courses and merges them into the cache. On failure the cache is untouched. */
    suspend fun refresh(): Result<Unit> {
        val courses = try {
            remote.fetchCourses()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return try {
            transactions.run { merge(courses) }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marks a lesson completed and recalculates course progress in one transaction.
     * Idempotent: completing an already-completed lesson changes nothing.
     */
    suspend fun completeLesson(courseId: Long, lessonId: Long) {
        transactions.run {
            val lessons = dao.getLessons(courseId)
            val lesson = lessons.firstOrNull { it.id == lessonId } ?: return@run
            if (lesson.completed) return@run
            dao.markLessonCompleted(lessonId)
            val completed = lessons.count { it.completed } + 1
            dao.updateProgress(courseId, ProgressCalculator.percent(completed, lessons.size))
        }
    }

    private suspend fun merge(remoteCourses: List<RemoteCourse>) {
        dao.deleteCoursesNotIn(remoteCourses.map { it.id })
        for (rc in remoteCourses) {
            val existing = dao.getCourse(rc.id)
            val localById = dao.getLessons(rc.id).associateBy { it.id }
            // Local completion state always wins over the remote seed.
            val lessons = seedLessons(rc).map { seed ->
                localById[seed.id]?.let { seed.copy(completed = it.completed) } ?: seed
            }
            val progress = when {
                existing == null -> rc.progress
                existing.totalLessons == lessons.size -> existing.progress
                else -> ProgressCalculator.percent(lessons.count { it.completed }, lessons.size)
            }
            dao.upsertCourse(CourseEntity(rc.id, rc.title, rc.instructor, progress, lessons.size))
            dao.upsertLessons(lessons)
            dao.deleteLessonsNotIn(rc.id, lessons.map { it.id })
        }
    }

    /** The API only reports a lesson count and progress, so initial completed lessons are derived. */
    private fun seedLessons(rc: RemoteCourse): List<LessonEntity> {
        val completedCount = (rc.progress * rc.lessons / 100.0).roundToInt()
        return (0 until rc.lessons).map { i ->
            LessonEntity(
                id = lessonId(rc.id, i),
                courseId = rc.id,
                title = rc.lessonTitles.getOrNull(i)?.takeIf { it.isNotBlank() } ?: "Lesson ${i + 1}",
                completed = i < completedCount,
                position = i
            )
        }
    }

    private fun lessonId(courseId: Long, index: Int): Long = courseId * 1000 + index + 1
}
