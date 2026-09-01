package com.example.parttimego.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parttimego.data.model.Worker
import com.example.parttimego.data.repository.WorkerRepository
import kotlinx.coroutines.launch

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
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                worker = repository.getWorkerByUserId(userId)
                if (worker == null) {
                    errorMessage = "Worker profile not found."
                }
            } catch (e: Exception) {
                errorMessage = e.message?: "Failed to load worker profile."
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun createWorker(worker: Worker){
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
        }
        try {
            repository.createWorker(worker)
            // use to update UI immediately
            this@WorkerViewModel.worker = worker
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load worker profile"
        } finally {
            isLoading = false
        }
    }

    fun updateWorker(worker: Worker) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                repository.updateWorker(worker)
                // use to update UI immediately
                this@WorkerViewModel.worker = worker
            } catch (e: Exception) {
                errorMessage = e.message ?:"Failed to create worker profile"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteWorker(worker: Worker){
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                repository.deleteWorker(worker)
                // Remove worker from current UI state
                this@WorkerViewModel.worker = null
            } catch (e: Exception) {
                errorMessage = e.message?: "Failed to delete worker profile"
            } finally {
                isLoading = false
            }
        }
    }

    // Clear error message
    fun clearError(){
        errorMessage = null
    }
}