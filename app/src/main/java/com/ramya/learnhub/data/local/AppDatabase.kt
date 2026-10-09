package com.ramya.learnhub.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CourseEntity::class, LessonEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao

    companion object {
        const val NAME = "learnhub.db"
    }
}
