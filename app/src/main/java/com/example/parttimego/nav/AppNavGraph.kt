package com.example.parttimego.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.parttimego.data.JobPost
import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.data.local.JobEntity
import com.example.parttimego.data.repository.ApplicationRepository
import com.example.parttimego.screen.ApplicantStatus
import com.example.parttimego.screen.ApplicantUiModel
import com.example.parttimego.screen.DashboardScreen
import com.example.parttimego.screen.DetailsScreen
import com.example.parttimego.screen.EditEmployerProfileRoute
import com.example.parttimego.screen.EmployerProfileRoute
import com.example.parttimego.screen.ForgotPasswordScreen
import com.example.parttimego.screen.JobStatusFilter
import com.example.parttimego.screen.LoginScreen
import com.example.parttimego.screen.ManageApplicantsScreen
import com.example.parttimego.screen.PostJobFormData
import com.example.parttimego.screen.PostJobScreen
import com.example.parttimego.screen.RegisterScreen
import com.example.parttimego.screen.SplashScreen
import com.example.parttimego.screen.UpdatePasswordScreen
import com.example.parttimego.viewmodel.AuthState
import com.example.parttimego.viewmodel.AuthViewModel
import com.example.parttimego.viewmodel.EmployerProfileViewModel
import com.example.parttimego.viewmodel.EmployerProfileViewModelFactory
import com.example.parttimego.viewmodel.JobViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

// Sealed class for Routes
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register/{role}") {
        fun createRoute(role: String) = "register/$role"
    }
    object ForgotPassword : Screen("forgot_password")
    object UpdatePassword : Screen("update_password")
    object Dashboard : Screen("dashboard")
    object PostJob : Screen("post_job")
    object JobSeekerHome : Screen("job_seeker_home") // TODO: change to job seeker homepage
    object EmployerProfile : Screen("employer_profile")
    object EditEmployerProfile : Screen("edit_employer_profile")

    object Details : Screen("details/{jobId}") {
        fun createRoute(jobId: String) = "details/$jobId"
    }

    object ManageApplicants : Screen("manage_applicants")
}

private val DASHBOARD_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy")

private fun JobEntity.toStatusFilter(): JobStatusFilter {
    val today = LocalDate.now()
    val start = startDate?.let { runCatching { LocalDate.parse(it, DASHBOARD_DATE_FORMAT) }.getOrNull() }
    val end = endDate?.let { runCatching { LocalDate.parse(it, DASHBOARD_DATE_FORMAT) }.getOrNull() }

    return when {
        start != null && today.isBefore(start) -> JobStatusFilter.UPCOMING
        end != null && today.isAfter(end) -> JobStatusFilter.ENDED
        else -> JobStatusFilter.ACTIVE
    }
}

