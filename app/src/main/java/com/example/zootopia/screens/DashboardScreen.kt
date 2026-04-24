package com.example.zootopia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zootopia.ui.theme.BrandDark
import com.example.zootopia.ui.theme.BrandMedium
import com.example.zootopia.ui.theme.ZootopiaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onNavigateToProfile: () -> Unit, onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Zootopia", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandDark),
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F6F6))
                .padding(16.dp)
        ) {
            // Hero Section
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandMedium)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append("Your Pet's Happiness, \n")
                                withStyle(style = SpanStyle(color = ZootopiaPrimary)) {
                                    append("Our Priority")
                                }
                            },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            lineHeight = 34.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Professional and compassionate animal care services tailored to your pet's unique needs.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { /* Book Now */ },
                            colors = ButtonDefaults.buttonColors(containerColor = ZootopiaPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Book Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Our Premium Services",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandDark
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Services Grid (Simplified for list)
            items(3) { index ->
                val serviceTitles = listOf("Pet Grooming", "Health Checkups", "Gourmet Treats")
                val serviceIcons = listOf(Icons.Default.ContentCut, Icons.Default.MedicalServices, Icons.Default.Restaurant)
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ZootopiaPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(serviceIcons[index], contentDescription = null, tint = ZootopiaPrimary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(serviceTitles[index], fontWeight = FontWeight.Bold, color = BrandDark)
                            Text("Learn more about our $index service", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
