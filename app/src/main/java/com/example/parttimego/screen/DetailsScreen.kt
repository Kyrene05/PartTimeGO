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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.MutedText
import com.example.parttimego.ui.theme.PartTimeGOTheme
import com.example.parttimego.ui.theme.SoftGrey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    initialData: PostJobFormData,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onBackClick: () -> Unit = {},
    onUpdateClick: (PostJobFormData) -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onDashboardTabClick: () -> Unit = {},
    onProfileTabClick: () -> Unit = {}
) {
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showUpdateConfirm by remember { mutableStateOf(false) }

    // Editable copies of every field, seeded from initialData
    var title by remember { mutableStateOf(initialData.title) }
    var companyName by remember { mutableStateOf(initialData.companyName) }
    var category by remember { mutableStateOf(initialData.category) }
    var salary by remember { mutableStateOf(initialData.salary) }
    var startDate by remember { mutableStateOf(initialData.startDate) }
    var endDate by remember { mutableStateOf(initialData.endDate) }
    var startTime by remember { mutableStateOf(initialData.workingHoursStart) }
    var endTime by remember { mutableStateOf(initialData.workingHoursEnd) }
    var location by remember { mutableStateOf(initialData.location) }
    var description by remember { mutableStateOf(initialData.description) }
    var requirements by remember { mutableStateOf(initialData.requirements) }
    var peopleNeeded by remember { mutableStateOf(initialData.peopleNeeded) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var companyNameError by remember { mutableStateOf<String?>(null) }
    var salaryError by remember { mutableStateOf<String?>(null) }
    var startDateError by remember { mutableStateOf<String?>(null) }
    var endDateError by remember { mutableStateOf<String?>(null) }
    var startTimeError by remember { mutableStateOf<String?>(null) }
    var endTimeError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var requirementsError by remember { mutableStateOf<String?>(null) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val categories = listOf("Event Crew", "Promoter", "Retail", "F&B")

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
                    onClick = { },
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
            // Header — back arrow, title, edit icon on the right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))

                if (!isEditing) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { isEditing = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

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
                    if (isEditing) {
                        // --- EDIT MODE: same form components as Post Job ---
                        SectionLabel("1", "Basic Information")
                        FormField("Job Title", title, isError = titleError != null, errorMessage = titleError) {
                            title = it; titleError = null
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        FormField("Company Name", companyName, isError = companyNameError != null, errorMessage = companyNameError) {
                            companyName = it; companyNameError = null
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                CategoryChip(label = cat, selected = category == cat, onClick = { category = cat })
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        SectionLabel("2", "Salary & Duration")
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Salary (RM/day)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = salary,
                                onValueChange = { input -> if (input.all { it.isDigit() }) { salary = input; salaryError = null } },
                                prefix = { Text("RM ", color = MutedText) },
                                suffix = { Text(" /day", color = MutedText, fontSize = 12.sp) },
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                isError = salaryError != null,
                                supportingText = salaryError?.let { { Text(it, color = Color.Red, fontSize = 12.sp) } },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Black, focusedBorderColor = DarkNavy, errorBorderColor = Color.Red),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Job Duration", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Working Date", fontSize = 11.sp, color = DarkNavy, modifier = Modifier.weight(1f))
                            Text("Working Hours", fontSize = 11.sp, color = DarkNavy, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PickerField(startDate, "Select date", isError = startDateError != null, errorMessage = startDateError, modifier = Modifier.weight(1f)) { showStartDatePicker = true }
                            PickerField(startTime, "Start time", isError = startTimeError != null, errorMessage = startTimeError, modifier = Modifier.weight(1f)) { showStartTimePicker = true }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PickerField(endDate, "Select date", isError = endDateError != null, errorMessage = endDateError, modifier = Modifier.weight(1f)) { showEndDatePicker = true }
                            PickerField(endTime, "End time", isError = endTimeError != null, errorMessage = endTimeError, modifier = Modifier.weight(1f)) { showEndTimePicker = true }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        SectionLabel("3", "Details")
                        FormField("Location", location, isError = locationError != null, errorMessage = locationError) { location = it; locationError = null }
                        Spacer(modifier = Modifier.height(12.dp))
                        FormField("Job Description", description, isError = descriptionError != null, errorMessage = descriptionError) { description = it; descriptionError = null }
                        Spacer(modifier = Modifier.height(12.dp))
                        FormField("Requirements", requirements, isError = requirementsError != null, errorMessage = requirementsError) { requirements = it; requirementsError = null }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Number of People Needed", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (peopleNeeded > 1) peopleNeeded-- }) { Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                            Text(peopleNeeded.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                            IconButton(onClick = { peopleNeeded++ }) { Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(errorMessage, color = Color.Red, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    titleError = null; companyNameError = null; salaryError = null
                                    startDateError = null; endDateError = null; startTimeError = null; endTimeError = null
                                    locationError = null; descriptionError = null; requirementsError = null

                                    if (title.isBlank()) titleError = "Job title is required"
                                    if (companyName.isBlank()) companyNameError = "Company name is required"
                                    if (salary.isBlank() || salary.toIntOrNull() == null || salary.toInt() <= 0) salaryError = "Salary must be more than RM0"
                                    if (startDate.isBlank()) startDateError = "Start date is required"
                                    if (endDate.isBlank()) endDateError = "End date is required"
                                    if (startTime.isBlank()) startTimeError = "Start time is required"
                                    if (endTime.isBlank()) endTimeError = "End time is required"
                                    if (location.isBlank()) locationError = "Location is required"
                                    if (description.isBlank()) descriptionError = "Job description is required"
                                    if (requirements.isBlank()) requirementsError = "Requirements are required"

                                    val hasError = listOf(
                                        titleError, companyNameError, salaryError, startDateError,
                                        endDateError, startTimeError, endTimeError, locationError,
                                        descriptionError, requirementsError
                                    ).any { it != null }

                                    if (hasError) return@Button

                                    showUpdateConfirm = true
                                },
                                enabled = !isSubmitting,
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                if (isSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text("Update", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Button(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.weight(1f).height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                            ) {
                                Text("Delete", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        // --- READ-ONLY VIEW: plain text, no input boxes ---
                        ReadOnlySection("1", "Basic Information") {
                            ReadOnlyRow("Job Title", title)
                            ReadOnlyRow("Company Name", companyName)
                            ReadOnlyRow("Category", category)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        ReadOnlySection("2", "Salary & Duration") {
                            ReadOnlyRow("Salary", "RM $salary / day")
                            ReadOnlyRow("Working Date", "$startDate  →  $endDate")
                            ReadOnlyRow("Working Hours", "$startTime  –  $endTime")
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        ReadOnlySection("3", "Details") {
                            ReadOnlyRow("Location", location)
                            ReadOnlyRow("Job Description", description)
                            ReadOnlyRow("Requirements", requirements)
                            ReadOnlyRow("People Needed", peopleNeeded.toString())
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = MutedText,
            title = { Text("Delete this job?", fontWeight = FontWeight.Bold,color=DarkNavy) },
            text = { Text("This can't be undone. Applicants will no longer see this listing.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDeleteClick()
                }) { Text("Delete", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = MutedText) }
            }
        )
    }

    // Update confirmation
    if (showUpdateConfirm) {
        AlertDialog(
            onDismissRequest = { showUpdateConfirm = false },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = MutedText,
            title = { Text("Save these changes?", fontWeight = FontWeight.Bold,color=DarkNavy) },
            text = { Text("This will update the job posting immediately.") },
            confirmButton = {
                TextButton(onClick = {
                    showUpdateConfirm = false
                    onUpdateClick(
                        PostJobFormData(
                            title = title, companyName = companyName, category = category,
                            salary = salary, startDate = startDate, endDate = endDate,
                            workingHoursStart = startTime, workingHoursEnd = endTime,
                            location = location, description = description,
                            requirements = requirements, peopleNeeded = peopleNeeded
                        )
                    )
                    isEditing = false
                }) { Text("Update", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateConfirm = false }) { Text("Cancel", color = MutedText) }
            }
        )
    }

    // Date/Time pickers — same pattern as PostJobScreen
    if (showStartDatePicker) {
        val today = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val state = rememberDatePickerState(selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= today
        })
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        startDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        startDateError = null
                    }
                    showStartDatePicker = false
                }) { Text("OK", color = DarkNavy) }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel", color = MutedText) } }
        ) { DatePicker(state = state) }
    }

    if (showEndDatePicker) {
        val minMillis = try {
            LocalDate.parse(startDate, DateTimeFormatter.ofPattern("MMM dd, yyyy")).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (e: Exception) {
            LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val state = rememberDatePickerState(selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= minMillis
        })
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        endDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        endDateError = null
                    }
                    showEndDatePicker = false
                }) { Text("OK", color = DarkNavy) }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel", color = MutedText) } }
        ) { DatePicker(state = state) }
    }

    if (showStartTimePicker) {
        val state = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startTime = LocalTime.of(state.hour, state.minute).format(DateTimeFormatter.ofPattern("h:mm a"))
                    startTimeError = null
                    showStartTimePicker = false
                }) { Text("OK", color = DarkNavy) }
            },
            dismissButton = { TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel", color = MutedText) } },
            text = { TimePicker(state = state) }
        )
    }

    if (showEndTimePicker) {
        val state = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endTime = LocalTime.of(state.hour, state.minute).format(DateTimeFormatter.ofPattern("h:mm a"))
                    endTimeError = null
                    showEndTimePicker = false
                }) { Text("OK", color = DarkNavy) }
            },
            dismissButton = { TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel", color = MutedText) } },
            text = { TimePicker(state = state) }
        )
    }
}

