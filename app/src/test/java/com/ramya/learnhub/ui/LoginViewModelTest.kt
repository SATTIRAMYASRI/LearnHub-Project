package com.ramya.learnhub.ui

import com.ramya.learnhub.MainDispatcherRule
import com.ramya.learnhub.R
import com.ramya.learnhub.data.remote.InvalidCredentialsException
import com.ramya.learnhub.data.repository.AuthRepository
import com.ramya.learnhub.fakes.FakeAuthService
import com.ramya.learnhub.fakes.FakeTokenStore
import com.ramya.learnhub.ui.login.LoginViewModel
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val tokenStore = FakeTokenStore()

    private fun viewModel(service: FakeAuthService) = LoginViewModel(AuthRepository(service, tokenStore))

    @Test
    fun `empty fields show errors and do not call the server`() {
        val service = FakeAuthService()
        val vm = viewModel(service)

        vm.onLoginClicked("", "")

        assertEquals(R.string.login_error_email_required, vm.state.value.emailError)
        assertEquals(R.string.login_error_password_required, vm.state.value.passwordError)
        assertEquals(0, service.callCount)
    }

    @Test
    fun `successful login saves the token and marks the user logged in`() {
        val vm = viewModel(FakeAuthService())

        vm.onLoginClicked("demo@learnhub.com", "secret")

        assertTrue(vm.state.value.isLoggedIn)
        assertNotNull(tokenStore.get())
    }

    @Test
    fun `wrong credentials show an error and save no token`() {
        val vm = viewModel(FakeAuthService(Result.failure(InvalidCredentialsException())))

        vm.onLoginClicked("demo@learnhub.com", "wrong")

        assertEquals(R.string.login_error_invalid_credentials, vm.state.value.formError)
        assertFalse(vm.state.value.isLoggedIn)
        assertNull(tokenStore.get())
    }

    @Test
    fun `second click while loading is ignored`() {
        val gate = CompletableDeferred<Unit>() // keeps the first login "in progress"
        val service = FakeAuthService(gate = gate)
        val vm = viewModel(service)

        vm.onLoginClicked("demo@learnhub.com", "secret")
        assertTrue(vm.state.value.isLoading)
        vm.onLoginClicked("demo@learnhub.com", "secret")
        gate.complete(Unit)

        assertEquals(1, service.callCount)
    }
}
