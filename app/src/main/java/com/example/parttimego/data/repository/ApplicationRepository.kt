package com.example.parttimego.data.repository

import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.data.model.Worker
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.collections.emptyList

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

    suspend fun getApplicationsForEmployer(employerId: String): List<Triple<JobApplicationDto, JobSummaryDto, ApplicantProfileDto?>> {
        return try {
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

            val profiles = postgrest["worker"]
                .select {
                    filter { isIn("user_id", applicantIds) }
                }
                .decodeList<Worker>()

            val jobsById = jobs.associateBy { it.id }
            val workersByUserId = profiles.associateBy { it.userId }

            applications.mapNotNull { app ->
                val job = jobsById[app.jobId] ?: return@mapNotNull null
                val worker = workersByUserId[app.applicantId]

                val resolvedProfile= ApplicantProfileDto(
                    id=app.applicantId,
                    fullName=worker?.workerName
                )
                Triple(app,job,resolvedProfile)
            }
        } catch (e: Exception) {
            // Offline/network failure — return empty rather than crashing.
            emptyList()
        }
    }

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
                java.time.OffsetDateTime.parse(app.appliedAt).toInstant().toEpochMilli()
            } catch (e: Exception) {
                return@count false
            }
            appliedAtMillis >= oneWeekAgoMillis
        }
    }

    suspend fun updateApplicationStatus(applicationId: String, status: String) {
        try {
            postgrest["job_applications"].update(
                mapOf("status" to status)
            ) {
                filter { eq("id", applicationId) }
            }
        } catch (e: Exception) {
            // Offline/network failure — silently fail for now.
            // Consider surfacing this to the UI if you want user-visible feedback.
        }
    }

    // For Job Seeker
    suspend fun getApplicationsForJobSeeker(
        applicantId: String
    ): List<Pair<JobApplicationDto, JobSummaryDto>> {

        return try {

            val applications = postgrest["job_applications"]
                .select {
                    filter {
                        eq("applicant_id", applicantId)
                    }
                }
                .decodeList<JobApplicationDto>()

            if (applications.isEmpty()) {
                return emptyList()
            }

            val jobIds = applications.map { it.jobId }

            val jobs = postgrest["jobs"]
                .select {
                    filter {
                        isIn("id", jobIds)
                    }
                }
                .decodeList<JobSummaryDto>()

            val jobsById = jobs.associateBy { it.id }

            applications.mapNotNull { application ->
                val job = jobsById[application.jobId]

                if (job != null) {
                    Pair(application, job)
                } else {
                    null
                }
            }

        } catch (e: Exception) {
            emptyList()
        }
    }

    // Check if Job Seeker already applied
    suspend fun hasApplied(
        jobId: String,
        applicantId: String
    ): Boolean {

        return try {

            val applications =
                postgrest["job_applications"]
                    .select {
                        filter {
                            eq(
                                "job_id",
                                jobId
                            )

                            eq(
                                "applicant_id",
                                applicantId
                            )
                        }
                    }
                    .decodeList<JobApplicationDto>()

            applications.isNotEmpty()

        } catch (e: Exception) {
            false
        }
    }

    suspend fun applyForJob(
        jobId: String,
        applicantId: String
    ) {
        postgrest["job_applications"]
            .insert(
                mapOf(
                    "job_id" to jobId,
                    "applicant_id" to applicantId,
                    "status" to "pending",
                    "applied_at" to
                            java.time.OffsetDateTime
                                .now()
                                .toString()
                )
            )
    }

    // Cancel Application
    suspend fun cancelApplication(
        jobId: String,
        applicantId: String
    ) {

        postgrest["job_applications"]
            .delete {
                filter {

                    eq(
                        "job_id",
                        jobId
                    )

                    eq(
                        "applicant_id",
                        applicantId
                    )
                }
            }
    }
}