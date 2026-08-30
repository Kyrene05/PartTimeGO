package com.example.parttimego.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.MutedText
import com.example.parttimego.ui.theme.PartTimeGOTheme
import com.example.parttimego.ui.theme.SoftGrey

// Local UI model — independent of teammate's Application/Worker data classes for now.
// Once their ApplicationRepository is ready, we map their real model into this shape
// (or replace this entirely) without needing to rewrite the screen below.
data class ApplicantUiModel(
    val id: String,
    val name: String,
    val jobTitle: String,
    val location: String,
    val salary: String,
    val appliedDate: String,
    val status: ApplicantStatus
)

enum class ApplicantStatus { ACCEPTED, PENDING, REJECTED }

@Composable
fun ManageApplicantsScreen(
    applicants: List<ApplicantUiModel> = emptyList(),
    onBackClick: () -> Unit = {},
    onAcceptClick: (String) -> Unit = {},
    onRejectClick: (String) -> Unit = {},
    onDashboardTabClick: () -> Unit = {},
    onPostTabClick: () -> Unit = {},
    onProfileTabClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(ApplicantStatus.PENDING) }
    var pendingAction by remember { mutableStateOf<Pair<ApplicantUiModel, ApplicantStatus>?>(null) }

    val accepted = applicants.count { it.status == ApplicantStatus.ACCEPTED }
    val pending = applicants.count { it.status == ApplicantStatus.PENDING }
    val rejected = applicants.count { it.status == ApplicantStatus.REJECTED }

    val filtered = applicants.filter { it.status == selectedFilter }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = onDashboardTabClick,
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onPostTabClick,
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Post") },
                    label = { Text("Post") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onProfileTabClick,
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkNavy)
                .statusBarsPadding()
        ) {
            // Header
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Manage Applicants", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusTab(
                        count = accepted,
                        label = "Accepted",
                        selected = selectedFilter == ApplicantStatus.ACCEPTED,
                        accentColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    ) { selectedFilter = ApplicantStatus.ACCEPTED }
                    StatusTab(
                        count = pending,
                        label = "Pending",
                        selected = selectedFilter == ApplicantStatus.PENDING,
                        accentColor = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    ) { selectedFilter = ApplicantStatus.PENDING }
                    StatusTab(
                        count = rejected,
                        label = "Rejected",
                        selected = selectedFilter == ApplicantStatus.REJECTED,
                        accentColor = Color(0xFF29B6F6),
                        modifier = Modifier.weight(1f)
                    ) { selectedFilter = ApplicantStatus.REJECTED }
                }
            }

            // White content area
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White
            ) {
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No applicants here yet", color = MutedText, fontSize = 14.sp)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        filtered.forEach { applicant ->
                            ApplicantCard(
                                applicant = applicant,
                                onAcceptClick = { pendingAction = applicant to ApplicantStatus.ACCEPTED },
                                onRejectClick = { pendingAction = applicant to ApplicantStatus.REJECTED }
                            )
                        }
                    }
                }
            }
        }
    }
    pendingAction?.let { (applicant, decision) ->
        val isAccept = decision == ApplicantStatus.ACCEPTED
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = MutedText,
            title = {
                Text(
                    if (isAccept) "Accept this applicant?" else "Reject this applicant?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (isAccept)
                        "${applicant.name} will be notified they're accepted for ${applicant.jobTitle}."
                    else
                        "${applicant.name} will be notified they're not selected for ${applicant.jobTitle}."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isAccept) onAcceptClick(applicant.id) else onRejectClick(applicant.id)
                    pendingAction = null
                }) {
                    Text(
                        if (isAccept) "Accept" else "Reject",
                        color = if (isAccept) Color(0xFF4CAF50) else Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text("Cancel", color = MutedText)
                }
            }
        )
    }
}

@Composable
private fun StatusTab(
    count: Int,
    label: String,
    selected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(count.toString(), color = accentColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ApplicantCard(
    applicant: ApplicantUiModel,
    onAcceptClick: () -> Unit,
    onRejectClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SoftGrey),
        color = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(applicant.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                StatusBadge(applicant.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(applicant.jobTitle, fontSize = 13.sp, color = MutedText)
            Text(applicant.location, fontSize = 13.sp, color = MutedText)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(applicant.salary, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                Text("Applied ${applicant.appliedDate}", fontSize = 11.sp, color = MutedText)
            }

            if (applicant.status == ApplicantStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onAcceptClick,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Accept", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Button(
                        onClick = onRejectClick,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Reject", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ApplicantStatus) {
    val (label, bg, fg) = when (status) {
        ApplicantStatus.ACCEPTED -> Triple("Accepted", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        ApplicantStatus.PENDING -> Triple("Pending", Color(0xFFFFF3E0), Color(0xFFE65100))
        ApplicantStatus.REJECTED -> Triple("Rejected", Color(0xFFFFEBEE), Color(0xFFC62828))
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(label, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ManageApplicantsScreenPreview() {
    PartTimeGOTheme {
        ManageApplicantsScreen(
            applicants = listOf(
                ApplicantUiModel(
                    id = "1",
                    name = "Derick Ang",
                    jobTitle = "Event Crew",
                    location = "Kuala Lumpur City Centre (KLCC)",
                    salary = "RM 180 / day",
                    appliedDate = "10 Jul 2026",
                    status = ApplicantStatus.PENDING
                ),
                ApplicantUiModel(
                    id = "2",
                    name = "Sunny Kim",
                    jobTitle = "Promoter",
                    location = "Pavilion Mall",
                    salary = "RM 100 / day",
                    appliedDate = "10 Jul 2026",
                    status = ApplicantStatus.PENDING
                ),
                ApplicantUiModel(
                    id = "3",
                    name = "Ahmad Faris",
                    jobTitle = "Event Crew",
                    location = "Kuala Lumpur City Centre (KLCC)",
                    salary = "RM 150 / day",
                    appliedDate = "10 Jul 2026",
                    status = ApplicantStatus.PENDING
                ),
                ApplicantUiModel(
                    id = "4",
                    name = "Andrew Lim",
                    jobTitle = "Event Crew",
                    location = "Kuala Lumpur City Centre (KLCC)",
                    salary = "RM 120 / day",
                    appliedDate = "9 Jul 2026",
                    status = ApplicantStatus.ACCEPTED
                )
            )
        )
    }
}

@Preview(showBackground = true, name = "Empty state")
@Composable
fun ManageApplicantsEmptyPreview() {
    PartTimeGOTheme {
        ManageApplicantsScreen(applicants = emptyList())
    }
}