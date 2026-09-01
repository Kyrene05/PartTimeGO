package com.example.parttimego.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class EmployerProfileDto(
    @SerialName("full_name") val userName: String? = null,
    @SerialName("company_name") val companyName: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("company_background") val companyBackground: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

data class EmployerProfileUiState(
    val userName: String = "",
    val companyName: String = "",
    val phone: String = "",
    val companyEmail: String = "",
    val companyBackground: String = "",
    val avatarUrl: String? = null,
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val updateSuccess: Boolean = false,
    val errorMessage: String? = null
)

class EmployerProfileViewModel(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmployerProfileUiState())
    val uiState: StateFlow<EmployerProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                val userId = currentUser?.id ?: return@launch
                val email = currentUser.email.orEmpty()

                val authMetaData = currentUser.userMetadata
                val registeredName = authMetaData?.get("full_name")?.jsonPrimitive?.content
                    ?: authMetaData?.get("user_name")?.jsonPrimitive?.content
                    ?: authMetaData?.get("name")?.jsonPrimitive?.content

                val profile = supabaseClient.postgrest["profiles"]
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<EmployerProfileDto>()

                val finalUserName = when {
                    !profile?.userName.isNullOrBlank() -> profile.userName
                    !registeredName.isNullOrBlank() -> registeredName
                    email.isNotBlank() -> email.substringBefore("@")
                    else -> ""
                }

                val rawPhone = profile?.phone.orEmpty().trim()
                val formattedPhone = when {
                    rawPhone.isBlank() -> ""
                    rawPhone.startsWith("+60") -> rawPhone
                    rawPhone.startsWith("60") -> "+$rawPhone"
                    else -> "+60$rawPhone"
                }

                _uiState.update {
                    it.copy(
                        userName = finalUserName,
                        companyName = profile?.companyName.orEmpty(),
                        phone = formattedPhone,
                        companyBackground = profile?.companyBackground.orEmpty(),
                        companyEmail = email,
                        avatarUrl = profile?.avatarUrl,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load profile: ${e.message}"
                    )
                }
            }
        }
    }

    fun onUserNameChange(value: String) { _uiState.update { it.copy(userName = value) } }
    fun onCompanyNameChange(value: String) { _uiState.update { it.copy(companyName = value) } }
    fun onPhoneChange(value: String) { _uiState.update { it.copy(phone = value) } }
    fun onCompanyBackgroundChange(value: String) { _uiState.update { it.copy(companyBackground = value) } }
    fun onAvatarSelected(uri: Uri) { _uiState.update { it.copy(selectedImageUri = uri) } }
    fun resetUpdateSuccess() { _uiState.update { it.copy(updateSuccess = false) } }
    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }

    fun saveProfile(context: Context) {
        viewModelScope.launch {
            val companyName = _uiState.value.companyName.trim()
            val rawDigits = _uiState.value.phone
                .removePrefix("+60")
                .removePrefix("60")
                .filter { it.isDigit() }

            if (companyName.isBlank()) {
                _uiState.update {
                    it.copy(errorMessage = "Company name is required.")
                }
                return@launch
            }

            if (rawDigits.isBlank()) {
                _uiState.update {
                    it.copy(errorMessage = "Phone number is required.")
                }
                return@launch
            }

            if (rawDigits.length < 8) {
                _uiState.update {
                    it.copy(errorMessage = "Invalid phone number (must be at least 8 digits)")
                }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true) }
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                    ?: throw Exception("User not authenticated.")

                var uploadedAvatarUrl = _uiState.value.avatarUrl

                _uiState.value.selectedImageUri?.let { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.use { it.readBytes() }

                    if (bytes != null) {
                        val fileName = "$userId-${System.currentTimeMillis()}.jpg"
                        val bucket = supabaseClient.storage.from("avatars")

                        bucket.upload(path = fileName, data = bytes) {
                            upsert = true
                        }

                        uploadedAvatarUrl = bucket.publicUrl(fileName)
                    }
                }

                val fullPhoneToSave = "+60$rawDigits"

                val updateParams = buildMap {
                    put("full_name", _uiState.value.userName.trim())
                    put("company_name", companyName)
                    put("phone", fullPhoneToSave)
                    put("company_background", _uiState.value.companyBackground.trim())
                    uploadedAvatarUrl?.let { put("avatar_url", it) }
                }

                supabaseClient.postgrest["profiles"].update(updateParams) {
                    filter { eq("id", userId) }
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        updateSuccess = true,
                        phone = fullPhoneToSave,
                        avatarUrl = uploadedAvatarUrl,
                        selectedImageUri = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Save failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun deleteAccount(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                if (userId != null) {
                    // 1. 调用 Supabase 后端的 RPC 函数（以 SECURITY DEFINER 权限一次性级联清理关联表 + auth.users）
                    supabaseClient.postgrest.rpc("delete_current_user")

                    // 2. 登出 Auth session
                    supabaseClient.auth.signOut()
                    onResult(true, null)
                } else {
                    onResult(false, "User not authenticated")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to delete account")
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signOut()
                onLogoutSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Logout failed: ${e.message}") }
            }
        }
    }
}

class EmployerProfileViewModelFactory(
    private val supabaseClient: SupabaseClient
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmployerProfileViewModel::class.java)) {
            return EmployerProfileViewModel(supabaseClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}