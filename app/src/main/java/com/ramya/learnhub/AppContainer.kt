package com.ramya.learnhub

import android.content.Context
import androidx.room.Room
import com.ramya.learnhub.data.local.AppDatabase
import com.ramya.learnhub.data.local.RoomTransactionRunner
import com.ramya.learnhub.data.remote.AssetCourseRemoteDataSource
import com.ramya.learnhub.data.remote.ConnectivityNetworkChecker
import com.ramya.learnhub.data.remote.MockAuthService
import com.ramya.learnhub.data.repository.AuthRepository
import com.ramya.learnhub.data.repository.CourseRepository
import com.ramya.learnhub.data.security.EncryptedTokenStore

/** Manual dependency container; created once by [LearnHubApp]. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val networkChecker = ConnectivityNetworkChecker(appContext)
    private val database = Room.databaseBuilder(appContext, AppDatabase::class.java, AppDatabase.NAME).build()

    val authRepository = AuthRepository(MockAuthService(networkChecker), EncryptedTokenStore(appContext))
    val courseRepository = CourseRepository(
        dao = database.courseDao(),
        remote = AssetCourseRemoteDataSource(appContext, networkChecker),
        transactions = RoomTransactionRunner(database)
    )
}
