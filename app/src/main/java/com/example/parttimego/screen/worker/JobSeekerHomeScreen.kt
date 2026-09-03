package com.example.parttimego.screen.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
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

private val Purple = Color(0xFF262075)
private val AccentGreen = Color(0xFF2E9E5B)
private val TagRed = Color(0xFFE53935)


@Composable
fun JobSeekerHomeScreen(
    userName: String = "User",
    onGigClick: (String) -> Unit = {},
    onExploreClick: () -> Unit = {},
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

    JobSeekerHomeContent(
        jobs = jobs,
        userName = userName,
        onGigClick = onGigClick,
        onExploreClick = onExploreClick,
        onAppliedClick = onAppliedClick,
        onProfileClick = onProfileClick
    )
}


@Composable
fun JobSeekerHomeContent(
    jobs: List<JobEntity>,
    userName: String = "User",
    onGigClick: (String) -> Unit = {},
    onExploreClick: () -> Unit = {},
    onAppliedClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {

    var searchText by remember {
        mutableStateOf("")
    }

    val filteredJobs = jobs.filter { job ->

        job.title.contains(searchText, ignoreCase = true) ||
                job.category.contains(searchText, ignoreCase = true) ||
                job.location.contains(searchText, ignoreCase = true)
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

            JobSeekerHomeHeader(userName = userName)

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

                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = Purple
                                )
                            },
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    item {

                        TodayOpportunityCard(
                            jobsCount = jobs.size,
                            onClick = onExploreClick
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
                                onClick = {
                                    onGigClick(job.id)
                                },
                                onApplyClick = {
                                    onGigClick(job.id)
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
    }
}


@Composable
private fun JobSeekerHomeHeader(
    userName: String
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
                text = "Good Morning",
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
                .background(Color.White),
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
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = "$jobsCount new gigs posted",
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
                modifier = Modifier.clickable {
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
        "Event",
        "Retail",
        "Food & Beverage",
        "Delivery"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        categories.chunked(2).forEach { rowCategories ->

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
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
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
    onClick: () -> Unit,
    onApplyClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
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
                        color = Color.DarkGray,
                        fontSize = 13.sp
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


@Composable
private fun EmptyJobsCard() {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF5F5F5)
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