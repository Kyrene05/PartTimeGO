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

    fun updateJob(job: JobEntity) {
        viewModelScope.launch {
            repository.updateJob(job)
        }
    }

    fun deleteJob(jobId: String) {
        viewModelScope.launch {
            repository.deleteJob(jobId)
        }
    }

    fun syncPending() {
        viewModelScope.launch {
            repository.syncPendingJobs()
        }
    }
}