@Composable
fun AppNavGraph(navController: NavHostController, authViewModel: AuthViewModel = viewModel()) {

    var sessionReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        SupabaseClient.client.auth.sessionStatus.collect { status ->
            if (status is SessionStatus.Authenticated && status.source is SessionSource.External) {
                sessionReady = true
                if (navController.currentDestination?.route != Screen.UpdatePassword.route) {
                    navController.navigate(Screen.UpdatePassword.route) {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash Screen
        composable(
            Screen.Splash.route,
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ) {
            val coroutineScope = rememberCoroutineScope()

            SplashScreen(
                onNavigateToLogin = {
                    coroutineScope.launch {
                        val status = SupabaseClient.client.auth.sessionStatus.first { it !is SessionStatus.Initializing }

                        val destination = if (status is SessionStatus.Authenticated) {
                            val role = authViewModel.getCurrentUserRole()
                            if (role == "employer") Screen.Dashboard.route else Screen.JobSeekerHome.route
                        } else {
                            Screen.Login.route
                        }

                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Login Screen
        composable(Screen.Login.route) {
            LaunchedEffect(authViewModel.authState) {
                if (navController.currentDestination?.route == Screen.Login.route &&
                    authViewModel.authState is AuthState.Success
                ) {
                    authViewModel.resetState()
                    val role = authViewModel.getCurrentUserRole()
                    val destination = if (role == "employer") {
                        Screen.Dashboard.route
                    } else {
                        Screen.JobSeekerHome.route // TODO: change to real job seeker homepage
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                authState = authViewModel.authState,
                onLoginClick = { email, password -> authViewModel.login(email, password) },
                onForgotPasswordClick = {
                    authViewModel.resetState()
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onJobSeekerClick = {
                    authViewModel.resetState()
                    authViewModel.setRole("job_seeker")
                    navController.navigate(Screen.Register.createRoute("job_seeker"))
                },
                onEmployerClick = {
                    authViewModel.resetState()
                    authViewModel.setRole("employer")
                    navController.navigate(Screen.Register.createRoute("employer"))
                }
            )
        }

        // Forgot Password Screen
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                authState = authViewModel.authState,
                onSendResetLinkClick = { email -> authViewModel.sendPasswordResetEmail(email) },
                onBackToLoginClick = {
                    authViewModel.resetState()
                    navController.popBackStack()
                }
            )
        }

        // Register Screen
        composable(
            Screen.Register.route,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "job_seeker"

            LaunchedEffect(authViewModel.authState) {
                if (navController.currentDestination?.route?.startsWith("register") == true &&
                    authViewModel.authState is AuthState.Success
                ) {
                    delay(2000)
                    authViewModel.resetState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                authState = authViewModel.authState,
                selectedRole = authViewModel.roleLabel(role),
                onRegisterClick = { fullName, email, password, confirmPassword ->
                    authViewModel.signUp(fullName, email, password, confirmPassword)
                },
                onLoginClick = {
                    authViewModel.resetState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        // Update Password Screen
        composable(Screen.UpdatePassword.route) {
            LaunchedEffect(authViewModel.authState) {
                if (authViewModel.authState is AuthState.Success) {
                    delay(2000)
                    authViewModel.resetState()
                    sessionReady = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            UpdatePasswordScreen(
                authState = authViewModel.authState,
                sessionReady = sessionReady,
                onUpdatePasswordClick = { newPassword -> authViewModel.updatePassword(newPassword) },
                onRequestNewLinkClick = {
                    authViewModel.resetState()
                    sessionReady = false
                    navController.navigate(Screen.ForgotPassword.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Dashboard Screen (employer side)
        composable(Screen.Dashboard.route) {
            val jobViewModel: JobViewModel = viewModel()
            val applicationRepository = remember { ApplicationRepository() }
            val employerId = SupabaseClient.client.auth.currentUserOrNull()?.id

            var totalApplicantsCount by remember { mutableStateOf(0) }
            var pendingReviewCount by remember { mutableStateOf(0) }
            var thisWeekHiresCount by remember { mutableStateOf(0) }

            LaunchedEffect(employerId) {
                if (employerId != null) {
                    jobViewModel.refreshJobs(employerId)
                    totalApplicantsCount = applicationRepository.getApplicationCountForEmployer(employerId)
                    pendingReviewCount = applicationRepository.getPendingReviewCount(employerId)
                    thisWeekHiresCount = applicationRepository.getThisWeekHiresCount(employerId)
                }
            }

            val jobs by (employerId?.let { jobViewModel.getJobsForEmployer(it) }
                ?: flowOf(emptyList()))
                .collectAsState(initial = emptyList())
            var searchQuery by remember { mutableStateOf("") }
            var selectedCategory by remember { mutableStateOf<String?>(null) }
            var selectedStatus by remember { mutableStateOf<JobStatusFilter?>(null) }

            val filteredJobs = jobs.filter { job ->
                val matchesSearch = searchQuery.isBlank() || job.title.contains(searchQuery, ignoreCase = true)
                val matchesCategory = selectedCategory == null || job.category == selectedCategory
                val matchesStatus = selectedStatus == null || job.toStatusFilter() == selectedStatus
                matchesSearch && matchesCategory && matchesStatus
            }

            DashboardScreen(
                activeJobsCount = jobs.size,  // total, not filtered
                totalApplicantsCount = totalApplicantsCount,
                thisWeekHires = thisWeekHiresCount,
                pendingReviewCount = pendingReviewCount,
                jobs = filteredJobs.map { it.toDashboardJobPost() },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it },
                onJobDetailsClick = { jobId -> navController.navigate(Screen.Details.createRoute(jobId)) },
                onTotalApplicantsClick = { navController.navigate(Screen.ManageApplicants.route) },
                onDashboardTabClick = { },
                onPostTabClick = { navController.navigate(Screen.PostJob.route) },
                onProfileTabClick = { navController.navigate(Screen.EmployerProfile.route) }
            )
        }

        // Post Job Screen
        composable(Screen.PostJob.route) {
            val jobViewModel: JobViewModel = viewModel()
            var isSubmitting by remember { mutableStateOf(false) }
            var postError by remember { mutableStateOf<String?>(null) }

            PostJobScreen(
                isSubmitting = isSubmitting,
                errorMessage = postError,
                onBackClick = { navController.popBackStack() },
                onDashboardTabClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onProfileTabClick = { navController.navigate(Screen.EmployerProfile.route) },
                onPostClick = { formData ->
                    val employerId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    if (employerId == null) {
                        postError = "You must be logged in to post a job."
                        return@PostJobScreen
                    }

                    val job = JobEntity(
                        id = UUID.randomUUID().toString(),
                        employerId = employerId,
                        title = formData.title,
                        companyName = formData.companyName.ifBlank { null },
                        category = formData.category,
                        salary = formData.salary.toDoubleOrNull() ?: 0.0,
                        salaryPeriod = "day",
                        startDate = formData.startDate.ifBlank { null },
                        endDate = formData.endDate.ifBlank { null },
                        workingHoursStart = formData.workingHoursStart.ifBlank { null },
                        workingHoursEnd = formData.workingHoursEnd.ifBlank { null },
                        location = formData.location,
                        description = formData.description.ifBlank { null },
                        requirements = formData.requirements.ifBlank { null },
                        peopleNeeded = formData.peopleNeeded,
                        tag = null,
                        createdAt = Instant.now().toString()
                    )

                    isSubmitting = true
                    postError = null
                    jobViewModel.postJob(job)
                    isSubmitting = false
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.PostJob.route) { inclusive = true }
                    }
                }
            )
        }

        // Details Screen (view/edit/delete a posted job)
        composable(
            Screen.Details.route,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            val jobViewModel: JobViewModel = viewModel()
            val employerId = SupabaseClient.client.auth.currentUserOrNull()?.id

            val jobs by (employerId?.let { jobViewModel.getJobsForEmployer(it) }
                ?: flowOf(emptyList()))
                .collectAsState(initial = emptyList())

            val job = jobs.find { it.id == jobId }

            var isSubmitting by remember { mutableStateOf(false) }
            var updateError by remember { mutableStateOf<String?>(null) }

            if (job == null) {
                Text("Job not found")
            } else {
                DetailsScreen(
                    initialData = job.toPostJobFormData(),
                    isSubmitting = isSubmitting,
                    errorMessage = updateError,
                    onBackClick = { navController.popBackStack() },
                    onUpdateClick = { formData -> /* unchanged */ },
                    onDeleteClick = { onResult ->
                        jobViewModel.deleteJob(jobId) { result ->
                            onResult(result)
                            if (result.isSuccess) {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    onDashboardTabClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    },
                    onProfileTabClick = { navController.navigate(Screen.EmployerProfile.route) }
                )
            }
        }

        // Manage Applicants Screen
        composable(Screen.ManageApplicants.route) {
            val employerId = SupabaseClient.client.auth.currentUserOrNull()?.id
            val applicationRepository = remember { ApplicationRepository() }
            var applicants by remember { mutableStateOf<List<ApplicantUiModel>>(emptyList()) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(employerId) {
                if (employerId != null) {
                    val results = applicationRepository.getApplicationsForEmployer(employerId)
                    applicants = results.map { (app, job, profile) ->
                        ApplicantUiModel(
                            id = app.id,
                            name = profile?.fullName ?: "Applicant ${app.applicantId.take(8)}",
                            jobTitle = job.title,
                            location = job.location,
                            salary = "RM ${job.salary.toInt()} / ${job.salaryPeriod}",
                            appliedDate = app.appliedAt?.take(10) ?: "",
                            status = when (app.status) {
                                "accepted" -> ApplicantStatus.ACCEPTED
                                "rejected" -> ApplicantStatus.REJECTED
                                else -> ApplicantStatus.PENDING
                            }
                        )
                    }
                }
            }

            ManageApplicantsScreen(
                applicants = applicants,
                onBackClick = { navController.popBackStack() },
                onAcceptClick = { applicationId ->
                    coroutineScope.launch {
                        applicationRepository.updateApplicationStatus(applicationId, "accepted")
                        applicants = applicants.map {
                            if (it.id == applicationId) it.copy(status = ApplicantStatus.ACCEPTED) else it
                        }
                    }
                },
                onRejectClick = { applicationId ->
                    coroutineScope.launch {
                        applicationRepository.updateApplicationStatus(applicationId, "rejected")
                        applicants = applicants.map {
                            if (it.id == applicationId) it.copy(status = ApplicantStatus.REJECTED) else it
                        }
                    }
                },
                onDashboardTabClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onPostTabClick = { navController.navigate(Screen.PostJob.route) },
                onProfileTabClick = { navController.navigate(Screen.EmployerProfile.route) }
            )
        }

        // Employer Profile Screen
        composable(Screen.EmployerProfile.route) {
            val profileViewModel: EmployerProfileViewModel = viewModel(
                factory = EmployerProfileViewModelFactory(SupabaseClient.client)
            )

            // Re-sync with database every time returning to this screen
            LaunchedEffect(Unit) {
                profileViewModel.loadUserProfile()
            }

            EmployerProfileRoute(
                viewModel = profileViewModel,
                onDashboardClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onPostClick = {
                    navController.navigate(Screen.PostJob.route)
                },
                onEditProfileClick = {
                    navController.navigate(Screen.EditEmployerProfile.route)
                },
                onChangePasswordClick = {
                    // TODO: navigate to Change Password Screen when implemented
                },
                onTermsClick = {
                    // TODO: navigate to Terms & Conditions Screen
                },
                onMoreOptionsClick = {
                    // TODO: navigate to More Options Screen
                },
                onLogoutNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Edit Employer Profile Screen
        composable(Screen.EditEmployerProfile.route) {
            // Distinct ViewModel instance to isolate edits until saved
            val editViewModel: EmployerProfileViewModel = viewModel(
                factory = EmployerProfileViewModelFactory(SupabaseClient.client)
            )

            EditEmployerProfileRoute(
                viewModel = editViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // TODO: Job Seeker home
        composable(Screen.JobSeekerHome.route) {
            Text("Job Seeker home — coming soon")
        }
    }
}

private fun JobEntity.toDashboardJobPost() = JobPost(
    id = id,
    title = title,
    companyOrLocation = companyName ?: location,
    salary = "RM ${salary.toInt()} / $salaryPeriod",
    tag = tag ?: "",
    durationLabel = startDate ?: ""
)

private fun JobEntity.toPostJobFormData() = PostJobFormData(
    title = title,
    companyName = companyName ?: "",
    category = category,
    salary = salary.toInt().toString(),
    startDate = startDate ?: "",
    endDate = endDate ?: "",
    workingHoursStart = workingHoursStart ?: "",
    workingHoursEnd = workingHoursEnd ?: "",
    location = location,
    description = description ?: "",
    requirements = requirements ?: "",
    peopleNeeded = peopleNeeded
)