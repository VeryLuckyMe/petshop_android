package com.example.zootopia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.zootopia.ui.theme.AndroidstuTheme
import com.example.zootopia.screens.AuthScreen
import com.example.zootopia.screens.DashboardScreen
import com.example.zootopia.screens.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidstuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PetshopApp()
                }
            }
        }
    }
}

@Composable
fun PetshopApp() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") { 
            AuthScreen(onLoginSuccess = { navController.navigate("dashboard") }) 
        }
        composable("dashboard") { 
            DashboardScreen(
                onNavigateToProfile = { navController.navigate("profile") },
                onLogout = { navController.navigate("auth") }
            ) 
        }
        composable("profile") { 
            ProfileScreen(onBack = { navController.popBackStack() }) 
        }
    }
}