package com.example.zootopia.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zootopia.ui.theme.BrandDark
import com.example.zootopia.ui.theme.BrandMedium
import com.example.zootopia.ui.theme.ZootopiaPrimary

data class ServiceItem(val title: String, val description: String, val icon: ImageVector)
data class ProductItem(val name: String, val price: String, val category: String, val isHot: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onNavigateToProfile: () -> Unit, onLogout: () -> Unit) {
    val services = listOf(
        ServiceItem("Pet Grooming", "Full-service spa treatments and hygiene care.", Icons.Default.ContentCut),
        ServiceItem("Health Checkups", "Routine wellness exams and vaccinations.", Icons.Default.MedicalServices),
        ServiceItem("Gourmet Treats", "Organic and nutritionist-approved snacks.", Icons.Default.Restaurant)
    )

    val products = listOf(
        ProductItem("Organic Chicken Bites", "$14.99", "Nutrition", true),
        ProductItem("Rubber Bone", "$9.50", "Toys"),
        ProductItem("Dream Cloud Bed", "$45.00", "Bedding"),
        ProductItem("Water Fountain", "$29.99", "Accessories")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = ZootopiaPrimary,
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Pets, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Zootopia", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandDark),
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
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
        ) {
            // Header Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandDark)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        color = BrandMedium.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search care plans...", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                        }
                    }
                }
            }

            // Hero Section
            item {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandMedium)
                ) {
                    Column {
                        // Place for an image - using a generic placeholder box for now
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(BrandDark.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                        }
                        
                        Column(modifier = Modifier.padding(20.dp)) {
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
                                lineHeight = 32.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Professional care tailored to your pet's unique needs.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { },
                                    colors = ButtonDefaults.buttonColors(containerColor = ZootopiaPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(44.dp)
                                ) {
                                    Text("Book Now", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { },
                                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(44.dp)
                                ) {
                                    Text("Our Story", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Services Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        "Our Premium Services",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandDark
                    )
                    Text(
                        "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZootopiaPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Services List
            items(services) { service ->
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ZootopiaPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(service.icon, contentDescription = null, tint = ZootopiaPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.title, fontWeight = FontWeight.Bold, color = BrandDark, fontSize = 16.sp)
                            Text(service.description, fontSize = 12.sp, color = Color.Gray)
                        }
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = ZootopiaPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Products Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Featured Products",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandDark,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(products) { product ->
                        ProductCard(product)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProductCard(product: ProductItem) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Pets, contentDescription = null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                if (product.isHot) {
                    Surface(
                        color = ZootopiaPrimary,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Text("HOT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(product.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandDark, maxLines = 1)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(product.price, fontWeight = FontWeight.Black, color = BrandDark, fontSize = 16.sp)
                    Surface(
                        color = BrandDark,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(28.dp).clickable { }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}
