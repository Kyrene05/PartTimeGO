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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class JobSummaryDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("category") val category: String? = null,
    @SerialName("salary") val salary: String? = null,
    @SerialName("status") val status: String? = "open"
)

data class JobItemSummary(
    val id: String,
    val title: String,
    val category: String,
    val salary: String,
    val status: String
)

data class EmployerProfileUiState(
    val userName: String = "",
    val companyName: String = "",
    val phone: String = "",
    val companyEmail: String = "",
    val companyBackground: String = "",
    val avatarUrl: String? = null,
    val selectedImageUri: Uri? = null,

    val activeJobs: List<JobItemSummary> = emptyList(),
    val isLoadingJobs: Boolean = false,

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

    fun loadUserProfile(targetEmployerId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                val userId = targetEmployerId ?: currentUser?.id ?: return@launch
                val responseList = supabaseClient.postgrest["profiles"]
                    .select { filter { eq("id", userId) } }
                    .decodeList<Map<String, JsonElement>>()

                val profileMap = responseList.firstOrNull()

                val dbCompanyName = profileMap?.get("company_name")?.jsonPrimitive?.content.orEmpty()
                val dbPhone = profileMap?.get("phone")?.jsonPrimitive?.content.orEmpty()
                val dbBackground = profileMap?.get("company_background")?.jsonPrimitive?.content.orEmpty()
                val dbAvatarUrl = profileMap?.get("avatar_url")?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                val dbUserName = profileMap?.get("full_name")?.jsonPrimitive?.content.orEmpty()


                val profileEmail = profileMap?.get("email")?.jsonPrimitive?.content?.takeIf { it.isNotBlank() && it != "null" }
                android.util.Log.d("DEBUG_EMAIL", "profileEmail: $profileEmail")

                val finalEmail = if (!profileEmail.isNullOrBlank()) {
                    profileEmail
                } else {
                    try {
                        val rpcResult = supabaseClient.postgrest.rpc(
                            "get_employer_email",
                            mapOf("target_user_id" to userId)
                        ).decodeAs<String>()

                        android.util.Log.d("DEBUG_EMAIL", "RPC Result: $rpcResult")
                        rpcResult
                    } catch (e: Exception) {
                        android.util.Log.e("DEBUG_EMAIL", "RPC Error: ${e.localizedMessage}")
                        ""
                    }
                }

                val authMetaData = currentUser?.userMetadata
                val registeredName = authMetaData?.get("full_name")?.jsonPrimitive?.content
                    ?: authMetaData?.get("user_name")?.jsonPrimitive?.content
                    ?: authMetaData?.get("name")?.jsonPrimitive?.content

                val finalUserName = when {
                    dbUserName.isNotBlank() -> dbUserName
                    !registeredName.isNullOrBlank() -> registeredName
                    finalEmail.isNotBlank() -> finalEmail.substringBefore("@")
                    else -> ""
                }

                val rawPhone = dbPhone.trim()
                val formattedPhone = when {
                    rawPhone.isBlank() -> ""
                    rawPhone.startsWith("+60") -> rawPhone
                    rawPhone.startsWith("60") -> "+$rawPhone"
                    else -> "+60$rawPhone"
                }

                _uiState.update {
                    it.copy(
                        userName = finalUserName,
                        companyName = dbCompanyName,
                        phone = formattedPhone,
                        companyBackground = dbBackground,
                        companyEmail = finalEmail,
                        avatarUrl = dbAvatarUrl,
                        isLoading = false
                    )
                }

                loadActiveJobs(userId)

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load profile: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun loadActiveJobs(employerId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingJobs = true) }
            try {
                val userId = employerId ?: supabaseClient.auth.currentUserOrNull()?.id ?: return@launch

                val jobsList = supabaseClient.postgrest["jobs"]
                    .select {
                        filter {
                            eq("employer_id", userId)
                            eq("status", "open")
                        }
                    }
                    .decodeList<JobSummaryDto>()
                    .map { dto ->
                        JobItemSummary(
                            id = dto.id,
                            title = dto.title,
                            category = dto.category.orEmpty(),
                            salary = dto.salary.orEmpty(),
                            status = dto.status.orEmpty()
                        )
                    }

                _uiState.update {
                    it.copy(
                        activeJobs = jobsList,
                        isLoadingJobs = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingJobs = false) }
            }
        }
    }

    fun onUserNameChange(value: String) { _uiState.update { it.copy(userName = value) } }
    fun onCompanyNameChange(value: String) { _uiState.update { it.copy(companyName = value) } }

    fun onPhoneChange(value: String) {
        val digits = value.removePrefix("+60").removePrefix("60").filter { it.isDigit() }
        _uiState.update { it.copy(phone = digits) }
    }

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
                _uiState.update { it.copy(errorMessage = "Company name is required.") }
                return@launch
            }

            if (rawDigits.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Phone number is required.") }
                return@launch
            }

            if (rawDigits.length < 8) {
                _uiState.update { it.copy(errorMessage = "Invalid phone number (must be at least 8 digits)") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                val userId = currentUser?.id ?: throw Exception("User not authenticated.")

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
                val userEmail = currentUser.email.orEmpty()

                // 將 email 一起放進更新參數中寫入資料庫
                val updateParams = buildMap {
                    put("full_name", _uiState.value.userName.trim())
                    put("company_name", companyName)
                    put("phone", fullPhoneToSave)
                    put("company_background", _uiState.value.companyBackground.trim())
                    if (userEmail.isNotBlank()) {
                        put("email", userEmail)
                    }
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
                        companyEmail = userEmail,
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
                    supabaseClient.postgrest.rpc("delete_current_user")
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