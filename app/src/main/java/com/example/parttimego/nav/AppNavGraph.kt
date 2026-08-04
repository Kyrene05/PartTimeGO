package com.example.parttimego.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.parttimego.screen.LoginScreen
import com.example.parttimego.screen.RegisterScreen
import com.example.parttimego.screen.SplashScreen
import com.example.parttimego.viewmodel.AuthState
import com.example.parttimego.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

// Sealed class for Routes
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
}

@Composable
fun AppNavGraph(navController: NavHostController,authViewModel: AuthViewModel= viewModel()) {
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
            // Listen for login success state to navigate away
            LaunchedEffect(authViewModel.authState) {
                if (authViewModel.authState is AuthState.Success) {
                    // TODO: Navigate to Home Screen when ready
                    // navController.navigate("home") {
                    //     popUpTo(Screen.Login.route) { inclusive = true }
                    // }
                }
            }

            LoginScreen(
                authState = authViewModel.authState,
                onLoginClick = { email, password ->
                    authViewModel.login(email, password)
                },
                onRegisterClick = {
                    authViewModel.resetState() // Clear old errors when switching screens
                    navController.navigate(Screen.Register.route)
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
    }
}