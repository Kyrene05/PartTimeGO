package com.example.parttimego.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parttimego.screen.EmployerProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmployerProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EmployerProfileUiState())
    val uiState: StateFlow<EmployerProfileUiState> = _uiState.asStateFlow()

    fun updateProfile(
        companyName: String? = null,
        companyPhone: String? = null,
        companyEmail: String? = null,
        avatarUrl: String? = null
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                companyName = companyName ?: currentState.companyName,
                companyPhone = companyPhone ?: currentState.companyPhone,
                companyEmail = companyEmail ?: currentState.companyEmail,
                avatarUrl = avatarUrl ?: currentState.avatarUrl
            )
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            // Reset state to default data state on logout action
            _uiState.value = EmployerProfileUiState()
            onLogoutSuccess()
        }
    }
}