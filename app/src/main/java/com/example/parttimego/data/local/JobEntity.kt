package com.example.parttimego.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val employerId: String,
    val title: String,
    val companyName: String?,
    val category: String,
    val salary: Double,
    val salaryPeriod: String,
    val workingDate: String?,
    val workingHoursStart: String?,
    val workingHoursEnd: String?,
    val location: String,
    val description: String?,
    val requirements: String?,
    val peopleNeeded: Int,
    val tag: String?,
    val createdAt: String,
    val syncStatus: String = "SYNCED" // "SYNCED", "PENDING_CREATE", "PENDING_UPDATE", "PENDING_DELETE"
)