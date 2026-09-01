package com.example.parttimego.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.MutedText
import com.example.parttimego.ui.theme.PartTimeGOTheme
import com.example.parttimego.viewmodel.ChangePasswordUiState
import com.example.parttimego.viewmodel.ChangePasswordViewModel

@Composable
fun ChangePasswordRoute(
    viewModel: ChangePasswordViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ChangePasswordScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onForgotPasswordClick = {
            if (onForgotPasswordClick != {}) {
                onForgotPasswordClick()
            } else {
                Toast.makeText(context, "Forgot Password screen coming soon!", Toast.LENGTH_SHORT).show()
            }
        },
        onCurrentPasswordChange = viewModel::onCurrentPasswordChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onToggleCurrentVisibility = viewModel::toggleCurrentPasswordVisibility,
        onToggleNewVisibility = viewModel::toggleNewPasswordVisibility,
        onToggleConfirmVisibility = viewModel::toggleConfirmPasswordVisibility,
        onSubmitClick = {
            viewModel.changePassword(
                onSuccess = {
                    Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    onBackClick()
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    uiState: ChangePasswordUiState,
    onBackClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onCurrentPasswordChange: (String) -> Unit = {},
    onNewPasswordChange: (String) -> Unit = {},
    onConfirmPasswordChange: (String) -> Unit = {},
    onToggleCurrentVisibility: () -> Unit = {},
    onToggleNewVisibility: () -> Unit = {},
    onToggleConfirmVisibility: () -> Unit = {},
    onSubmitClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = DarkNavy, // 确保整体底层背景是深蓝色，解决状态栏白块
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Change Password",
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
                    containerColor = Color.Transparent // 设为透明直接融合底色
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
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Text(
                        text = "Create a New Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavy
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Your new password must be different from previous used passwords and at least 6 characters long.",
                        fontSize = 13.sp,
                        color = MutedText,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // 1. Current Password
                    PasswordInputField(
                        label = "Current Password",
                        value = uiState.currentPassword,
                        onValueChange = onCurrentPasswordChange,
                        isPasswordVisible = uiState.isCurrentPasswordVisible,
                        onToggleVisibility = onToggleCurrentVisibility,
                        imeAction = ImeAction.Next,
                        trailingLabelAction = {
                            Text(
                                text = "Forgot Password?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkNavy,
                                modifier = Modifier.clickable { onForgotPasswordClick() }
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. New Password
                    PasswordInputField(
                        label = "New Password",
                        value = uiState.newPassword,
                        onValueChange = onNewPasswordChange,
                        isPasswordVisible = uiState.isNewPasswordVisible,
                        onToggleVisibility = onToggleNewVisibility,
                        imeAction = ImeAction.Next
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. Confirm New Password
                    PasswordInputField(
                        label = "Confirm New Password",
                        value = uiState.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        isPasswordVisible = uiState.isConfirmPasswordVisible,
                        onToggleVisibility = onToggleConfirmVisibility,
                        imeAction = ImeAction.Done
                    )

                    // Error Message Display
                    if (!uiState.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.errorMessage,
                            color = Color(0xFFDC2626),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Submit Button
                    Button(
                        onClick = onSubmitClick,
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkNavy,
                            disabledContainerColor = DarkNavy.copy(alpha = 0.6f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Update Password",
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

@Composable
private fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    imeAction: ImeAction,
    trailingLabelAction: (@Composable () -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkNavy
            )
            trailingLabelAction?.invoke()
        }

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                        tint = MutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkNavy,
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedContainerColor = Color(0xFFF8FAFC),
                unfocusedContainerColor = Color(0xFFF8FAFC)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChangePasswordScreenPreview() {
    PartTimeGOTheme {
        ChangePasswordScreen(
            uiState = ChangePasswordUiState(
                errorMessage = "New passwords do not match."
            )
        )
    }
}