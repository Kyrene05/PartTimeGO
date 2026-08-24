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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import kotlinx.coroutines.launch

// Sealed class for Routes
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    object UpdatePassword: Screen("update_password")
}

@Composable
fun AppNavGraph(navController: NavHostController,authViewModel: AuthViewModel= viewModel()) {

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
            exitTransition = {
                fadeOut(animationSpec=tween(700))
            }) {
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
            // Check that the current route is actually Login before triggering success actions
            LaunchedEffect(authViewModel.authState) {
                if (navController.currentDestination?.route == Screen.Login.route &&
                    authViewModel.authState is AuthState.Success
                ) {
                    // TODO: Navigate to Home Screen when ready
                    // navController.navigate("home") {
                    //     popUpTo(Screen.Login.route) { inclusive = true }
                    // }
                }
            }

            LoginScreen(
                authState = authViewModel.authState,
                onLoginClick = { email, password -> authViewModel.login(email, password) },
                onRegisterClick = {
                    authViewModel.resetState()
                    navController.navigate(Screen.Register.route)
                },
                onForgotPasswordClick = {
                    authViewModel.resetState()
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        // Forgot Password Screen
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                authState = authViewModel.authState,
                onSendResetLinkClick = { email ->
                    authViewModel.sendPasswordResetEmail(email)
                },
                onBackToLoginClick = {
                    authViewModel.resetState()
                    navController.popBackStack()
                }
            )
        }
        // Register Screen
        composable(Screen.Register.route) {
            // Listen for register success state
            LaunchedEffect(authViewModel.authState) {
                if (authViewModel.authState is AuthState.Success) {
                    delay(2000) // Wait 2 seconds so the green text & Toast remain visible
                    authViewModel.resetState() // Clear state before moving to Login
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            }

            RegisterScreen(
                authState = authViewModel.authState,
                onRegisterClick = { fullName, email, password, confirmPassword ->
                    authViewModel.signUp(fullName, email, password, confirmPassword)
                },
                onLoginClick = {
                    authViewModel.resetState() // Clear old errors when switching screens
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        //Update Password Screen
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
    }
}