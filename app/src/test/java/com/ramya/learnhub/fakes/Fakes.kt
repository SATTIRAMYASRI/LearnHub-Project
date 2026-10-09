package com.ramya.learnhub.fakes

import com.ramya.learnhub.data.local.CourseDao
import com.ramya.learnhub.data.local.CourseEntity
import com.ramya.learnhub.data.local.LessonEntity
import com.ramya.learnhub.data.local.TransactionRunner
import com.ramya.learnhub.data.remote.AuthService
import com.ramya.learnhub.data.remote.CourseRemoteDataSource
import com.ramya.learnhub.data.remote.RemoteCourse
import com.ramya.learnhub.data.security.TokenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// "Fakes" are tiny pretend versions of real classes, so tests run fast on the JVM
// without a phone, a real database or a network.

/** A database that lives in memory. */
class FakeCourseDao : CourseDao {
    private val courses = MutableStateFlow<Map<Long, CourseEntity>>(emptyMap())
    private val lessons = MutableStateFlow<Map<Long, LessonEntity>>(emptyMap())

    override fun observeCourses(): Flow<List<CourseEntity>> =
        courses.map { it.values.sortedBy { c -> c.id } }

    override fun observeCourse(courseId: Long): Flow<CourseEntity?> =
        courses.map { it[courseId] }

    override fun observeLessons(courseId: Long): Flow<List<LessonEntity>> =
        lessons.map { all -> all.values.filter { it.courseId == courseId }.sortedBy { it.position } }

    override suspend fun getCourse(courseId: Long) = courses.value[courseId]

    override suspend fun getLessons(courseId: Long) =
        lessons.value.values.filter { it.courseId == courseId }.sortedBy { it.position }

    override suspend fun courseCount() = courses.value.size

    override suspend fun upsertCourse(course: CourseEntity) {
        courses.value = courses.value + (course.id to course)
    }

    override suspend fun upsertLessons(lessons: List<LessonEntity>) {
        this.lessons.value = this.lessons.value + lessons.associateBy { it.id }
    }

    override suspend fun deleteCoursesNotIn(keepIds: List<Long>) {
        courses.value = courses.value.filterKeys { it in keepIds }
        lessons.value = lessons.value.filterValues { it.courseId in keepIds }
    }

    override suspend fun deleteLessonsNotIn(courseId: Long, keepIds: List<Long>) {
        lessons.value = lessons.value.filterValues { it.courseId != courseId || it.id in keepIds }
    }

    override suspend fun markLessonCompleted(lessonId: Long) {
        lessons.value[lessonId]?.let { lessons.value = lessons.value + (lessonId to it.copy(completed = true)) }
    }

    override suspend fun updateProgress(courseId: Long, progress: Int) {
        courses.value[courseId]?.let { courses.value = courses.value + (courseId to it.copy(progress = progress)) }
    }
}

/** Runs the code directly; a real transaction is not needed in unit tests. */
class FakeTransactionRunner : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T = block()
}

/** A pretend server: set [result] to a list (success) or to an exception (failure). */
class FakeRemote(var result: Result<List<RemoteCourse>>) : CourseRemoteDataSource {
    override suspend fun fetchCourses(): List<RemoteCourse> = result.getOrThrow()
}

class FakeTokenStore : TokenStore {
    var token: String? = null
    override fun save(token: String) { this.token = token }
    override fun get() = token
    override fun clear() { token = null }
}

/** Pretend login service. [gate] lets a test hold the login "in progress". */
class FakeAuthService(
    var result: Result<String> = Result.success("token-123"),
    private val gate: kotlinx.coroutines.CompletableDeferred<Unit>? = null
) : AuthService {
    var callCount = 0
    override suspend fun login(email: String, password: String): String {
        callCount++
        gate?.await()
        return result.getOrThrow()
    }
}

/** A course with 4 lessons, 50% done, so 2 lessons start as completed. */
fun sampleRemoteCourse(id: Long = 1) = RemoteCourse(
    id = id,
    title = "Python Programming",
    instructor = "John Smith",
    progress = 50,
    lessons = 4,
    lessonTitles = listOf("Intro", "Variables", "Functions", "OOP")
)
