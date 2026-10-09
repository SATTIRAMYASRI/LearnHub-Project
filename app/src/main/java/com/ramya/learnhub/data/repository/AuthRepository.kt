package com.ramya.learnhub.data.repository

import com.ramya.learnhub.data.remote.AuthService
import com.ramya.learnhub.data.security.TokenStore
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository(
    private val authService: AuthService,
    private val tokenStore: TokenStore
) {
    fun isLoggedIn(): Boolean = tokenStore.get() != null

    suspend fun login(email: String, password: String): Result<Unit> = try {
        tokenStore.save(authService.login(email.trim(), password))
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun logout() = tokenStore.clear()
}
