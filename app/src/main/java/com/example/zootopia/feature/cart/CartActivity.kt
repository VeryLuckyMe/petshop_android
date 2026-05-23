package com.example.zootopia.feature.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.BrandMedium
import com.example.zootopia.core.theme.ZootopiaPrimary
import com.example.zootopia.feature.dashboard.PawBounceLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartActivity(
    presenter: CartPresenter = viewModel()
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
                title = { Text("Shopping Cart", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.8f)
                ),
                actions = {
                    if (state.items.isNotEmpty()) {
                        IconButton(onClick = { presenter.clearCart() }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Cart", tint = BrandDark)
                        }
                    }
                }
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
            if (state.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            color = BrandDark.copy(alpha = 0.08f),
                            shape = CircleShape,
                            modifier = Modifier.size(96.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = BrandDark.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Your cart is empty", fontSize = 18.sp, fontWeight = FontWeight.Black, color = BrandDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Premium pet care treats and products are waiting!", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.items) { item ->
                            CartItemRow(
                                item = item,
                                onIncrease = { presenter.updateQuantity(item.product.id ?: 0, item.quantity + 1) },
                                onDecrease = { presenter.updateQuantity(item.product.id ?: 0, item.quantity - 1) },
                                onRemove = { presenter.removeItem(item.product.id ?: 0) }
                            )
                        }
                    }

                    // Bottom Checkout details panel
                    Surface(
                        color = Color.White.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        modifier = Modifier.fillMaxWidth(),
                        shadowElevation = 16.dp
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            if (state.loyaltyPoints >= 10) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Loyalty Points: ${state.loyaltyPoints}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                            }
                                            if (state.redeemedPoints > 0) {
                                                Text("Redeeming: ${state.redeemedPoints} pts", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF166534))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        val maxRedeemableVal = (state.loyaltyPoints / 10) * 10
                                        var sliderValue by remember(state.redeemedPoints) { mutableStateOf(state.redeemedPoints.toFloat()) }
                                        val steps = if (maxRedeemableVal > 10) (maxRedeemableVal / 10) - 1 else 0
                                        Slider(
                                            value = sliderValue,
                                            onValueChange = { 
                                                sliderValue = it
                                                presenter.redeemPoints(it.toInt())
                                            },
                                            valueRange = 0f..maxRedeemableVal.toFloat(),
                                            steps = steps,
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFF16A34A),
                                                activeTrackColor = Color(0xFF22C55E),
                                                inactiveTrackColor = Color(0xFFDCFCE7)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text("Saves ₱${(state.redeemedPoints / 10) * 50} on this order", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", color = Color.Gray, fontSize = 14.sp)
                                Text("₱${String.format("%.2f", state.subtotal)}", color = BrandDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Shipping", color = Color.Gray, fontSize = 14.sp)
                                val shippingText = if (state.shipping == 0.0) "FREE" else "₱${String.format("%.2f", state.shipping)}"
                                Text(
                                    text = shippingText,
                                    color = if (state.shipping == 0.0) ZootopiaPrimary else BrandDark,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }

                            if (state.discountAmount > 0.0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Loyalty Discount", color = Color(0xFF166534), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("-₱${String.format("%.2f", state.discountAmount)}", color = Color(0xFF16A34A), fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }

                            Divider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total", color = BrandDark, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                Text("₱${String.format("%.2f", state.total)}", color = ZootopiaPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp)
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { presenter.startCheckout() },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !state.isCheckingOut
                            ) {
                                if (state.isCheckingOut) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Proceed to Checkout", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Checkout Success Dialog
            if (state.checkoutSuccess) {
                AlertDialog(
                    onDismissRequest = { presenter.dismissSuccess() },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(56.dp)) },
                    title = { Text("Checkout Successful!", color = BrandDark, fontWeight = FontWeight.Black) },
                    text = { Text("Thank you for your purchase! Your premium pet essentials will be delivered to your address shortly.", color = Color.Gray, fontSize = 14.sp) },
                    confirmButton = {
                        Button(
                            onClick = { presenter.dismissSuccess() },
                            colors = ButtonDefaults.buttonColors(containerColor = ZootopiaPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Okay", fontWeight = FontWeight.Bold)
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = Color.White
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: com.example.zootopia.core.model.CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                if (!item.product.imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = item.product.imageUrl,
                        contentDescription = item.product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.category.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Black, color = ZootopiaPrimary)
                Text(item.product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandDark, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text("₱${String.format("%.2f", item.product.price)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = BrandDark)
            }

            // Quantity Editor controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onDecrease() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = BrandDark, modifier = Modifier.size(14.dp))
                    }
                }

                Text(
                    text = item.quantity.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandDark
                )

                Surface(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onIncrease() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = BrandDark, modifier = Modifier.size(14.dp))
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
