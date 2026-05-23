package com.example.zootopia.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.BrandMedium
import com.example.zootopia.core.theme.ZootopiaPrimary
import com.example.zootopia.feature.auth.ZootopiaTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileActivity(
    onBack: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAdmin: () -> Unit,
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
                // Ignore internal image errors
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pet Care Profile", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandDark)
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
                    CircularProgressIndicator(color = ZootopiaPrimary)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    
                    // Glassmorphic Hero Banner (remains persistent at the top)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = BrandDark.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        shadowElevation = 8.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF2C5E80).copy(alpha = 0.2f),
                                            Color.Transparent
                                        ),
                                        radius = 350f
                                    )
                                )
                                .padding(20.dp)
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
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .graphicsLayer {
                                                scaleX = borderScale
                                                scaleY = borderScale
                                                alpha = borderAlpha
                                            }
                                            .border(2.dp, Color(0xFFFFB74D), RoundedCornerShape(18.dp))
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                                    ) {
                                        if (!state.profile?.avatarUrl.isNullOrEmpty()) {
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
                                                    fontSize = 26.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                        
                                        IconButton(
                                            onClick = { imagePicker.launch("image/*") },
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(20.dp)
                                                .background(Color.White, CircleShape)
                                        ) {
                                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(11.dp), tint = ZootopiaPrimary)
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column {
                                    Text(
                                        "${state.profile?.firstName ?: ""} ${state.profile?.lastName ?: ""}",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        "@${state.profile?.username ?: ""}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Horizontally Scrollable Custom Profile Tab Pills
                    val tabs = listOf("Overview", "Appointments", "Edit Details", "Security", "My Pets")
                    ScrollableTabRow(
                        selectedTabIndex = state.selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = ZootopiaPrimary,
                        edgePadding = 16.dp,
                        divider = {},
                        indicator = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = state.selectedTab == index
                            Tab(
                                selected = isSelected,
                                onClick = { presenter.setTab(index) },
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ZootopiaPrimary else Color.White.copy(alpha = 0.6f))
                                    .border(1.dp, if (isSelected) ZootopiaPrimary else Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else BrandDark,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tab Contents Container
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()) {
                        when (state.selectedTab) {
                            0 -> OverviewTab(
                                state = state,
                                onNavigateToWishlist = onNavigateToWishlist,
                                onNavigateToAddresses = onNavigateToAddresses,
                                onNavigateToHelp = onNavigateToHelp,
                                onNavigateToAdmin = onNavigateToAdmin
                            )
                            1 -> AppointmentsTab(state = state)
                            2 -> EditDetailsTab(state = state, presenter = presenter)
                            3 -> SecurityTab(state = state, presenter = presenter)
                            4 -> PetsTab(state = state, presenter = presenter)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewTab(
    state: ProfileContract.State,
    onNavigateToWishlist: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Account Overview", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandDark)
                    
                    Surface(
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${state.profile?.loyaltyPoints ?: 0} pts", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF166534))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                ProfileDetailRow(Icons.Default.Badge, "Username", state.profile?.username ?: "")
                ProfileDetailRow(Icons.Default.Email, "Email Address", state.profile?.email ?: "")
                ProfileDetailRow(Icons.Default.Person, "First Name", state.profile?.firstName ?: "")
                ProfileDetailRow(Icons.Default.Person, "Last Name", state.profile?.lastName ?: "")
                ProfileDetailRow(Icons.Default.Shield, "Role Profile", state.profile?.role?.uppercase() ?: "USER")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Quick Links", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandDark)
        Spacer(modifier = Modifier.height(10.dp))
        
        QuickLinkRow(Icons.Default.Favorite, "My Wishlist", "View saved premium essentials", onNavigateToWishlist)
        Spacer(modifier = Modifier.height(8.dp))
        QuickLinkRow(Icons.Default.HomeWork, "Manage Addresses", "Update your shipping destinations", onNavigateToAddresses)
        Spacer(modifier = Modifier.height(8.dp))
        QuickLinkRow(Icons.Default.HelpCenter, "Help & Support FAQs", "Browse self-service guides and chat", onNavigateToHelp)
        
        if (state.profile?.role?.lowercase() == "admin") {
            Spacer(modifier = Modifier.height(8.dp))
            QuickLinkRow(
                Icons.Default.AdminPanelSettings,
                "Admin Command Center",
                "Moderate users and product reviews",
                onNavigateToAdmin,
                highlightColor = ZootopiaPrimary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AppointmentsTab(state: ProfileContract.State) {
    if (state.appointments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(60.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No appointments booked yet", color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Your booked services will display here.", color = Color.Gray, fontSize = 11.sp)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.appointments) { appointment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ZootopiaPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = ZootopiaPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(appointment.serviceName, fontWeight = FontWeight.Black, color = BrandDark, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Booked by: ${appointment.userEmail}", fontSize = 11.sp, color = Color.Gray)
                        }

                        val statusColor = when(appointment.status.lowercase()) {
                            "approved", "completed" -> Color(0xFF10B981)
                            "pending" -> ZootopiaPrimary
                            else -> Color.Red
                        }

                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = appointment.status.uppercase(),
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditDetailsTab(state: ProfileContract.State, presenter: ProfilePresenter) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Edit Profile Details", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandDark)
                Spacer(modifier = Modifier.height(16.dp))

                ZootopiaTextField(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    value = state.editUsername,
                    onValueChange = { presenter.updateForm(it, state.editFirstName, state.editLastName) },
                    label = "Username",
                    icon = Icons.Default.Badge
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

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { presenter.saveProfile() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SecurityTab(state: ProfileContract.State, presenter: ProfilePresenter) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Change Password", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandDark)
                Spacer(modifier = Modifier.height(16.dp))

                ZootopiaTextField(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    value = state.newPassword,
                    onValueChange = { presenter.updatePasswordForm(it, state.confirmPassword) },
                    label = "New Password",
                    icon = Icons.Default.Lock,
                    isPassword = true
                )

                ZootopiaTextField(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    value = state.confirmPassword,
                    onValueChange = { presenter.updatePasswordForm(state.newPassword, it) },
                    label = "Confirm New Password",
                    icon = Icons.Default.Lock,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { presenter.changePassword() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Update Password", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
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
                text = label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDark.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandDark
            )
        }
    }
}

@Composable
fun QuickLinkRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    highlightColor: Color = BrandDark
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = highlightColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = highlightColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = BrandDark, fontSize = 13.sp)
                Text(description, color = Color.Gray, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun PetsTab(state: ProfileContract.State, presenter: ProfilePresenter) {
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var specialInstructions by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("dog") }
    var isFormExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFormExpanded = !isFormExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = ZootopiaPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Register a New Pet", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 14.sp)
                    }
                    Icon(
                        if (isFormExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = BrandDark
                    )
                }

                if (isFormExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ZootopiaTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Pet Name",
                        icon = Icons.Default.Pets,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pet Type", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("dog", "cat", "other").forEach { type ->
                            val isSelected = selectedType == type
                            Surface(
                                color = if (isSelected) ZootopiaPrimary else Color.LightGray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedType = type }
                            ) {
                                Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        type.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else BrandDark,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    ZootopiaTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = "Breed (e.g. Golden Retriever)",
                        icon = Icons.Default.Info,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    ZootopiaTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = "Age (e.g. 2 years)",
                        icon = Icons.Default.Schedule,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    ZootopiaTextField(
                        value = specialInstructions,
                        onValueChange = { specialInstructions = it },
                        label = "Special Instructions / Allergies",
                        icon = Icons.Default.Warning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            presenter.addPet(name, selectedType, breed, age, specialInstructions)
                            name = ""
                            breed = ""
                            age = ""
                            specialInstructions = ""
                            isFormExpanded = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Add Pet Registry", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Registered Pets", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandDark)
        Spacer(modifier = Modifier.height(8.dp))

        if (state.pets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No pets registered yet", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Add your pets to schedule grooming appointments easily.", color = Color.Gray, fontSize = 11.sp)
                }
            }
        } else {
            state.pets.forEach { pet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = ZootopiaPrimary.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Pets, contentDescription = null, tint = ZootopiaPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pet.name, fontWeight = FontWeight.Bold, color = BrandDark, fontSize = 14.sp)
                            Text("${pet.type.uppercase()} • ${pet.breed ?: "Unknown breed"} • ${pet.age ?: "Unknown age"}", color = Color.Gray, fontSize = 11.sp)
                            if (!pet.specialInstructions.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Notes: ${pet.specialInstructions}", color = Color.Red.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        IconButton(onClick = { presenter.deletePet(pet.id ?: 0) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
