package com.example.zootopia.feature.profile

import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.ZootopiaPrimary
import com.example.zootopia.core.model.Address
import com.example.zootopia.core.utils.NetworkUtils
import com.example.zootopia.feature.auth.ZootopiaTextField
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressActivity(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Form fields
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var addressLine1 by remember { mutableStateOf("") }
    var addressLine2 by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var stateProvince by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("Home") }
    var isDefault by remember { mutableStateOf(false) }
    var isFormExpanded by remember { mutableStateOf(false) }

    fun loadAddresses() {
        coroutineScope.launch {
            isLoading = true
            try {
                val user = NetworkUtils.client.auth.currentUserOrNull()
                if (user != null) {
                    val fetched = NetworkUtils.client.postgrest["addresses"]
                        .select {
                            filter {
                                eq("user_id", user.id)
                            }
                        }
                        .decodeList<Address>()
                        .sortedWith(compareByDescending<Address> { it.isDefault }.thenByDescending { it.createdAt })
                    addresses = fetched
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Failed to load addresses: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAddresses()
    }

    fun addAddress() {
        if (fullName.isBlank() || phone.isBlank() || addressLine1.isBlank() || city.isBlank() || stateProvince.isBlank() || postalCode.isBlank()) {
            coroutineScope.launch { snackbarHostState.showSnackbar("Please fill in all required fields.") }
            return
        }
        coroutineScope.launch {
            isLoading = true
            try {
                val user = NetworkUtils.client.auth.currentUserOrNull()
                if (user != null) {
                    if (isDefault) {
                        // Reset all other addresses default flag
                        NetworkUtils.client.postgrest["addresses"]
                            .update({
                                set("is_default", false)
                            }) {
                                filter { eq("user_id", user.id) }
                            }
                    }

                    val newAddr = Address(
                        userId = user.id,
                        fullName = fullName,
                        phone = phone,
                        addressLine1 = addressLine1,
                        addressLine2 = addressLine2.ifBlank { null },
                        city = city,
                        stateProvince = stateProvince,
                        postalCode = postalCode,
                        label = label,
                        isDefault = isDefault
                    )
                    NetworkUtils.client.postgrest["addresses"].insert(newAddr)
                    snackbarHostState.showSnackbar("Address added successfully!")
                    
                    // Clear fields
                    fullName = ""
                    phone = ""
                    addressLine1 = ""
                    addressLine2 = ""
                    city = ""
                    stateProvince = ""
                    postalCode = ""
                    isDefault = false
                    isFormExpanded = false
                    
                    loadAddresses()
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Failed to add address: ${e.message}")
                isLoading = false
            }
        }
    }

    fun deleteAddress(id: Long) {
        coroutineScope.launch {
            isLoading = true
            try {
                NetworkUtils.client.postgrest["addresses"].delete {
                    filter { eq("id", id) }
                }
                snackbarHostState.showSnackbar("Address deleted successfully!")
                loadAddresses()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Failed to delete address: ${e.message}")
                isLoading = false
            }
        }
    }

    fun makeDefault(id: Long) {
        coroutineScope.launch {
            isLoading = true
            try {
                val user = NetworkUtils.client.auth.currentUserOrNull()
                if (user != null) {
                    // Reset all other addresses default flag
                    NetworkUtils.client.postgrest["addresses"]
                        .update({
                            set("is_default", false)
                        }) {
                            filter { eq("user_id", user.id) }
                        }
                    
                    // Set this one as default
                    NetworkUtils.client.postgrest["addresses"]
                        .update({
                            set("is_default", true)
                        }) {
                            filter { eq("id", id) }
                        }
                    
                    snackbarHostState.showSnackbar("Default address updated!")
                    loadAddresses()
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Failed to update default address: ${e.message}")
                isLoading = false
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Addresses", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 20.sp) },
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
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Form component to Add Address
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { isFormExpanded = !isFormExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = ZootopiaPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add a New Address", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 14.sp)
                            }
                            Icon(
                                if (isFormExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = BrandDark
                            )
                        }

                        if (isFormExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 280.dp)
                                    .verticalScroll(scrollState)
                            ) {
                                ZootopiaTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = "Full Name *",
                                    icon = Icons.Default.Person,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                                ZootopiaTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = "Phone Number *",
                                    icon = Icons.Default.Phone,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                                ZootopiaTextField(
                                    value = addressLine1,
                                    onValueChange = { addressLine1 = it },
                                    label = "Address Line 1 *",
                                    icon = Icons.Default.Home,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                                ZootopiaTextField(
                                    value = addressLine2,
                                    onValueChange = { addressLine2 = it },
                                    label = "Address Line 2 (Optional)",
                                    icon = Icons.Default.Home,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                                ZootopiaTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = "City *",
                                    icon = Icons.Default.LocationCity,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                                ZootopiaTextField(
                                    value = stateProvince,
                                    onValueChange = { stateProvince = it },
                                    label = "State / Province *",
                                    icon = Icons.Default.Map,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                                ZootopiaTextField(
                                    value = postalCode,
                                    onValueChange = { postalCode = it },
                                    label = "Postal Code *",
                                    icon = Icons.Default.Pin,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Address Label", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Home", "Work").forEach { lbl ->
                                        val isSelected = label == lbl
                                        Surface(
                                            color = if (isSelected) ZootopiaPrimary else Color.LightGray.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { label = lbl }
                                        ) {
                                            Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                                Text(
                                                    lbl.uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else BrandDark,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isDefault,
                                        onCheckedChange = { isDefault = it },
                                        colors = CheckboxDefaults.colors(checkedColor = ZootopiaPrimary)
                                    )
                                    Text("Set as Default Shipping Address", fontSize = 12.sp, color = BrandDark)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { addAddress() },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Save Shipping Address", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Saved Shipping Locations", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandDark)
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading && addresses.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ZootopiaPrimary)
                    }
                } else if (addresses.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.LocationOff, contentDescription = null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No saved addresses found", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Add your shipping address for a swift checkout experience.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(addresses) { addr ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val labelColor = if (addr.label?.lowercase() == "work") Color(0xFF3B82F6) else ZootopiaPrimary
                                            Surface(
                                                color = labelColor.copy(alpha = 0.12f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = (addr.label ?: "Home").uppercase(),
                                                    color = labelColor,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            if (addr.isDefault) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = Color(0xFFF0FDF4),
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(0.5.dp, Color(0xFFBBF7D0))
                                                ) {
                                                    Text(
                                                        text = "DEFAULT",
                                                        color = Color(0xFF166534),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        Row {
                                            if (!addr.isDefault) {
                                                IconButton(
                                                    onClick = { makeDefault(addr.id ?: 0) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = "Make Default", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            IconButton(
                                                onClick = { deleteAddress(addr.id ?: 0) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(addr.fullName, fontWeight = FontWeight.Bold, color = BrandDark, fontSize = 14.sp)
                                    Text(addr.phone, color = Color.Gray, fontSize = 12.sp)
                                    Text("${addr.addressLine1}${if (!addr.addressLine2.isNullOrBlank()) ", ${addr.addressLine2}" else ""}", color = BrandDark, fontSize = 13.sp)
                                    Text("${addr.city}, ${addr.stateProvince} - ${addr.postalCode}", color = BrandDark, fontSize = 13.sp)
                                    Text(addr.country, color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
