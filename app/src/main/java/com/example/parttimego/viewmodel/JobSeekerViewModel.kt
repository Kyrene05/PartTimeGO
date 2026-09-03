package com.example.parttimego.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.parttimego.data.model.Worker
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
data class WorkerProfileDto(
    @SerialName("full_name") val userName: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("gender") val gender: String? = null,
    @SerialName("about_me") val aboutMe: String? = null
)

@Serializable
data class WorkerDetailDto(
    @SerialName("worker_id") val workerId: String? = null,
    @SerialName("worker_name") val workerName: String? = null,
    @SerialName("worker_phoneNo") val workerPhoneNo: String? = null,
    @SerialName("worker_availability") val workerAvailability: Boolean? = true,
    @SerialName("worker_availableDays") val workerAvailableDays: String? = null, // 存储如 "Mon, Tue, Sat"
    @SerialName("worker_skills") val workerSkills: String? = null,
    @SerialName("worker_preferredLocation") val workerPreferredLocation: String? = null,
    @SerialName("worker_preferredState") val workerPreferredState: String? = null,
    @SerialName("worker_preferredJobCategories") val workerPreferredJobCategories: String? = null,
    @SerialName("worker_workHistory") val workerWorkHistory: String? = null,
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

data class WorkerUiState(
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

    val worker: Worker? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val updateSuccess: Boolean = false,
    val errorMessage: String? = null
)

class WorkerViewModel(
    private val supabaseClient: SupabaseClient,
    private val repository: JobSeekerRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerUiState())
    val uiState: StateFlow<WorkerUiState> = _uiState.asStateFlow()

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
                    .decodeSingleOrNull<WorkerProfileDto>()

                val workerDetail = supabaseClient.postgrest["workers"]
                    .select { filter { eq("user_id", userId) } }
                    .decodeSingleOrNull<WorkerDetailDto>()

                mapDtoToUiState(profile, workerDetail, email, registeredName)
                loadWorker(userId)
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


    fun loadWorkerByWorkerId(workerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val workerDetail = supabaseClient.postgrest["workers"]
                    .select { filter { eq("worker_id", workerId) } }
                    .decodeSingleOrNull<WorkerDetailDto>()

                var profile: WorkerProfileDto? = null
                if (!workerDetail?.userId.isNullOrBlank()) {
                    profile = supabaseClient.postgrest["profiles"]
                        .select { filter { eq("id", workerDetail!!.userId!!) } }
                        .decodeSingleOrNull<WorkerProfileDto>()
                }

                var totalGigsCount = 0
                var recentGigs = emptyList<GigExperienceItem>()

                if (!workerDetail?.workerId.isNullOrBlank()) {
                    // 计算完成的总 Gigs 数
                    val allCompletedGigs = supabaseClient.postgrest["gigs"]
                        .select {
                            filter {
                                eq("worker_id", workerDetail!!.workerId!!)
                                eq("status", "COMPLETED")
                            }
                        }
                        .decodeList<GigHistoryDto>()

                    totalGigsCount = allCompletedGigs.size

                    recentGigs = supabaseClient.postgrest["gigs"]
                        .select {
                            filter {
                                eq("worker_id", workerDetail!!.workerId!!)
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

                mapDtoToUiState(profile, workerDetail, "", null)
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
                        errorMessage = "Failed to load worker detail: ${e.message}"
                    )
                }
            }
        }
    }

    private fun mapDtoToUiState(
        profile: WorkerProfileDto?,
        workerDetail: WorkerDetailDto?,
        email: String,
        registeredName: String?
    ) {
        val finalUserName = when {
            !workerDetail?.workerName.isNullOrBlank() -> workerDetail.workerName
            !profile?.userName.isNullOrBlank() -> profile.userName
            !registeredName.isNullOrBlank() -> registeredName
            email.isNotBlank() -> email.substringBefore("@")
            else -> ""
        }

        val rawPhone = (workerDetail?.workerPhoneNo ?: profile?.phone).orEmpty().trim()
        val formattedPhone = when {
            rawPhone.isBlank() -> ""
            rawPhone.startsWith("+60") -> rawPhone
            rawPhone.startsWith("60") -> "+$rawPhone"
            else -> "+60$rawPhone"
        }

        val parsedSkills = workerDetail?.workerSkills
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        val locations = mutableListOf<String>()
        workerDetail?.workerPreferredLocation?.let { if (it.isNotBlank()) locations.add(it) }
        workerDetail?.workerPreferredState?.let { if (it.isNotBlank()) locations.add(it) }

        val parsedCategories = workerDetail?.workerPreferredJobCategories
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        val parsedDays = workerDetail?.workerAvailableDays
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

                availability = workerDetail?.workerAvailability ?: true,
                availableDays = parsedDays,
                skills = parsedSkills,
                preferredLocations = locations,
                preferredCategories = parsedCategories,
                workHistory = workerDetail?.workerWorkHistory ?: "None",

                isLoading = false
            )
        }
    }

    fun onAvailabilityChanged(isAvailable: Boolean) {
        _uiState.update { it.copy(availability = isAvailable) }
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                supabaseClient.postgrest["workers"].update(
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
                supabaseClient.postgrest["workers"].update(
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

                val workerUpdateParams = buildMap {
                    put("worker_name", _uiState.value.userName.trim())
                    put("worker_phoneNo", fullPhoneToSave)
                }
                supabaseClient.postgrest["workers"].update(workerUpdateParams) {
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

    fun loadWorker(userId: String) {
        val repo = repository ?: return
        viewModelScope.launch {
            try {
                val workerData = repo.getWorkerByUserId(userId)
                _uiState.update { it.copy(worker = workerData) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to load worker profile.") }
            }
        }
    }

    fun createWorker(worker: Worker) {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repo.createWorker(worker)
                _uiState.update { it.copy(worker = worker, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to create worker profile.") }
            }
        }
    }

    fun updateWorker(worker: Worker) {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repo.updateWorker(worker)
                _uiState.update { it.copy(worker = worker, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to update worker profile.") }
            }
        }
    }

    fun deleteWorker(worker: Worker) {
        val repo = repository ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repo.deleteWorker(worker)
                _uiState.update { it.copy(worker = null, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to delete worker profile.") }
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

class WorkerViewModelFactory(
    private val supabaseClient: SupabaseClient,
    private val repository: JobSeekerRepository? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkerViewModel::class.java)) {
            return WorkerViewModel(supabaseClient, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}