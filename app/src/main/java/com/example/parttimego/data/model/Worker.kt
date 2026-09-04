package com.example.parttimego.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobSeeker(
    @SerialName("worker_id")
    val jobSeekerId: String = "",

    @SerialName("user_id")
    val userId: String = "",

    @SerialName("worker_name")
    val jobSeekerName: String = "",

    @SerialName("worker_phoneNo")
    val jobSeekerPhoneNo: String = "",

    @SerialName("worker_email")
    val jobSeekerEmail: String = "",

    @SerialName("worker_availability")
    val jobSeekerAvailability: Boolean = true,

    @SerialName("worker_availabilityDay")
    val jobSeekerAvailabilityDay: String = "",

    @SerialName("worker_preferredJobCategories")
    val jobSeekerPreferredJobCategories: String = "",

    @SerialName("worker_skills")
    val jobSeekerSkills: String = "",

    @SerialName("worker_preferredState")
    val jobSeekerPreferredState: String = "",

    @SerialName("worker_preferredLocation")
    val jobSeekerPreferredLocation: String = "",

    @SerialName("worker_workHistory")
    val jobSeekerWorkHistory: String = "",

    @SerialName("worker_createdAt")
    val jobSeekerCreatedAt: String? = null,

    @SerialName("worker_updatedAt")
    val jobSeekerUpdatedAt: String? = null
)