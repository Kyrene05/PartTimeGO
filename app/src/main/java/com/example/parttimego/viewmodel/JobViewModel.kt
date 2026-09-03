package com.example.parttimego.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.parttimego.data.local.JobEntity
import com.example.parttimego.data.repository.JobRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class JobViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = JobRepository(application)

    fun getJobsForEmployer(employerId: String): Flow<List<JobEntity>> {
        return repository.getJobsByEmployer(employerId)
    }

    fun getAllJobs(): Flow<List<JobEntity>> {
        return repository.getAllJobs()
    }

    fun refreshAllJobs() {
        viewModelScope.launch {
            repository.refreshAllJobsFromRemote()
        }
    }

    fun refreshJobs(employerId: String) {
        viewModelScope.launch {
            repository.refreshJobsFromRemote(employerId)
        }
    }

    fun postJob(job: JobEntity) {
        viewModelScope.launch {
            repository.postJob(job)
        }
    }

    fun updateJob(job: JobEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateJob(job)
            onComplete()
        }
    }

    fun deleteJob(jobId: String, onComplete: (Result<Unit>) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteJob(jobId)
            onComplete(result)
        }
    }

    fun syncPending() {
        viewModelScope.launch {
            repository.syncPendingJobs()
        }
    }
}