package com.ramya.learnhub.data

import com.ramya.learnhub.data.repository.CourseRepository
import com.ramya.learnhub.fakes.FakeCourseDao
import com.ramya.learnhub.fakes.FakeRemote
import com.ramya.learnhub.fakes.FakeTransactionRunner
import com.ramya.learnhub.fakes.sampleRemoteCourse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class CourseRepositoryTest {

    private val remote = FakeRemote(Result.success(listOf(sampleRemoteCourse())))
    private lateinit var repository: CourseRepository

    @Before
    fun setUp() {
        repository = CourseRepository(FakeCourseDao(), remote, FakeTransactionRunner())
    }

    // Lesson ids are courseId * 1000 + position + 1, so course 1 has lessons 1001..1004.

    @Test
    fun `refresh saves the courses from the server`() = runTest {
        repository.refresh()

        val courses = repository.observeCourses().first()
        assertEquals(1, courses.size)
        assertEquals("Python Programming", courses[0].title)
        assertEquals(50, courses[0].progress)
    }

    @Test
    fun `completing a lesson recalculates progress`() = runTest {
        repository.refresh() // 2 of 4 lessons done = 50%

        repository.completeLesson(courseId = 1, lessonId = 1003) // now 3 of 4

        val details = repository.observeCourseDetails(1).first()!!
        assertEquals(75, details.course.progress)
        assertTrue(details.lessons.first { it.id == 1003L }.isCompleted)
    }

    @Test
    fun `completing the same lesson twice does not increase progress again`() = runTest {
        repository.refresh()

        repository.completeLesson(1, 1003)
        repository.completeLesson(1, 1003) // duplicate

        assertEquals(75, repository.observeCourseDetails(1).first()!!.course.progress)
    }

    @Test
    fun `completing an already completed lesson keeps progress unchanged`() = runTest {
        repository.refresh()

        repository.completeLesson(1, 1001) // lesson 1 was already completed by the seed

        assertEquals(50, repository.observeCourseDetails(1).first()!!.course.progress)
    }

    @Test
    fun `failed refresh keeps the cached courses`() = runTest {
        repository.refresh() // fills the cache
        remote.result = Result.failure(IOException("offline"))

        val result = repository.refresh()

        assertTrue(result.isFailure)
        assertEquals(1, repository.observeCourses().first().size)
    }

    @Test
    fun `refresh does not overwrite lessons the user already completed`() = runTest {
        repository.refresh()
        repository.completeLesson(1, 1003)

        repository.refresh() // server still says 50%, but the user's progress must win

        val details = repository.observeCourseDetails(1).first()!!
        assertEquals(75, details.course.progress)
        assertTrue(details.lessons.first { it.id == 1003L }.isCompleted)
    }
}
