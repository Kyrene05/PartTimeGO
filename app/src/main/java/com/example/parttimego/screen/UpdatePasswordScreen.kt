package com.example.parttimego.screen

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parttimego.nav.Screen
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.PartTimeGOTheme
import com.example.parttimego.ui.theme.SoftGrey
import com.example.parttimego.viewmodel.AuthState
import kotlinx.coroutines.delay

@Composable
fun UpdatePasswordScreen(
    authState:AuthState= AuthState.Idle,
    sessionReady:Boolean=true,
    onUpdatePasswordClick:(String) -> Unit={},
    onRequestNewLinkClick:() -> Unit={}
){
    var newPassword by remember{ mutableStateOf("") }
    var linkExpired by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    //Fail safe: if no session arrives within 10s, assume the link failed/expired
    LaunchedEffect(sessionReady) {
        if (!sessionReady){
            delay(10_000)
            if (!sessionReady)linkExpired=true
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .statusBarsPadding()
            .navigationBarsPadding()

    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(scrollState)
            ) {
            Text(
                "Set New Password",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color=Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Enter your new password below",
                fontSize = 14.sp,
                color=Color.White.copy(alpha = 0.8f)
                )
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color=Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {

                when{
                    //Fallback state: link expired/session never arrived
                    linkExpired -> {
                        Text(
                            "This reset link has expired or is invalid.",
                            color= Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onRequestNewLinkClick,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                        ) {
                            Text("Request a New Link", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    //waiting state: still resolving the session
                    !sessionReady ->{
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = DarkNavy, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Verifying your reset link...", fontSize = 14.sp, color = Color.Gray)
                        }
                    }

                    //Normal state: form ready
                    else ->{
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {newPassword=it},
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors= OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = SoftGrey,
                                focusedBorderColor = DarkNavy
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(20.dp))

                        if (authState is AuthState.Error){
                            Text(authState.message, color=Color.Red, fontSize = 12.sp)
                            Spacer(Modifier.height(12.dp))
                        }
                        if (authState is AuthState.Success){
                            Text(authState.message,
                                color = Color(0xFF2E7D32),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                        }
                        Button(
                            onClick = {onUpdatePasswordClick(newPassword)},
                            enabled = authState !is AuthState.Loading,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                        ) {
                            if (authState is AuthState.Loading){
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            }else{
                                Text("Update Password", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdatePasswordScreenPreview_Form() {
    PartTimeGOTheme {
        UpdatePasswordScreen(
            authState = AuthState.Idle,
            sessionReady = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UpdatePasswordScreenPreview_Waiting() {
    PartTimeGOTheme {
        UpdatePasswordScreen(
            authState = AuthState.Idle,
            sessionReady = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UpdatePasswordScreenPreview_Error() {
    PartTimeGOTheme {
        UpdatePasswordScreen(
            authState = AuthState.Error("Password must be at least 6 characters."),
            sessionReady = true
        )
    }
}