package com.ramya.learnhub.domain

data class Course(
    val id: Long,
    val title: String,
    val instructor: String,
    val progress: Int,
    val totalLessons: Int
)

data class Lesson(
    val id: Long,
    val courseId: Long,
    val title: String,
    val isCompleted: Boolean
)

data class CourseDetails(
    val course: Course,
    val lessons: List<Lesson>
)
