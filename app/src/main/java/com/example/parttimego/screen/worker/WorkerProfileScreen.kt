package com.example.parttimego.screen.worker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.example.parttimego.data.model.Worker
import com.example.parttimego.ui.theme.DarkText
import com.example.parttimego.ui.theme.PartTimeGOTheme

@Composable
fun WorkerProfileScreen(
    worker: Worker,
    onEditProfileClick: () -> Unit = {},
    onAvailabilityChange: (Boolean) -> Unit = {}
) {
    var isAvailable by remember(worker.workerAvailability) {
        mutableStateOf(worker.workerAvailability)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF262075))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // Profile Header
            WorkerProfileHeader(
                worker = worker,
                onEditProfileClick = onEditProfileClick
            )

            // White Content Area
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp
                )
            ) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        top = 15.dp,
                        bottom = 25.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {

                    // Availability
                    item {
                        AvailabilityCard(
                            available = isAvailable,
                            onAvailabilityChange = { value ->
                                isAvailable = value
                                onAvailabilityChange(value)
                            }
                        )
                    }

                    // Skills
                    item {
                        ProfileSectionTitle(
                            title = "Skills"
                        )
                    }

                    item {
                        val skills = worker.workerSkills
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                        FlowChipRow(
                            chips = skills,
                            showAddButton = true
                        )
                    }

                    // Preferred Locations
                    item {
                        ProfileSectionTitle(
                            title = "Preferred Location"
                        )
                    }

                    item {
                        val locations = listOf(
                            worker.workerPreferredState,
                            worker.workerPreferredLocation
                        )
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .distinct()

                        FlowChipRow(
                            chips = locations,
                            chipType = ChipType.GRAY,
                            showAddButton = false
                        )
                    }

                    // Gig Experiences
                    item {
                        ProfileSectionTitle(
                            title = "Gigs Experiences"
                        )
                    }

                    item {
                        GigExperienceCard(
                            workHistory = worker.workerWorkHistory
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkerProfileHeader(
    worker: Worker,
    onEditProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 14.dp,
                bottom = 17.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Profile",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.weight(1f))

            Surface(
                onClick = onEditProfileClick,
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.22f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp, 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )

                    Spacer(Modifier.width(3.dp))

                    Text(
                        text = "Edit Profile",
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = worker.workerName
                        .firstOrNull()
                        ?.uppercase()
                        ?: "A",
                    color = Color(0xFF262075),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Online indicator
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF118C20))
                        .border(
                            width = 2.dp,
                            color = Color(0xFF262075),
                            shape = CircleShape
                        )
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(Modifier.width(9.dp))

            Column {
                Text(
                    text = worker.workerName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = worker.workerPhoneNo,
                    color = Color.White,
                    fontSize = 7.sp
                )
            }
        }
    }
}

@Composable
private fun AvailabilityCard(
    available: Boolean,
    onAvailabilityChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = DarkText
        )
    ) {

        Row(
            modifier = Modifier.padding(12.dp, 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF118C20).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color(0xFF4D8C62),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = "Available",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Switch(
                checked = available,
                onCheckedChange = onAvailabilityChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF118C20),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFF8E8E80)
                )
            )
        }
    }
}

@Composable
private fun ProfileSectionTitle(
    title: String
) {
    Text(
        text = title,
        color = Color.Black,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )
}

private enum class ChipType {
    PURPLE,
    GRAY
}

@Composable
private fun FlowChipRow(
    chips: List<String>,
    chipType: ChipType = ChipType.PURPLE,
    showAddButton: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        chips.forEach { chip ->
            ProfileChip(
                text = chip,
                chipType = chipType
            )
        }

        if (showAddButton) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = RoundedCornerShape(9.dp),
                color = Color.White,
                border = BorderStroke(
                    width = 1.dp,
                    color = Color(0xFFD0D0CC)
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Skills",
                        tint = Color.Black,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileChip(
    text: String,
    chipType: ChipType
) {
    val backgroundColor: Color
    val textColor: Color

    when (chipType) {

        ChipType.PURPLE -> {
            backgroundColor = Color(0xFF262075).copy(alpha = 0.5f)
            textColor = Color(0xFF262075)
        }

        ChipType.GRAY -> {
            backgroundColor = Color(0xFF8E8E80).copy(alpha = 0.5f)
            textColor = Color(0xFF8E8E80)
        }
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 7.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(8.dp, 5.dp)
        )
    }
}

@Composable
private fun GigExperienceCard(
    workHistory: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(9.dp),
        color = Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = Color.Black
        )
    ) {

        Column(
            modifier = Modifier.padding(10.dp, 8.dp)
        ) {

            Text(
                text = "Work History",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = if (workHistory.isBlank()) {
                    "No work history available"
                } else {
                    workHistory
                },
                color = Color.Black,
                fontSize = 8.sp,
                lineHeight = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkerProfileScreenPreview(){
    PartTimeGOTheme() {
        WorkerProfileScreen(
            worker = Worker(
                workerId = "W001",
                userId = "U001",
                workerName = "Ahmad",
                workerPhoneNo = "0123456789",
                workerAvailability = true,
                workerSkills = "Event Crew, Customer Service",
                workerPreferredJobCategories = "F&B, Retail",
                workerPreferredState = "Penang",
                workerPreferredLocation = "Georgetown",
                workerWorkHistory = "Worked as event crew for 2 years",
                workerCreatedAt = "2026-08-26T10:00:00",
                workerUpdatedAt = "2026-08-26T10:00:00"
            )
        )
    }
}