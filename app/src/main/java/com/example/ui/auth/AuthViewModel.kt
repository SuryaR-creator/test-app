package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.UserSession
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val session: UserSession) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentSession = authRepository.currentSession

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun loginStaff(email: String, pass: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your registered Staff email.")
            return
        }
        if (pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your password.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.loginStaff(email, pass)
                .onSuccess { session ->
                    _uiState.value = AuthUiState.Authenticated(session)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Authentication failed.")
                }
        }
    }

    fun loginAdmin(email: String, pass: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your registered Administrator email.")
            return
        }
        if (pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your password.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.loginAdmin(email, pass)
                .onSuccess { session ->
                    _uiState.value = AuthUiState.Authenticated(session)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Admin authentication failed.")
                }
        }
    }

    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Please enter your registered email address.")
            return
        }
        viewModelScope.launch {
            authRepository.resetPassword(email)
                .onSuccess {
                    onResult(true, "If an account exists with this email, a password reset link has been dispatched.")
                }
                .onFailure { error ->
                    onResult(false, error.message ?: "Failed to dispatch password reset request.")
                }
        }
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
