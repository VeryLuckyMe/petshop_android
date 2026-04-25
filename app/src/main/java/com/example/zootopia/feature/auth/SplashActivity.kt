package com.example.zootopia.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zootopia.core.theme.BrandDark
import kotlinx.coroutines.delay

@Composable
fun SplashActivity(onSplashComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // 2 second delay
        onSplashComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BrandDark),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = Color.White, modifier = Modifier.size(50.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("ZOOTOPIA", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
            Text("ANIMAL CARE", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, letterSpacing = 8.sp)
        }
    }
}
