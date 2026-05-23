package com.example.zootopia.feature.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.zootopia.core.model.Product
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.BrandMedium
import com.example.zootopia.core.theme.ZootopiaPrimary
import com.example.zootopia.feature.dashboard.PawBounceLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsActivity(
    onProductClick: (Long) -> Unit = {},
    presenter: ProductsPresenter = viewModel()
) {
    val state by presenter.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            presenter.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Care Products", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.8f)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF6F0),
                            Color(0xFFF8F6F6),
                            Color(0xFFEDF4F9)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar and Categories Section
                Surface(
                    color = Color.White.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Search Text Field
                        OutlinedTextField(
                            value = state.searchTerm,
                            onValueChange = { presenter.setSearchTerm(it) },
                            placeholder = { Text("Search premium essentials...", color = Color.Gray) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandMedium) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ZootopiaPrimary,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Tabs (Horizontal Scroll)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(state.categories) { category ->
                                val isSelected = state.selectedCategory == category
                                val containerColor = if (isSelected) ZootopiaPrimary else Color.White.copy(alpha = 0.8f)
                                val contentColor = if (isSelected) Color.White else BrandDark

                                Surface(
                                    modifier = Modifier.clickable { presenter.setCategory(category) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = containerColor,
                                    border = BorderStroke(1.dp, if (isSelected) ZootopiaPrimary else Color.LightGray.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = category,
                                        color = contentColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        PawBounceLoader()
                    }
                } else if (state.filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Pets, contentDescription = null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No products found matching filters.", color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.filteredProducts) { product ->
                            CatalogProductCard(
                                product = product,
                                onAddToCart = { presenter.addToCart(product) },
                                onClick = { product.id?.let { onProductClick(it) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogProductCard(
    product: Product,
    onAddToCart: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                if (!product.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(40.dp))
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.category.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = ZootopiaPrimary
                )
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandDark,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₱${String.format("%.2f", product.price)}",
                        fontWeight = FontWeight.Black,
                        color = BrandDark,
                        fontSize = 15.sp
                    )
                    Surface(
                        color = BrandDark,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onAddToCart() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = "Add to Cart", tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }
    }
}
