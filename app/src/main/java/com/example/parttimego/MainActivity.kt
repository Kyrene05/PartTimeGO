package com.example.parttimego

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.parttimego.nav.AppNavGraph
import com.example.parttimego.screen.LoginScreen
import com.example.parttimego.screen.SplashScreen
import com.example.parttimego.ui.theme.PartTimeGOTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PartTimeGOTheme {
                val navController = rememberNavController()
                AppNavGraph(navController = navController)
            }
        }
    }
}