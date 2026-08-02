package com.example.parttimego.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parttimego.R
import com.example.parttimego.ui.theme.DarkNavy
import com.example.parttimego.ui.theme.LightBlueIcon
import com.example.parttimego.ui.theme.SoftGrey
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(onNavigateToLogin:() -> Unit){
    val scrollState = rememberScrollState()
      //Auto navigate to login after 2.5s
    LaunchedEffect(Unit) {
        delay(2500)
        onNavigateToLogin()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkNavy)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState) // Handles landscape & short screen heights gracefully
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ){
            //App Logo Badge "P"
            Box(
                modifier=Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SoftGrey),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text="P",
                    fontSize=42.sp,
                    fontWeight= FontWeight.Bold,
                    color=DarkNavy
                )
            }
            Spacer(modifier=Modifier.height(16.dp))

            Text(
                text="PartTimeGO",
                fontSize =32.sp,
                fontWeight = FontWeight.Bold,
                color=Color.White
            )
            Text(
                text="Malaysia's Part-Time Jobs Platform",
                fontSize=14.sp,
                color= Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier=Modifier.height(48.dp))

            Image(
                painter= painterResource(id=R.drawable.ic_people),
                contentDescription="Community",
                modifier= Modifier.size(180.dp),
                contentScale= ContentScale.Fit,
                colorFilter = ColorFilter.tint(LightBlueIcon)
            )

            Spacer(modifier=Modifier.height(48.dp))

            Text(
                text="Find temporary jobs near you -\nstudents, freelancers & event crew welcome",
                fontSize=15.sp,
                color=Color.White.copy(alpha=0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}

@Preview
@Composable
fun SplashScreenPreview(){
    SplashScreen(onNavigateToLogin = {})
}