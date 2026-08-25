package com.example.parttimego.data.repository


import android.content.Context
import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.data.local.AppDatabase
import com.example.parttimego.data.local.JobEntity
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

// DTO for Supabase — separate from JobEntity since Room and Postgrest
// have different serialization needs (e.g. snake_case columns)
@Serializable
data class JobDto(
    val id: String,
    @SerialName("employer_id") val employerId: String,
    val title: String,
    @SerialName("company_name") val companyName: String? = null,
    val category: String,
    val salary: Double,
    @SerialName("salary_period") val salaryPeriod: String,
    @SerialName("working_date") val workingDate: String? = null,
    @SerialName("working_hours_start") val workingHoursStart: String? = null,
    @SerialName("working_hours_end") val workingHoursEnd: String? = null,
    val location: String,
    val description: String? = null,
    val requirements: String? = null,
    @SerialName("people_needed") val peopleNeeded: Int,
    val tag: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

class JobRepository(context: Context) {
    private val jobDao = AppDatabase.getInstance(context).jobDao()
    private val postgrest get() = SupabaseClient.client.postgrest

    // READS: Room is the source of truth for the UI

    fun getJobsByEmployer(employerId: String): Flow<List<JobEntity>> {
        return jobDao.getJobsByEmployer(employerId)
    }

    // Call this to refresh Room from Supabase in the background.
    // UI doesn't wait on this, it already has Room's data via the Flow above.
    suspend fun refreshJobsFromRemote(employerId: String) {
        try {
            val remoteJobs = postgrest["jobs"]
                .select {
                    filter { eq("employer_id", employerId) }
                }
                .decodeList<JobDto>()

            jobDao.insertJobs(remoteJobs.map { it.toEntity(syncStatus = "SYNCED") })
        } catch (e: Exception) {
            // Fail-safe: refresh failed (offline/network issue) — Room's existing
            // cached data stays as-is, UI keeps showing it. No crash, no blank screen.
        }
    }

    //  WRITES: try remote first, fall back to local queue on failure

    suspend fun postJob(job: JobEntity) {
        // 1. Always write to Room immediately — instant UI feedback, works offline
        jobDao.insertJob(job.copy(syncStatus = "PENDING_CREATE"))

        // 2. Try pushing to Supabase
        try {
            postgrest["jobs"].insert(job.toDto())
            jobDao.updateSyncStatus(job.id, "SYNCED")
        } catch (e: Exception) {
            // 3. Failure: leave it as PENDING_CREATE. It'll be picked up by syncPendingJobs().
        }
    }

    suspend fun updateJob(job: JobEntity) {
        jobDao.updateJob(job.copy(syncStatus = "PENDING_UPDATE"))
        try {
            postgrest["jobs"].update(job.toDto()) {
                filter { eq("id", job.id) }
            }
            jobDao.updateSyncStatus(job.id, "SYNCED")
        } catch (e: Exception) {
            // stays PENDING_UPDATE, retried later
        }
    }

    suspend fun deleteJob(jobId: String) {
        try {
            postgrest["jobs"].delete { filter { eq("id", jobId) } }
            jobDao.deleteJob(jobId) // only remove locally once remote succeeds
        } catch (e: Exception) {
            // Fail-safe: if delete fails offline, we do NOT delete locally either —
            // otherwise the job would vanish from UI but still exist remotely,
            // which is worse than a stale-but-consistent state.
            // (Optional improvement: mark PENDING_DELETE and hide it in UI via a filter,
            // rather than fully deleting — ask me if you want that version instead.)
        }
    }

    // RETRY: call this whenever connectivity is restored

    suspend fun syncPendingJobs() {
        val pending = jobDao.getPendingJobs()
        for (job in pending) {
            try {
                when (job.syncStatus) {
                    "PENDING_CREATE" -> postgrest["jobs"].insert(job.toDto())
                    "PENDING_UPDATE" -> postgrest["jobs"].update(job.toDto()) {
                        filter { eq("id", job.id) }
                    }
                }
                jobDao.updateSyncStatus(job.id, "SYNCED")
            } catch (e: Exception) {
                // still offline — leave it pending, try again next time
            }
        }
    }
}

// Mapping helpers between Room's JobEntity and Supabase's JobDto

private fun JobEntity.toDto() = JobDto(
    id = id, employerId = employerId, title = title, companyName = companyName,
    category = category, salary = salary, salaryPeriod = salaryPeriod,
    workingDate = workingDate, workingHoursStart = workingHoursStart, workingHoursEnd = workingHoursEnd,
    location = location, description = description, requirements = requirements,
    peopleNeeded = peopleNeeded, tag = tag, createdAt = createdAt
)

private fun JobDto.toEntity(syncStatus: String) = JobEntity(
    id = id, employerId = employerId, title = title, companyName = companyName,
    category = category, salary = salary, salaryPeriod = salaryPeriod,
    workingDate = workingDate, workingHoursStart = workingHoursStart, workingHoursEnd = workingHoursEnd,
    location = location, description = description, requirements = requirements,
    peopleNeeded = peopleNeeded, tag = tag, createdAt = createdAt ?: "", syncStatus = syncStatus
)