@Composable
private fun ReadOnlySection(number: String, label: String, content: @Composable ColumnScope.() -> Unit) {
    SectionLabel(number, label)
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        content()
    }
}

@Composable
private fun ReadOnlyRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = MutedText)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value.ifBlank { "—" },
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsScreenPreview() {
    PartTimeGOTheme {
        DetailsScreen(
            initialData = PostJobFormData(
                title = "Event Crew",
                companyName = "AEG Live MY",
                category = "Event Crew",
                salary = "100",
                startDate = "Jul 20, 2026",
                endDate = "Jul 20, 2026",
                workingHoursStart = "9:00 AM",
                workingHoursEnd = "6:00 PM",
                location = "KLCC",
                description = "Help set up and manage event booths",
                requirements = "21 years old and above",
                peopleNeeded = 10
            ),
            isSubmitting = false,
            errorMessage = null
        )
    }
}

@Preview(showBackground = true, name = "Editing")
@Composable
fun DetailsScreenEditingPreview() {
    PartTimeGOTheme {
        DetailsScreen(
            initialData = PostJobFormData(
                title = "Sales Promoter",
                companyName = "HLA",
                category = "Promoter",
                salary = "90",
                startDate = "Jul 25, 2026",
                endDate = "Jul 26, 2026",
                workingHoursStart = "10:00 AM",
                workingHoursEnd = "7:00 PM",
                location = "Pavilion Mall",
                description = "Promote seasonal collection",
                requirements = "Sales experience preferred",
                peopleNeeded = 5
            ),
            isSubmitting = false,
            errorMessage = null
        )
    }
}