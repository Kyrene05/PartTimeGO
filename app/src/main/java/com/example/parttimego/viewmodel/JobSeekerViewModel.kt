package com.example.parttimego.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class JobSeekerProfileDto(
    @SerialName("full_name") val userName: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("gender") val gender: String? = null,
    @SerialName("about_me") val aboutMe: String? = null,
    @SerialName("email") val email: String? = null
)

@Serializable
data class JobSeekerDetailDto(
    @SerialName("worker_id") val jobSeekerId: String? = null,
    @SerialName("worker_name") val jobSeekerName: String? = null,
    @SerialName("worker_phoneNo") val jobSeekerPhoneNo: String? = null,
    @SerialName("worker_availability") val jobSeekerAvailability: Boolean? = true,
    @SerialName("worker_availableDays") val jobSeekerAvailableDays: String? = null,
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
    val preferredStates: List<String> = emptyList(),
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
    private val repository: JobSeekerRepository? = null,
    autoLoadOwnProfile: Boolean = true
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobSeekerUiState())
    val uiState: StateFlow<JobSeekerUiState> = _uiState.asStateFlow()

    init {
        if (autoLoadOwnProfile) {
            loadUserProfile()
        }
    }

    fun loadUserProfile(targetUserId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val currentUser = supabaseClient.auth.currentUserOrNull()
                val userId = targetUserId ?: currentUser?.id ?: return@launch
                val authEmail = currentUser?.email.orEmpty()

                // 以 Map 形式穩定撈取 profiles 資料
                val responseList = supabaseClient.postgrest["profiles"]
                    .select { filter { eq("id", userId) } }
                    .decodeList<Map<String, JsonElement>>()

                val profileMap = responseList.firstOrNull()
                val profileEmail = profileMap?.get("email")?.jsonPrimitive?.content?.takeIf { it.isNotBlank() && it != "null" }

                // 優先使用 profiles 表裡的 email，如果沒有則退回使用 Auth 的 email
                val finalEmail = when {
                    !profileEmail.isNullOrBlank() -> profileEmail
                    authEmail.isNotBlank() -> authEmail
                    else -> ""
                }

                val profileDto = supabaseClient.postgrest["profiles"]
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<JobSeekerProfileDto>()

                val jobSeekerDetail = supabaseClient.postgrest["worker"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeSingleOrNull<JobSeekerDetailDto>()

                val authMetaData = currentUser?.userMetadata
                val registeredName = authMetaData?.get("full_name")?.jsonPrimitive?.content
                    ?: authMetaData?.get("user_name")?.jsonPrimitive?.content
                    ?: authMetaData?.get("name")?.jsonPrimitive?.content

                mapDtoToUiState(profileDto, jobSeekerDetail, finalEmail, registeredName)
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
                val jobSeekerDetail = supabaseClient.postgrest["worker"]
                    .select { filter { eq("user_id", jobSeekerId) } }
                    .decodeSingleOrNull<JobSeekerDetailDto>()

                var profile: JobSeekerProfileDto? = null
                if (!jobSeekerDetail?.userId.isNullOrBlank()) {
                    profile = supabaseClient.postgrest["profiles"]
                        .select { filter { eq("id", jobSeekerDetail!!.userId!!) } }
                        .decodeSingleOrNull<JobSeekerProfileDto>()
                }

                val targetEmail = profile?.email.orEmpty()

                var totalGigsCount = 0
                var recentGigs = emptyList<GigExperienceItem>()

                if (!jobSeekerDetail?.jobSeekerId.isNullOrBlank()) {
                    try {
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
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                mapDtoToUiState(profile, jobSeekerDetail, targetEmail, null)
                _uiState.update {
                    it.copy(
                        completedGigsCount = totalGigsCount,
                        gigExperiences = recentGigs
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
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

        val rawPhone = (profile?.phone ?: jobSeekerDetail?.jobSeekerPhoneNo).orEmpty().trim()
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

        val locations = jobSeekerDetail?.jobSeekerPreferredLocation
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        val states = jobSeekerDetail?.jobSeekerPreferredState
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

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
                preferredStates = states,
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

                val rawInputPhone = _uiState.value.phone
                    .removePrefix("+60")
                    .removePrefix("60")
                    .trim()
                val fullPhoneToSave = if (rawInputPhone.isNotBlank()) "+60$rawInputPhone" else ""

                val userEmail = currentUser.email.orEmpty()

                // 使用 upsert 確保即使 profiles 沒有該列也能順利新增/更新，同時寫入 email
                val profileUpdateParams = buildMap {
                    put("id", userId)
                    put("full_name", _uiState.value.userName.trim())
                    put("phone", fullPhoneToSave)
                    put("gender", _uiState.value.gender)
                    put("about_me", _uiState.value.aboutMe.trim())
                    if (userEmail.isNotBlank()) {
                        put("email", userEmail)
                    }
                    uploadedAvatarUrl?.let { put("avatar_url", it) }
                }

                supabaseClient.postgrest["profiles"].upsert(profileUpdateParams)

                val workerUpdateParams = buildMap {
                    put("worker_name", _uiState.value.userName.trim())
                }

                try {
                    supabaseClient.postgrest["worker"].update(workerUpdateParams) {
                        filter { eq("user_id", userId) }
                    }
                } catch (_: Exception) {
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        updateSuccess = true,
                        phone = fullPhoneToSave,
                        email = if (userEmail.isNotBlank()) userEmail else it.email,
                        avatarUrl = uploadedAvatarUrl,
                        selectedImageUri = null
                    )
                }
            } catch (e: Exception) {
                Log.e("JobSeekerVM", "Save profile failed", e)
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

    fun updateAvailability(isAvailable: Boolean) {
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

    // Save CSV string or list updates for Skills
    fun updateSkills(skillsCsv: String) {
        val skillsList = skillsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        _uiState.update { it.copy(skills = skillsList) }

        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                supabaseClient.postgrest["worker"].update(
                    mapOf("worker_skills" to skillsCsv)
                ) {
                    filter { eq("user_id", userId) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update skills: ${e.message}") }
            }
        }
    }

    // Save Preferred Locations
    fun updatePreferredLocation(locationsCsv: String) {
        val locationList = locationsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        _uiState.update { it.copy(preferredLocations = locationList) }

        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                supabaseClient.postgrest["worker"].update(
                    mapOf("worker_preferredLocation" to locationsCsv)
                ) {
                    filter { eq("user_id", userId) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update locations: ${e.message}") }
            }
        }
    }

    fun updatePreferredState(statesCsv: String) {
        val stateList = statesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
        _uiState.update { it.copy(preferredStates = stateList) }

        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                supabaseClient.postgrest["worker"].update(
                    mapOf("worker_preferredState" to statesCsv)
                ) {
                    filter { eq("user_id", userId) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update states: ${e.message}") }
            }
        }
    }

    // Save Work History
    fun updateWorkHistory(workHistoryText: String) {
        _uiState.update { it.copy(workHistory = workHistoryText) }

        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                supabaseClient.postgrest["worker"].update(
                    mapOf("worker_workHistory" to workHistoryText)
                ) {
                    filter { eq("user_id", userId) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to update work history: ${e.message}") }
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
    private val repository: JobSeekerRepository? = null,
    private val autoLoadOwnProfile: Boolean = true
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobSeekerViewModel::class.java)) {
            return JobSeekerViewModel(supabaseClient, repository, autoLoadOwnProfile) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}