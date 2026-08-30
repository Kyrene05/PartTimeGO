package com.example.parttimego.viewmodel

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parttimego.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    var selectedRole by mutableStateOf("job_seeker")
        private set

    fun setRole(role: String) {
        selectedRole = role
    }

    fun resetState() {
        authState = AuthState.Idle
    }

    // Validation Helpers
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun roleLabel(role: String?): String = when (role?.lowercase()) {
        "employer" -> "Employer"
        "job_seeker" -> "Job Seeker"
        else -> "User"
    }

    // 1. Register action
    fun signUp(fullName: String, emailInput: String, passwordInput: String, confirmPasswordInput: String) {
        val trimmedEmail = emailInput.trim()

        if (fullName.isBlank() || trimmedEmail.isEmpty() || passwordInput.isEmpty()) {
            authState = AuthState.Error("Please fill in all fields.")
            return
        }

        if (!isValidEmail(trimmedEmail)) {
            authState = AuthState.Error("Please enter a valid email address.")
            return
        }

        if (!isValidPassword(passwordInput)) {
            authState = AuthState.Error("Password must be at least 6 characters.")
            return
        }

        if (passwordInput != confirmPasswordInput) {
            authState = AuthState.Error("Passwords do not match.")
            return
        }

        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    email = trimmedEmail
                    password = passwordInput
                    data = buildJsonObject {
                        put("full_name", fullName)
                        put("role", selectedRole)
                    }
                }

                // Also write to the public "profiles" table so other users
                // (e.g. an employer viewing applicants) can look up this name.
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    SupabaseClient.client.postgrest["profiles"].insert(
                        mapOf("id" to userId, "full_name" to fullName, "role" to selectedRole)
                    )
                }

                authState = AuthState.Success("Registered successfully as ${roleLabel(selectedRole)}!")
            } catch (e: Exception) {
                authState = AuthState.Error(e.localizedMessage ?: "Registration failed")
            }
        }
    }

    // 2. Login action
    fun login(emailInput: String, passwordInput: String) {
        val trimmedEmail = emailInput.trim()

        if (trimmedEmail.isEmpty() || passwordInput.isEmpty()) {
            authState = AuthState.Error("Please fill in all fields.")
            return
        }

        if (!isValidEmail(trimmedEmail)) {
            authState = AuthState.Error("Please enter a valid email address.")
            return
        }

        if (!isValidPassword(passwordInput)) {
            authState = AuthState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    email = trimmedEmail
                    password = passwordInput
                }
                val role = getCurrentUserRole()
                authState = AuthState.Success("Login successful! Logged in as ${roleLabel(role)}.")
            } catch (e: Exception) {
                authState = AuthState.Error(e.localizedMessage ?: "Login failed")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            authState = AuthState.Error("Please enter your email address first.")
            return
        }

        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                SupabaseClient.client.auth.resetPasswordForEmail(
                    email = email,
                    redirectUrl = "parttimego://reset-password"
                )
                authState = AuthState.Success("Password reset link sent! Check your inbox.")
            } catch (e: Exception) {
                authState = AuthState.Error(e.localizedMessage ?: "Failed to send reset link.")
            }
        }
    }

    fun updatePassword(newPassword: String) {
        if (!isValidPassword(newPassword)) {
            authState = AuthState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            authState = AuthState.Loading
            try {
                SupabaseClient.client.auth.updateUser {
                    password = newPassword
                }
                SupabaseClient.client.auth.signOut()
                authState = AuthState.Success("Password updated! Please log in again.")
            } catch (e: Exception) {
                authState = AuthState.Error(e.localizedMessage ?: "Failed to update password.")
            }
        }
    }

    fun getCurrentUserRole(): String? {
        return SupabaseClient.client.auth.currentUserOrNull()
            ?.userMetadata?.get("role")?.toString()?.trim('"')
    }
}