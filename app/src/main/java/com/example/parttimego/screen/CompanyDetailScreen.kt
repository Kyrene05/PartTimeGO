package com.example.parttimego.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.PartTimeGOTheme
import com.example.parttimego.ui.theme.SoftGrey
import com.example.parttimego.viewmodel.EmployerProfileUiState
import com.example.parttimego.viewmodel.EmployerProfileViewModel
import com.example.parttimego.viewmodel.JobItemSummary

@Composable
fun CompanyDetailRoute(
    viewModel: EmployerProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onJobClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    CompanyDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onJobClick = onJobClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailScreen(
    uiState: EmployerProfileUiState,
    onBackClick: () -> Unit = {},
    onJobClick: (String) -> Unit = {}
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Company Detail",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DarkNavy)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. 公司资料卡片 (Company Card)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1.1 公司名称
                            Text(
                                text = uiState.companyName.ifBlank { "Company Name" },
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy
                            )

                            // 1.2 公司简介 (Company Background)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "About Company",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )
                                Text(
                                    text = uiState.companyBackground.ifBlank { "No company description provided yet." },
                                    fontSize = 14.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 20.sp
                                )
                            }

                            HorizontalDivider(
                                color = Color.LightGray.copy(alpha = 0.4f),
                                thickness = 1.dp
                            )

                            // 1.3 负责人板块 (Person in Charge)
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Person in Charge:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray
                                )

                                // 头像 + 右侧名字
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(SoftGrey),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!uiState.avatarUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = uiState.avatarUrl,
                                                contentDescription = "Employer Avatar",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Default Avatar",
                                                tint = DarkNavy,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Text(
                                        text = uiState.userName.ifBlank { "Manager Name" },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkNavy
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // 联系方式（电话 + 邮箱）
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (uiState.phone.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = null,
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = uiState.phone,
                                                fontSize = 13.sp,
                                                color = Color(0xFF334155)
                                            )
                                        }
                                    }

                                    if (uiState.companyEmail.isNotBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = null,
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = uiState.companyEmail,
                                                fontSize = 13.sp,
                                                color = Color(0xFF334155)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. 正在招募岗位板块 (Active Job Postings)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active Job Postings",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy
                            )
                            Text(
                                text = "${uiState.activeJobs.size} Open",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2563EB)
                            )
                        }

                        if (uiState.isLoadingJobs) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(vertical = 16.dp),
                                color = DarkNavy
                            )
                        } else if (uiState.activeJobs.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                            ) {
                                Text(
                                    text = "No active job openings at the moment.",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            uiState.activeJobs.forEach { job ->
                                ActiveJobItemCard(job = job, onClick = { onJobClick(job.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveJobItemCard(
    job: JobItemSummary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = job.salary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }

            if (job.category.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.category,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompanyDetailScreenPreview() {
    PartTimeGOTheme {
        CompanyDetailScreen(
            uiState = EmployerProfileUiState(
                userName = "Alex Tan",
                companyName = "Mixue Penang",
                phone = "+60123456789",
                companyEmail = "mixue@example.com",
                companyBackground = "Mixue is a famous F&B chain store specializing in ice cream and fresh tea. We welcome energetic young people to join our team!",
                activeJobs = listOf(
                    JobItemSummary("1", "Event Crew", "Events", "RM 100/day", "open"),
                    JobItemSummary("2", "Promoter", "Retail", "RM 120/day", "open")
                ),
                isLoading = false
            )
        )
    }
}