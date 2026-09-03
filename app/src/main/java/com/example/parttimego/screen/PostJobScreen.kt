package com.example.parttimego.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.MutedText
import com.example.parttimego.ui.theme.PartTimeGOTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.material.icons.filled.Lock

// companyName is supplied by the caller (read from the employer profile) and
// is locked/read-only in this form — see the "Company Name (Locked)" field below.
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

private object PostJobDraftPrefs {
    private const val PREFS_NAME = "post_job_draft"

    fun save(context: Context, data: PostJobFormData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("title", data.title)
            .putString("category", data.category)
            .putString("salary", data.salary)
            .putString("startDate", data.startDate)
            .putString("endDate", data.endDate)
            .putString("workingHoursStart", data.workingHoursStart)
            .putString("workingHoursEnd", data.workingHoursEnd)
            .putString("location", data.location)
            .putString("description", data.description)
            .putString("requirements", data.requirements)
            .putInt("peopleNeeded", data.peopleNeeded)
            .apply()
    }

    fun load(context: Context): PostJobFormData? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val title = prefs.getString("title", "") ?: ""
        // If there's no title saved, treat it as "no draft exists" — avoids restoring
        // an all-empty form and pretending that's a meaningful draft.
        if (title.isBlank()) return null

        return PostJobFormData(
            title = title,
            companyName = "",
            category = prefs.getString("category", "Event Crew") ?: "Event Crew",
            salary = prefs.getString("salary", "") ?: "",
            startDate = prefs.getString("startDate", "") ?: "",
            endDate = prefs.getString("endDate", "") ?: "",
            workingHoursStart = prefs.getString("workingHoursStart", "") ?: "",
            workingHoursEnd = prefs.getString("workingHoursEnd", "") ?: "",
            location = prefs.getString("location", "") ?: "",
            description = prefs.getString("description", "") ?: "",
            requirements = prefs.getString("requirements", "") ?: "",
            peopleNeeded = prefs.getInt("peopleNeeded", 1)
        )
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobScreen(
    companyName: String,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onBackClick: () -> Unit = {},
    onPostClick: (PostJobFormData) -> Unit = {},
    onDashboardTabClick: () -> Unit = {},
    onProfileTabClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val draft = remember { PostJobDraftPrefs.load(context) }

    var title by remember { mutableStateOf(draft?.title ?: "") }
    var category by remember { mutableStateOf(draft?.category ?: "Event Crew") }
    var salary by remember { mutableStateOf(draft?.salary ?: "") }
    var startDate by remember { mutableStateOf(draft?.startDate ?: "") }
    var endDate by remember { mutableStateOf(draft?.endDate ?: "") }
    var startTime by remember { mutableStateOf(draft?.workingHoursStart ?: "") }
    var endTime by remember { mutableStateOf(draft?.workingHoursEnd ?: "") }
    var location by remember { mutableStateOf(draft?.location ?: "") }
    var description by remember { mutableStateOf(draft?.description ?: "") }
    var requirements by remember { mutableStateOf(draft?.requirements ?: "") }
    var peopleNeeded by remember { mutableStateOf(draft?.peopleNeeded ?: 1) }

    LaunchedEffect(title, category, salary, startDate, endDate, startTime, endTime, location, description, requirements, peopleNeeded) {
        PostJobDraftPrefs.save(
            context,
            PostJobFormData(
                title = title, companyName = companyName, category = category,
                salary = salary, startDate = startDate, endDate = endDate,
                workingHoursStart = startTime, workingHoursEnd = endTime,
                location = location, description = description,
                requirements = requirements, peopleNeeded = peopleNeeded
            )
        )
    }

    // Per-field error messages — null means no error
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
    var showPostConfirm by remember { mutableStateOf(false) }

    val categories = listOf("Event Crew", "Promoter", "Retail", "F&B","Other")

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Tracks each field's vertical position within the scrollable form,
    // so we can scroll to the first invalid one on submit.
    val fieldPositions = remember { mutableStateMapOf<String, Float>() }

    fun scrollToField(key: String) {
        fieldPositions[key]?.let { y ->
            coroutineScope.launch {
                scrollState.animateScrollTo((y - 24f).coerceAtLeast(0f).toInt())
            }
        }
    }

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
                        .verticalScroll(scrollState)
                        .padding(20.dp)
                ) {
                    SectionLabel("1", "Basic Information")

                    FormField(
                        label = "Job Title",
                        value = title,
                        isError = titleError != null,
                        errorMessage = titleError,
                        modifier = Modifier.trackPosition("title", fieldPositions)
                    ) { title = it; titleError = null }
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Company Name (Locked)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = companyName.ifBlank { "Set company name in Profile" },
                            onValueChange = { },
                            readOnly = true,
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Locked",
                                    tint = MutedText,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Black,
                                disabledBorderColor = Color.Black,
                                disabledTextColor = if (companyName.isBlank()) Color.Red else Color.Black,
                                disabledContainerColor = Color(0xFFF5F5F5)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
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

                    // Salary — fixed RM prefix, digits only
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .trackPosition("salary", fieldPositions)
                    ) {
                        Text("Salary (RM/day)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = salary,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    salary = input
                                    salaryError = null
                                }
                            },
                            prefix = { Text("RM ", color = MutedText) },
                            suffix = { Text(" /day", color = MutedText, fontSize = 12.sp) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = salaryError != null,
                            supportingText = salaryError?.let { { Text(it, color = Color.Red, fontSize = 12.sp) } },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Black,
                                focusedBorderColor = DarkNavy,
                                errorBorderColor = Color.Red
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Job Duration
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
                            isError = startDateError != null,
                            errorMessage = startDateError,
                            modifier = Modifier
                                .weight(1f)
                                .trackPosition("startDate", fieldPositions),
                            onClick = { showStartDatePicker = true }
                        )
                        PickerField(
                            value = startTime,
                            placeholder = "Start time",
                            isError = startTimeError != null,
                            errorMessage = startTimeError,
                            modifier = Modifier
                                .weight(1f)
                                .trackPosition("startTime", fieldPositions),
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
                            isError = endDateError != null,
                            errorMessage = endDateError,
                            modifier = Modifier
                                .weight(1f)
                                .trackPosition("endDate", fieldPositions),
                            onClick = { showEndDatePicker = true }
                        )
                        PickerField(
                            value = endTime,
                            placeholder = "End time",
                            isError = endTimeError != null,
                            errorMessage = endTimeError,
                            modifier = Modifier
                                .weight(1f)
                                .trackPosition("endTime", fieldPositions),
                            onClick = { showEndTimePicker = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    SectionLabel("3", "Details")

                    FormField(
                        label = "Location",
                        value = location,
                        isError = locationError != null,
                        errorMessage = locationError,
                        modifier = Modifier.trackPosition("location", fieldPositions)
                    ) { location = it; locationError = null }
                    Spacer(modifier = Modifier.height(12.dp))

                    FormField(
                        label = "Job Description",
                        value = description,
                        isError = descriptionError != null,
                        errorMessage = descriptionError,
                        modifier = Modifier.trackPosition("description", fieldPositions)
                    ) { description = it; descriptionError = null }
                    Spacer(modifier = Modifier.height(12.dp))

                    FormField(
                        label = "Requirements",
                        value = requirements,
                        isError = requirementsError != null,
                        errorMessage = requirementsError,
                        modifier = Modifier.trackPosition("requirements", fieldPositions)
                    ) { requirements = it; requirementsError = null }

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

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorMessage, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            // Reset all errors first
                            titleError = null; companyNameError = null; salaryError = null
                            startDateError = null; endDateError = null
                            startTimeError = null; endTimeError = null
                            locationError = null; descriptionError = null; requirementsError = null

                            if (title.isBlank()) titleError = "Job title is required"
                            if (companyName.isBlank()) companyNameError = "Company name is required. Set it in your Profile first."
                            if (salary.isBlank() || salary.toIntOrNull() == null || salary.toInt() <= 0) {
                                salaryError = "Salary must be more than RM0"
                            }
                            if (startDate.isBlank()) startDateError = "Start date is required"
                            if (endDate.isBlank()) endDateError = "End date is required"
                            if (startTime.isBlank()) startTimeError = "Start time is required"
                            if (endTime.isBlank()) endTimeError = "End time is required"
                            if (location.isBlank()) locationError = "Location is required"
                            if (description.isBlank()) descriptionError = "Job description is required"
                            if (requirements.isBlank()) requirementsError = "Requirements are required"

                            // Order matters: scroll to whichever invalid field appears first on screen
                            val firstErrorKey = listOf(
                                "title" to titleError,
                                "companyName" to companyNameError,
                                "salary" to salaryError,
                                "startDate" to startDateError,
                                "startTime" to startTimeError,
                                "endDate" to endDateError,
                                "endTime" to endTimeError,
                                "location" to locationError,
                                "description" to descriptionError,
                                "requirements" to requirementsError
                            ).firstOrNull { it.second != null }?.first

                            if (firstErrorKey != null) {
                                scrollToField(firstErrorKey)
                                return@Button
                            }

                            showPostConfirm = true
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

    // Post confirmation
    if (showPostConfirm) {
        AlertDialog(
            onDismissRequest = { showPostConfirm = false },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = MutedText,
            title = { Text("Post this job?", fontWeight = FontWeight.Bold,color=DarkNavy) },
            text = { Text("This job will be visible to job seekers immediately.") },
            confirmButton = {
                TextButton(onClick = {
                    showPostConfirm = false
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
                    PostJobDraftPrefs.clear(context)
                }) { Text("Post", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showPostConfirm = false }) { Text("Cancel", color = MutedText) }
            }
        )
    }

    // Start Date Picker
    if (showStartDatePicker) {
        val today = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= today
            }
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        startDateError = null
                    }
                    showStartDatePicker = false
                }) { Text("OK", color = DarkNavy) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel", color = MutedText) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End Date Picker
    if (showEndDatePicker) {
        val minEndDateMillis = if (startDate.isNotBlank()) {
            try {
                LocalDate.parse(startDate, DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            } catch (e: Exception) {
                LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }
        } else {
            LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= minEndDateMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        endDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                        endDateError = null
                    }
                    showEndDatePicker = false
                }) { Text("OK", color = DarkNavy) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel", color = MutedText) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Start Time Picker
    if (showStartTimePicker) {
        val timeState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startTime = LocalTime.of(timeState.hour, timeState.minute)
                        .format(DateTimeFormatter.ofPattern("h:mm a"))
                    startTimeError = null
                    showStartTimePicker = false
                }) { Text("OK", color = DarkNavy) }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel", color = MutedText) }
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
                        periodSelectorBorderColor = Color.Black,
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

    // End Time Picker
    if (showEndTimePicker) {
        val timeState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endTime = LocalTime.of(timeState.hour, timeState.minute)
                        .format(DateTimeFormatter.ofPattern("h:mm a"))
                    endTimeError = null
                    showEndTimePicker = false
                }) { Text("OK", color = DarkNavy) }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel", color = MutedText) }
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
                        periodSelectorBorderColor = Color.Black,
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

