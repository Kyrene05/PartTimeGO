package com.example.parttimego.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.MutedText
import com.example.parttimego.ui.theme.PartTimeGOTheme
import com.example.parttimego.ui.theme.SoftGrey
import com.example.parttimego.viewmodel.EmployerProfileUiState
import com.example.parttimego.viewmodel.EmployerProfileViewModel

enum class EmployerBottomTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.BarChart),
    POST("Post", Icons.Default.Add),
    PROFILE("Profile", Icons.Default.Person)
}

data class ProfileMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isLogout: Boolean = false
)

fun openWhatsApp(
    context: Context,
    phoneNumber: String = "601139539985",
    message: String = "Hi, I need support regarding my account."
) {
    var cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
    if (cleanNumber.startsWith("0")) {
        cleanNumber = "60" + cleanNumber.substring(1)
    }

    val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open WhatsApp", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun EmployerProfileRoute(
    viewModel: EmployerProfileViewModel = viewModel(),
    onDashboardClick: () -> Unit = {},
    onPostClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onMoreOptionsClick: () -> Unit = {},
    onLogoutNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    EmployerProfileScreen(
        uiState = uiState,
        selectedTab = EmployerBottomTab.PROFILE,
        onTabSelected = { tab ->
            when (tab) {
                EmployerBottomTab.DASHBOARD -> onDashboardClick()
                EmployerBottomTab.POST -> onPostClick()
                EmployerBottomTab.PROFILE -> { }
            }
        },
        onEditProfileClick = onEditProfileClick,
        onChangePasswordClick = onChangePasswordClick,
        onContactUsClick = {
            openWhatsApp(context = context, phoneNumber = "601139539985", message = "Hi, I need support regarding my employer account.")
        },
        onTermsClick = onTermsClick,
        onMoreOptionsClick = onMoreOptionsClick,
        onLogoutClick = {
            viewModel.logout(onLogoutSuccess = onLogoutNavigateToLogin)
        }
    )
}

@Composable
fun EmployerProfileScreen(
    uiState: EmployerProfileUiState,
    selectedTab: EmployerBottomTab = EmployerBottomTab.PROFILE,
    onTabSelected: (EmployerBottomTab) -> Unit = {},
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

    // Displays name; falls back to email string before @ or fallback text if name is not set
    val displayName = remember(uiState.userName, uiState.companyEmail, uiState.companyName) {
        when {
            uiState.userName.isNotBlank() -> uiState.userName
            uiState.companyEmail.isNotBlank() -> uiState.companyEmail.substringBefore("@")
            uiState.companyName.isNotBlank() -> uiState.companyName
            else -> "User"
        }
    }

    Scaffold(
        containerColor = DarkNavy, // 设置底层背景为深蓝色，消除顶部状态栏区域的白块
        bottomBar = {
            EmployerBottomNavigationBar(
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
        ) {
            // Header Profile Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(SoftGrey),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!uiState.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = uiState.avatarUrl,
                                        contentDescription = "Employer Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Default Avatar",
                                        tint = DarkNavy,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(18.dp))

                        // User & Profile Details Area
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = displayName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(enabled = uiState.companyName.isBlank()) { onEditProfileClick() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = if (uiState.companyName.isNotBlank()) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.companyName.isNotBlank()) uiState.companyName else "+ Add Company",
                                    color = if (uiState.companyName.isNotBlank()) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Company Phone or Clean Clickable Prompt
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(enabled = uiState.phone.isBlank()) { onEditProfileClick() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = if (uiState.phone.isNotBlank()) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.phone.isNotBlank()) uiState.phone else "+ Add Phone",
                                    color = if (uiState.phone.isNotBlank()) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (uiState.companyEmail.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = uiState.companyEmail,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 35.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    menuItems.forEachIndexed { index, item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { item.onClick() },
                            color = cardBackgroundColor
                        ) {
                            ProfileMenuItemRow(item = item)
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
            title = { Text(text = "Logout", fontWeight = FontWeight.Bold,color=DarkNavy) },
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
fun EmployerBottomNavigationBar(
    selectedTab: EmployerBottomTab,
    onTabSelected: (EmployerBottomTab) -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        EmployerBottomTab.entries.forEach { tab ->
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
private fun ProfileMenuItemRow(item: ProfileMenuItem) {
    val textColor = if (item.isLogout) Color(0xFFDC2626) else Color.Black
    val iconTint = if (item.isLogout) Color(0xFFDC2626) else DarkNavy

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }

        if (!item.isLogout) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                tint = MutedText,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmployerProfileScreenPreview() {
    PartTimeGOTheme {
        EmployerProfileScreen(
            uiState = EmployerProfileUiState(
                userName = "Cheng",
                companyName = "",
                phone = "",
                companyEmail = "jane@techsolutions.com",
                isLoading = false
            )
        )
    }
}