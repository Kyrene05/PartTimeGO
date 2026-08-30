package com.example.parttimego.data.repository

import com.example.parttimego.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobApplicationDto(
    val id: String,
    @SerialName("job_id") val jobId: String,
    @SerialName("applicant_id") val applicantId: String,
    val status: String,
    @SerialName("applied_at") val appliedAt: String? = null
)

@Serializable
data class JobSummaryDto(
    val id: String,
    val title: String,
    val category: String,
    val location: String,
    val salary: Double,
    @SerialName("salary_period") val salaryPeriod: String,
    @SerialName("employer_id") val employerId: String
)

@Serializable
data class ApplicantProfileDto(
    val id: String,
    @SerialName("full_name") val fullName: String? = null
)
class ApplicationRepository {
    private val postgrest get() = SupabaseClient.client.postgrest

    suspend fun getApplicationCountForEmployer(employerId: String): Int {
        return getApplicationsForEmployer(employerId).size
    }

    suspend fun getPendingReviewCount(employerId: String): Int {
        return getApplicationsForEmployer(employerId).count { (app, _, _) -> app.status == "pending" }
    }

    suspend fun getThisWeekHiresCount(employerId: String): Int {
        val oneWeekAgoMillis = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
        return getApplicationsForEmployer(employerId).count { (app, _, _) ->
            if (app.status != "accepted") return@count false
            val appliedAtMillis = try {
                java.time.Instant.parse(app.appliedAt).toEpochMilli()
            } catch (e: Exception) {
                return@count false
            }
            appliedAtMillis >= oneWeekAgoMillis
        }
    }

    // Fetch every application for jobs owned by this employer, joined with job info
    suspend fun getApplicationsForEmployer(employerId: String): List<Triple<JobApplicationDto, JobSummaryDto, ApplicantProfileDto?>> {
        val jobs = postgrest["jobs"]
            .select {
                filter { eq("employer_id", employerId) }
            }
            .decodeList<JobSummaryDto>()

        if (jobs.isEmpty()) return emptyList()

        val jobIds = jobs.map { it.id }

        val applications = postgrest["job_applications"]
            .select {
                filter { isIn("job_id", jobIds) }
            }
            .decodeList<JobApplicationDto>()

        if (applications.isEmpty()) return emptyList()

        val applicantIds = applications.map { it.applicantId }.distinct()

        val profiles = postgrest["profiles"]
            .select {
                filter { isIn("id", applicantIds) }
            }
            .decodeList<ApplicantProfileDto>()

        val jobsById = jobs.associateBy { it.id }
        val profilesById = profiles.associateBy { it.id }

        return applications.mapNotNull { app ->
            val job = jobsById[app.jobId] ?: return@mapNotNull null
            val profile = profilesById[app.applicantId]
            Triple(app, job, profile)
        }
    }

    suspend fun updateApplicationStatus(applicationId: String, status: String) {
        postgrest["job_applications"].update(
            mapOf("status" to status)
        ) {
            filter { eq("id", applicationId) }
        }
    }
}