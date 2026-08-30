package com.example.parttimego.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parttimego.data.JobPost
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.MutedText
import com.example.parttimego.ui.theme.PartTimeGOTheme
import com.example.parttimego.ui.theme.SoftGrey

@Composable
fun DashboardScreen(
    activeJobsCount: Int = 2,
    totalApplicantsCount: Int = 82,
    thisWeekHires: Int = 8,
    pendingReviewCount: Int = 20,
    jobs: List<JobPost> = emptyList(),
    onJobDetailsClick: (String) -> Unit = {},
    onTotalApplicantsClick: () -> Unit = {},
    onDashboardTabClick: () -> Unit = {},
    onPostTabClick: () -> Unit = {},
    onProfileTabClick: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                onDashboardClick = onDashboardTabClick,
                onPostClick = onPostTabClick,
                onProfileClick = onProfileTabClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkNavy)
                .statusBarsPadding()
        ) {
            // --- Header ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SoftGrey),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("P", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("PartTimeGO", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // --- White content area ---
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Active Jobs card
                    StatCardLarge(
                        icon = Icons.Filled.Work,
                        value = activeJobsCount.toString(),
                        label = "Active Jobs"
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Total Applicants card
                    StatCardLarge(
                        icon = Icons.Filled.Groups,
                        value = totalApplicantsCount.toString(),
                        label = "Total Applicants",
                        onClick = onTotalApplicantsClick
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Small stat row: This Week Hires / Pending Review
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCardSmall(
                            value = thisWeekHires.toString(),
                            label = "This Week Hires",
                            modifier = Modifier.weight(1f)
                        )
                        StatCardSmall(
                            value = pendingReviewCount.toString(),
                            label = "Pending Review",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // My Posted Jobs header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("My Posted Jobs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    jobs.forEach { job ->
                        JobPostCard(
                            job = job,
                            onViewDetailsClick = { onJobDetailsClick(job.id) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCardLarge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Black),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = label, tint = Color.Black, modifier = Modifier.size(50.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(label, fontSize = 13.sp, color = MutedText)
            }
        }
    }
}

@Composable
private fun StatCardSmall(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Black),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = MutedText)
        }
    }
}

@Composable
private fun JobPostCard(job: JobPost, onViewDetailsClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Black),
        color = Color.White
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(job.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                if (job.tag.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (job.tag == "Hot") Color(0xFFE53935) else Color(0xFF43A047)
                    ) {
                        Text(
                            text = job.tag,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(job.companyOrLocation, fontSize = 13.sp, color = MutedText)

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(job.salary, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                Spacer(modifier = Modifier.width(10.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFD6E8FA)) {
                    Text(
                        text = job.durationLabel,
                        fontSize = 11.sp,
                        color = DarkNavy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onViewDetailsClick,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("View Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun DashboardBottomBar(
    onDashboardClick: () -> Unit,
    onPostClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = true,
            onClick = onDashboardClick,
            icon = { Icon(Icons.Filled.BarChart, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkNavy, selectedTextColor = DarkNavy)
        )
        NavigationBarItem(
            selected = false,
            onClick = onPostClick,
            icon = { Icon(Icons.Filled.Add, contentDescription = "Post") },
            label = { Text("Post") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    PartTimeGOTheme {
        DashboardScreen(
            activeJobsCount = 2,
            totalApplicantsCount = 82,
            thisWeekHires = 8,
            pendingReviewCount = 20,
            jobs = listOf(
                JobPost(
                    id = "1",
                    title = "Event Crew",
                    companyOrLocation = "AEG Live MY",
                    salary = "RM 100 / day",
                    tag = "Hot",
                    durationLabel = "1 Day"
                ),
                JobPost(
                    id = "2",
                    title = "Sales Promoter",
                    companyOrLocation = "HLA",
                    salary = "RM 90 / day",
                    tag = "New",
                    durationLabel = "Weekend"
                ),
                JobPost(
                    id = "3",
                    title = "Event Crew",
                    companyOrLocation = "Rainforest Music Festival",
                    salary = "RM 120 / day",
                    tag = "Hot",
                    durationLabel = "1 Day"
                )
            )
        )
    }
}