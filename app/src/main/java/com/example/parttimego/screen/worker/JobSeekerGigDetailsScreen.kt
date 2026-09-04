package com.example.parttimego.screen.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parttimego.data.local.JobEntity
import com.example.parttimego.data.repository.ApplicationRepository
import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.nav.JobSeekerNavBar
import com.example.parttimego.nav.JobSeekerNavItem
import com.example.parttimego.viewmodel.JobViewModel
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.util.Locale

private val Purple = Color(0xFF262075)
private val Green = Color(0xFF22A447)
private val LightBlue = Color(0xFFEAF4FF)
private val GreyText = Color(0xFF777777)
private val DarkText = Color(0xFF222222)
private val Red = Color(0xFFE53935)
private val LightRed = Color(0xFFFFEBEE)

@Composable
fun JobSeekerGigDetailScreen(
    jobId: String,
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onCompanyClick: (String) -> Unit = {},
    onAppliedClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    jobViewModel: JobViewModel = viewModel()
) {

    val scope = rememberCoroutineScope()
    val repository = remember { ApplicationRepository() }

    val userId =
        SupabaseClient.client
            .auth
            .currentUserOrNull()
            ?.id

    val jobs by jobViewModel
        .getAllJobs()
        .collectAsState(initial = emptyList())

    val job = jobs.find {
        it.id == jobId
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isBookmarked by remember {
        mutableStateOf(false)
    }

    var isApplied by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    if (errorMessage != null) {

        AlertDialog(
            onDismissRequest = {
                errorMessage = null
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        errorMessage = null
                    }
                ) {
                    Text("OK")
                }
            },
            title = {
                Text("Error")
            },
            text = {
                Text(errorMessage!!)
            }
        )
    }

    var showApplyDialog by remember {
        mutableStateOf(false)
    }

    var showCancelDialog by remember {
        mutableStateOf(false)
    }

    // Load Job
    LaunchedEffect(Unit) {
        jobViewModel.refreshAllJobs()
    }

    LaunchedEffect(jobs) {
        if (jobs.isNotEmpty()) {
            isLoading = false
        }
    }

    LaunchedEffect(jobId, userId) {

        if (userId != null) {

            isApplied =
                repository.hasApplied(
                    jobId = jobId,
                    applicantId = userId
                )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Purple)
                .padding(
                    top = 32.dp,
                    bottom = 18.dp
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBackClick
                ) {

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Gig Details",
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        isBookmarked = !isBookmarked
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(LightBlue)
                ) {

                    Icon(
                        imageVector =
                            if (isBookmarked)
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,
                        contentDescription = "Bookmark",
                        tint = Purple
                    )
                }
            }
        }

        // Content
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Loading gig...",
                    color = GreyText
                )
            }

        } else if (job == null) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Gig not found.",
                    color = GreyText,
                    fontSize = 16.sp
                )
            }

        } else {

            val currentJob = job!!

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 20.dp,
                    bottom = 16.dp
                ),
                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {
                // Job Title
                item {

                    Row(
                        verticalAlignment =
                            Alignment.Top
                    ) {

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    currentJob.title,
                                fontSize = 25.sp,
                                fontWeight =
                                    FontWeight.Bold,
                                color = DarkText
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(5.dp)
                            )

                            Text(
                                text = job.companyName ?: "Company",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                                ),
                                modifier = Modifier
                                    .clickable {
                                        onCompanyClick(job.employerId)
                                    }
                                    .padding(vertical = 2.dp)
                            )
                        }

                        if (
                            currentJob.tag
                                ?.lowercase() == "hot"
                        ) {

                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            50.dp
                                        )
                                    )
                                    .background(
                                        LightRed
                                    )
                                    .padding(
                                        horizontal = 10.dp,
                                        vertical = 5.dp
                                    )
                            ) {

                                Text(
                                    text = "Hot",
                                    color = Red,
                                    fontSize = 12.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Salary
                item {
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            text =
                                "RM ${formatSalary(currentJob.salary)}",
                            color = Purple,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text =
                                " / ${currentJob.salaryPeriod}",
                            color = Purple,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Information Box
                item {
                    Column(
                        verticalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {
                            InfoBox(
                                modifier =
                                    Modifier.weight(1f),
                                title = "Working Hours",
                                value = formatWorkingHours(
                                    currentJob.workingHoursStart,
                                    currentJob.workingHoursEnd
                                )
                            )
                            InfoBox(
                                modifier =
                                    Modifier.weight(1f),
                                title = "Working Dates",
                                value = formatWorkingDate(
                                    currentJob.startDate,
                                    currentJob.endDate
                                )
                            )
                        }

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {

                            InfoBox(
                                modifier =
                                    Modifier.weight(1f),
                                title = "Duration",
                                value = calculateDuration(
                                    currentJob.startDate,
                                    currentJob.endDate
                                )
                            )

                            InfoBox(
                                modifier =
                                    Modifier.weight(1f),
                                title = "Vacancies",
                                value =
                                    "${currentJob.peopleNeeded} spots left"
                            )
                        }
                    }
                }

                // Location
                item {

                    InfoBox(
                        modifier =
                            Modifier.fillMaxWidth(),
                        title = "Location",
                        value = currentJob.location
                    )
                }

                // Description
                item {
                    Column {
                        Text(
                            text = "Job Description",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text =
                                currentJob.description
                                    ?: "No description provided.",
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = GreyText
                        )
                    }
                }

                // Requirement
                item {

                    Column {

                        Text(
                            text = "Requirements",
                            fontSize = 18.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color = DarkText
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(
                            text =
                                currentJob.requirements
                                    ?: "No specific requirements.",
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = GreyText
                        )
                    }
                }

            }
        }

        // Apply Button
        if (job != null) {

            Button(
                onClick = {

                    if (isApplied) {
                        showCancelDialog = true
                    } else {
                        showApplyDialog = true
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                    .height(52.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (isApplied)
                            Color.Gray
                        else
                            Green
                ),

                shape =
                    RoundedCornerShape(10.dp)
            ) {

                Text(
                    text =
                        if (isApplied)
                            "Applied"
                        else
                            "Apply Now",

                    fontSize = 16.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        JobSeekerNavBar(
            selectedItem =
                JobSeekerNavItem.HOME,
            onHomeClick = onHomeClick,
            onExploreClick = onExploreClick,
            onAppliedClick = onAppliedClick,
            onProfileClick = onProfileClick
        )
    }

    // APPLY DIALOG
    if (showApplyDialog && job != null) {

        AlertDialog(
            onDismissRequest = {
                showApplyDialog = false
            },

            title = {
                Text("Confirm Application")
            },

            text = {
                Text(
                    "Are you sure you want to apply for this gig?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        if (userId != null) {

                            scope.launch {

                                try {

                                    repository.applyForJob(
                                        jobId = job.id,
                                        applicantId = userId
                                    )

                                    isApplied = true

                                } catch (e: Exception) {

                                    errorMessage =
                                        e.message ?: "Unable to apply for this job."
                                } finally {
                                    showApplyDialog = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showApplyDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // CANCEL DIALOG
    if (showCancelDialog && job != null) {

        AlertDialog(
            onDismissRequest = {
                showCancelDialog = false
            },

            title = {
                Text("Cancel Application")
            },

            text = {
                Text(
                    "Are you sure you want to cancel your application?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        if (userId != null) {

                            scope.launch {

                                try {

                                    repository.cancelApplication(
                                        jobId = job.id,
                                        applicantId = userId
                                    )

                                    isApplied = false

                                } catch (e: Exception) {

                                    errorMessage =
                                        e.message ?: "Unable to cancel application."
                                } finally {
                                    showCancelDialog = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showCancelDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun InfoBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
        color = Color.White
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = title,
                fontSize = 12.sp,
                color = GreyText
            )

            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight =
                    FontWeight.Bold,
                color = DarkText
            )
        }
    }
}


private fun formatSalary(
    salary: Double
): String {

    return if (salary % 1.0 == 0.0) {
        salary.toInt().toString()
    } else {
        String.format(
            Locale.US,
            "%.2f",
            salary
        )
    }
}


private fun formatWorkingHours(
    start: String?,
    end: String?
): String {

    if (
        start.isNullOrBlank() &&
        end.isNullOrBlank()
    ) {
        return "-"
    }

    if (start.isNullOrBlank()) {
        return end ?: "-"
    }

    if (end.isNullOrBlank()) {
        return start
    }

    return "$start – $end"
}


private fun formatWorkingDate(
    start: String?,
    end: String?
): String {

    if (start.isNullOrBlank()) {
        return "-"
    }

    if (
        end.isNullOrBlank() ||
        start == end
    ) {
        return start
    }

    return "$start – $end"
}


private fun calculateDuration(
    start: String?,
    end: String?
): String {

    if (
        start.isNullOrBlank() ||
        end.isNullOrBlank()
    ) {
        return "-"
    }

    return if (start == end) {
        "1 day"
    } else {
        "Multiple days"
    }
}