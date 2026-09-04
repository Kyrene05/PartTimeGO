package com.example.parttimego.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.PartTimeGOTheme
import com.example.parttimego.ui.theme.SoftGrey
import com.example.parttimego.viewmodel.EmployerProfileUiState
import com.example.parttimego.viewmodel.EmployerProfileViewModel

@Composable
fun EditEmployerProfileRoute(
    viewModel: EmployerProfileViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            viewModel.resetUpdateSuccess()
            onBackClick()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    EditEmployerProfileScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onImageSelected = { uri -> viewModel.onAvatarSelected(uri) },
        onUserNameChange = { viewModel.onUserNameChange(it) },
        onCompanyNameChange = { viewModel.onCompanyNameChange(it) },
        onPhoneChange = { viewModel.onPhoneChange(it) },
        onBackgroundChange = { viewModel.onCompanyBackgroundChange(it) },
        onSaveClick = { viewModel.saveProfile(context) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEmployerProfileScreen(
    uiState: EmployerProfileUiState,
    onBackClick: () -> Unit = {},
    onImageSelected: (Uri) -> Unit = {},
    onUserNameChange: (String) -> Unit = {},
    onCompanyNameChange: (String) -> Unit = {},
    onPhoneChange: (String) -> Unit = {},
    onBackgroundChange: (String) -> Unit = {},
    onSaveClick: () -> Unit = {}
) {

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    val wordCount = remember(uiState.companyBackground) {
        if (uiState.companyBackground.isBlank()) 0
        else uiState.companyBackground.trim().split("\\s+".toRegex()).size
    }

    val phoneDigits = uiState.phone.removePrefix("+60").removePrefix("60").filter { it.isDigit() }
    val isPhoneTooShort = phoneDigits.isNotEmpty() && phoneDigits.length < 8

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = DarkNavy,
        unfocusedBorderColor = Color(0xFFCBD5E1),
        focusedLabelColor = DarkNavy,
        unfocusedLabelColor = Color.Gray,
        cursorColor = DarkNavy,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black
    )

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkNavy)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(SoftGrey),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.selectedImageUri != null) {
                                AsyncImage(
                                    model = uiState.selectedImageUri,
                                    contentDescription = "Selected Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (!uiState.avatarUrl.isNullOrBlank()) {
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
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change Photo",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to change avatar",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.userName,
                        onValueChange = onUserNameChange,
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )

                    // Company Name 輸入框
                    Column {
                        OutlinedTextField(
                            value = uiState.companyName,
                            onValueChange = onCompanyNameChange,
                            label = { Text("Company Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                    }

                    // Phone Number 輸入框
                    Column {
                        OutlinedTextField(
                            value = phoneDigits,
                            onValueChange = { input ->
                                val filteredInput = input.filter { it.isDigit() }
                                if (filteredInput.length <= 10) {
                                    onPhoneChange(filteredInput)
                                }
                            },
                            prefix = {
                                Text(
                                    text = "+60 ",
                                    fontWeight = FontWeight.Bold,
                                    color = DarkNavy
                                )
                            },
                            label = { Text("Phone Number *") },
                            isError = isPhoneTooShort,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                        if (isPhoneTooShort) {
                            Text(
                                text = "Invalid phone number (must be at least 8 digits)",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.companyEmail,
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        label = { Text("Email Address (Locked)") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked Field",
                                tint = Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = Color(0xFFF1F5F9),
                            disabledTextColor = Color.DarkGray,
                            disabledBorderColor = Color(0xFFCBD5E1),
                            disabledLabelColor = Color.Gray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Company Background 字段
                    Column {
                        OutlinedTextField(
                            value = uiState.companyBackground,
                            onValueChange = { input ->
                                val words = input.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
                                if (words.size <= 300 || input.length < uiState.companyBackground.length) {
                                    onBackgroundChange(input)
                                }
                            },
                            label = { Text("Company Background") },
                            placeholder = { Text("Write your company background here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5,
                            colors = textFieldColors
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$wordCount / 300 words",
                            fontSize = 11.sp,
                            color = if (wordCount > 300) Color.Red else Color.Gray,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Save Button
                    Button(
                        onClick = onSaveClick,
                        enabled = !uiState.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Save Changes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditEmployerProfileScreenPreview() {
    PartTimeGOTheme {
        EditEmployerProfileScreen(
            uiState = EmployerProfileUiState(
                userName = "Alex",
                companyName = "Tech Solutions Inc.",
                phone = "123456789",
                companyEmail = "alex@techsolutions.com",
                companyBackground = "We provide full-stack IT solutions and software engineering services for small to medium enterprises.",
                isSaving = false
            )
        )
    }
}