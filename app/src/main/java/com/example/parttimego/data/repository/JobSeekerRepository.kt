package com.example.parttimego.data.repository

import com.example.parttimego.data.model.JobSeeker
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class JobSeekerRepository(
    private val supabase: SupabaseClient
) {
    // 根据 userId 获取 JobSeeker
    suspend fun getJobSeekerByUserId(userId: String): JobSeeker? {
        return supabase
            .from("worker")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<JobSeeker>()
    }

    // CRUD 操作
    suspend fun createJobSeeker(jobSeeker: JobSeeker) {
        supabase
            .from("worker")
            .insert(jobSeeker)
    }

    suspend fun updateJobSeeker(jobSeeker: JobSeeker) {
        supabase
            .from("worker")
            .update(jobSeeker) {
                filter {
                    eq("worker_id", jobSeeker.jobSeekerId)
                }
            }
    }

    suspend fun deleteJobSeeker(jobSeeker: JobSeeker) {
        supabase
            .from("worker")
            .delete {
                filter {
                    eq("worker_id", jobSeeker.jobSeekerId)
                }
            }
    }
}