package com.ramya.learnhub.data.remote

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException

interface CourseRemoteDataSource {
    /** Throws on any failure (offline, timeout, malformed data). */
    suspend fun fetchCourses(): List<RemoteCourse>
}

/**
 * Mock "API" backed by assets/courses.json. It behaves like a remote call: it needs a network
 * connection, has latency and a timeout. Replace with a Retrofit implementation for a real backend.
 */
class AssetCourseRemoteDataSource(
    private val context: Context,
    private val networkChecker: NetworkChecker,
    private val latencyMs: Long = 800,
    private val timeoutMs: Long = 10_000
) : CourseRemoteDataSource {

    override suspend fun fetchCourses(): List<RemoteCourse> = try {
        withTimeout(timeoutMs) {
            if (!networkChecker.isOnline()) throw IOException("No internet connection")
            delay(latencyMs)
            val json = withContext(Dispatchers.IO) {
                context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
            }
            CourseJsonParser.parse(json)
        }
    } catch (e: TimeoutCancellationException) {
        // Timeout is a CancellationException; convert it so callers see a normal failure.
        throw IOException("Request timed out", e)
    }

    private companion object {
        const val ASSET_NAME = "courses.json"
    }
}
