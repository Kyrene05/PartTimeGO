package com.example.parttimego.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.SoftGrey
import com.example.parttimego.viewmodel.WorkerUiState

// 数据模型：定义单条 Gig 历史记录
data class GigExperienceItem(
    val title: String,      // 兼职名称，例如 "Event Promoter"
    val companyName: String, // 雇主/公司名称，例如 "XYZ Agency"
    val dateText: String     // 日期，例如 "2026-08-15"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkerDetailScreen(
    uiState: WorkerUiState,
    completedGigsCount: Int = uiState.completedGigsCount,        // 默认直接绑定 uiState
    ratingScore: Double = 5.0,                                     // 评分
    gigExperiences: List<GigExperienceItem> = uiState.gigExperiences, // 默认直接绑定 uiState
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Worker Detail",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkNavy
                )
            )
        },
        containerColor = DarkNavy
    ) { paddingValues ->
        // 加载中状态处理
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkNavy),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(DarkNavy)
            ) {
                // Header 个人信息区域
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(SoftGrey),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!uiState.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = uiState.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = DarkNavy,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = uiState.userName.ifBlank { "Worker" },
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.gender.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = uiState.gender,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        if (uiState.phone.isNotBlank()) {
                            Text(
                                text = uiState.phone,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                        // 动态显示总完成的 Gigs 数量
                        Text(
                            text = "Rate: $ratingScore  •  $completedGigsCount ${if (completedGigsCount == 1) "gig" else "gigs"}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }

                // 内容白底卡片
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Work Availability 卡片
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFF166534),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Work Availability",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = DarkNavy
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))

                                    val displayTimeText = if (uiState.availability) {
                                        "available work in weekday and weekend"
                                    } else {
                                        if (uiState.availableDays.isNotEmpty()) {
                                            "available on: ${uiState.availableDays.joinToString(", ")}"
                                        } else {
                                            "available on: -"
                                        }
                                    }

                                    Text(
                                        text = displayTimeText,
                                        fontSize = 13.sp,
                                        color = Color(0xFF166534),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // About Me
                        if (uiState.aboutMe.isNotBlank()) {
                            SectionTitle(title = "About Me")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    text = uiState.aboutMe,
                                    modifier = Modifier.padding(14.dp),
                                    fontSize = 13.sp,
                                    color = DarkNavy
                                )
                            }
                        }

                        // Skills
                        SectionTitle(title = "Skills")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.skills.isEmpty()) {
                                Text(text = "No skills added", fontSize = 13.sp, color = Color.Gray)
                            } else {
                                uiState.skills.forEach { skill ->
                                    ChipItem(text = skill, backgroundColor = Color(0xFFE0E7FF), textColor = Color(0xFF3730A3))
                                }
                            }
                        }

                        // Preferred Job Categories
                        SectionTitle(title = "Preferred Job Categories")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.preferredCategories.isEmpty()) {
                                Text(text = "No categories added", fontSize = 13.sp, color = Color.Gray)
                            } else {
                                uiState.preferredCategories.forEach { cat ->
                                    ChipItem(text = cat, backgroundColor = Color(0xFFDCFCE7), textColor = Color(0xFF166534))
                                }
                            }
                        }

                        // Preferred Locations
                        SectionTitle(title = "Preferred Locations")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.preferredLocations.isEmpty()) {
                                Text(text = "No locations added", fontSize = 13.sp, color = Color.Gray)
                            } else {
                                uiState.preferredLocations.forEach { loc ->
                                    ChipItem(text = loc, backgroundColor = Color(0xFFF1F5F9), textColor = Color(0xFF475569))
                                }
                            }
                        }

                        // Gigs Experience (在 ViewModel 中已限制最多获取 10 条)
                        SectionTitle(title = "Gigs Experience")

                        if (gigExperiences.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    text = "No past gig experience",
                                    modifier = Modifier.padding(14.dp),
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                gigExperiences.forEach { gig ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = gig.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = DarkNavy
                                                )
                                                Text(
                                                    text = gig.dateText,
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                            if (gig.companyName.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = gig.companyName,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF475569)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = DarkNavy
    )
}

@Composable
private fun ChipItem(text: String, backgroundColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun WorkerDetailScreenPreview() {
    MaterialTheme {
        WorkerDetailScreen(
            uiState = WorkerUiState(
                userName = "Jane Doe",
                gender = "Female",
                phone = "+60123456789",
                aboutMe = "Responsible and punctual part-time worker with 2 years of retail experience.",
                availability = false,
                availableDays = listOf("Sat", "Sun"),
                skills = listOf("Kotlin", "Jetpack Compose"),
                preferredCategories = listOf("Event Crew", "Retail"),
                preferredLocations = listOf("Penang", "Kuala Lumpur"),
                completedGigsCount = 12,
                gigExperiences = listOf(
                    GigExperienceItem("Event Assistant", "Sunway Carnival Mall", "2026-08-10"),
                    GigExperienceItem("Retail Sales Representative", "Queensbay Outlet", "2026-07-22"),
                    GigExperienceItem("Flyer Promoter", "Mid Valley KL", "2026-06-15")
                )
            ),
            ratingScore = 4.9
        )
    }
}