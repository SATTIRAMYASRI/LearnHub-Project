package com.ramya.learnhub.ui.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramya.learnhub.R
import com.ramya.learnhub.data.remote.InvalidCredentialsException
import com.ramya.learnhub.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

data class LoginUiState(
    val isLoading: Boolean = false,
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val formError: Int? = null,
    val isLoggedIn: Boolean = false
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onLoginClicked(email: String, password: String) {
        if (_state.value.isLoading) return

        val validation = LoginValidator.validate(email, password)
        if (!validation.isValid) {
            _state.value = LoginUiState(
                emailError = validation.emailError,
                passwordError = validation.passwordError
            )
            return
        }

        _state.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { _state.update { LoginUiState(isLoggedIn = true) } }
                .onFailure { error -> _state.update { LoginUiState(formError = error.toMessage()) } }
        }
    }

    private fun Throwable.toMessage(): Int = when (this) {
        is InvalidCredentialsException -> R.string.login_error_invalid_credentials
        is IOException -> R.string.error_no_connection
        else -> R.string.error_generic
    }
}
