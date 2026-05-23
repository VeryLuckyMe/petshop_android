package com.example.zootopia.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.ZootopiaPrimary
import com.example.zootopia.core.model.UserProfile
import com.example.zootopia.core.model.Appointment
import com.example.zootopia.core.model.Product
import com.example.zootopia.core.utils.NetworkUtils
import com.example.zootopia.feature.auth.ZootopiaTextField
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminActivity(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var activeTab by remember { mutableStateOf("users") }
    var isLoading by remember { mutableStateOf(true) }

    // Database models state
    var users by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }

    // Dialog controllers
    var userToEdit by remember { mutableStateOf<UserProfile?>(null) }
    var appointmentToEdit by remember { mutableStateOf<Appointment?>(null) }
    
    var userToDelete by remember { mutableStateOf<UserProfile?>(null) }
    var appointmentToDelete by remember { mutableStateOf<Appointment?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // User Edit Form Fields
    var editUsername by remember { mutableStateOf("") }
    var editRole by remember { mutableStateOf("user") }
    var editPoints by remember { mutableStateOf("") }

    // Appointment Edit Form Fields
    var editStatus by remember { mutableStateOf("pending") }

    fun loadAllData() {
        coroutineScope.launch {
            isLoading = true
            try {
                // Fetch Users
                val fetchedUsers = NetworkUtils.client.postgrest["zootopiaDatabase"]
                    .select()
                    .decodeList<UserProfile>()
                    .sortedByDescending { it.createdAt }

                // Fetch Appointments
                val fetchedAppts = NetworkUtils.client.postgrest["appointments"]
                    .select()
                    .decodeList<Appointment>()
                    .sortedByDescending { it.createdAt }

                // Fetch Products
                val fetchedProds = NetworkUtils.client.postgrest["products"]
                    .select()
                    .decodeList<Product>()
                    .sortedByDescending { it.createdAt }

                users = fetchedUsers
                appointments = fetchedAppts
                products = fetchedProds
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Failed to load admin data: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAllData()
    }

    // Handlers
    fun updateUser(userId: Long) {
        coroutineScope.launch {
            isLoading = true
            try {
                val pts = editPoints.toIntOrNull() ?: 0
                NetworkUtils.client.postgrest["zootopiaDatabase"]
                    .update({
                        set("username", editUsername)
                        set("role", editRole)
                        set("loyalty_points", pts)
                    }) {
                        filter { eq("id", userId) }
                    }
                snackbarHostState.showSnackbar("User updated successfully!")
                userToEdit = null
                loadAllData()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Update failed: ${e.message}")
                isLoading = false
            }
        }
    }

    fun deleteUser(userId: Long) {
        coroutineScope.launch {
            isLoading = true
            try {
                NetworkUtils.client.postgrest["zootopiaDatabase"]
                    .delete { filter { eq("id", userId) } }
                snackbarHostState.showSnackbar("User profile deleted.")
                userToDelete = null
                loadAllData()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Delete failed: ${e.message}")
                isLoading = false
            }
        }
    }

    fun updateAppointment(apptId: Long) {
        coroutineScope.launch {
            isLoading = true
            try {
                NetworkUtils.client.postgrest["appointments"]
                    .update({
                        set("status", editStatus)
                    }) {
                        filter { eq("id", apptId) }
                    }
                snackbarHostState.showSnackbar("Appointment updated!")
                appointmentToEdit = null
                loadAllData()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Update failed: ${e.message}")
                isLoading = false
            }
        }
    }

    fun deleteAppointment(apptId: Long) {
        coroutineScope.launch {
            isLoading = true
            try {
                NetworkUtils.client.postgrest["appointments"]
                    .delete { filter { eq("id", apptId) } }
                snackbarHostState.showSnackbar("Appointment cancelled and removed.")
                appointmentToDelete = null
                loadAllData()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Delete failed: ${e.message}")
                isLoading = false
            }
        }
    }

    fun deleteProduct(prodId: Long) {
        coroutineScope.launch {
            isLoading = true
            try {
                NetworkUtils.client.postgrest["products"]
                    .delete { filter { eq("id", prodId) } }
                snackbarHostState.showSnackbar("Product removed from database.")
                productToDelete = null
                loadAllData()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Delete failed: ${e.message}")
                isLoading = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Admin Command Center", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BrandDark)
                    }
                },
                actions = {
                    IconButton(onClick = { loadAllData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BrandDark)
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
            Column(modifier = Modifier.fillMaxSize()) {
                // KPI statistics card list
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val kpis = listOf(
                        Triple("Users", users.size.toString(), Icons.Default.Group),
                        Triple("Booked", appointments.size.toString(), Icons.Default.Event),
                        Triple("Pending", appointments.count { it.status == "pending" }.toString(), Icons.Default.PendingActions),
                        Triple("Products", products.size.toString(), Icons.Default.Inventory2)
                    )

                    kpis.forEach { (lbl, valStr, icon) ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(icon, contentDescription = null, tint = ZootopiaPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(valStr, fontWeight = FontWeight.Black, fontSize = 16.sp, color = BrandDark)
                                Text(lbl, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                        }
                    }
                }

                // Tab selection chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        "users" to "Profiles",
                        "appointments" to "Bookings",
                        "products" to "Products"
                    )

                    tabs.forEach { (tabId, label) ->
                        val isSelected = activeTab == tabId
                        val containerColor = if (isSelected) BrandDark else Color.White.copy(alpha = 0.75f)
                        val contentColor = if (isSelected) Color.White else BrandDark

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = tabId },
                            shape = RoundedCornerShape(12.dp),
                            color = containerColor,
                            border = BorderStroke(1.dp, if (isSelected) BrandDark else Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    color = contentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading && users.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ZootopiaPrimary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // Profiles Moderation
                        if (activeTab == "users") {
                            items(users) { u ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = u.username ?: "Anonymous User",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                color = BrandDark
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val isAdmin = u.role?.lowercase() == "admin"
                                                val badgeBg = if (isAdmin) Color(0xFFFEF2F2) else Color(0xFFF1F5F9)
                                                val badgeTxt = if (isAdmin) Color(0xFFEF4444) else Color(0xFF64748B)
                                                Surface(
                                                    color = badgeBg,
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(0.5.dp, badgeTxt.copy(alpha = 0.3f))
                                                ) {
                                                    Text(
                                                        text = (u.role ?: "user").uppercase(),
                                                        color = badgeTxt,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = {
                                                        userToEdit = u
                                                        editUsername = u.username ?: ""
                                                        editRole = u.role ?: "user"
                                                        editPoints = u.loyaltyPoints.toString()
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = { userToDelete = u },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(u.email ?: "", color = Color.Gray, fontSize = 12.sp)

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = Color.LightGray.copy(alpha = 0.2f), thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Loyalty Rewards", fontSize = 12.sp, color = Color.Gray)
                                            Text("${u.loyaltyPoints} PTS", fontWeight = FontWeight.Bold, color = ZootopiaPrimary, fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Bookings Moderation
                        if (activeTab == "appointments") {
                            items(appointments) { appt ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = appt.serviceName,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp,
                                                color = BrandDark,
                                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val statusColor = when (appt.status.lowercase()) {
                                                    "completed" -> Color(0xFF10B981)
                                                    "confirmed" -> Color(0xFF3B82F6)
                                                    "cancelled" -> Color(0xFFEF4444)
                                                    else -> Color(0xFFF59E0B)
                                                }
                                                Surface(
                                                    color = statusColor.copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = appt.status.uppercase(),
                                                        color = statusColor,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                IconButton(
                                                    onClick = {
                                                        appointmentToEdit = appt
                                                        editStatus = appt.status
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit Status", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = { appointmentToDelete = appt },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Booked by: ${appt.userEmail}", color = Color.Gray, fontSize = 12.sp)
                                        if (!appt.createdAt.isNullOrEmpty()) {
                                            Text("Date booked: ${appt.createdAt.substringBefore("T")}", color = Color.Gray, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Products Inventory Moderation
                        if (activeTab == "products") {
                            items(products) { prod ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = prod.imageUrl,
                                            contentDescription = prod.name,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.LightGray.copy(alpha = 0.2f))
                                        )

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = prod.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = BrandDark
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Surface(
                                                color = Color.LightGray.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.width(IntrinsicSize.Min)
                                            ) {
                                                Text(
                                                    text = prod.category.uppercase(),
                                                    color = Color.Gray,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("₱${prod.price}", fontWeight = FontWeight.Black, color = ZootopiaPrimary, fontSize = 14.sp)
                                        }

                                        IconButton(
                                            onClick = { productToDelete = prod }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= DIALOGS =================

        // 1. User Edit Dialog
        if (userToEdit != null) {
            AlertDialog(
                onDismissRequest = { userToEdit = null },
                title = { Text("Edit User Profile", fontWeight = FontWeight.Black, fontSize = 18.sp, color = BrandDark) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ZootopiaTextField(
                            value = editUsername,
                            onValueChange = { editUsername = it },
                            label = "Username",
                            icon = Icons.Default.Person,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("User Role", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("user", "admin").forEach { role ->
                                val isSelected = editRole == role
                                val chipBg = if (isSelected) ZootopiaPrimary else Color.LightGray.copy(alpha = 0.2f)
                                val chipTxt = if (isSelected) Color.White else BrandDark
                                Surface(
                                    color = chipBg,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { editRole = role }
                                ) {
                                    Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                        Text(role.uppercase(), fontWeight = FontWeight.Bold, color = chipTxt, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        ZootopiaTextField(
                            value = editPoints,
                            onValueChange = { editPoints = it },
                            label = "Loyalty Points",
                            icon = Icons.Default.Star,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { updateUser(userToEdit?.id ?: 0L) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandDark)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToEdit = null }) {
                        Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // 2. Appointment Edit Dialog
        if (appointmentToEdit != null) {
            AlertDialog(
                onDismissRequest = { appointmentToEdit = null },
                title = { Text("Update Status", fontWeight = FontWeight.Black, fontSize = 18.sp, color = BrandDark) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Select status for appointment:", fontSize = 13.sp, color = Color.Gray)
                        val statuses = listOf("pending", "confirmed", "completed", "cancelled")
                        
                        statuses.forEach { stat ->
                            val isSelected = editStatus == stat
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ZootopiaPrimary.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { editStatus = stat }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { editStatus = stat },
                                    colors = RadioButtonDefaults.colors(selectedColor = ZootopiaPrimary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stat.uppercase(), fontWeight = FontWeight.Bold, color = if (isSelected) ZootopiaPrimary else BrandDark, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { updateAppointment(appointmentToEdit?.id ?: 0L) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandDark)
                    ) {
                        Text("Update", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { appointmentToEdit = null }) {
                        Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // 3. User Delete Dialog
        if (userToDelete != null) {
            AlertDialog(
                onDismissRequest = { userToDelete = null },
                title = { Text("Delete Profile?", fontWeight = FontWeight.Black, color = Color.Red) },
                text = { Text("Are you sure you want to permanently delete the profile of '${userToDelete?.username}'? This operation is permanent.") },
                confirmButton = {
                    Button(
                        onClick = { deleteUser(userToDelete?.id ?: 0L) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Confirm Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { userToDelete = null }) {
                        Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // 4. Appointment Cancel/Delete Dialog
        if (appointmentToDelete != null) {
            AlertDialog(
                onDismissRequest = { appointmentToDelete = null },
                title = { Text("Remove Appointment?", fontWeight = FontWeight.Black, color = Color.Red) },
                text = { Text("Are you sure you want to remove the booking record of '${appointmentToDelete?.serviceName}'?") },
                confirmButton = {
                    Button(
                        onClick = { deleteAppointment(appointmentToDelete?.id ?: 0L) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Confirm Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { appointmentToDelete = null }) {
                        Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // 5. Product Delete Dialog
        if (productToDelete != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Remove Product?", fontWeight = FontWeight.Black, color = Color.Red) },
                text = { Text("Are you sure you want to delete '${productToDelete?.name}' from the store product inventory?") },
                confirmButton = {
                    Button(
                        onClick = { deleteProduct(productToDelete?.id ?: 0L) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Confirm Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}
