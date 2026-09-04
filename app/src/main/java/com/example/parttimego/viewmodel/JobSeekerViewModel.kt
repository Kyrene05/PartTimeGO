package com.example.parttimego.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.parttimego.data.model.JobSeeker
import com.example.parttimego.data.repository.JobSeekerRepository
import com.example.parttimego.screen.GigExperienceItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
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
data class JobSeekerProfileDto(
    @SerialName("full_name") val userName: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("gender") val gender: String? = null,
    @SerialName("about_me") val aboutMe: String? = null
)

// DTO 中的 @SerialName 映射 Supabase 原生字段名，绝对不能变
@Serializable
data class JobSeekerDetailDto(
    @SerialName("worker_id") val jobSeekerId: String? = null,
    @SerialName("worker_name") val jobSeekerName: String? = null,
    @SerialName("worker_phoneNo") val jobSeekerPhoneNo: String? = null,
    @SerialName("worker_availability") val jobSeekerAvailability: Boolean? = true,
    @SerialName("worker_availableDays") val jobSeekerAvailableDays: String? = null, // 存储如 "Mon, Tue, Sat"
    @SerialName("worker_skills") val jobSeekerSkills: String? = null,
    @SerialName("worker_preferredLocation") val jobSeekerPreferredLocation: String? = null,
    @SerialName("worker_preferredState") val jobSeekerPreferredState: String? = null,
    @SerialName("worker_preferredJobCategories") val jobSeekerPreferredJobCategories: String? = null,
    @SerialName("worker_workHistory") val jobSeekerWorkHistory: String? = null,
    @SerialName("user_id") val userId: String? = null
)

@Serializable
data class GigHistoryDto(
    @SerialName("gig_id") val gigId: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("company_name") val companyName: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("status") val status: String? = null
)

data class JobSeekerUiState(
    val userName: String = "",
    val phone: String = "",
    val email: String = "",
    val gender: String = "",
    val aboutMe: String = "",
    val avatarUrl: String? = null,
    val selectedImageUri: Uri? = null,

    val availability: Boolean = true,
    val availableDays: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val preferredLocations: List<String> = emptyList(),
    val preferredCategories: List<String> = emptyList(),
    val workHistory: String = "None",

    val completedGigsCount: Int = 0,
    val gigExperiences: List<GigExperienceItem> = emptyList(),

    val jobSeeker: JobSeeker? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val updateSuccess: Boolean = false,
    val errorMessage: String? = null
)

