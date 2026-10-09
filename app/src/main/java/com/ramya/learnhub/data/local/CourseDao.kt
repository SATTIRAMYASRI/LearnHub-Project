package com.ramya.learnhub.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses ORDER BY id")
    fun observeCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun observeCourse(courseId: Long): Flow<CourseEntity?>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY position")
    fun observeLessons(courseId: Long): Flow<List<LessonEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    suspend fun getCourse(courseId: Long): CourseEntity?

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY position")
    suspend fun getLessons(courseId: Long): List<LessonEntity>

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun courseCount(): Int

    // @Upsert (not REPLACE) so updating a course never cascades into deleting its lessons.
    @Upsert
    suspend fun upsertCourse(course: CourseEntity)

    @Upsert
    suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM courses WHERE id NOT IN (:keepIds)")
    suspend fun deleteCoursesNotIn(keepIds: List<Long>)

    @Query("DELETE FROM lessons WHERE courseId = :courseId AND id NOT IN (:keepIds)")
    suspend fun deleteLessonsNotIn(courseId: Long, keepIds: List<Long>)

    @Query("UPDATE lessons SET completed = 1 WHERE id = :lessonId")
    suspend fun markLessonCompleted(lessonId: Long)

    @Query("UPDATE courses SET progress = :progress WHERE id = :courseId")
    suspend fun updateProgress(courseId: Long, progress: Int)
}
