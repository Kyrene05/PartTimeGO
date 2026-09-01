package com.example.parttimego.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Worker(
    @SerialName("worker_id")
    val workerId: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("worker_name")
    val workerName: String,

    @SerialName("worker_phoneNo")
    val workerPhoneNo: String,

    @SerialName("worker_availability")
    val workerAvailability: Boolean,

    @SerialName("worker_preferredJobCategories")
    val workerPreferredJobCategories: String,

    @SerialName("worker_skills")
    val workerSkills: String,

    @SerialName("worker_preferredState")
    val workerPreferredState: String,

    @SerialName("worker_preferredLocation")
    val workerPreferredLocation: String,

    @SerialName("worker_workHistory")
    val workerWorkHistory: String,

    @SerialName("worker_createdAt")
    val workerCreatedAt: String,

    @SerialName("worker_updatedAt")
    val workerUpdatedAt: String
)