package com.example.parttimego.screen.worker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
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
import com.example.parttimego.nav.JobSeekerNavBar
import com.example.parttimego.nav.JobSeekerNavItem
import com.example.parttimego.ui.theme.PartTimeGOTheme

@Composable
fun JobSeekerProfileScreen(
    worker: Worker,
    onEditProfileClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onAppliedClick: () -> Unit = {},
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
            JobSeekerProfileHeader(
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
                            availableDays = worker.workerAvailabilityDay
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() },
                            onAvailabilityChange = { value ->
                                isAvailable = value
                                onAvailabilityChange(value)
                            },
                            onAvailableDaysChange = {
                                // Later, this can be sent to WorkerViewModel
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
                            title = "Preferred Locations"
                        )
                    }

                    item {
                        val locations = worker.workerPreferredLocation
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

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
            JobSeekerNavBar(
                selectedItem = JobSeekerNavItem.PROFILE,
                onHomeClick = onHomeClick,
                onExploreClick = onExploreClick,
                onAppliedClick = onAppliedClick,
                onProfileClick = {
                    // Already on Profile screen
                }
            )
        }
    }
}

@Composable
private fun JobSeekerProfileHeader(
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
                fontSize = 18.sp,
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
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Edit Profile",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
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
                    .size(84.dp)
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
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                // Online indicator
                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF118C20))
                        .border(
                            width = 5.dp,
                            color = Color(0xFF262075),
                            shape = CircleShape
                        )
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = worker.workerName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "email",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )

                    Spacer(Modifier.width(5.dp))

                    Text(
                        text = worker.workerEmail,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "phone",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )

                    Spacer(Modifier.width(5.dp))

                    Text(
                        text = worker.workerPhoneNo,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailabilityCard(
    available: Boolean,
    availableDays: List<String>,
    onAvailabilityChange: (Boolean) -> Unit,
    onAvailableDaysChange: (List<String>) -> Unit
) {
    var selectedDays by remember {
        mutableStateOf(availableDays)
    }

    val days = listOf(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Black),
        color = Color.White
    ) {

        Row(
            modifier = Modifier.padding(12.dp, 10.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF118C20).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color(0xFF4D8C62),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = if (available) {
                        "Available Everyday"
                    } else {
                        getAvailabilityText(selectedDays)
                    },
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Switch(
                checked = available,
                onCheckedChange = { value ->

                    onAvailabilityChange(value)

                    // If user turns ON, they are available every day
                    if (value) {
                        selectedDays = emptyList()
                        onAvailableDaysChange(emptyList())
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF118C20),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFF8E8E80)
                )
            )
        }

        // Only show day selection when toggle is OFF
        if (!available){
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                days.forEach { day ->
                    val shortDay = when (day) {
                        "Monday" -> "M"
                        "Tuesday" -> "T"
                        "Wednesday" -> "W"
                        "Thursday" -> "T"
                        "Friday" -> "F"
                        "Saturday" -> "S"
                        else -> "S"
                    }
                    DayButton(
                        day = shortDay,
                        selected = day in selectedDays,
                        onClick = {

                            selectedDays =
                                if (day in selectedDays) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }

                            onAvailableDaysChange(
                                selectedDays
                            )
                        }
                    )
                }
            }
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
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

private enum class ChipType {
    PURPLE,
    GRAY
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowChipRow(
    chips: List<String>,
    chipType: ChipType = ChipType.PURPLE,
    showAddButton: Boolean
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                        modifier = Modifier.size(14.dp)
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
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(8.dp, 5.dp)
        )
    }
}

@Composable
private fun AreaOption(
    area: String,
    selected: Boolean
) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) {
            Color(0xFF262075).copy(alpha = 0.12f)
        } else {
            Color.White
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                Color(0xFF262075)
            } else {
                Color(0xFFD0D0CC)
            }
        )
    ) {

        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = area,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp,
                color = Color.Black
            )

            if (selected) {
                Text(
                    text = "Selected",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF262075)
                )
            }
        }
    }
}

@Composable
private fun GigExperienceCard(
    workHistory: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Black),
        color = Color.White
    ) {

        Column(
            modifier = Modifier.padding(10.dp, 8.dp)
        ) {

            Text(
                text = "Work History",
                color = Color.Black,
                fontSize = 16.sp,
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
                fontSize = 14.sp,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun DayButton(
    day: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(24.dp)
            .clickable {
                onClick()
            },
        shape = CircleShape,
        color = if (selected) {
            Color(0xFF262075)
        } else {
            Color.White
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                Color(0xFF262075)
            } else {
                Color(0xFFD0D0CC)
            }
        )
    ) {

        Box(
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = day,
                color = if (selected) {
                    Color.White
                } else {
                    Color.Black
                },
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun getAvailabilityText(
    selectedDays: List<String>
): String {
    if(selectedDays.isEmpty()){
        return "Select available days"
    }
    return "Every" + selectedDays.joinToString(", ")
}

