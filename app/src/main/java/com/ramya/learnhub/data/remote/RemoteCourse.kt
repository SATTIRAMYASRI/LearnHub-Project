package com.ramya.learnhub.data.remote

data class RemoteCourse(
    val id: Long,
    val title: String,
    val instructor: String,
    val progress: Int,
    val lessons: Int,
    val lessonTitles: List<String> = emptyList()
)
