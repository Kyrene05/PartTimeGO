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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parttimego.data.location.LocationData
import com.example.parttimego.data.model.JobSeeker
import com.example.parttimego.nav.JobSeekerNavBar
import com.example.parttimego.nav.JobSeekerNavItem
import com.example.parttimego.viewmodel.JobSeekerUiState

private data class GigExperience(
    val jobTitle: String,
    val companyName: String,
    val date: String,
    val jobPeriod: String
)

@Composable
fun JobSeekerProfileScreen(
    worker: JobSeeker,
    onSettingClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onAppliedClick: () -> Unit = {},
    onAvailabilityChange: (Boolean) -> Unit = {},
    onAvailableDaysChange: (List<String>) -> Unit = {},
    onUpdateSkills: (String) -> Unit = {},
    onUpdatePreferredLocation: (String) -> Unit = {},
    onUpdatePreferredState: (String) -> Unit = {},
    onUpdateWorkHistory: (String) -> Unit = {},
    uiState: JobSeekerUiState
) {
    var isAvailable by remember(worker.jobSeekerAvailability) {
        mutableStateOf(worker.jobSeekerAvailability)
    }

    var skills by remember(worker.jobSeekerSkills) {
        mutableStateOf(
            worker.jobSeekerSkills
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        )
    }

    var showAddSkillDialog by remember { mutableStateOf(false) }
    var skillToRemove by remember { mutableStateOf<String?>(null) }

    var gigExperiences by remember(worker.jobSeekerWorkHistory) {
        mutableStateOf(
            if (worker.jobSeekerWorkHistory.isBlank()) {
                emptyList()
            } else {
                worker.jobSeekerWorkHistory
                    .split("\n\n")
                    .filter { it.isNotBlank() }
                    .filterNot { it.trim().equals("None", ignoreCase = true) }
            }
        )
    }

    var showAddExperienceDialog by remember { mutableStateOf(false) }
    var experienceToRemove by remember { mutableStateOf<String?>(null) }

    var selectedLocations by remember(worker.jobSeekerPreferredLocation) {
        mutableStateOf(
            worker.jobSeekerPreferredLocation
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
        )
    }

    // Preserve states explicitly so empty selections don't reset chosen states
    var selectedStates by remember(worker.jobSeekerPreferredState, worker.jobSeekerPreferredLocation) {
        val storedStates = worker.jobSeekerPreferredState
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
        val initialStates = LocationData.areasByState.filter { (_, areas) ->
            areas.any { it in selectedLocations }
        }.keys
        mutableStateOf(storedStates.ifEmpty { initialStates })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF262075))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            JobSeekerProfileHeader(
                worker = worker,
                onEditProfileClick = onSettingClick
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 15.dp, bottom = 25.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    // Availability
                    item {
                        AvailabilityCard(
                            available = isAvailable,
                            availableDays = worker.jobSeekerAvailabilityDay
                                .split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() },
                            onAvailabilityChange = { value ->
                                isAvailable = value
                                onAvailabilityChange(value)
                            },
                            onAvailableDaysChange = onAvailableDaysChange
                        )
                    }

                    // Skills
                    item { ProfileSectionTitle(title = "Skills") }

                    item {
                        FlowChipRow(
                            chips = skills,
                            showAddButton = true,
                            onAddClick = { showAddSkillDialog = true },
                            onRemoveClick = { skill -> skillToRemove = skill }
                        )
                    }

                    // Preferred States
                    item { ProfileSectionTitle(title = "Preferred States") }

                    item {
                        FlowStateRow(
                            states = LocationData.states,
                            selectedStates = selectedStates,
                            onStateToggle = { state ->
                                val updatedStates = if (state in selectedStates) {
                                    val remainingStates = selectedStates - state
                                    val removedAreas = LocationData.getAreas(state)
                                    selectedLocations = selectedLocations.filterNot { it in removedAreas }.toSet()
                                    remainingStates
                                } else {
                                    selectedStates + state
                                }
                                selectedStates = updatedStates
                                onUpdatePreferredState(updatedStates.joinToString(","))
                                onUpdatePreferredLocation(selectedLocations.joinToString(","))
                            }
                        )
                    }

                    // Preferred Locations Section
                    item { ProfileSectionTitle(title = "Preferred Locations") }

                    item {
                        if (selectedStates.isEmpty()) {
                            Text(
                                text = "Select at least one state to view available locations",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                selectedStates.forEach { state ->
                                    val areas = LocationData.getAreas(state)

                                    Text(
                                        text = state.uppercase(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )

                                    FlowAreaRow(
                                        areas = areas,
                                        selectedLocations = selectedLocations,
                                        onAreaToggle = { area ->
                                            val updatedLocations = if (area in selectedLocations) {
                                                selectedLocations - area
                                            } else {
                                                selectedLocations + area
                                            }
                                            selectedLocations = updatedLocations
                                            onUpdatePreferredLocation(updatedLocations.joinToString(","))
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Gig Experiences
                    item { ProfileSectionTitle(title = "Gigs Experiences") }

                    item {
                        GigExperienceSection(
                            experiences = gigExperiences,
                            onAddClick = { showAddExperienceDialog = true },
                            onRemoveClick = { experience -> experienceToRemove = experience }
                        )
                    }
                }
            }

            JobSeekerNavBar(
                selectedItem = JobSeekerNavItem.PROFILE,
                onHomeClick = onHomeClick,
                onExploreClick = onExploreClick,
                onAppliedClick = onAppliedClick,
                onProfileClick = {}
            )
        }

        if (showAddSkillDialog) {
            AddSkillDialog(
                onDismiss = { showAddSkillDialog = false },
                onAdd = { newSkill ->
                    if (newSkill.isNotBlank() && newSkill.trim() !in skills) {
                        val updatedSkills = skills + newSkill.trim()
                        skills = updatedSkills
                        onUpdateSkills(updatedSkills.joinToString(","))
                    }
                    showAddSkillDialog = false
                }
            )
        }

        if (showAddExperienceDialog) {
            AddGigExperienceDialog(
                onDismiss = { showAddExperienceDialog = false },
                onAdd = { experience ->
                    val updatedExperiences = gigExperiences + experience
                    gigExperiences = updatedExperiences
                    onUpdateWorkHistory(updatedExperiences.joinToString("\n\n"))
                    showAddExperienceDialog = false
                }
            )
        }

        if (skillToRemove != null) {
            AlertDialog(
                onDismissRequest = { skillToRemove = null },
                title = { Text("Remove Skill") },
                text = { Text("Are you sure you want to remove ${skillToRemove.orEmpty()}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val skill = skillToRemove
                            if (skill != null) {
                                val updatedSkills = skills.filterNot { it == skill }
                                skills = updatedSkills
                                onUpdateSkills(updatedSkills.joinToString(","))
                            }
                            skillToRemove = null
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { skillToRemove = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (experienceToRemove != null) {
            AlertDialog(
                onDismissRequest = { experienceToRemove = null },
                title = { Text("Remove Gig Experience") },
                text = { Text("Are you sure you want to remove this gig experience?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val experience = experienceToRemove
                            if (experience != null) {
                                val updatedExperiences = gigExperiences.filterNot { it == experience }
                                gigExperiences = updatedExperiences
                                onUpdateWorkHistory(
                                    if (updatedExperiences.isEmpty()) "None"
                                    else updatedExperiences.joinToString("\n\n")
                                )
                            }
                            experienceToRemove = null
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { experienceToRemove = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowAreaRow(
    areas: List<String>,
    selectedLocations: Set<String>,
    onAreaToggle: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        areas.forEach { area ->
            val selected = area in selectedLocations
            AreaChip(
                area = area,
                selected = selected,
                onClick = { onAreaToggle(area) }
            )
        }
    }
}

@Composable
private fun AreaChip(
    area: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color(0xFF262075) else Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Color(0xFF262075) else Color(0xFFD0D0CC)
        )
    ) {
        Text(
            text = if (selected) "$area ✓" else area,
            color = if (selected) Color.White else Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun JobSeekerProfileHeader(
    worker: JobSeeker,
    onEditProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 17.dp)
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

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = worker.jobSeekerName.firstOrNull()?.uppercase() ?: "A",
                    color = Color(0xFF262075),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF118C20))
                        .border(width = 5.dp, color = Color(0xFF262075), shape = CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = worker.jobSeekerName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "email",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = worker.jobSeekerEmail,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "phone",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = worker.jobSeekerPhoneNo,
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
    var selectedDays by remember { mutableStateOf(availableDays) }
    var showDaySelection by remember(available) { mutableStateOf(!available) }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Black),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(12.dp, 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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

                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (available) "Available Everyday" else getAvailabilityText(selectedDays),
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Switch(
                    checked = available,
                    onCheckedChange = { value ->
                        onAvailabilityChange(value)
                        showDaySelection = !value
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF118C20),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF8E8E80)
                    )
                )
            }

            if (!available && showDaySelection) {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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
                                selectedDays = if (day in selectedDays) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                                onAvailableDaysChange(selectedDays)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        color = Color.Black,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

private enum class ChipType { PURPLE, GRAY }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowChipRow(
    chips: List<String>,
    chipType: ChipType = ChipType.PURPLE,
    showAddButton: Boolean,
    onAddClick: () -> Unit = {},
    onRemoveClick: (String) -> Unit = {}
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chips.forEach { chip ->
            ProfileChip(
                text = chip,
                chipType = chipType,
                onRemoveClick = { onRemoveClick(chip) }
            )
        }

        if (showAddButton) {
            Surface(
                modifier = Modifier
                    .size(30.dp)
                    .clickable { onAddClick() },
                shape = RoundedCornerShape(9.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Box(contentAlignment = Alignment.Center) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowStateRow(
    states: List<String>,
    selectedStates: Set<String>,
    onStateToggle: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        states.forEach { state ->
            val selected = state in selectedStates
            StateOption(
                state = state,
                selected = selected,
                onClick = { onStateToggle(state) }
            )
        }
    }
}

@Composable
private fun StateOption(
    state: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color(0xFF262075) else Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Color(0xFF262075) else Color(0xFFD0D0CC)
        )
    ) {
        Text(
            text = if (selected) "$state ✓" else state,
            color = if (selected) Color.White else Color.Black,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ProfileChip(
    text: String,
    chipType: ChipType,
    onRemoveClick: () -> Unit
) {
    val backgroundColor = when (chipType) {
        ChipType.PURPLE -> Color(0xFF262075).copy(alpha = 0.5f)
        ChipType.GRAY -> Color(0xFF8E8E80).copy(alpha = 0.5f)
    }
    val textColor = when (chipType) {
        ChipType.PURPLE -> Color(0xFF262075)
        ChipType.GRAY -> Color(0xFF8E8E80)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 5.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove $text",
                tint = textColor,
                modifier = Modifier
                    .size(14.dp)
                    .clickable { onRemoveClick() }
            )
        }
    }
}

@Composable
private fun AddSkillDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var skill by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Skill", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = skill,
                onValueChange = { skill = it },
                label = { Text("Skill") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(
                onClick = { onAdd(skill) },
                enabled = skill.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262075))
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddGigExperienceDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var jobTitle by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var jobPeriod by remember { mutableStateOf("") }

    // Regex Validations
    val dateRegex = Regex("^(0[1-9]|1[0-2])/([0-9]{4})$") // MM/YYYY format
    val periodRegex = Regex("^[0-9]+\\s*(day|days|week|weeks|month|months|year|years|m|yr|yrs|d|w)?$", RegexOption.IGNORE_CASE)

    val isDateValid = date.isBlank() || dateRegex.matches(date.trim())
    val isPeriodValid = jobPeriod.isBlank() || periodRegex.matches(jobPeriod.trim())

    val isFormValid = jobTitle.isNotBlank() &&
            companyName.isNotBlank() &&
            date.isNotBlank() && dateRegex.matches(date.trim()) &&
            jobPeriod.isNotBlank() && periodRegex.matches(jobPeriod.trim())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Gig Experience", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("MM/YYYY (e.g. 05/2024)") },
                    isError = !isDateValid,
                    supportingText = {
                        if (!isDateValid) {
                            Text("Use MM/YYYY format (e.g., 08/2023)", color = Color.Red, fontSize = 11.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = jobPeriod,
                    onValueChange = { jobPeriod = it },
                    label = { Text("Job Period (e.g. 6 months, 2 weeks)") },
                    isError = !isPeriodValid,
                    supportingText = {
                        if (!isPeriodValid) {
                            Text("Enter duration (e.g., 3 months, 1 year)", color = Color.Red, fontSize = 11.sp)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val experience = "$jobTitle\n$companyName\n${date.trim()}\n${jobPeriod.trim()}"
                    onAdd(experience)
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262075))
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun GigExperienceCard(
    experience: GigExperience?,
    onRemoveClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Black),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            if (experience == null) {
                Text(
                    text = "No gig experiences available",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            } else {
                Text(
                    text = experience.jobTitle,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = experience.companyName, color = Color.Black, fontSize = 13.sp)
                    Spacer(Modifier.width(5.dp))
                    Text(text = experience.date, color = Color.Black, fontSize = 13.sp)
                    Spacer(Modifier.width(5.dp))
                    Text(text = experience.jobPeriod, color = Color.Black, fontSize = 13.sp)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove Gig Experience",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onRemoveClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun GigExperienceCard(
    rawExperience: String,
    onRemoveClick: () -> Unit
) {
    val lines = rawExperience.split("\n")
    val parsedExperience = if (lines.size >= 4) {
        GigExperience(
            jobTitle = lines[0],
            companyName = lines[1],
            date = lines[2],
            jobPeriod = lines[3]
        )
    } else {
        GigExperience(
            jobTitle = rawExperience,
            companyName = "",
            date = "",
            jobPeriod = ""
        )
    }

    GigExperienceCard(
        experience = parsedExperience,
        onRemoveClick = onRemoveClick
    )
}

@Composable
private fun GigExperienceSection(
    experiences: List<String>,
    onAddClick: () -> Unit,
    onRemoveClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (experiences.isEmpty()) {
            Text(
                text = "No gig experiences available",
                color = Color.Gray,
                fontSize = 13.sp
            )
        } else {
            experiences.forEach { experience ->
                GigExperienceCard(
                    rawExperience = experience,
                    onRemoveClick = { onRemoveClick(experience) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddClick() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Gig Experience",
                tint = Color(0xFF262075),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Add Gig Experience",
                color = Color(0xFF262075),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
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
            .clickable { onClick() },
        shape = CircleShape,
        color = if (selected) Color(0xFF262075) else Color.White,
        border = BorderStroke(1.dp, if (selected) Color(0xFF262075) else Color(0xFFD0D0CC))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = day,
                color = if (selected) Color.White else Color.Black,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getAvailabilityText(selectedDays: List<String>): String {
    if (selectedDays.isEmpty()) return "Select available days"
    return "Every " + selectedDays.joinToString(", ")
}