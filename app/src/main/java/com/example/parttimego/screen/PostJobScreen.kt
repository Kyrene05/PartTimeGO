package com.example.parttimego.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.MutedText
import com.example.parttimego.ui.theme.SoftGrey

data class PostJobFormData(
    val title: String,
    val companyName: String,
    val category: String,
    val salary: String,
    val workingDate: String,
    val workingHoursStart: String,
    val workingHoursEnd: String,
    val location: String,
    val description: String,
    val requirements: String,
    val peopleNeeded: Int
)

@Composable
fun PostJobScreen(
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onBackClick: () -> Unit = {},
    onPostClick: (PostJobFormData) -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Event Crew") }
    var salary by remember { mutableStateOf("") }
    var workingDate by remember { mutableStateOf("") }
    var workingHoursStart by remember { mutableStateOf("") }
    var workingHoursEnd by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var peopleNeeded by remember { mutableStateOf(10) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Event Crew", "Promoter", "Retail", "F&B")

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkNavy)
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Post Job", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            SectionLabel("1", "Basic Information")
            FormField("Job Title", title) { title = it }
            Spacer(modifier = Modifier.height(12.dp))
            FormField("Company Name", companyName) { companyName = it }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Category", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    CategoryChip(
                        label = cat,
                        selected = category == cat,
                        onClick = { category = cat }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("2", "Salary & Duration")
            FormField("Salary (RM/day)", salary, suffix = "/day") { salary = it }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FormField("Working Date", workingDate, modifier = Modifier.weight(1f)) { workingDate = it }
                FormField("Start Time", workingHoursStart, modifier = Modifier.weight(1f)) { workingHoursStart = it }
            }
            Spacer(modifier = Modifier.height(12.dp))
            FormField("End Time", workingHoursEnd) { workingHoursEnd = it }

            Spacer(modifier = Modifier.height(20.dp))
            SectionLabel("3", "Details")
            FormField("Location", location) { location = it }
            Spacer(modifier = Modifier.height(12.dp))
            FormField("Job Description", description) { description = it }
            Spacer(modifier = Modifier.height(12.dp))
            FormField("Requirements", requirements) { requirements = it }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Number of People Needed", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (peopleNeeded > 1) peopleNeeded-- }) {
                    Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    peopleNeeded.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                IconButton(onClick = { peopleNeeded++ }) {
                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (validationError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(validationError!!, color = Color.Red, fontSize = 12.sp)
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorMessage, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (title.isBlank() || salary.isBlank() || location.isBlank()) {
                        validationError = "Please fill in job title, salary, and location."
                        return@Button
                    }
                    validationError = null
                    onPostClick(
                        PostJobFormData(
                            title = title,
                            companyName = companyName,
                            category = category,
                            salary = salary,
                            workingDate = workingDate,
                            workingHoursStart = workingHoursStart,
                            workingHoursEnd = workingHoursEnd,
                            location = location,
                            description = description,
                            requirements = requirements,
                            peopleNeeded = peopleNeeded
                        )
                    )
                },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text("Post", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(number: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
        Box(
            modifier = Modifier.size(20.dp).clip(RoundedCornerShape(50)).background(DarkNavy),
            contentAlignment = Alignment.Center
        ) {
            Text(number, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    suffix: String? = null,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            trailingIcon = suffix?.let { { Text(it, color = MutedText, fontSize = 12.sp) } },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = SoftGrey,
                focusedBorderColor = DarkNavy
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) DarkNavy else SoftGrey,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else MutedText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}