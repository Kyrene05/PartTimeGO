package com.example.parttimego.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.data.local.JobEntity
import com.example.parttimego.data.JobPost
import com.example.parttimego.screen.DashboardScreen
import com.example.parttimego.screen.ForgotPasswordScreen
import com.example.parttimego.screen.LoginScreen
import com.example.parttimego.screen.PostJobScreen
import com.example.parttimego.screen.RegisterScreen
import com.example.parttimego.screen.SplashScreen
import com.example.parttimego.screen.UpdatePasswordScreen
import com.example.parttimego.viewmodel.AuthState
import com.example.parttimego.viewmodel.AuthViewModel
import com.example.parttimego.viewmodel.JobViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import java.time.Instant
import java.util.UUID
import androidx.navigation.NavType
import androidx.navigation.navArgument

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
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
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
                    // TODO: re-enable role-based routing once Dashboard/PostJob are ready to merge
                    // val role = authViewModel.getCurrentUserRole()
                    // val destination = if (role == "employer") Screen.Dashboard.route else Screen.Login.route
                    // navController.navigate(destination) {
                    //     popUpTo(Screen.Login.route) { inclusive = true }
                    // }
                }
            }

            LoginScreen(
                authState = authViewModel.authState,
                onLoginClick = { email, password -> authViewModel.login(email, password) },
                onRegisterClick = {
                    authViewModel.resetState()
                    navController.navigate(Screen.Register.createRoute("job_seeker"))
                },
                onForgotPasswordClick = {
                    authViewModel.resetState()
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onJobSeekerClick = {
                    authViewModel.resetState()
                    navController.navigate(Screen.Register.createRoute("job_seeker"))
                },
                onEmployerClick = {
                    authViewModel.resetState()
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
                if (authViewModel.authState is AuthState.Success) {
                    delay(2000)
                    authViewModel.resetState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                authState = authViewModel.authState,
                onRegisterClick = { fullName, email, password, confirmPassword ->
                    authViewModel.signUp(fullName, email, password, confirmPassword, role)
                },
                onLoginClick = {
                    authViewModel.resetState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
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

        /*
        // Dashboard Screen (employer side)
        composable(Screen.Dashboard.route) {
            val jobViewModel: JobViewModel = viewModel()
            val employerId = SupabaseClient.client.auth.currentUserOrNull()?.id

            LaunchedEffect(employerId) {
                if (employerId != null) {
                    jobViewModel.refreshJobs(employerId)
                }
            }

            val jobs by (employerId?.let { jobViewModel.getJobsForEmployer(it) }
                ?: flowOf(emptyList()))
                .collectAsState(initial = emptyList())

            DashboardScreen(
                jobs = jobs.map { it.toDashboardJobPost() },
                onViewAllClick = { },
                onJobDetailsClick = { },
                onDashboardTabClick = { },
                onPostTabClick = { navController.navigate(Screen.PostJob.route) },
                onProfileTabClick = { }
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
                        workingDate = formData.workingDate.ifBlank { null },
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
        */
    }
}

/*
private fun JobEntity.toDashboardJobPost() = JobPost(
    id = id,
    title = title,
    companyOrLocation = companyName ?: location,
    salary = "RM ${salary.toInt()} / $salaryPeriod",
    tag = tag ?: "",
    durationLabel = workingDate ?: ""
)
*/