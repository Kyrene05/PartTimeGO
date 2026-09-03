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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.data.local.JobEntity
import com.example.parttimego.data.repository.ApplicationRepository
import com.example.parttimego.nav.JobSeekerNavBar
import com.example.parttimego.nav.JobSeekerNavItem
import com.example.parttimego.viewmodel.JobViewModel
import io.github.jan.supabase.auth.auth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

private val Purple = Color(0xFF262075)
private val Green = Color(0xFF2E9E5B)

private enum class DateFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    WEEKEND("Weekend"),
    ONE_WEEK("1 Week"),
    ONE_MONTH("1 Month")
}

private val jobDateFormat =
    SimpleDateFormat(
        "dd MMM yyyy",
        Locale.ENGLISH
    )

private fun jobMatchesDateFilter(
    job: JobEntity,
    filter: DateFilter
): Boolean {

    if (filter == DateFilter.ALL) {
        return true
    }

    val startDateText =
        job.startDate ?: return false

    val jobDate =
        try {
            jobDateFormat.parse(startDateText)
        } catch (_: Exception) {
            null
        } ?: return false

    val today =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    val jobCal =
        Calendar.getInstance().apply {
            time = jobDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    val diffDays =
        (
                (jobCal.timeInMillis -
                        today.timeInMillis) /
                        (1000 * 60 * 60 * 24)
                ).toInt()

    return when (filter) {

        DateFilter.TODAY ->
            diffDays == 0

        DateFilter.WEEKEND -> {

            val day =
                jobCal.get(Calendar.DAY_OF_WEEK)

            diffDays in 0..7 &&
                    (
                            day == Calendar.SATURDAY ||
                                    day == Calendar.SUNDAY
                            )
        }

        DateFilter.ONE_WEEK ->
            diffDays in 0..7

        DateFilter.ONE_MONTH ->
            diffDays in 0..30

        DateFilter.ALL ->
            true
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

    val scope = rememberCoroutineScope()
    val repository = remember {
        ApplicationRepository()
    }

    val userId =
        SupabaseClient.client
            .auth
            .currentUserOrNull()
            ?.id

    var searchText by remember {
        mutableStateOf("")
    }

    var selectedCategory by remember {
        mutableStateOf("All")
    }

    var selectedDateFilter by remember {
        mutableStateOf(DateFilter.ALL)
    }

    var selectedJob by remember {
        mutableStateOf<JobEntity?>(null)
    }

    var showApplyDialog by remember {
        mutableStateOf(false)
    }

    var showCancelDialog by remember {
        mutableStateOf(false)
    }

    var appliedJobIds by remember {
        mutableStateOf(setOf<String>())
    }

    LaunchedEffect(Unit) {
        jobViewModel.refreshAllJobs()
    }

    LaunchedEffect(jobs, userId) {

        if (userId != null) {

            val applied = mutableSetOf<String>()

            jobs.forEach { job ->

                if (
                    repository.hasApplied(
                        jobId = job.id,
                        applicantId = userId
                    )
                ) {
                    applied.add(job.id)
                }
            }

            appliedJobIds = applied
        }
    }

    val categories = listOf(
        "All",
        "Event Crew",
        "Promoter",
        "Retail",
        "F&B",
        "Other"
    )

    val filteredJobs =
        jobs.filter { job ->

            val matchesSearch =
                searchText.isBlank() ||
                        job.title.contains(
                            searchText,
                            ignoreCase = true
                        ) ||
                        job.category.contains(
                            searchText,
                            ignoreCase = true
                        ) ||
                        job.location.contains(
                            searchText,
                            ignoreCase = true
                        )

            val matchesCategory =
                selectedCategory == "All" ||
                        job.category.equals(
                            selectedCategory,
                            ignoreCase = true
                        )

            val matchesDate =
                jobMatchesDateFilter(
                    job,
                    selectedDateFilter
                )

            matchesSearch &&
                    matchesCategory &&
                    matchesDate
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

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
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBackClick
            ) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                color = Green
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
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
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
                            "Search by title, company, location..."
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

            item {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        )
                ) {

                    DateFilter.values()
                        .forEach { filter ->

                            Surface(
                                modifier =
                                    Modifier.clickable {
                                        selectedDateFilter =
                                            filter
                                    },
                                shape =
                                    RoundedCornerShape(20.dp),
                                color =
                                    if (
                                        selectedDateFilter ==
                                        filter
                                    )
                                        Purple
                                    else
                                        Color(0xFFF1F1F1)
                            ) {

                                Text(
                                    text = filter.label,
                                    color =
                                        if (
                                            selectedDateFilter ==
                                            filter
                                        )
                                            Color.White
                                        else
                                            Color.DarkGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 8.dp
                                    )
                                )
                            }
                        }
                }
            }

            item {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(
                            rememberScrollState()
                        )
                ) {

                    categories.forEach { category ->

                        Surface(
                            modifier =
                                Modifier.clickable {
                                    selectedCategory =
                                        category
                                },
                            shape =
                                RoundedCornerShape(10.dp),
                            color =
                                if (
                                    selectedCategory ==
                                    category
                                )
                                    Purple.copy(
                                        alpha = 0.10f
                                    )
                                else
                                    Color(0xFFF1F1F1)
                        ) {

                            Text(
                                text = category,
                                color =
                                    if (
                                        selectedCategory ==
                                        category
                                    )
                                        Purple
                                    else
                                        Color.DarkGray,
                                fontSize = 12.sp,
                                fontWeight =
                                    if (
                                        selectedCategory ==
                                        category
                                    )
                                        FontWeight.Bold
                                    else
                                        FontWeight.Normal,
                                modifier = Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 9.dp
                                )
                            )
                        }
                    }
                }
            }

            if (filteredJobs.isEmpty()) {

                item {

                    Text(
                        text = "No gigs found.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        color = Color.Gray
                    )
                }

            } else {

                items(
                    items = filteredJobs,
                    key = { it.id }
                ) { job ->

                    GigListingCard(
                        job = job,
                        isApplied =
                            job.id in appliedJobIds,

                        onGigClick = {
                            onGigClick(job.id)
                        },

                        onApplyClick = {

                            selectedJob = job

                            if (
                                job.id in appliedJobIds
                            ) {
                                showCancelDialog = true
                            } else {
                                showApplyDialog = true
                            }
                        }
                    )
                }
            }
        }

        JobSeekerNavBar(
            selectedItem =
                JobSeekerNavItem.EXPLORE,
            onHomeClick = onHomeClick,
            onExploreClick = {},
            onAppliedClick = onAppliedClick,
            onProfileClick = onProfileClick
        )
    }

    // APPLY DIALOG
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
                                        jobId =
                                            selectedJob!!.id,
                                        applicantId =
                                            userId
                                    )

                                    appliedJobIds =
                                        appliedJobIds +
                                                selectedJob!!.id

                                } catch (_: Exception) {
                                }

                                showApplyDialog = false
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
                        showApplyDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // CANCEL DIALOG
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
                                        jobId =
                                            selectedJob!!.id,
                                        applicantId =
                                            userId
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


@Composable
private fun GigListingCard(
    job: JobEntity,
    isApplied: Boolean,
    onGigClick: () -> Unit,
    onApplyClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onGigClick()
            },
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
        color = Color.White
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = job.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            job.companyName ?: "Company",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                if (!job.tag.isNullOrBlank()) {

                    Surface(
                        shape =
                            RoundedCornerShape(20.dp),
                        color = Purple
                    ) {

                        Text(
                            text = job.tag!!,
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 5.dp
                            )
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Default.LocationOn,
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
                    color = Color.Gray
                )
            }

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(17.dp)
                )

                Spacer(
                    modifier = Modifier.width(5.dp)
                )

                Text(
                    text =
                        job.startDate ?: "Flexible",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "RM ${job.salary} / ${job.salaryPeriod}",
                    color = Purple,
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
                                    Green
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