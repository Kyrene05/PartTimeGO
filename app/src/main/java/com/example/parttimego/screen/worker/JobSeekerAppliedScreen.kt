package com.example.parttimego.screen.worker

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.data.repository.ApplicationRepository
import com.example.parttimego.data.repository.JobApplicationDto
import com.example.parttimego.data.repository.JobSummaryDto
import com.example.parttimego.nav.JobSeekerNavBar
import com.example.parttimego.nav.JobSeekerNavItem
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

private val Purple = Color(0xFF262075)
private val Green = Color(0xFF22A447)
private val Orange = Color(0xFFFF9800)
private val Blue = Color(0xFF1976D2)
private val Grey = Color(0xFF777777)


@Composable
fun JobSeekerAppliedScreen(
    onHomeClick: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {

    val repository = remember {
        ApplicationRepository()
    }

    val scope = rememberCoroutineScope()

    val userId =
        SupabaseClient.client
            .auth
            .currentUserOrNull()
            ?.id

    android.util.Log.d("DebugUserId", "JobSeekerAppliedScreen opened, userId=$userId")

    var applications by remember {
        mutableStateOf(
            emptyList<
                    Pair<
                            JobApplicationDto,
                            JobSummaryDto
                            >
                    >()
        )
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    val refreshApplications: () -> Unit = {
        if (userId != null) {
            scope.launch {
                isLoading = true
                applications = repository.getApplicationsForJobSeeker(
                    applicantId = userId
                )
                isLoading = false
            }
        }
    }

    LaunchedEffect(userId) {
        refreshApplications()
    }

    val total =
        applications.size

    val accepted =
        applications.count {
            it.first.status == "accepted"
        }

    val pending =
        applications.count {
            it.first.status == "pending"
        }

    val done =
        applications.count {
            it.first.status == "done_accepted" ||
                    it.first.status == "done_rejected"
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // HEADER
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Purple)
                .padding(20.dp)
        ) {
            Text(
                text = "My Applications",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = "Track your job applications",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            // Status Row
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                ApplicationStatCard(
                    modifier =
                        Modifier.weight(1f),
                    title = "Applied",
                    value = total.toString(),
                    color = Purple
                )

                ApplicationStatCard(
                    modifier =
                        Modifier.weight(1f),
                    title = "Pending",
                    value = pending.toString(),
                    color = Orange
                )

                ApplicationStatCard(
                    modifier =
                        Modifier.weight(1f),
                    title = "Accepted",
                    value = accepted.toString(),
                    color = Green
                )

                ApplicationStatCard(
                    modifier =
                        Modifier.weight(1f),
                    title = "Done",
                    value = done.toString(),
                    color = Blue
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),

            contentPadding =
                PaddingValues(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {
            if (isLoading) {

                item {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "Loading applications...",
                            color = Grey
                        )
                    }
                }

            } else if (applications.isEmpty()) {

                item {

                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text =
                                "You haven't applied for any gigs yet.",
                            color = Grey
                        )
                    }
                }

            } else {

                items(
                    items = applications,
                    key = {
                        it.first.id
                    }
                ) { pair ->

                    val application = pair.first
                    val job = pair.second

                    ApplicationCard(
                        application = application,
                        job = job,

                        onAcceptOffer = {

                            scope.launch {

                                repository.updateApplicationStatus(
                                    application.id,
                                    "done_accepted"
                                )

                                refreshApplications()
                            }
                        },

                        onRejectOffer = {

                            scope.launch {

                                repository.updateApplicationStatus(
                                    application.id,
                                    "done_rejected"
                                )

                                refreshApplications()
                            }
                        }
                    )
                }
            }
        }

        // BOTTOM NAVIGATION
        JobSeekerNavBar(
            selectedItem =
                JobSeekerNavItem.APPLIED,

            onHomeClick =
                onHomeClick,

            onExploreClick =
                onExploreClick,

            onAppliedClick = {},

            onProfileClick =
                onProfileClick
        )
    }
}


@Composable
private fun ApplicationStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
        color = Color.White
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 12.dp
            ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = value,
                color = color,
                fontSize = 20.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(3.dp)
            )

            Text(
                text = title,
                color = Grey,
                fontSize = 10.sp
            )
        }
    }
}


@Composable
private fun ApplicationCard(
    application: JobApplicationDto,
    job: JobSummaryDto,
    onAcceptOffer: () -> Unit,
    onRejectOffer: () -> Unit
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
        color = Color.White
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Text(
                text = job.title,
                fontSize = 17.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )

            Text(
                text = job.category,
                fontSize = 13.sp,
                color = Grey
            )

            Spacer(
                modifier =
                    Modifier.height(5.dp)
            )

            Text(
                text = job.location,
                fontSize = 13.sp,
                color = Grey
            )

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text =
                    "RM ${job.salary} / ${job.salaryPeriod}",
                color = Purple,
                fontSize = 14.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            when (application.status) {

                "pending" -> {

                    StatusText(
                        text =
                            "Waiting for employer response",
                        color = Orange
                    )
                }

                "rejected" -> {
                    StatusText(
                        text = "Not selected this time",
                        color = Grey
                    )
                }

                "accepted" -> {
                    Text(
                        text = "Application Accepted!",
                        color = Green,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    if (!application.employerNote.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"${application.employerNote}\"",
                            color = Grey,
                            fontSize = 13.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        Button(
                            onClick = onAcceptOffer
                        ) {
                            Text("Accept Offer")
                        }

                        OutlinedButton(
                            onClick = onRejectOffer
                        ) {
                            Text("Reject Offer")
                        }
                    }
                }

                "done_accepted" -> {

                    StatusText(
                        text =
                            "Done — Accepted Offer",
                        color = Blue
                    )
                }

                "done_rejected" -> {

                    StatusText(
                        text =
                            "Done — Declined Offer",
                        color = Grey
                    )
                }

                else -> {

                    StatusText(
                        text =
                            application.status,
                        color = Grey
                    )
                }
            }
        }
    }
}


@Composable
private fun StatusText(
    text: String,
    color: Color
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color.copy(alpha = 0.08f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {

        Text(
            text = text,
            color = color,
            fontSize = 13.sp,
            fontWeight =
                FontWeight.Medium
        )
    }
}