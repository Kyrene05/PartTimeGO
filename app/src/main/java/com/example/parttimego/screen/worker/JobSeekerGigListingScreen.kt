package com.example.parttimego.screen.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parttimego.data.local.JobEntity
import com.example.parttimego.nav.JobSeekerNavBar
import com.example.parttimego.nav.JobSeekerNavItem
import com.example.parttimego.ui.theme.PartTimeGOTheme
import com.example.parttimego.viewmodel.JobViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val Purple = Color(0xFF262075)
private val AccentGreen = Color(0xFF2E9E5B)
private val TagRed = Color(0xFFE53935)

private enum class DateFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    WEEKEND("Weekend"),
    ONE_WEEK("1 Week"),
    ONE_MONTH("1 Month")
}

private val jobDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

private fun jobMatchesDateFilter(job: JobEntity, filter: DateFilter): Boolean {

    if (filter == DateFilter.ALL) return true

    val startDateText = job.startDate ?: return false

    val jobDate = try {
        jobDateFormat.parse(startDateText)
    } catch (e: Exception) {
        null
    } ?: return false

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val jobCal = Calendar.getInstance().apply {
        time = jobDate
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val diffDays = ((jobCal.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

    return when (filter) {
        DateFilter.TODAY -> diffDays == 0
        DateFilter.WEEKEND -> {
            val dow = jobCal.get(Calendar.DAY_OF_WEEK)
            diffDays in 0..7 && (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY)
        }
        DateFilter.ONE_WEEK -> diffDays in 0..7
        DateFilter.ONE_MONTH -> diffDays in 0..30
        DateFilter.ALL -> true
    }
}


@Composable
fun JobSeekerGigListingScreen(
    onBackClick: () -> Unit = {},
    onGigClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAppliedClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    jobViewModel: JobViewModel = viewModel()
) {

    val jobs by jobViewModel
        .getAllJobs()
        .collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        jobViewModel.refreshAllJobs()
    }

    JobSeekerGigListingContent(
        jobs = jobs,
        onBackClick = onBackClick,
        onGigClick = onGigClick,
        onHomeClick = onHomeClick,
        onAppliedClick = onAppliedClick,
        onProfileClick = onProfileClick
    )
}


@Composable
fun JobSeekerGigListingContent(
    jobs: List<JobEntity>,
    onBackClick: () -> Unit = {},
    onGigClick: (String) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onAppliedClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedCategory by remember {
        mutableStateOf("All")
    }

    var selectedDateFilter by remember {
        mutableStateOf(DateFilter.ALL)
    }

    val categories = listOf(
        "All",
        "Event Crew",
        "Promoter",
        "Retail",
        "F&B",
        "Other"
    )

    val filteredJobs = jobs.filter { job ->

        val matchesSearch =
            job.title.contains(searchText, ignoreCase = true) ||
                    job.category.contains(searchText, ignoreCase = true) ||
                    job.location.contains(searchText, ignoreCase = true)

        val matchesCategory =
            selectedCategory == "All" ||
                    job.category.equals(
                        selectedCategory,
                        ignoreCase = true
                    )

        val matchesDate = jobMatchesDateFilter(job, selectedDateFilter)

        matchesSearch && matchesCategory && matchesDate
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Purple)
                .padding(
                    start = 8.dp,
                    end = 20.dp,
                    top = 14.dp,
                    bottom = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBackClick
            ) {

                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Gig Listings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AccentGreen
            ) {

                Text(
                    text = "${filteredJobs.size} jobs",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Search bar
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
                            text = "Search by title, company, location...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {

                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Date filter tabs
            item {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {

                    DateFilter.values().forEach { filter ->

                        DateFilterTab(
                            text = filter.label,
                            selected = selectedDateFilter == filter,
                            onClick = {
                                selectedDateFilter = filter
                            }
                        )
                    }
                }
            }

            // Categories
            item {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {

                    categories.forEach { category ->

                        CategoryTab(
                            text = category,
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category
                            }
                        )
                    }
                }
            }

            // Gig cards
            if (filteredJobs.isEmpty()) {

                item {

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF5F5F5)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(25.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "No gigs found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(5.dp)
                            )

                            Text(
                                text = "Try another search or category.",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

            } else {

                items(
                    items = filteredJobs,
                    key = { it.id }
                ) { job ->

                    JobSeekerGigListingCard(
                        job = job,
                        onGigClick = {
                            onGigClick(job.id)
                        },
                        onApplyClick = {
                            onGigClick(job.id)
                        }
                    )
                }
            }
        }

        JobSeekerNavBar(
            selectedItem = JobSeekerNavItem.EXPLORE,
            onHomeClick = onHomeClick,
            onExploreClick = {},
            onAppliedClick = onAppliedClick,
            onProfileClick = onProfileClick
        )
    }
}


@Composable
private fun DateFilterTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier.clickable {
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Purple else Color(0xFFF1F1F1)
    ) {

        Text(
            text = text,
            color = if (selected) Color.White else Color.DarkGray,
            fontSize = 12.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            modifier = Modifier.padding(
                horizontal = 14.dp,
                vertical = 8.dp
            )
        )
    }
}


@Composable
private fun CategoryTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier.clickable {
            onClick()
        },
        shape = RoundedCornerShape(10.dp),
        color = if (selected) Purple.copy(alpha = 0.10f) else Color(0xFFF1F1F1)
    ) {

        Text(
            text = text,
            color = if (selected) Purple else Color.DarkGray,
            fontSize = 12.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 9.dp
            )
        )
    }
}


@Composable
private fun JobSeekerGigListingCard(
    job: JobEntity,
    onGigClick: () -> Unit,
    onApplyClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onGigClick()
            },
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.Top
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = job.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = job.companyName ?: "Company",
                        fontSize = 13.sp,
                        color = Color.DarkGray
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
                    modifier = Modifier.width(5.dp)
                )

                Text(
                    text = job.location,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(
                    modifier = Modifier.width(5.dp)
                )

                Text(
                    text = job.startDate ?: "Flexible date",
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "RM ${job.salary} / ${job.salaryPeriod}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onApplyClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen
                    )
                ) {

                    Text(
                        text = "Apply Now",
                        fontSize = 13.sp
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

/*
 * PREVIEW
 * No ViewModel is created here.
 */

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun JobSeekerGigListingScreenPreview() {

    PartTimeGOTheme {

        JobSeekerGigListingContent(
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
                    tag = "Popular",
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