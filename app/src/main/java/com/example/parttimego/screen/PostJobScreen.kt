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
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class PostJobFormData(
    val title: String,
    val companyName: String,
    val category: String,
    val salary: String,
    val startDate: String,
    val endDate: String,
    val workingHoursStart: String,
    val workingHoursEnd: String,
    val location: String,
    val description: String,
    val requirements: String,
    val peopleNeeded: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobScreen(
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onBackClick: () -> Unit = {},
    onPostClick: (PostJobFormData) -> Unit = {},
    onDashboardTabClick: () -> Unit = {},
    onProfileTabClick: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Event Crew") }
    var salary by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var peopleNeeded by remember { mutableStateOf(10) }
    var validationError by remember { mutableStateOf<String?>(null) }

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
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Post") },
                    label = { Text("Post") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = DarkNavy, selectedTextColor = DarkNavy)
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
                Text("Post Job", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // White form card
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
                    SectionLabel("1", "Basic Information")
                    FormField("Job Title", title) { title = it }
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("Company Name", companyName) { companyName = it }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.forEach { cat ->
                            CategoryChip(label = cat, selected = category == cat, onClick = { category = cat })
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    SectionLabel("2", "Salary & Duration")

                    // Salary: fixed RM prefix, digits only
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Salary (RM/day)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = salary,
                            onValueChange = { input -> if (input.all { it.isDigit() }) salary = input },
                            placeholder = { Text("xxx") },
                            prefix = { Text("RM ", color = MutedText) },
                            suffix = { Text(" /day", color = MutedText, fontSize = 12.sp) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Black, focusedBorderColor = DarkNavy),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Job Duration — date/time pickers
                    Text("Job Duration", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Working Date", fontSize = 11.sp, color = DarkNavy, modifier = Modifier.weight(1f))
                        Text("Working Hours", fontSize = 11.sp, color = DarkNavy, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PickerField(
                            value = startDate,
                            placeholder = "Select date",
                            modifier = Modifier.weight(1f),
                            onClick = { showStartDatePicker = true }
                        )
                        PickerField(
                            value = startTime,
                            placeholder = "Start time",
                            modifier = Modifier.weight(1f),
                            onClick = { showStartTimePicker = true }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PickerField(
                            value = endDate,
                            placeholder = "Select date",
                            modifier = Modifier.weight(1f),
                            onClick = { showEndDatePicker = true }
                        )
                        PickerField(
                            value = endTime,
                            placeholder = "End time",
                            modifier = Modifier.weight(1f),
                            onClick = { showEndTimePicker = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    SectionLabel("3", "Details")
                    FormField("Location", location) { location = it }
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("Job Description", description) { description = it }
                    Spacer(modifier = Modifier.height(12.dp))
                    FormField("Requirements", requirements) { requirements = it }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Number of People Needed", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (peopleNeeded > 1) peopleNeeded-- }) {
                            Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(peopleNeeded.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                        IconButton(onClick = { peopleNeeded++ }) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(validationError!!, color = Color.Red, fontSize = 12.sp)
                    }
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorMessage, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || salary.isBlank() || location.isBlank()) {
                                validationError = "Please fill in job title, salary, and location."
                                return@Button
                            }
                            validationError = null
                            onPostClick(
                                PostJobFormData(
                                    title = title,
                                    companyName = companyName,
                                    category = category,
                                    salary = salary,
                                    startDate = startDate,
                                    endDate = endDate,
                                    workingHoursStart = startTime,
                                    workingHoursEnd = endTime,
                                    location = location,
                                    description = description,
                                    requirements = requirements,
                                    peopleNeeded = peopleNeeded
                                )
                            )
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Text("Post", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    // Start Date Picker Dialog
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

// End Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        endDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        val timeState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startTime = LocalTime.of(timeState.hour, timeState.minute)
                        .format(DateTimeFormatter.ofPattern("h:mm a"))
                    showStartTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(
                    state = timeState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF0F0F5),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = DarkNavy,
                        selectorColor = DarkNavy,
                        containerColor = Color.White,
                        periodSelectorBorderColor = SoftGrey,
                        periodSelectorSelectedContainerColor = DarkNavy,
                        periodSelectorUnselectedContainerColor = Color.White,
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = DarkNavy,
                        timeSelectorSelectedContainerColor = DarkNavy,
                        timeSelectorUnselectedContainerColor = Color(0xFFF0F0F5),
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = DarkNavy
                    )
                )
            }
        )
    }

    //  End Time Picker Dialog
    if (showEndTimePicker) {
        val timeState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endTime = LocalTime.of(timeState.hour, timeState.minute)
                        .format(DateTimeFormatter.ofPattern("h:mm a"))
                    showEndTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(
                    state = timeState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFF0F0F5),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = DarkNavy,
                        selectorColor = DarkNavy,
                        containerColor = Color.White,
                        periodSelectorBorderColor = SoftGrey,
                        periodSelectorSelectedContainerColor = DarkNavy,
                        periodSelectorUnselectedContainerColor = Color.White,
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = DarkNavy,
                        timeSelectorSelectedContainerColor = DarkNavy,
                        timeSelectorUnselectedContainerColor = Color(0xFFF0F0F5),
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = DarkNavy
                    )
                )
            }
        )
    }
}

@Composable
private fun SectionLabel(number: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
        Box(
            modifier = Modifier.size(20.dp).clip(RoundedCornerShape(50)).background(DarkNavy),
            contentAlignment = Alignment.Center
        ) {
            Text(number, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Black, focusedBorderColor = DarkNavy),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PickerField(
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { },
        readOnly = true,
        enabled = false,
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.Black,
            disabledBorderColor = Color.Black,
            disabledTextColor = Color.Black,
            disabledPlaceholderColor = MutedText
        ),
        modifier = modifier.clickable { onClick() }
    )
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) DarkNavy else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) DarkNavy else Color.Black),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else MutedText,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PostJobScreenPreview() {
    PartTimeGOTheme {
        PostJobScreen(
            isSubmitting = false,
            errorMessage = null
        )
    }
}

@Preview(showBackground = true, name = "Submitting")
@Composable
fun PostJobScreenSubmittingPreview() {
    PartTimeGOTheme {
        PostJobScreen(
            isSubmitting = true,
            errorMessage = null
        )
    }
}

@Preview(showBackground = true, name = "With Error")
@Composable
fun PostJobScreenErrorPreview() {
    PartTimeGOTheme {
        PostJobScreen(
            isSubmitting = false,
            errorMessage = "You must be logged in to post a job."
        )
    }
}