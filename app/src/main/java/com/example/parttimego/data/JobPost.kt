package com.example.parttimego.data

import kotlin.time.Duration

data class JobPost(
    val id:String,
    val title:String,
    val companyOrLocation: String,
    val salary: String,
    val tag: String,
    val durationLabel:String,
    val extraLocation:String?=null,
    val postedDate:String?=null
)
