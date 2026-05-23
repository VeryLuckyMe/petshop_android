package com.example.zootopia.feature.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.BrandMedium
import com.example.zootopia.core.theme.ZootopiaPrimary
import com.example.zootopia.core.utils.RecentViewedManager
import com.example.zootopia.feature.dashboard.PawBounceLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailActivity(
    productId: Long,
    onBack: () -> Unit,
    presenter: ProductDetailPresenter = viewModel()
) {
    val state by presenter.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Load product on startup
    LaunchedEffect(productId) {
        presenter.loadProduct(productId)
    }

    // Register viewed item when loaded
    LaunchedEffect(state.product) {
        state.product?.let { product ->
            RecentViewedManager.addProduct(context, product)
        }
    }

    // Success and Error messaging
    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            presenter.clearError()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            presenter.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(state.product?.name ?: "Product Details", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandDark)
                    }
                },
                actions = {
                    IconButton(onClick = { presenter.toggleWishlist() }) {
                        Icon(
                            imageVector = if (state.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (state.isWishlisted) Color.Red else BrandDark
                        )
                    }
                },
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
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    PawBounceLoader()
                }
            } else if (state.product == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Product not found.", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                val product = state.product!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Image Gallery
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (state.images.isNotEmpty()) {
                                AsyncImage(
                                    model = state.images[state.selectedImageIndex],
                                    contentDescription = product.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                               )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Pets, contentDescription = null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(60.dp))
                                }
                            }

                            // SAVE tag
                            if (state.discount.isNotEmpty()) {
                                Surface(
                                    color = ZootopiaPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .align(Alignment.TopStart)
                                ) {
                                    Text(
                                        text = state.discount,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Clickable Thumbnails Row
                    if (state.images.size > 1) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            items(state.images.size) { index ->
                                val isSelected = index == state.selectedImageIndex
                                Surface(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clickable { presenter.selectImage(index) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = 2.dp,
                                        color = if (isSelected) ZootopiaPrimary else Color.Transparent
                                    ),
                                    color = Color.White
                                ) {
                                    AsyncImage(
                                        model = state.images[index],
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }
                    }

                    // Product Information Envelope
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Category & SKU
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = product.category.uppercase(),
                                    fontWeight = FontWeight.Black,
                                    color = ZootopiaPrimary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "SKU: ${state.sku}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Name
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandDark,
                                fontSize = 22.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Star Rating & Stock status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "${state.rating} (${state.reviewsCount} reviews)",
                                        fontWeight = FontWeight.Bold,
                                        color = BrandMedium,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                                Surface(
                                    color = Color(0xFFE2F3E7),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = state.stockStatus,
                                        color = Color(0xFF1E7035),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.3f))

                            // Price Section
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "₱${String.format("%.2f", product.price)}",
                                    fontWeight = FontWeight.Black,
                                    color = BrandDark,
                                    fontSize = 26.sp
                                )
                                if (state.oldPrice > 0.0) {
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "₱${String.format("%.2f", state.oldPrice)}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Size Selection
                            if (state.sizes.isNotEmpty() && state.sizes != listOf("Standard")) {
                                Text("Weight / Size", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(state.sizes) { size ->
                                        val isSelected = size == state.selectedSize
                                        Surface(
                                            modifier = Modifier.clickable { presenter.setSize(size) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) ZootopiaPrimary else Color.White,
                                            border = BorderStroke(1.dp, if (isSelected) ZootopiaPrimary else Color.LightGray.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = size,
                                                color = if (isSelected) Color.White else BrandDark,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Flavor Selection
                            if (state.flavors.isNotEmpty() && state.flavors != listOf("Original") && state.flavors != listOf("Classic Silver")) {
                                Text("Flavor / Variety", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(state.flavors) { flavor ->
                                        val isSelected = flavor == state.selectedFlavor
                                        Surface(
                                            modifier = Modifier.clickable { presenter.setFlavor(flavor) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) ZootopiaPrimary else Color.White,
                                            border = BorderStroke(1.dp, if (isSelected) ZootopiaPrimary else Color.LightGray.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = flavor,
                                                color = if (isSelected) Color.White else BrandDark,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Quantity Adjuster
                            Text("Quantity", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { presenter.decrementQuantity() },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = BrandDark, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    text = state.quantity.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandDark,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Surface(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { presenter.incrementQuantity() },
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = BrandDark, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Dynamic Tabs: Description, Specs, Reviews
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 80.dp), // Leaves room for the bottom floating bar
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            // Tab Headers
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val tabs = listOf("Description", "Specifications", "Reviews")
                                tabs.forEachIndexed { index, title ->
                                    val isSelected = index == state.selectedTab
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { presenter.setTab(index) }
                                            .background(
                                                if (isSelected) Color.Transparent else Color.LightGray.copy(alpha = 0.15f)
                                            )
                                            .padding(vertical = 14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = title,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            color = if (isSelected) ZootopiaPrimary else BrandMedium,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            // Tab Body
                            Box(modifier = Modifier.padding(20.dp)) {
                                when (state.selectedTab) {
                                    0 -> {
                                        Text(
                                            text = state.longDescription.ifEmpty { "Premium choice product from Zootopia's curated catalogue." },
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                            color = BrandDark
                                        )
                                    }
                                    1 -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            state.features.forEach { feature ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF1E7035), modifier = Modifier.size(16.dp))
                                                    Text(
                                                        text = feature,
                                                        fontSize = 14.sp,
                                                        color = BrandDark,
                                                        modifier = Modifier.padding(start = 8.dp)
                                                    )
                                                }
                                            }
                                            if (state.features.isEmpty()) {
                                                Text("Specifications not available.", fontSize = 14.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                    2 -> {
                                        // Product reviews mock list matching star count
                                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Customer Feedback", fontWeight = FontWeight.Bold, color = BrandDark, fontSize = 14.sp)
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                                    Text("${state.rating} out of 5", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandDark, modifier = Modifier.padding(start = 4.dp))
                                                }
                                            }

                                            // Render review entries
                                            val reviewers = listOf("Marielle V.", "Dave A.", "John D.")
                                            val ratings = listOf(5, 4, 5)
                                            val comments = listOf(
                                                "My pet absolutely loves this! Will definitely order again. Super fast delivery!",
                                                "Item matches description. Shipping took a couple of days but product quality is great.",
                                                "Highly recommended! The glass packaging was perfect and pet shop support is very responsive."
                                            )

                                            reviewers.forEachIndexed { i, reviewer ->
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(reviewer, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrandDark)
                                                        Row {
                                                            repeat(ratings[i]) {
                                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(12.dp))
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(comments[i], fontSize = 12.sp, color = Color.DarkGray, lineHeight = 16.sp)
                                                    Divider(modifier = Modifier.padding(top = 12.dp), color = Color.LightGray.copy(alpha = 0.2f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Floating Buy Row
            if (state.product != null) {
                Surface(
                    color = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(76.dp),
                    border = BorderStroke(width = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f)),
                    shadowElevation = 24.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { presenter.addToCart() },
                            colors = ButtonDefaults.buttonColors(containerColor = ZootopiaPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add to Cart", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                            }
                        }

                        Button(
                            onClick = { 
                                presenter.addToCart()
                                // Call back to go to cart directly or navigate
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Buy Now", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
