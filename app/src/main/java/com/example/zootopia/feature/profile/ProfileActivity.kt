package com.example.zootopia.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.ZootopiaPrimary

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
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { presenter.saveProfile() }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = ZootopiaPrimary)
                        }
                    } else {
                        IconButton(onClick = { presenter.setEditing(true) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ZootopiaPrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF1F5F9))
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ZootopiaPrimary, Color(0xFFFFB74D))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.3f))
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
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text("${state.profile?.firstName ?: ""} ${state.profile?.lastName ?: ""}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            Text("@${state.profile?.username ?: ""}", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        }
                    }
                }

                // Details
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    if (state.isEditing) {
                        OutlinedTextField(
                            value = state.editUsername,
                            onValueChange = { presenter.updateForm(it, state.editFirstName, state.editLastName) },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = state.editFirstName,
                            onValueChange = { presenter.updateForm(state.editUsername, it, state.editLastName) },
                            label = { Text("First Name") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = state.editLastName,
                            onValueChange = { presenter.updateForm(state.editUsername, state.editFirstName, it) },
                            label = { Text("Last Name") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
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
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = BrandDark)
        }
    }
}
