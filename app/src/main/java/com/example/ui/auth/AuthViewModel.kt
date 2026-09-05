package com.example.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.UserRole
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

    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    fun loginStaff(staffId: String, pass: String) {
        if (staffId.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your assigned Staff ID or Username.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.loginStaff(staffId, pass)
                .onSuccess { session ->
                    _uiState.value = AuthUiState.Authenticated(session)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Authentication failed.")
                }
        }
    }

    fun requestAdminOtp(phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your registered Admin phone number.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.requestAdminOtp(phoneNumber)
                .onSuccess {
                    _otpSent.value = true
                    _uiState.value = AuthUiState.Idle
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Phone number is not authorized for Admin access.")
                }
        }
    }

    fun verifyAdminOtpAndLogin(phoneNumber: String, otp: String) {
        if (otp.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter the 6-digit OTP code.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            authRepository.loginAdmin(phoneNumber, otp)
                .onSuccess { session ->
                    _uiState.value = AuthUiState.Authenticated(session)
                }
                .onFailure { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Invalid Admin OTP or unauthorized.")
                }
        }
    }

    fun resetPassword(emailOrStaffId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.resetPassword(emailOrStaffId)
            onComplete()
        }
    }

    fun switchRoleQuickDemo(role: UserRole) {
        viewModelScope.launch {
            if (role == UserRole.ADMIN) {
                authRepository.loginAdmin("+91 98765 43210", "123456")
            } else {
                authRepository.loginStaff("GP-STAFF-101", "password")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
            _otpSent.value = false
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
