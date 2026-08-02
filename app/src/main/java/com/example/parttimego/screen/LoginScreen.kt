package com.example.parttimego.screen


import androidx.compose.foundation.BorderStroke
import com.example.parttimego.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.MutedText
import com.example.parttimego.ui.theme.SoftGrey

@Composable
fun LoginScreen(
    onLoginSuccess: () ->Unit={},
    onRegisterClick: () ->Unit={}
){
    var email by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var isLoginTab by remember { mutableStateOf(true)}

    val scrollState= rememberScrollState()

    Column(
        modifier= Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
    ){
        Column(
            modifier=Modifier
                .fillMaxWidth()
                .padding(horizontal=24.dp,vertical=24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier= Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftGrey),
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text="P",
                        fontSize=22.sp,
                        fontWeight= FontWeight.Bold,
                        color=DarkNavy
                    )
                }
                Spacer(modifier=Modifier.width(12.dp))
                Text(
                    text="PartTimeGO",
                    fontSize=22.sp,
                    fontWeight = FontWeight.Bold,
                    color=Color.White
                )
            }
            Spacer(modifier=Modifier.height(16.dp))
            Text(
                text="Welcome Back!",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color=Color.White
            )
            Text(
                text="Log in to find your job",
                fontSize = 14.sp,
                color=Color.White.copy(alpha=0.8f)
            )
        }
        Surface(
            modifier= Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f,fill=false),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color=Color.White
        ){
            Column(
                modifier= Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Login/register toggle tab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftGrey)
                        .padding(4.dp)
                ){
                    Box(
                        modifier= Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isLoginTab) Color.White else Color.Transparent)
                            .clickable{isLoginTab=true},
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            text="Log In",
                            fontWeight = FontWeight.Bold,
                            color=DarkNavy
                        )
                    }
                    Box(
                        modifier= Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isLoginTab)Color.White else Color.Transparent)
                            .clickable{
                                isLoginTab=false
                                onRegisterClick()
                            },
                        contentAlignment= Alignment.Center
                    ){
                        Text(
                            text="Register",
                            fontWeight = FontWeight.Bold,
                            color= MutedText
                        )
                    }
                }
                Spacer(modifier= Modifier.height(20.dp))

                Column(modifier= Modifier.fillMaxWidth()) {
                    Text(
                        text="Email Address",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color=MutedText
                    )
                    Spacer(modifier=Modifier.height(6.dp))
                    OutlinedTextField(
                        value=email,
                        onValueChange = {email=it},
                        placeholder = {Text("you@example.com",color=Color.Gray)},
                        leadingIcon = {
                            Icon(
                                painter= painterResource(id= R.drawable.ic_email),
                                contentDescription = "Email Icon",
                                tint = MutedText,
                                modifier = Modifier.size(20.dp)

                            )
                        },
                        singleLine = true,
                        shape=RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SoftGrey,
                            focusedBorderColor = DarkNavy
                        ),
                        modifier=Modifier.fillMaxWidth()
                    )

                }
                Spacer(modifier=Modifier.height(16.dp))

                //password field
                Column(modifier=Modifier.fillMaxWidth()){
                    Text(
                        text="Password",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color=MutedText
                    )
                    Spacer(modifier= Modifier.height(6.dp))
                    OutlinedTextField(
                        value=password,
                        onValueChange = {password=it},
                        placeholder = {Text("• • • • • • • •", color= Color.Gray)},
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = "Password Icon",
                                tint = MutedText,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape=RoundedCornerShape(12.dp),
                        colors= OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SoftGrey,
                            focusedBorderColor = DarkNavy
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                //forget password link
                Text(
                    text = "Forget Password ?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .clickable { /* Handle forgot password */ }
                )
                Spacer(modifier = Modifier.height(20.dp))
                // Log In Button
                Button(
                    onClick = { onLoginSuccess() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                ) {
                    Text(text = "Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = SoftGrey)
                    Text(
                        text = " or continue as ",
                        fontSize = 12.sp,
                        color = MutedText,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = SoftGrey)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Role Options: Job Seeker & Employer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Job Seeker guest mode */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SoftGrey)
                    ) {
                        Text(text = "Job Seeker", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { /* Employer guest mode */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SoftGrey)
                    ) {
                        Text(text = "Employer", color = DarkNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

    }
}


// Preview to test layout
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}