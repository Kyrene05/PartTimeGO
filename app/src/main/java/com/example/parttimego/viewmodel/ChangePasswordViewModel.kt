package com.example.parttimego.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parttimego.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isCurrentPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChangePasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun onCurrentPasswordChange(value: String) {
        _uiState.update { it.copy(currentPassword = value, errorMessage = null) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun toggleCurrentPasswordVisibility() {
        _uiState.update { it.copy(isCurrentPasswordVisible = !it.isCurrentPasswordVisible) }
    }

    fun toggleNewPasswordVisibility() {
        _uiState.update { it.copy(isNewPasswordVisible = !it.isNewPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun changePassword(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val currentPwd = currentState.currentPassword.trim()
        val newPwd = currentState.newPassword.trim()
        val confirmPwd = currentState.confirmPassword.trim()

        if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all fields.") }
            return
        }

        val hasLetter = newPwd.any { it.isLetter() }
        val hasDigit = newPwd.any { it.isDigit() }

        if (newPwd.length < 6 || !hasLetter || !hasDigit) {
            _uiState.update {
                it.copy(errorMessage = "Password must be at least 6 characters and contain both letters and numbers.")
            }
            return
        }

        if (newPwd != confirmPwd) {
            _uiState.update { it.copy(errorMessage = "New passwords do not match.") }
            return
        }

        if (currentPwd == newPwd) {
            _uiState.update { it.copy(errorMessage = "New password must be different from current password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // 1. 获取当前登录用户的 Email
                val userEmail = SupabaseClient.client.auth.currentUserOrNull()?.email
                if (userEmail.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "User session expired. Please log in again.")
                    }
                    return@launch
                }

                try {
                    SupabaseClient.client.auth.signInWith(Email) {
                        email = userEmail
                        password = currentPwd
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Incorrect current password. Please check and try again."
                        )
                    }
                    return@launch
                }

                SupabaseClient.client.auth.updateUser {
                    password = newPwd
                }

                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to update password. Please try again."
                    )
                }
            }
        }
    }
}