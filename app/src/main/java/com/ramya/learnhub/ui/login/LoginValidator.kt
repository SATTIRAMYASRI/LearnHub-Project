package com.ramya.learnhub.ui.login

import androidx.annotation.StringRes
import com.ramya.learnhub.R

data class LoginValidation(
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null
) {
    val isValid: Boolean get() = emailError == null && passwordError == null
}

object LoginValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")

    fun validate(email: String, password: String): LoginValidation {
        val trimmed = email.trim()
        return LoginValidation(
            emailError = when {
                trimmed.isEmpty() -> R.string.login_error_email_required
                !EMAIL_REGEX.matches(trimmed) -> R.string.login_error_email_invalid
                else -> null
            },
            passwordError = if (password.isEmpty()) R.string.login_error_password_required else null
        )
    }
}
