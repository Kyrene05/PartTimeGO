package com.example.parttimego.screen.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.parttimego.data.model.Worker

@Composable
fun WorkerProfileScreen(
    worker: Worker,
    onEditProfileClick: () -> Unit = {},
    onAvailabilityChange: (Boolean) -> Unit = {}
    ){
    var isAvailable by remember(worker.workerAvailability){
        mutableStateOf(worker.workerAvailability)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF262075))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Profile Header
            WorkerProfileHeader(
                worker = worker,
                onEditProfileClick = onEditProfileClick
            )

            // White Content Area
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                color = Color.White,
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp
                )
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        top = 15.dp,
                        bottom = 25.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    // Availability
                    item {
                        AvailabilityCard(
                            available = isAvailable,
                            onAvailabilityChange = { value ->
                                isAvailable = value
                                onAvailabilityChange(value)
                            }
                        )
                    }

                    // Skills
                    item {
                        ProfileSectionTitle(
                            title = "Skills"
                        )
                    }
                    item {
                        val skills = worker.workerSkills
                            .split(",")
                            .map{it.trim()}
                            .filter{it.isNotEmpty()}
                    }
                    FlowChipRow(
                        chips = skills,
                        showAddButton = true
                    )
                }

                // Preferred Locations
                item{
                    ProfileSectionTitle(
                        title = "Preferred Location"
                    )
                }
                item{
                    val locations = listOf(
                        worker.workerPreferredState,
                        worker.workerPreferredLocation
                    )
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()

                    FlowChipRow(
                        chips = locations,
                        chipType = ChipType.GRAY
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerProfileHeader(worker: Worker, onEditProfileClick: () -> Unit) {
    TODO("Not yet implemented")
}

@Composable
private fun AvailabilityCard(available: Boolean, onAvailabilityChange: (ERROR) -> Unit) {
    TODO("Not yet implemented")
}

@Composable
private fun ProfileSectionTitle(title: String) {
    TODO("Not yet implemented")
}

private enum class ChipType{
    PURPLE,
    GRAY
}

@Composable
fun FlowChipRow(chips: List<String>, showAddButton: Boolean) {
    TODO("Not yet implemented")
}