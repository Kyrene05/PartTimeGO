package com.example.parttimego.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.parttimego.data.model.Worker
import com.example.parttimego.data.repository.WorkerRepository

class WorkerViewModel(
    private val repository: WorkerRepository
) : ViewModel() {

    // Worker Profile
    var worker by mutableStateOf<Worker?>(null)
        private set

    // UI State
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadWorker(userId: String){
        // Get worker from repository
    }

    fun createWorker(worker: Worker){
        // Save worker through repository
    }

    fun updateWorker(worker: Worker) {
        // Update worker through repository
    }

    fun deleteWorker(worker: Worker){
        // Delete worker from repository
    }
}