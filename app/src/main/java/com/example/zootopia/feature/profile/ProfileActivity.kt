package com.example.zootopia.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.ZootopiaPrimary
import com.example.zootopia.feature.auth.ZootopiaTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileActivity(
    onBack: () -> Unit,
    presenter: ProfilePresenter = viewModel()
) {
    val state by presenter.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            presenter.clearMessages()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            presenter.clearMessages()
        }
    }

    // Image Picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bytes = context.contentResolver.openInputStream(it)?.readBytes()
                if (bytes != null) {
                    presenter.uploadAvatar(bytes)
                }
            } catch (e: Exception) {
                // Handled internally by presenter if needed, or ignored here
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.8f) // Glassmorphic translucent white
                ),
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { presenter.saveProfile() }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = ZootopiaPrimary)
                        }
                    } else {
                        IconButton(onClick = { presenter.setEditing(true) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BrandDark)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF8F6F6)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ZootopiaPrimary)
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFF6F0), // Soft warm orange wash
                                Color(0xFFF8F6F6), // Premium neutral base
                                Color(0xFFEDF4F9)  // Soft slate blue wash
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Floating High-Fidelity Glassmorphic Hero Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = BrandDark.copy(alpha = 0.85f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF2C5E80).copy(alpha = 0.2f),
                                            Color.Transparent
                                        ),
                                        radius = 300f
                                    )
                                )
                                .padding(24.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Pulsing Avatar Component
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse_avatar")
                                val borderScale by infiniteTransition.animateFloat(
                                    initialValue = 1.0f,
                                    targetValue = 1.15f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "scale"
                                )
                                val borderAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.8f,
                                    targetValue = 0.1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "alpha"
                                )

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(96.dp)
                                ) {
                                    // Pulsing outline ring
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .graphicsLayer {
                                                scaleX = borderScale
                                                scaleY = borderScale
                                                alpha = borderAlpha
                                            }
                                            .border(2.5.dp, Color(0xFFFFB74D), RoundedCornerShape(20.dp))
                                    )
                                    
                                    // Actual Avatar container
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    ) {
                                        if (state.profile?.avatarUrl != null) {
                                            AsyncImage(
                                                model = state.profile?.avatarUrl,
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = (state.profile?.firstName?.take(1) ?: "U").uppercase(),
                                                    fontSize = 32.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                        
                                        IconButton(
                                            onClick = { imagePicker.launch("image/*") },
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(24.dp)
                                                .background(Color.White, CircleShape)
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = ZootopiaPrimary)
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        "${state.profile?.firstName ?: ""} ${state.profile?.lastName ?: ""}",
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        "@${state.profile?.username ?: ""}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // Floating Glassmorphic Details Card
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.6f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            if (state.isEditing) {
                                ZootopiaTextField(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    value = state.editUsername,
                                    onValueChange = { presenter.updateForm(it, state.editFirstName, state.editLastName) },
                                    label = "Username",
                                    icon = Icons.Default.Person
                                )
                                ZootopiaTextField(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    value = state.editFirstName,
                                    onValueChange = { presenter.updateForm(state.editUsername, it, state.editLastName) },
                                    label = "First Name",
                                    icon = Icons.Default.Person
                                )
                                ZootopiaTextField(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    value = state.editLastName,
                                    onValueChange = { presenter.updateForm(state.editUsername, state.editFirstName, it) },
                                    label = "Last Name",
                                    icon = Icons.Default.Person
                                )
                            } else {
                                ProfileDetailRow(Icons.Default.Badge, "Username", state.profile?.username ?: "")
                                ProfileDetailRow(Icons.Default.Email, "Email", state.profile?.email ?: "")
                                ProfileDetailRow(Icons.Default.Person, "First Name", state.profile?.firstName ?: "")
                                ProfileDetailRow(Icons.Default.Person, "Last Name", state.profile?.lastName ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.5f))
                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ZootopiaPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDark.copy(alpha = 0.5f)
            )
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandDark
            )
        }
    }
}
