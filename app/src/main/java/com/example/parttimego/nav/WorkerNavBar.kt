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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class WorkerNavItem {
    HOME,
    EXPLORE,
    APPLIED,
    PROFILE
}

@Composable
fun WorkerNavBar(
    selectedItem: WorkerNavItem,
    onHomeClick: () -> Unit = {},
    onExploreClick: () -> Unit = {},
    onAppliedClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        color = Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFF8E8E80)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
        ) {
            WorkerNavBarItem(
                icon = Icons.Default.Home,
                label = "Home",
                selected = selectedItem == WorkerNavItem.HOME,
                onClick = onHomeClick,
                modifier = Modifier.weight(1f)
            )
            WorkerNavBarItem(
                icon = Icons.Default.Search,
                label = "Explore",
                selected = selectedItem == WorkerNavItem.EXPLORE,
                onClick = onExploreClick,
                modifier = Modifier.weight(1f)
            )
            WorkerNavBarItem(
                icon = Icons.AutoMirrored.Filled.ListAlt,
                label = "Applied",
                selected = selectedItem == WorkerNavItem.APPLIED,
                onClick = onAppliedClick,
                modifier = Modifier.weight(1f)
            )
            WorkerNavBarItem(
                icon = Icons.Default.Person,
                label = "Profile",
                selected = selectedItem == WorkerNavItem.PROFILE,
                onClick = onProfileClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WorkerNavBarItem(
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
                .height(18.dp)
                .padding(bottom = 2.dp)
        )
        Text(
            text = label,
            color = itemColor,
            fontWeight = if (selected){
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            fontSize = 7.sp
        )
    }
}