package com.example.parttimego.screen.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.parttimego.data.repository.ApplicationRepository
import com.example.parttimego.data.repository.JobApplicationDto
import com.example.parttimego.data.repository.JobSummaryDto
import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.nav.JobSeekerNavBar
import com.example.parttimego.nav.JobSeekerNavItem
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

private val Purple = Color(0xFF7B61FF)
private val LightPurple = Color(0xFFF1EEFF)
private val Green = Color(0xFF22A447)
private val LightGreen = Color(0xFFE8F7ED)
private val Orange = Color(0xFFFF9800)
private val LightOrange = Color(0xFFFFF3E0)
private val Blue = Color(0xFF2196F3)
private val LightBlue = Color(0xFFEAF4FF)
private val DarkText = Color(0xFF222222)
private val GreyText = Color(0xFF777777)

@Composable
fun JobSeekerAppliedScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {

    val repository = remember {
        ApplicationRepository()
    }

    val scope = rememberCoroutineScope()

    var applications by remember {
        mutableStateOf(
            emptyList<Pair<JobApplicationDto, JobSummaryDto>>()
        )
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    val currentUserId =
        SupabaseClient.client.auth.currentUserOrNull()?.id

    // Load applications
    LaunchedEffect(currentUserId) {

        if (currentUserId != null) {

            isLoading = true

            applications =
                repository.getApplicationsForJobSeeker(
                    currentUserId
                )

            isLoading = false
        } else {
            isLoading = false
        }
    }

    // Statistics
    val totalApplications =
        applications.size

    val acceptedCount =
        applications.count {
            it.first.status == "accepted"
        }

    val pendingCount =
        applications.count {
            it.first.status == "pending"
        }

    val doneCount =
        applications.count {
            it.first.status == "done_accepted" ||
                    it.first.status == "done_rejected"
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
                    start = 8.dp,
                    end = 16.dp,
                    top = 36.dp,
                    bottom = 18.dp
                )
        ) {

            Column {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = onBackClick
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "My Application",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                // -------------------------------------------------
                // ONE ROW STATUS
                // -------------------------------------------------

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {

                    StatusBox(
                        modifier = Modifier.weight(1f),
                        title = "Applied",
                        number = totalApplications,
                        numberColor = Purple
                    )

                    StatusBox(
                        modifier = Modifier.weight(1f),
                        title = "Accepted",
                        number = acceptedCount,
                        numberColor = Green
                    )

                    StatusBox(
                        modifier = Modifier.weight(1f),
                        title = "Pending",
                        number = pendingCount,
                        numberColor = Orange
                    )

                    StatusBox(
                        modifier = Modifier.weight(1f),
                        title = "Done",
                        number = doneCount,
                        numberColor = Blue
                    )
                }
            }
        }

        // Application List
        if (isLoading) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Loading applications...",
                    color = GreyText
                )
            }

        } else if (applications.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "You have not applied for any jobs yet.",
                    color = GreyText,
                    fontSize = 16.sp
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(14.dp),
                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        top = 16.dp,
                        bottom = 16.dp
                    )
            ) {

                items(
                    items = applications,
                    key = {
                        it.first.id
                    }
                ) { applicationPair ->

                    ApplicationCard(
                        application = applicationPair.first,
                        job = applicationPair.second,
                        onAcceptOffer = {

                            scope.launch {

                                repository.updateApplicationStatus(
                                    applicationPair.first.id,
                                    "done_accepted"
                                )

                                applications =
                                    repository.getApplicationsForJobSeeker(
                                        currentUserId ?: return@launch
                                    )
                            }
                        },
                        onRejectOffer = {

                            scope.launch {

                                repository.updateApplicationStatus(
                                    applicationPair.first.id,
                                    "done_rejected"
                                )

                                applications =
                                    repository.getApplicationsForJobSeeker(
                                        currentUserId ?: return@launch
                                    )
                            }
                        }
                    )
                }
            }
        }

        // Bottom Navigation
        JobSeekerNavBar(
            selectedItem = JobSeekerNavItem.APPLIED,
            onHomeClick = onHomeClick,
            onExploreClick = onExploreClick,
            onAppliedClick = {},
            onProfileClick = onProfileClick
        )
    }
}


// Status Box
@Composable
private fun StatusBox(
    modifier: Modifier,
    title: String,
    number: Int,
    numberColor: Color
) {

    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(10.dp)
            )
            .background(Color.White)
            .padding(
                vertical = 8.dp,
                horizontal = 3.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = title,
            fontSize = 11.sp,
            color = GreyText,
            maxLines = 1
        )

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = number.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = numberColor
        )
    }
}


// Application Card
@Composable
private fun ApplicationCard(
    application: JobApplicationDto,
    job: JobSummaryDto,
    onAcceptOffer: () -> Unit,
    onRejectOffer: () -> Unit
) {

    val status = application.status

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {

                Text(
                    text = job.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                StatusPill(
                    status = status
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = job.category,
                fontSize = 14.sp,
                color = GreyText
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "📍 ${job.location}",
                fontSize = 14.sp,
                color = GreyText
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            // -------------------------------------------------
            // SALARY + DATE
            // -------------------------------------------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "RM ${job.salary} / ${job.salaryPeriod}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple
                )

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Applied ${formatAppliedDate(application.appliedAt)}",
                    fontSize = 12.sp,
                    color = GreyText
                )
            }

            // Button Response Offer
            if (status == "accepted") {

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = "The employer has accepted your application.",
                    fontSize = 14.sp,
                    color = Green,
                    fontWeight = FontWeight.Medium
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    Button(
                        onClick = onAcceptOffer,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )

                        Text(
                            text = "Accept Offer"
                        )
                    }

                    OutlinedButton(
                        onClick = onRejectOffer,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )

                        Text(
                            text = "Reject Offer"
                        )
                    }
                }
            }

            // Pending Status
            if (status == "pending") {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    shape = RoundedCornerShape(8.dp)
                ) {

                    Text(
                        text = "Waiting for employer response"
                    )
                }
            }

            // Done Status
            if (status == "done_accepted") {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(8.dp)
                        )
                        .background(LightGreen)
                        .padding(12.dp)
                ) {

                    Text(
                        text = "Done — Accepted Offer",
                        color = Green,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // -------------------------------------------------
            // DONE REJECTED
            // -------------------------------------------------

            if (status == "done_rejected") {

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(8.dp)
                        )
                        .background(LightPurple)
                        .padding(12.dp)
                ) {

                    Text(
                        text = "Done — Rejected Offer",
                        color = Purple,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
private fun StatusPill(
    status: String
) {

    val background: Color
    val textColor: Color
    val text: String

    when (status) {

        "accepted" -> {
            background = LightGreen
            textColor = Green
            text = "Accepted"
        }

        "pending" -> {
            background = LightOrange
            textColor = Orange
            text = "Pending"
        }

        "done_accepted" -> {
            background = LightBlue
            textColor = Blue
            text = "Done"
        }

        "done_rejected" -> {
            background = LightPurple
            textColor = Purple
            text = "Done"
        }

        else -> {
            background = LightPurple
            textColor = Purple
            text = "Applied"
        }
    }

    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(50.dp)
            )
            .background(background)
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
    ) {

        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatAppliedDate(
    date: String?
): String {

    if (date.isNullOrBlank()) {
        return "-"
    }

    return try {

        val parsed =
            java.time.OffsetDateTime.parse(date)

        val formatter =
            java.time.format.DateTimeFormatter.ofPattern(
                "dd MMM yyyy"
            )

        parsed.format(formatter)

    } catch (e: Exception) {

        date.take(10)
    }
}