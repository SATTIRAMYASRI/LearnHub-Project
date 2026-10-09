package com.ramya.learnhub.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ramya.learnhub.domain.Course
import com.ramya.learnhub.domain.Lesson

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val instructor: String,
    val progress: Int,
    val totalLessons: Int
)

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("courseId")]
)
data class LessonEntity(
    @PrimaryKey val id: Long,
    val courseId: Long,
    val title: String,
    val completed: Boolean,
    val position: Int
)

fun CourseEntity.toDomain() = Course(id, title, instructor, progress, totalLessons)

fun LessonEntity.toDomain() = Lesson(id, courseId, title, completed)
