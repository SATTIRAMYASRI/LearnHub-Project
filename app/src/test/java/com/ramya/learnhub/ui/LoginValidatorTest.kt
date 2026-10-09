package com.ramya.learnhub.ui

import com.ramya.learnhub.R
import com.ramya.learnhub.ui.login.LoginValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginValidatorTest {

    @Test
    fun `valid email and password pass`() {
        assertTrue(LoginValidator.validate("demo@learnhub.com", "secret").isValid)
    }

    @Test
    fun `empty email is required`() {
        assertEquals(R.string.login_error_email_required, LoginValidator.validate("", "secret").emailError)
    }

    @Test
    fun `email without at-sign is invalid`() {
        assertEquals(R.string.login_error_email_invalid, LoginValidator.validate("demo.learnhub.com", "secret").emailError)
    }

    @Test
    fun `empty password is required`() {
        val result = LoginValidator.validate("demo@learnhub.com", "")

        assertEquals(R.string.login_error_password_required, result.passwordError)
        assertNull(result.emailError)
    }
}
