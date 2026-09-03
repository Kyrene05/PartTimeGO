package com.example.parttimego.nav

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class JobSeekerNavItem {
    HOME,
    EXPLORE,
    APPLIED,
    PROFILE
}

@Composable
fun JobSeekerNavBar(
    selectedItem: JobSeekerNavItem,
    onHomeClick: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onAppliedClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        color = Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFF8E8E80)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
        ) {
            JobSeekerNavBarItem(
                icon = Icons.Default.Home,
                label = "Home",
                selected = selectedItem == JobSeekerNavItem.HOME,
                onClick = onHomeClick,
                modifier = Modifier.weight(1f)
            )
            JobSeekerNavBarItem(
                icon = Icons.Default.Search,
                label = "Explore",
                selected = selectedItem == JobSeekerNavItem.EXPLORE,
                onClick = onExploreClick,
                modifier = Modifier.weight(1f)
            )
            JobSeekerNavBarItem(
                icon = Icons.AutoMirrored.Filled.ListAlt,
                label = "Applied",
                selected = selectedItem == JobSeekerNavItem.APPLIED,
                onClick = onAppliedClick,
                modifier = Modifier.weight(1f)
            )
            JobSeekerNavBarItem(
                icon = Icons.Default.Person,
                label = "Profile",
                selected = selectedItem == JobSeekerNavItem.PROFILE,
                onClick = onProfileClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun JobSeekerNavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    val itemColor = if(selected){
        Color(0xFF262075)
    } else {
        Color(0xFF4A4A4A)
    }

    Column(
        modifier = modifier
            .clickable{
                onClick()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = itemColor,
            modifier = Modifier
                .height(36.dp)
                .padding(bottom = 1.dp)
        )
        Text(
            text = label,
            color = itemColor,
            fontWeight = if (selected){
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            fontSize = 13.sp
        )
    }
}