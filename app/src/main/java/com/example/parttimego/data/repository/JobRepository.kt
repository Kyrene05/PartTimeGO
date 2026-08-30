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
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("end_date") val endDate: String? = null,
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

    suspend fun deleteJob(jobId: String): Result<Unit> {
        return try {
            postgrest["jobs"].delete { filter { eq("id", jobId) } }
            jobDao.deleteJob(jobId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
    startDate = startDate, endDate = endDate, workingHoursStart = workingHoursStart, workingHoursEnd = workingHoursEnd,
    location = location, description = description, requirements = requirements,
    peopleNeeded = peopleNeeded, tag = tag, createdAt = createdAt
)

private fun JobDto.toEntity(syncStatus: String) = JobEntity(
    id = id, employerId = employerId, title = title, companyName = companyName,
    category = category, salary = salary, salaryPeriod = salaryPeriod,
    startDate = normalizeDateForDisplay(startDate),
    endDate = normalizeDateForDisplay(endDate),
    workingHoursStart = normalizeTimeForDisplay(workingHoursStart),
    workingHoursEnd = normalizeTimeForDisplay(workingHoursEnd),
    location = location, description = description, requirements = requirements,
    peopleNeeded = peopleNeeded, tag = tag, createdAt = createdAt ?: "", syncStatus = syncStatus
)
private fun normalizeDateForDisplay(raw: String?): String? {
    if (raw.isNullOrBlank()) return raw
    return try {
        // Already in display format? Try parsing it as-is first.
        LocalDate.parse(raw, DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        raw
    } catch (e: Exception) {
        try {
            // Otherwise assume it's Postgres ISO format (yyyy-MM-dd)
            LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e2: Exception) {
            raw // give up, show whatever we got rather than crash
        }
    }
}

private fun normalizeTimeForDisplay(raw: String?): String? {
    if (raw.isNullOrBlank()) return raw
    return try {
        LocalTime.parse(raw, DateTimeFormatter.ofPattern("h:mm a"))
        raw
    } catch (e: Exception) {
        try {
            // Postgres time format, e.g. "15:00:00"
            LocalTime.parse(raw).format(DateTimeFormatter.ofPattern("h:mm a"))
        } catch (e2: Exception) {
            raw
        }
    }
}