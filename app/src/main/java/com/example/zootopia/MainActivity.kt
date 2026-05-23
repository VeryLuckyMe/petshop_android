package com.example.zootopia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zootopia.core.theme.AndroidstuTheme
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.ZootopiaPrimary
import com.example.zootopia.core.utils.CartRepository
import com.example.zootopia.feature.auth.LoginActivity
import com.example.zootopia.feature.auth.RegisterActivity
import com.example.zootopia.feature.auth.SplashActivity
import com.example.zootopia.feature.dashboard.DashboardActivity
import com.example.zootopia.feature.profile.ProfileActivity
import com.example.zootopia.feature.products.ProductsActivity
import com.example.zootopia.feature.products.ProductDetailActivity
import com.example.zootopia.feature.services.ServicesActivity
import com.example.zootopia.feature.cart.CartActivity
import com.example.zootopia.feature.products.WishlistActivity
import com.example.zootopia.feature.profile.AddressActivity
import com.example.zootopia.feature.profile.HelpActivity
import com.example.zootopia.feature.dashboard.AdminActivity

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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavigationRoutes = listOf("dashboard", "products", "services", "cart", "profile")
    val showBottomBar = currentRoute in bottomNavigationRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                PetshopBottomBar(navController = navController)
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(padding)
        ) {
            composable("splash") {
                SplashActivity(onSplashComplete = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                })
            }
            composable("login") { 
                LoginActivity(
                    onLoginSuccess = { 
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
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
                    onProductClick = { productId -> navController.navigate("product_detail/$productId") },
                    onLogout = { 
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) 
            }
            composable("products") {
                ProductsActivity(
                    onProductClick = { productId -> navController.navigate("product_detail/$productId") }
                )
            }
            composable("services") {
                ServicesActivity()
            }
            composable("cart") {
                CartActivity()
            }
            composable("profile") { 
                ProfileActivity(
                    onBack = { navController.popBackStack() },
                    onNavigateToWishlist = { navController.navigate("wishlist") },
                    onNavigateToAddresses = { navController.navigate("addresses") },
                    onNavigateToHelp = { navController.navigate("help") },
                    onNavigateToAdmin = { navController.navigate("admin") }
                ) 
            }
            composable("wishlist") {
                WishlistActivity(
                    onProductClick = { productId -> navController.navigate("product_detail/$productId") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("addresses") {
                AddressActivity(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("help") {
                HelpActivity(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("admin") {
                AdminActivity(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("product_detail/{productId}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull() ?: 0L
                ProductDetailActivity(
                    productId = productId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetshopBottomBar(navController: androidx.navigation.NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        NavigationItem("dashboard", "Home", Icons.Default.Home),
        NavigationItem("products", "Products", Icons.Default.ShoppingBag),
        NavigationItem("services", "Services", Icons.Default.MedicalServices),
        NavigationItem("cart", "Cart", Icons.Default.ShoppingCart),
        NavigationItem("profile", "Profile", Icons.Default.Person)
    )

    Surface(
        color = Color.White.copy(alpha = 0.9f),
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        border = BorderStroke(width = 0.5.dp, color = Color.LightGray.copy(alpha = 0.4f)),
        shadowElevation = 16.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxHeight()
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo("dashboard") {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        if (item.route == "cart") {
                            val cartItems by CartRepository.items.collectAsState()
                            val cartCount = cartItems.sumOf { it.quantity }
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(
                                            containerColor = ZootopiaPrimary,
                                            contentColor = Color.White
                                        ) {
                                            Text(cartCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) ZootopiaPrimary else BrandDark.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) ZootopiaPrimary else BrandDark.copy(alpha = 0.6f)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) ZootopiaPrimary else BrandDark.copy(alpha = 0.6f)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = ZootopiaPrimary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}

data class NavigationItem(val route: String, val label: String, val icon: ImageVector)