class JobSeekerViewModel(
    private val supabaseClient: SupabaseClient,
    private val repository: JobSeekerRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobSeekerUiState())
    val uiState: StateFlow<JobSeekerUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
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
                    .decodeSingleOrNull<JobSeekerProfileDto>()

                val jobSeekerDetail = supabaseClient.postgrest["worker"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeSingleOrNull<JobSeekerDetailDto>()

                mapDtoToUiState(profile, jobSeekerDetail, email, registeredName)
                loadJobSeeker(userId)
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


    fun loadJobSeekerByJobSeekerId(jobSeekerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Supabase 中的表名和字段名保持原样
                val jobSeekerDetail = supabaseClient.postgrest["worker"]
                    .select { filter { eq("worker_id", jobSeekerId) } }
                    .decodeSingleOrNull<JobSeekerDetailDto>()

                var profile: JobSeekerProfileDto? = null
                if (!jobSeekerDetail?.userId.isNullOrBlank()) {
                    profile = supabaseClient.postgrest["profiles"]
                        .select { filter { eq("id", jobSeekerDetail!!.userId!!) } }
                        .decodeSingleOrNull<JobSeekerProfileDto>()
                }

                var totalGigsCount = 0
                var recentGigs = emptyList<GigExperienceItem>()

                if (!jobSeekerDetail?.jobSeekerId.isNullOrBlank()) {
                    // 计算完成的总 Gigs 数，后端字段为 worker_id
                    val allCompletedGigs = supabaseClient.postgrest["gigs"]
                        .select {
                            filter {
                                eq("worker_id", jobSeekerDetail!!.jobSeekerId!!)
                                eq("status", "COMPLETED")
                            }
                        }
                        .decodeList<GigHistoryDto>()

                    totalGigsCount = allCompletedGigs.size

                    recentGigs = supabaseClient.postgrest["gigs"]
                        .select {
                            filter {
                                eq("worker_id", jobSeekerDetail!!.jobSeekerId!!)
                                eq("status", "COMPLETED")
                            }
                            order("completed_at", order = Order.DESCENDING)
                            limit(10)
                        }
                        .decodeList<GigHistoryDto>()
                        .map { dto ->
                            GigExperienceItem(
                                title = dto.title ?: "Gig",
                                companyName = dto.companyName ?: "",
                                dateText = dto.completedAt?.take(10) ?: ""
                            )
                        }
                }

                mapDtoToUiState(profile, jobSeekerDetail, "", null)
                _uiState.update {
                    it.copy(
                        completedGigsCount = totalGigsCount,
                        gigExperiences = recentGigs
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load job seeker detail: ${e.message}"
                    )
                }
            }
        }
    }

    private fun mapDtoToUiState(
        profile: JobSeekerProfileDto?,
        jobSeekerDetail: JobSeekerDetailDto?,
        email: String,
        registeredName: String?
    ) {
        val finalUserName = when {
            !jobSeekerDetail?.jobSeekerName.isNullOrBlank() -> jobSeekerDetail.jobSeekerName
            !profile?.userName.isNullOrBlank() -> profile.userName
            !registeredName.isNullOrBlank() -> registeredName
            email.isNotBlank() -> email.substringBefore("@")
            else -> ""
        }

        val rawPhone = (jobSeekerDetail?.jobSeekerPhoneNo ?: profile?.phone).orEmpty().trim()
        val formattedPhone = when {
            rawPhone.isBlank() -> ""
            rawPhone.startsWith("+60") -> rawPhone
            rawPhone.startsWith("60") -> "+$rawPhone"
            else -> "+60$rawPhone"
        }

        val parsedSkills = jobSeekerDetail?.jobSeekerSkills
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        val locations = mutableListOf<String>()
        jobSeekerDetail?.jobSeekerPreferredLocation?.let { if (it.isNotBlank()) locations.add(it) }
        jobSeekerDetail?.jobSeekerPreferredState?.let { if (it.isNotBlank()) locations.add(it) }

        val parsedCategories = jobSeekerDetail?.jobSeekerPreferredJobCategories
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        val parsedDays = jobSeekerDetail?.jobSeekerAvailableDays
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        _uiState.update {
            it.copy(
                userName = finalUserName,
                phone = formattedPhone,
                email = email,
                gender = profile?.gender.orEmpty(),
                aboutMe = profile?.aboutMe.orEmpty(),
                avatarUrl = profile?.avatarUrl,

                availability = jobSeekerDetail?.jobSeekerAvailability ?: true,
                availableDays = parsedDays,
                skills = parsedSkills,
                preferredLocations = locations,
                preferredCategories = parsedCategories,
                workHistory = jobSeekerDetail?.jobSeekerWorkHistory ?: "None",

                isLoading = false
            )
        }
    }

    fun onAvailabilityChanged(isAvailable: Boolean) {
        _uiState.update { it.copy(availability = isAvailable) }
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                supabaseClient.postgrest["worker"].update(
                    mapOf("worker_availability" to isAvailable)
                ) {
                    filter { eq("user_id", userId) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update availability: ${e.message}") }
            }
        }
    }

    fun onDayToggled(day: String) {
        val currentDays = _uiState.value.availableDays.toMutableList()
        if (currentDays.contains(day)) {
            currentDays.remove(day)
        } else {
            currentDays.add(day)
        }
        _uiState.update { it.copy(availableDays = currentDays) }

        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                val daysString = currentDays.joinToString(", ")
                supabaseClient.postgrest["worker"].update(
                    mapOf("worker_availableDays" to daysString)
                ) {
                    filter { eq("user_id", userId) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update days: ${e.message}") }
            }
        }
    }

    fun onUserNameChange(value: String) { _uiState.update { it.copy(userName = value) } }
    fun onPhoneChange(value: String) { _uiState.update { it.copy(phone = value) } }
    fun onGenderSelected(value: String) { _uiState.update { it.copy(gender = value) } }
    fun onAboutMeChange(value: String) { _uiState.update { it.copy(aboutMe = value) } }
    fun onAvatarSelected(uri: Uri) { _uiState.update { it.copy(selectedImageUri = uri) } }
    fun resetUpdateSuccess() { _uiState.update { it.copy(updateSuccess = false) } }
    fun clearError() { _uiState.update { it.copy(errorMessage = null) } }

    fun saveProfile(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
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

                val rawInputPhone = _uiState.value.phone
                    .removePrefix("+60")
                    .removePrefix("60")
                    .trim()
                val fullPhoneToSave = if (rawInputPhone.isNotBlank()) "+60$rawInputPhone" else ""

                val updateParams = buildMap {
                    put("full_name", _uiState.value.userName.trim())
                    put("phone", fullPhoneToSave)
                    put("gender", _uiState.value.gender)
                    put("about_me", _uiState.value.aboutMe.trim())
                    uploadedAvatarUrl?.let { put("avatar_url", it) }
                }

                supabaseClient.postgrest["profiles"].update(updateParams) {
                    filter { eq("id", userId) }
                }

                val jobSeekerUpdateParams = buildMap {
                    put("worker_name", _uiState.value.userName.trim())
                    put("worker_phoneNo", fullPhoneToSave)
                }
                supabaseClient.postgrest["worker"].update(jobSeekerUpdateParams) {
                    filter { eq("user_id", userId) }
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

    fun loadJobSeeker(userId: String) {
        val repo = repository ?: return
        viewModelScope.launch {
            try {
                val jobSeekerData = repo.getJobSeekerByUserId(userId)
                _uiState.update { it.copy(jobSeeker = jobSeekerData) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to load job seeker profile.") }
            }
        }
    }

    fun createJobSeeker(jobSeeker: JobSeeker) {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repo.createJobSeeker(jobSeeker)
                _uiState.update { it.copy(jobSeeker = jobSeeker, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to create job seeker profile.") }
            }
        }
    }

    fun updateJobSeeker(jobSeeker: JobSeeker) {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repo.updateJobSeeker(jobSeeker)
                _uiState.update { it.copy(jobSeeker = jobSeeker, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to update job seeker profile.") }
            }
        }
    }

    fun deleteJobSeeker(jobSeeker: JobSeeker) {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repo.deleteJobSeeker(jobSeeker)
                _uiState.update { it.copy(jobSeeker = null, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to delete job seeker profile.") }
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

class JobSeekerViewModelFactory(
    private val supabaseClient: SupabaseClient,
    private val repository: JobSeekerRepository? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobSeekerViewModel::class.java)) {
            return JobSeekerViewModel(supabaseClient, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}