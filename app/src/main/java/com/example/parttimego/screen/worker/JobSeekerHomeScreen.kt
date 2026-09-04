package com.example.parttimego.screen.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.data.local.JobEntity
import com.example.parttimego.data.model.JobSeeker
import com.example.parttimego.data.repository.ApplicationRepository
import com.example.parttimego.nav.JobSeekerNavBar
import com.example.parttimego.nav.JobSeekerNavItem
import com.example.parttimego.ui.theme.PartTimeGOTheme
import com.example.parttimego.viewmodel.JobViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private val Purple = Color(0xFF262075)
private val AccentGreen = Color(0xFF2E9E5B)
private val TagRed = Color(0xFFE53935)


@Composable
fun JobSeekerHomeScreen(
    onGigClick: (String) -> Unit = {},
    onExploreClick: () -> Unit = {},
    onViewTodayGigsClick: () -> Unit = onExploreClick,
    onAppliedClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCompanyClick: (String) -> Unit,
    jobViewModel: JobViewModel = viewModel()
) {

    val jobs by jobViewModel
        .getAllJobs()
        .collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        jobViewModel.refreshAllJobs()
    }

    var userName by remember {
        mutableStateOf("User")
    }

    LaunchedEffect(Unit) {

        val currentUser =
            SupabaseClient.client.auth.currentUserOrNull()

        val currentUserId =
            currentUser?.id

        // get name
        val authUserName =
            currentUser
                ?.userMetadata
                ?.get("full_name")
                ?.toString()
                ?.trim('"')

        if (!authUserName.isNullOrBlank()) {
            userName = authUserName
        }

        // get name from worker table
        if (currentUserId != null) {

            try {

                val worker =
                    SupabaseClient.client
                        .postgrest["worker"]
                        .select {
                            filter {
                                eq(
                                    "user_id",
                                    currentUserId
                                )
                            }
                        }
                        .decodeSingleOrNull<JobSeeker>()

                if (
                    worker != null &&
                    worker.jobSeekerName.isNotBlank()
                ) {
                    userName = worker.jobSeekerName
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    JobSeekerHomeContent(
        jobs = jobs,
        userName = userName,
        onGigClick = onGigClick,
        onExploreClick = onExploreClick,
        onViewTodayGigsClick = onViewTodayGigsClick,
        onAppliedClick = onAppliedClick,
        onProfileClick = onProfileClick,
        onCompanyClick = onCompanyClick
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSeekerHomeContent(
    jobs: List<JobEntity>,
    userName: String = "",
    onGigClick: (String) -> Unit = {},
    onExploreClick: () -> Unit = {},
    onViewTodayGigsClick: () -> Unit = onExploreClick,
    onAppliedClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCompanyClick: (String) -> Unit = {}
) {

    var searchText by remember {
        mutableStateOf("")
    }

    var showFilterSheet by remember { mutableStateOf(false) }
    // Applied filter values
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var selectedSalaryRange by remember { mutableStateOf<String?>(null) }
    var selectedWorkingHour by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    // Temporary filter values inside Bottom Sheet
    var tempLocation by remember { mutableStateOf<String?>(null) }
    var tempSalaryRange by remember { mutableStateOf<String?>(null) }
    var tempWorkingHour by remember { mutableStateOf<String?>(null) }
    var tempCategory by remember { mutableStateOf<String?>(null) }

    // For Apply part
    var selectedJob by remember { mutableStateOf<JobEntity?>(null) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var appliedJobIds by remember { mutableStateOf(setOf<String>()) }

    val scope = rememberCoroutineScope()
    val repository = remember { ApplicationRepository() }
    val userId =
        SupabaseClient.client
            .auth
            .currentUserOrNull()
            ?.id

    LaunchedEffect(userId, jobs) {

        if (userId != null) {

            val appliedIds = mutableSetOf<String>()

            jobs.forEach { job ->

                try {

                    if (
                        repository.hasApplied(
                            jobId = job.id,
                            applicantId = userId
                        )
                    ) {
                        appliedIds.add(job.id)
                    }

                } catch (_: Exception) {
                }
            }

            appliedJobIds = appliedIds
        }
    }

    val today = LocalDate.now()

    val todayJobs = jobs.filter { job ->
        runCatching {
            Instant.parse(job.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() == today
        }.getOrDefault(false)
    }

    val locations = jobs
        .map { it.location }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    val filteredJobs = jobs.filter { job ->

        val matchesSearch =
            searchText.isBlank() ||
                    job.title.contains(searchText, ignoreCase = true) ||
                    job.category.contains(searchText, ignoreCase = true) ||
                    job.location.contains(searchText, ignoreCase = true) ||
                    job.companyName?.contains(searchText, ignoreCase = true) == true

        val matchesLocation =
            selectedLocation == null ||
                    job.location.equals(selectedLocation, ignoreCase = true)

        val matchesCategory =
            selectedCategory == null ||
                    job.category.equals(selectedCategory, ignoreCase = true)

        val matchesSalary = when (selectedSalaryRange) {
            null -> true
            "Below RM50" -> job.salary < 50
            "RM50 - RM100" -> job.salary in 50.0..100.0
            "RM101 - RM150" -> job.salary > 100 && job.salary <= 150
            "Above RM150" -> job.salary > 150
            else -> true
        }

        val matchesWorkingHour = when (selectedWorkingHour) {
            null -> true
            "Morning" -> job.workingHoursStart?.startsWith("0") == true ||
                    job.workingHoursStart?.startsWith("1") == true
            "Afternoon" -> job.workingHoursStart?.contains("12") == true ||
                    job.workingHoursStart?.contains("13") == true ||
                    job.workingHoursStart?.contains("14") == true ||
                    job.workingHoursStart?.contains("15") == true ||
                    job.workingHoursStart?.contains("16") == true ||
                    job.workingHoursStart?.contains("17") == true
            "Evening" -> job.workingHoursStart?.contains("18") == true ||
                    job.workingHoursStart?.contains("19") == true ||
                    job.workingHoursStart?.contains("20") == true ||
                    job.workingHoursStart?.contains("21") == true ||
                    job.workingHoursStart?.contains("22") == true ||
                    job.workingHoursStart?.contains("23") == true
            "Full Day" -> true
            else -> true
        }

        matchesSearch &&
                matchesLocation &&
                matchesCategory &&
                matchesSalary &&
                matchesWorkingHour
    }

    val categoryCounts = jobs.groupingBy { it.category }.eachCount()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Purple)
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            JobSeekerHomeHeader(
                userName = userName,
                onProfileClick = onProfileClick
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp
                )
            ) {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        end = 20.dp,
                        top = 20.dp,
                        bottom = 20.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    item {

                        OutlinedTextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    text = "Search jobs, companies ...",
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {

                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            trailingIcon = {

                                IconButton(
                                    onClick = {

                                        tempLocation = selectedLocation
                                        tempSalaryRange = selectedSalaryRange
                                        tempWorkingHour = selectedWorkingHour
                                        tempCategory = selectedCategory

                                        showFilterSheet = true
                                    }
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = Purple
                                    )
                                }
                            },
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    item {

                        TodayOpportunityCard(
                            jobsCount = todayJobs.size,
                            enabled = todayJobs.isNotEmpty(),
                            onClick = onViewTodayGigsClick
                        )
                    }

                    item {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Categories",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "See All",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Purple,
                                modifier = Modifier.clickable {
                                    onExploreClick()
                                }
                            )
                        }
                    }

                    item {

                        CategoryGrid(
                            categoryCounts = categoryCounts,
                            onCategoryClick = { category ->
                                searchText = category
                            }
                        )
                    }

                    item {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Recommended Gigs",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "See all",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Purple,
                                modifier = Modifier.clickable {
                                    onExploreClick()
                                }
                            )
                        }
                    }

                    if (filteredJobs.isEmpty()) {

                        item {
                            EmptyJobsCard()
                        }

                    } else {

                        items(
                            items = filteredJobs.take(5),
                            key = { it.id }
                        ) { job ->

                            JobSeekerJobCard(
                                job = job,
                                isApplied = job.id in appliedJobIds,
                                onClick = {
                                    onGigClick(job.id)
                                },
                                onApplyClick = {
                                    selectedJob = job
                                    if (job.id in appliedJobIds) {
                                        showCancelDialog = true
                                    } else {
                                        showApplyDialog = true
                                    }
                                },
                                onCompanyClick = { employerId ->
                                    onCompanyClick(employerId)
                                }
                            )
                        }
                    }
                }
            }

            JobSeekerNavBar(
                selectedItem = JobSeekerNavItem.HOME,
                onHomeClick = {},
                onExploreClick = onExploreClick,
                onAppliedClick = onAppliedClick,
                onProfileClick = onProfileClick
            )
        }

        // Apply Confirmation
        if (showApplyDialog && selectedJob != null) {

            AlertDialog(
                onDismissRequest = {
                    showApplyDialog = false
                },

                title = {
                    Text("Confirm Application")
                },

                text = {
                    Text(
                        "Are you sure you want to apply for \"${selectedJob!!.title}\"?"
                    )
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            if (userId != null) {

                                scope.launch {

                                    try {

                                        repository.applyForJob(
                                            jobId = selectedJob!!.id,
                                            applicantId = userId
                                        )

                                        appliedJobIds =
                                            appliedJobIds +
                                                    selectedJob!!.id

                                    } catch (_: Exception) {
                                    }

                                    showApplyDialog = false
                                }

                            } else {

                                showApplyDialog = false
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            showApplyDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Cancel Application
        if (showCancelDialog && selectedJob != null) {

            AlertDialog(
                onDismissRequest = {
                    showCancelDialog = false
                },

                title = {
                    Text("Cancel Application")
                },

                text = {
                    Text(
                        "Are you sure you want to cancel your application for \"${selectedJob!!.title}\"?"
                    )
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            if (userId != null) {

                                scope.launch {

                                    try {

                                        repository.cancelApplication(
                                            jobId = selectedJob!!.id,
                                            applicantId = userId
                                        )

                                        appliedJobIds =
                                            appliedJobIds -
                                                    selectedJob!!.id

                                    } catch (_: Exception) {
                                    }

                                    showCancelDialog = false
                                }
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            showCancelDialog = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (showFilterSheet) {

        ModalBottomSheet(
            onDismissRequest = {
                showFilterSheet = false
            }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {

                Text(
                    text = "Filter",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Location",
                    fontWeight = FontWeight.Bold
                )

                locations.forEach { location ->
                    FilterChip(
                        selected = tempLocation == location,
                        onClick = {
                            tempLocation =
                                if (tempLocation == location) null else location
                        },
                        label = {
                            Text(location)
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Salary Range",
                    fontWeight = FontWeight.Bold
                )

                listOf(
                    "Below RM50",
                    "RM50 - RM100",
                    "RM101 - RM150",
                    "Above RM150"
                ).forEach { salary ->
                    FilterChip(
                        selected = tempSalaryRange == salary,
                        onClick = {
                            tempSalaryRange =
                                if (tempSalaryRange == salary) null else salary
                        },
                        label = {
                            Text(salary)
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Working Hour",
                    fontWeight = FontWeight.Bold
                )

                listOf(
                    "Morning",
                    "Afternoon",
                    "Evening",
                    "Full Day"
                ).forEach { hour ->
                    FilterChip(
                        selected = tempWorkingHour == hour,
                        onClick = {
                            tempWorkingHour =
                                if (tempWorkingHour == hour) null else hour
                        },
                        label = {
                            Text(hour)
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Category",
                    fontWeight = FontWeight.Bold
                )

                listOf(
                    "Event Crew",
                    "Promoter",
                    "Retail",
                    "F&B",
                    "Other"
                ).forEach { category ->
                    FilterChip(
                        selected = tempCategory == category,
                        onClick = {
                            tempCategory =
                                if (tempCategory == category) null else category
                        },
                        label = {
                            Text(category)
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    OutlinedButton(
                        onClick = {
                            tempLocation = null
                            tempSalaryRange = null
                            tempWorkingHour = null
                            tempCategory = null
                        }
                    ) {
                        Text("Reset")
                    }

                    Button(
                        onClick = {
                            selectedLocation = tempLocation
                            selectedSalaryRange = tempSalaryRange
                            selectedWorkingHour = tempWorkingHour
                            selectedCategory = tempCategory
                            showFilterSheet = false
                        }
                    ) {
                        Text("Apply Filter")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}


@Composable
private fun JobSeekerHomeHeader(
    userName: String,
    onProfileClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp,
                vertical = 18.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = "Welcome",
                color = Color.White,
                fontSize = 14.sp
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = userName,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable{
                    onProfileClick()
                },
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = userName.take(1).uppercase(),
                color = Purple,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
private fun TodayOpportunityCard(
    jobsCount: Int,
    enabled: Boolean = jobsCount > 0,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.6f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = AccentGreen
        )
    ) {

        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )

            Spacer(
                modifier = Modifier.width(10.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Today's Opportunities",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = if (jobsCount > 0) {
                        "$jobsCount new gigs posted"
                    } else {
                        "No new gigs posted today"
                    },
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.20f),
                modifier = Modifier.clickable(enabled = enabled) {
                    onClick()
                }
            ) {

                Text(
                    text = "View All",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    )
                )
            }
        }
    }
}


@Composable
private fun CategoryGrid(
    categoryCounts: Map<String, Int>,
    onCategoryClick: (String) -> Unit
) {

    val categories = listOf(
        "Event Crew",
        "Promoter",
        "Retail",
        "F&B",
        "Other"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        categories.chunked(3).forEach { rowCategories ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                rowCategories.forEach { category ->

                    CategoryBox(
                        title = category,
                        count = categoryCounts[category] ?: 0,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onCategoryClick(category)
                        }
                    )
                }

                if (rowCategories.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
private fun CategoryBox(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Surface(
        modifier = modifier.clickable {
            onClick()
        },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 14.dp
            )
        ) {

            Text(
                text = title,
                color = Color.Black,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = "$count jobs",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}


@Composable
private fun JobSeekerJobCard(
    job: JobEntity,
    isApplied: Boolean,
    onClick: () -> Unit,
    onApplyClick: () -> Unit,
    onCompanyClick: (String) -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = job.title,
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = job.companyName ?: "Company",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        style = androidx.compose.ui.text.TextStyle(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        ),
                        modifier = Modifier
                            .clickable {
                                onCompanyClick(job.employerId)
                            }
                            .padding(vertical = 2.dp)
                    )
                }

                JobTagBadge(tag = job.tag)
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = job.location,
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(
                    modifier = Modifier.width(4.dp)
                )

                Text(
                    text = job.startDate ?: "Flexible date",
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "RM ${job.salary} / ${job.salaryPeriod}",
                    color = Purple,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onApplyClick,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                if (isApplied)
                                    Color.Gray
                                else
                                    AccentGreen
                        ),
                    shape =
                        RoundedCornerShape(10.dp)
                ) {

                    Text(
                        if (isApplied)
                            "Applied"
                        else
                            "Apply Now"
                    )
                }
            }
        }
    }
}


@Composable
private fun JobTagBadge(tag: String?) {

    if (tag.isNullOrBlank()) return

    val badgeColor = when (tag.lowercase()) {
        "hot" -> TagRed
        "new" -> AccentGreen
        else -> Purple
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = badgeColor
    ) {

        Text(
            text = tag,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
        )
    }
}


@Composable
private fun EmptyJobsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
        color = Color.White
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "No gigs available",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = "Check again later for new opportunities.",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}


/*
 * PREVIEW
 * This does NOT create JobViewModel.
 * Therefore, Android Studio Preview should not show
 * "Failed to instantiate a ViewModel".
 */

@Preview(showBackground = true)
@Composable
fun JobSeekerHomeScreenPreview() {

    PartTimeGOTheme {

        JobSeekerHomeContent(
            userName = "Ahmad Faris",
            jobs = listOf(
                JobEntity(
                    id = "1",
                    employerId = "employer1",
                    title = "Event Crew",
                    companyName = "ABC Events",
                    category = "Event",
                    salary = 100.0,
                    salaryPeriod = "day",
                    startDate = "5 Sep 2026",
                    endDate = "5 Sep 2026",
                    workingHoursStart = "9:00 AM",
                    workingHoursEnd = "6:00 PM",
                    location = "Georgetown",
                    description = "Help with event setup.",
                    requirements = "Friendly and responsible.",
                    peopleNeeded = 5,
                    tag = "Hot",
                    createdAt = "2026-09-02"
                ),
                JobEntity(
                    id = "2",
                    employerId = "employer2",
                    title = "Retail Assistant",
                    companyName = "ABC Store",
                    category = "Retail",
                    salary = 12.0,
                    salaryPeriod = "hour",
                    startDate = "6 Sep 2026",
                    endDate = "6 Sep 2026",
                    workingHoursStart = "10:00 AM",
                    workingHoursEnd = "6:00 PM",
                    location = "Bayan Lepas",
                    description = "Assist customers.",
                    requirements = "Good communication.",
                    peopleNeeded = 3,
                    tag = "New",
                    createdAt = "2026-09-02"
                )
            )
        )
    }
}