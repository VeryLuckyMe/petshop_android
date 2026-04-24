package com.example.zootopia.screens

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
import coil.compose.AsyncImage
import com.example.zootopia.SupabaseManager
import com.example.zootopia.UserProfile
import com.example.zootopia.ui.theme.BrandDark
import com.example.zootopia.ui.theme.ZootopiaPrimary
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Editable States
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Initial Fetch
    LaunchedEffect(Unit) {
        try {
            val user = SupabaseManager.client.auth.currentUserOrNull()
            if (user != null) {
                val fetchedProfile = SupabaseManager.client.postgrest["zootopiaDatabase"]
                    .select {
                        filter {
                            eq("email", user.email ?: "")
                        }
                    }
                    .decodeSingle<UserProfile>()
                
                profile = fetchedProfile
                username = fetchedProfile.username ?: ""
                firstName = fetchedProfile.firstName ?: ""
                lastName = fetchedProfile.lastName ?: ""
            }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Error fetching profile: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    // Image Picker
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val bytes = context.contentResolver.openInputStream(it)?.readBytes()
                    if (bytes != null) {
                        val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                        val bucket = SupabaseManager.client.storage.from("avatars")
                        
                        bucket.upload(fileName, bytes, upsert = true)
                        val publicUrl = bucket.publicUrl(fileName)
                        
                        SupabaseManager.client.postgrest["zootopiaDatabase"].update({
                            set("avatar_url", publicUrl)
                        }) {
                            filter { eq("email", profile?.email ?: "") }
                        }
                        
                        profile = profile?.copy(avatarUrl = publicUrl)
                        snackbarHostState.showSnackbar("Photo updated!")
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Upload failed: ${e.message}")
                }
            }
        }
    }

    fun handleSave() {
        scope.launch {
            try {
                SupabaseManager.client.postgrest["zootopiaDatabase"].update({
                    set("username", username)
                    set("first_name", firstName)
                    set("last_name", lastName)
                }) {
                    filter { eq("email", profile?.email ?: "") }
                }
                
                profile = profile?.copy(username = username, firstName = firstName, lastName = lastName)
                isEditing = false
                snackbarHostState.showSnackbar("Profile updated!")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Save failed: ${e.message}")
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
                    if (isEditing) {
                        IconButton(onClick = { handleSave() }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = ZootopiaPrimary)
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                            if (profile?.avatarUrl != null) {
                                AsyncImage(
                                    model = profile?.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = (profile?.firstName?.take(1) ?: "U").uppercase(),
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
                            Text("${profile?.firstName ?: ""} ${profile?.lastName ?: ""}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                            Text("@${profile?.username ?: ""}", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
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
                    if (isEditing) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            label = { Text("First Name") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            label = { Text("Last Name") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        ProfileDetailRow(Icons.Default.Badge, "Username", profile?.username ?: "")
                        ProfileDetailRow(Icons.Default.Email, "Email", profile?.email ?: "")
                        ProfileDetailRow(Icons.Default.Person, "First Name", profile?.firstName ?: "")
                        ProfileDetailRow(Icons.Default.Person, "Last Name", profile?.lastName ?: "")
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
