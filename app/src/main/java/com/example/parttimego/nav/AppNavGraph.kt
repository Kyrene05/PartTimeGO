package com.example.parttimego.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.screen.ForgotPasswordScreen
import com.example.parttimego.screen.LoginScreen
import com.example.parttimego.screen.RegisterScreen
import com.example.parttimego.screen.SplashScreen
import com.example.parttimego.screen.UpdatePasswordScreen
import com.example.parttimego.viewmodel.AuthState
import com.example.parttimego.viewmodel.AuthViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay

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
            LoginScreen(
                authState = authViewModel.authState,
                onLoginClick = { email, password -> authViewModel.login(email, password) },
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
        // Dashboard Screen (employer side) — re-enable once ready to merge
        composable(Screen.Dashboard.route) { ... }

        // Post Job Screen — re-enable once ready to merge
        composable(Screen.PostJob.route) { ... }
        */
    }
}