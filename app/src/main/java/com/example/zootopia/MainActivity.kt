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
import com.example.zootopia.core.theme.AndroidstuTheme
import com.example.zootopia.feature.auth.LoginActivity
import com.example.zootopia.feature.auth.RegisterActivity
import com.example.zootopia.feature.auth.SplashActivity
import com.example.zootopia.feature.dashboard.DashboardActivity
import com.example.zootopia.feature.profile.ProfileActivity

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
    
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashActivity(onSplashComplete = {
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("login") { 
            LoginActivity(
                onLoginSuccess = { navController.navigate("dashboard") },
                onNavigateToRegister = { navController.navigate("register") }
            ) 
        }
        composable("register") {
            RegisterActivity(
                onRegisterSuccess = { navController.navigate("login") },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }
        composable("dashboard") { 
            DashboardActivity(
                onNavigateToProfile = { navController.navigate("profile") },
                onLogout = { navController.navigate("login") }
            ) 
        }
        composable("profile") { 
            ProfileActivity(onBack = { navController.popBackStack() }) 
        }
    }
}