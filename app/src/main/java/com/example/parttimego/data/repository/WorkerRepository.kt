package com.example.parttimego.data.repository


import com.example.parttimego.data.model.Worker
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class WorkerRepository(
    private val supabase: SupabaseClient
){
    // make sure the userId is match with workerId
    suspend fun getWorkerByUserId (userId: String): Worker? {
        return supabase
            .from("worker")
            .select {
                filter{
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<Worker>()
    }

    // CRUD
    suspend fun createWorker (worker: Worker){
        supabase
            .from("worker")
            .insert(worker)
    }

    suspend fun updateWorker (worker: Worker){
        supabase
            .from("worker")
            .update(worker) {
                filter {
                    eq("worker_id", worker.workerId)
                }
            }
    }

    suspend fun deleteWorker (worker: Worker){
        supabase
            .from("worker")
            .delete{
                filter {
                    eq("worker_id", worker.workerId)
                }
            }
    }
}