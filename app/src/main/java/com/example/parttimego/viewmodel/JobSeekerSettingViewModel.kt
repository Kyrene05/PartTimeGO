package com.example.parttimego.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parttimego.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class JobSeekerSettingUiState(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = true
)

@Serializable
private data class ProfileDto(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

class JobSeekerSettingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(JobSeekerSettingUiState())
    val uiState: StateFlow<JobSeekerSettingUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser != null) {
                    val userId = currentUser.id
                    val authEmail = currentUser.email ?: ""

                    // 从 Supabase profiles 表查询当前用户的数据
                    val profile = SupabaseClient.client.from("profiles")
                        .select {
                            filter {
                                eq("id", userId)
                            }
                        }
                        .decodeSingleOrNull<ProfileDto>()

                    _uiState.update {
                        it.copy(
                            fullName = profile?.fullName ?: "",
                            phone = profile?.phone ?: "",
                            email = profile?.email ?: authEmail,
                            avatarUrl = profile?.avatarUrl,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onLogoutSuccess()
            }
        }
    }
}