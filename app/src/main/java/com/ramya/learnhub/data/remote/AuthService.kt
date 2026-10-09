package com.ramya.learnhub.data.remote

import kotlinx.coroutines.delay
import java.io.IOException
import java.util.UUID

class InvalidCredentialsException : Exception("Invalid email or password")

interface AuthService {
    /** Returns an auth token. Throws [InvalidCredentialsException] or [IOException]. */
    suspend fun login(email: String, password: String): String
}

/** Simulated authentication. Demo account only (documented in README); not a real credential. */
class MockAuthService(
    private val networkChecker: NetworkChecker,
    private val latencyMs: Long = 1_000
) : AuthService {

    override suspend fun login(email: String, password: String): String {
        delay(latencyMs)
        if (!networkChecker.isOnline()) throw IOException("No internet connection")
        if (!email.equals(DEMO_EMAIL, ignoreCase = true) || password != DEMO_PASSWORD) {
            throw InvalidCredentialsException()
        }
        return UUID.randomUUID().toString()
    }

    companion object {
        const val DEMO_EMAIL = "demo@learnhub.com"
        const val DEMO_PASSWORD = "Learn@123"
    }
}