// Helper: records a composable's y-position within the scrollable parent
internal fun Modifier.trackPosition(key: String, positions: MutableMap<String, Float>): Modifier =
    this.onGloballyPositioned { coordinates ->
        positions[key] = coordinates.boundsInParent().top
    }

@Composable
internal fun SectionLabel(number: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE8E9F5))
                .border(1.5.dp, DarkNavy, RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, fontSize = 13.sp, color = DarkNavy, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
    }
}

@Composable
internal fun FormField(
    label: String,
    value: String,
    isError: Boolean = false,
    errorMessage: String? = null,
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
            isError = isError,
            supportingText = errorMessage?.let { { Text(it, color = Color.Red, fontSize = 12.sp) } },
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Black,
                focusedBorderColor = DarkNavy,
                errorBorderColor = Color.Red
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun PickerField(
    value: String,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            enabled = false,
            isError = isError,
            placeholder = { Text(placeholder, fontSize = 12.sp) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Black,
                disabledBorderColor = if (isError) Color.Red else Color.Black,
                disabledTextColor = Color.Black,
                disabledPlaceholderColor = MutedText,
                errorBorderColor = Color.Red
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        )
        if (errorMessage != null) {
            Text(errorMessage, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
        }
    }
}

@Composable
internal fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) DarkNavy else Color.White,
        border = BorderStroke(1.dp, if (selected) DarkNavy else Color.Black),
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
        PostJobScreen(companyName = "AEG Live MY", isSubmitting = false, errorMessage = null)
    }
}

@Preview(showBackground = true, name = "Submitting")
@Composable
fun PostJobScreenSubmittingPreview() {
    PartTimeGOTheme {
        PostJobScreen(companyName = "AEG Live MY", isSubmitting = true, errorMessage = null)
    }
}
