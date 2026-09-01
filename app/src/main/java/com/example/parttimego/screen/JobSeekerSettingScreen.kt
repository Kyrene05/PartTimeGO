package com.example.parttimego.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.parttimego.data.SupabaseClient
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.MutedText
import com.example.parttimego.ui.theme.PartTimeGOTheme
import com.example.parttimego.ui.theme.SoftGrey
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// UI State
data class JobSeekerSettingUiState(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val isLoading: Boolean = true
)

@Serializable
private data class ProfileDto(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null
)

// ViewModel
class JobSeekerSettingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(JobSeekerSettingUiState())
    val uiState: StateFlow<JobSeekerSettingUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser != null) {
                    val userId = currentUser.id
                    val authEmail = currentUser.email ?: ""

                    val profile = SupabaseClient.client.from("profiles")
                        .select {
                            filter {
                                eq("id", userId)
                            }
                        }
                        .decodeSingleOrNull<ProfileDto>()

                    _uiState.update {
                        it.copy(
                            fullName = profile?.fullName ?: "",
                            phone = profile?.phone ?: "",
                            email = profile?.email ?: authEmail,
                            avatarUrl = profile?.avatarUrl,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                onLogoutSuccess()
            }
        }
    }
}

// Enums
enum class JobSeekerBottomTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    APPLICATIONS("My Jobs", Icons.Default.WorkHistory),
    SAVED("Saved", Icons.Default.Bookmark),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun JobSeekerSettingRoute(
    viewModel: JobSeekerSettingViewModel = viewModel(),
    onHomeClick: () -> Unit = {},
    onApplicationsClick: () -> Unit = {},
    onSavedClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    onLogoutNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    JobSeekerSettingScreen(
        uiState = uiState,
        selectedTab = JobSeekerBottomTab.PROFILE,
        onTabSelected = { tab ->
            when (tab) {
                JobSeekerBottomTab.HOME -> onHomeClick()
                JobSeekerBottomTab.APPLICATIONS -> onApplicationsClick()
                JobSeekerBottomTab.SAVED -> onSavedClick()
                JobSeekerBottomTab.PROFILE -> { }
            }
        },
        onEditProfileClick = onEditProfileClick,
        onChangePasswordClick = onChangePasswordClick,
        onContactUsClick = {
            openWhatsApp(
                context = context,
                phoneNumber = "601139539985",
                message = "Hi, I need support regarding my job seeker account."
            )
        },
        onTermsClick = onTermsClick,
        onMoreOptionsClick = onMoreOptionsClick,
        onLogoutClick = {
            viewModel.logout(onLogoutSuccess = onLogoutNavigateToLogin)
        }
    )
}

@Composable
fun JobSeekerSettingScreen(
    uiState: JobSeekerSettingUiState,
    selectedTab: JobSeekerBottomTab = JobSeekerBottomTab.PROFILE,
    onTabSelected: (JobSeekerBottomTab) -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onContactUsClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val cardBackgroundColor = Color(0xFFF1F5F9)
    val dividerColor = Color(0xFFE2E8F0)

    val menuItems = remember(
        onEditProfileClick,
        onChangePasswordClick,
        onContactUsClick,
        onTermsClick,
        onMoreOptionsClick
    ) {
        listOf(
            ProfileMenuItem("Edit Profile", Icons.Default.Edit, onEditProfileClick),
            ProfileMenuItem("Change Password", Icons.Default.Lock, onChangePasswordClick),
            ProfileMenuItem("Contact Us", Icons.Default.Phone, onContactUsClick),
            ProfileMenuItem("Terms and Conditions", Icons.Default.Description, onTermsClick),
            ProfileMenuItem("More Options", Icons.Default.MoreHoriz, onMoreOptionsClick),
            ProfileMenuItem("Logout", Icons.Default.ExitToApp, { showLogoutDialog = true }, isLogout = true)
        )
    }

    Scaffold(
        bottomBar = {
            JobSeekerBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkNavy)
                .statusBarsPadding()
        ) {
            // Header Profile Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(SoftGrey),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!uiState.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = uiState.avatarUrl,
                                    contentDescription = "User Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default Avatar",
                                    tint = DarkNavy,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.fullName.ifBlank { "User" },
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.phone.ifBlank { "No phone number added" },
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = uiState.email.ifBlank { "No email added" },
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Main Menu List
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    menuItems.forEachIndexed { index, item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { item.onClick() },
                            color = cardBackgroundColor
                        ) {
                            SettingMenuItemRow(item = item)
                        }

                        if (index < menuItems.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                thickness = 1.dp,
                                color = dividerColor.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Logout", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Are you sure you want to log out of your account?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    }
                ) {
                    Text(text = "Logout", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun JobSeekerBottomNavigationBar(
    selectedTab: JobSeekerBottomTab,
    onTabSelected: (JobSeekerBottomTab) -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        JobSeekerBottomTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarkNavy,
                    selectedTextColor = DarkNavy,
                    unselectedIconColor = MutedText,
                    unselectedTextColor = MutedText
                )
            )
        }
    }
}

@Composable
private fun SettingMenuItemRow(item: ProfileMenuItem) {
    val textColor = if (item.isLogout) Color(0xFFDC2626) else Color.Black
    val iconTint = if (item.isLogout) Color(0xFFDC2626) else DarkNavy

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }

        if (!item.isLogout) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                tint = MutedText,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JobSeekerSettingScreenPreview() {
    PartTimeGOTheme {
        JobSeekerSettingScreen(
            uiState = JobSeekerSettingUiState(isLoading = false)
        )
    }
}