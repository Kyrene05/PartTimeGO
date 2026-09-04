package com.example.parttimego.data.repository

import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.data.model.JobSeeker
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
    @SerialName("applied_at") val appliedAt: String? = null,
    @SerialName("employer_note") val employerNote: String? = null
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
                .decodeList<JobSeeker>()

            val jobsById = jobs.associateBy { it.id }
            val workersByUserId = profiles.associateBy { it.jobSeekerId }

            applications.mapNotNull { app ->
                val job = jobsById[app.jobId] ?: return@mapNotNull null
                val worker = workersByUserId[app.applicantId]

                val resolvedProfile= ApplicantProfileDto(
                    id=app.applicantId,
                    fullName=worker?.jobSeekerName
                )
                Triple(app,job,resolvedProfile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    suspend fun updateApplicationStatus(
        applicationId: String,
        status: String,
        employerNote: String? = null
    ) {
        try {
            val updateFields = buildMap<String, String> {
                put("status", status)
                employerNote?.let { put("employer_note", it) }
            }
            postgrest["job_applications"].update(updateFields) {
                filter { eq("id", applicationId) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

            // Filter out nulls BEFORE building the query — a job_id can be null
            // if the job was deleted (see on delete set null), and passing null
            // into isIn() breaks the query for every row, not just the null ones.
            val jobIds = applications.mapNotNull { it.jobId }.distinct()

            if (jobIds.isEmpty()) {
                return emptyList()
            }

            val jobs = postgrest["jobs"]
                .select {
                    filter {
                        isIn("id", jobIds)
                    }
                }
                .decodeList<JobSummaryDto>()

            val jobsById = jobs.associateBy { it.id }

            applications.mapNotNull { application ->
                val job = application.jobId?.let { jobsById[it] }
                if (job != null) Pair(application, job) else null
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
            val applications = postgrest["job_applications"]
                .select {
                    filter {
                        eq("job_id", jobId)
                        eq("applicant_id", applicantId)
                    }
                }
                .decodeList<JobApplicationDto>()

            // Only pending/accepted count as "actively applied" — a job the
            // employer already rejected, or one the applicant already resolved
            // (done_accepted/done_rejected), shouldn't block re-applying.
            applications.any {
                it.status == "pending" || it.status == "accepted" || it.status == "done_accepted"
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun applyForJob(
        jobId: String,
        applicantId: String
    ) {
        android.util.Log.d("DebugApply", "applyForJob CALLED: jobId=$jobId, applicantId=$applicantId")
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
                    eq("job_id", jobId)
                    eq("applicant_id", applicantId)
                    eq("status", "pending")
                }
            }
    }
}