package com.example.zootopia.feature.services

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.BrandMedium
import com.example.zootopia.core.theme.ZootopiaPrimary
import com.example.zootopia.core.model.Pet
import com.example.zootopia.feature.dashboard.PawBounceLoader
import com.example.zootopia.feature.auth.ZootopiaTextField
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesActivity(
    presenter: ServicesPresenter = viewModel()
) {
    val state by presenter.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 5-Step Wizard States
    var currentStep by remember { mutableStateOf(1) }
    var selectedService by remember { mutableStateOf<ServicesContract.Service?>(null) }
    var selectedAddons by remember { mutableStateOf<List<ServicesContract.Addon>>(emptyList()) }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedPet by remember { mutableStateOf<Pet?>(null) }
    
    // Inline quick pet fields
    var quickPetName by remember { mutableStateOf("") }
    var quickPetType by remember { mutableStateOf("Dog") }
    var quickPetBreed by remember { mutableStateOf("") }
    var quickPetAge by remember { mutableStateOf("") }

    var paymentMethod by remember { mutableStateOf("pay_in_store") }
    var redeemedPoints by remember { mutableStateOf(0) }

    val staticAddons = remember {
        listOf(
            ServicesContract.Addon("scent", "Premium Scented Shampoo", 10.0),
            ServicesContract.Addon("teeth", "Toothbrushing & Breath Spray", 15.0),
            ServicesContract.Addon("nails", "Organic Nail File & Trim", 12.0),
            ServicesContract.Addon("flea", "Flea & Tick Spot Treatment", 25.0)
        )
    }

    // Days calculation for next 7 days
    val datesList = remember {
        val list = mutableListOf<String>()
        val sdf = SimpleDateFormat("EEE, MMM dd", Locale.US)
        val cal = Calendar.getInstance()
        for (i in 0..6) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val timesList = remember {
        listOf("09:00 AM", "10:30 AM", "01:00 PM", "02:30 PM", "04:00 PM")
    }

    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            presenter.clearMessages()
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            presenter.clearMessages()
            // Reset wizard state on success
            currentStep = 1
            selectedService = null
            selectedAddons = emptyList()
            selectedDate = ""
            selectedTime = ""
            selectedPet = null
            quickPetName = ""
            quickPetBreed = ""
            quickPetAge = ""
            paymentMethod = "pay_in_store"
            redeemedPoints = 0
        }
    }

    val filteredServices = if (state.selectedCategory == "All") {
        state.services
    } else {
        state.services.filter { it.category.equals(state.selectedCategory, ignoreCase = true) }
    }

    // Helper calculate pricing
    val basePrice = selectedService?.price ?: 0.0
    val addonsTotal = selectedAddons.sumOf { it.price }
    val subtotal = basePrice + addonsTotal
    val maxRedeemable = (state.profile?.loyaltyPoints ?: 0) / 10 * 10
    val pointsDiscount = (redeemedPoints / 10) * 50
    val finalTotal = maxOf(0.0, subtotal - pointsDiscount)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Care Booking Wizard", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 20.sp) },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(onClick = { currentStep-- }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back Step", tint = BrandDark)
                        }
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
                // Stepper progress indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (step in 1..5) {
                        val isCurrentOrDone = step <= currentStep
                        val barColor = if (isCurrentOrDone) ZootopiaPrimary else Color.LightGray.copy(alpha = 0.4f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(barColor)
                        )
                    }
                }

                if (state.isLoading) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        PawBounceLoader()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // STEP 1: Select Service & Add-ons
                        if (currentStep == 1) {
                            item {
                                Text("Step 1: Choose a Care Service", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandDark)
                                Text("SELECT A BASE SERVICE FROM THE CATALOG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }

                            // Category scroll row
                            item {
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
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            items(filteredServices) { service ->
                                val isSelected = selectedService?.name == service.name
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedService = service },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) ZootopiaPrimary.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.75f)
                                    ),
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (isSelected) ZootopiaPrimary else Color.White.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(service.name, fontWeight = FontWeight.Black, fontSize = 15.sp, color = BrandDark)
                                            Text("₱${String.format("%.2f", service.price)}", fontWeight = FontWeight.Black, color = ZootopiaPrimary, fontSize = 15.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(service.description, fontSize = 12.sp, color = BrandDark.copy(alpha = 0.7f))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(service.duration, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }

                            if (selectedService != null) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Enhance Your Booking (Optional Add-ons)", fontSize = 14.sp, fontWeight = FontWeight.Black, color = BrandDark)
                                    Text("TICK TO ADD TREATMENTS AND LUXURY CARE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }

                                items(staticAddons) { addon ->
                                    val isChecked = selectedAddons.any { it.id == addon.id }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedAddons = if (isChecked) {
                                                    selectedAddons.filter { it.id != addon.id }
                                                } else {
                                                    selectedAddons + addon
                                                }
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = {
                                                        selectedAddons = if (isChecked) {
                                                            selectedAddons.filter { it.id != addon.id }
                                                        } else {
                                                            selectedAddons + addon
                                                        }
                                                    },
                                                    colors = CheckboxDefaults.colors(checkedColor = ZootopiaPrimary)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(addon.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandDark)
                                            }
                                            Text("+₱${String.format("%.2f", addon.price)}", fontWeight = FontWeight.Black, color = ZootopiaPrimary, fontSize = 12.sp)
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { currentStep = 2 },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Text("Continue to Schedule", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // STEP 2: Select Date & Time
                        if (currentStep == 2) {
                            item {
                                Text("Step 2: Choose Schedule Slot", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandDark)
                                Text("SELECT YOUR PREFERRED SERVICE DATE AND TIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text("Select Date", fontSize = 13.sp, fontWeight = FontWeight.Black, color = BrandDark)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        // Dynamic dates horizontal list
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            items(datesList) { date ->
                                                val isSelected = selectedDate == date
                                                Surface(
                                                    modifier = Modifier.clickable { selectedDate = date },
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = if (isSelected) ZootopiaPrimary else Color.LightGray.copy(alpha = 0.2f),
                                                    border = BorderStroke(1.dp, if (isSelected) ZootopiaPrimary else Color.Transparent)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(date.substringBefore(","), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Gray)
                                                        Text(date.substringAfter(", "), fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color.White else BrandDark)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))
                                        Text("Select Time Slot", fontSize = 13.sp, fontWeight = FontWeight.Black, color = BrandDark)
                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Times grid-like vertical column items
                                        timesList.forEach { time ->
                                            val isSelected = selectedTime == time
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) ZootopiaPrimary.copy(alpha = 0.08f) else Color.LightGray.copy(alpha = 0.12f))
                                                    .clickable { selectedTime = time }
                                                    .border(
                                                        BorderStroke(
                                                            1.5.dp,
                                                            if (isSelected) ZootopiaPrimary else Color.Transparent
                                                        ),
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(14.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = if (isSelected) ZootopiaPrimary else Color.Gray, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(time, fontWeight = FontWeight.Bold, color = if (isSelected) ZootopiaPrimary else BrandDark, fontSize = 13.sp)
                                                }
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ZootopiaPrimary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { currentStep = 3 },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Text("Continue to Pet Selection", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // STEP 3: Select Pet details
                        if (currentStep == 3) {
                            item {
                                Text("Step 3: Select Registered Pet", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandDark)
                                Text("ASSIGN ONE OF YOUR REGISTERED PETS OR TYPE QUICK DETAILS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }

                            if (state.pets.isNotEmpty()) {
                                items(state.pets) { pet ->
                                    val isSelected = selectedPet?.id == pet.id
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                selectedPet = pet
                                                quickPetName = "" 
                                            },
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) ZootopiaPrimary.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.75f)
                                        ),
                                        border = BorderStroke(
                                            width = 1.5.dp,
                                            color = if (isSelected) ZootopiaPrimary else Color.White.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (pet.type.lowercase() == "cat") Icons.Default.Pets else Icons.Default.Pets,
                                                contentDescription = null,
                                                tint = ZootopiaPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrandDark)
                                                Text("${pet.type.uppercase()} • ${pet.breed ?: "Unknown Breed"}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = ZootopiaPrimary)
                                            }
                                        }
                                    }
                                }
                            }

                            // Optional inline pet registration card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = ZootopiaPrimary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Assign a New Pet Profile", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 13.sp)
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))

                                        ZootopiaTextField(
                                            value = quickPetName,
                                            onValueChange = { 
                                                quickPetName = it
                                                selectedPet = null // Clear selected pet when typing custom name
                                            },
                                            label = "Pet Name",
                                            icon = Icons.Default.Pets,
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Pet Type", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("Dog", "Cat", "Other").forEach { type ->
                                                val isSelected = quickPetType == type
                                                Surface(
                                                    color = if (isSelected) ZootopiaPrimary else Color.LightGray.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable { quickPetType = type }
                                                ) {
                                                    Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                                        Text(type.uppercase(), fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else BrandDark, fontSize = 11.sp)
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        ZootopiaTextField(
                                            value = quickPetBreed,
                                            onValueChange = { quickPetBreed = it },
                                            label = "Breed (Optional)",
                                            icon = Icons.Default.Search,
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        )
                                        ZootopiaTextField(
                                            value = quickPetAge,
                                            onValueChange = { quickPetAge = it },
                                            label = "Age (Optional)",
                                            icon = Icons.Default.CalendarMonth,
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            if (selectedPet != null || quickPetName.isNotBlank()) {
                                item {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { currentStep = 4 },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Text("Continue to Checkout Details", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // STEP 4: Choose Payment method / loyalty points discount
                        if (currentStep == 4) {
                            item {
                                Text("Step 4: Select Checkout Payment", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandDark)
                                Text("REDEEM POINTS TO SAVE ₱50 FOR EVERY 10 PTS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        // Standard Payment Method Option
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (paymentMethod == "pay_in_store") ZootopiaPrimary.copy(alpha = 0.08f) else Color.Transparent)
                                                .clickable { paymentMethod = "pay_in_store" }
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = paymentMethod == "pay_in_store",
                                                onClick = { paymentMethod = "pay_in_store" },
                                                colors = RadioButtonDefaults.colors(selectedColor = ZootopiaPrimary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("Pay in Store", fontWeight = FontWeight.Bold, color = BrandDark, fontSize = 13.sp)
                                                Text("Pay with cash or card during appointment", color = Color.Gray, fontSize = 11.sp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Loyalty Point Option
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (paymentMethod == "loyalty_points") ZootopiaPrimary.copy(alpha = 0.08f) else Color.Transparent)
                                                .clickable { paymentMethod = "loyalty_points" }
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = paymentMethod == "loyalty_points",
                                                onClick = { paymentMethod = "loyalty_points" },
                                                colors = RadioButtonDefaults.colors(selectedColor = ZootopiaPrimary)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("Loyalty Rewards Discount", fontWeight = FontWeight.Bold, color = BrandDark, fontSize = 13.sp)
                                                Text("Redeem active points balance for discounts", color = Color.Gray, fontSize = 11.sp)
                                            }
                                        }

                                        if (paymentMethod == "loyalty_points") {
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                                            Spacer(modifier = Modifier.height(16.dp))

                                            Text("Your Points Balance", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                            Text("${state.profile?.loyaltyPoints ?: 0} PTS Available", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ZootopiaPrimary)
                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (maxRedeemable > 0) {
                                                Slider(
                                                    value = redeemedPoints.toFloat(),
                                                    onValueChange = { redeemedPoints = (it.toInt() / 10) * 10 },
                                                    valueRange = 0f..maxRedeemable.toFloat(),
                                                    steps = maxOf(0, (maxRedeemable / 10) - 1),
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = ZootopiaPrimary,
                                                        activeTrackColor = ZootopiaPrimary,
                                                        inactiveTrackColor = Color.LightGray.copy(alpha = 0.3f)
                                                    )
                                                )
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Redeeming: $redeemedPoints PTS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandDark)
                                                    Text("Discount: -₱$pointsDiscount.00", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color(0xFF10B981))
                                                }
                                            } else {
                                                Text("Requires at least 10 points to unlock rewards discounts.", color = Color.Gray, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { currentStep = 5 },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text("Continue to Summary Receipt", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // STEP 5: Summary receipt & final submission
                        if (currentStep == 5) {
                            item {
                                Text("Step 5: Booking Review & Receipt", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrandDark)
                                Text("FROSTED RECEIPT INVOICE DETAILING SELECTIONS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text("BOOKING INVOICE RECEIPT", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Itemized Rows
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(selectedService?.name ?: "", fontSize = 13.sp, color = BrandDark, fontWeight = FontWeight.Medium)
                                            Text("₱${String.format("%.2f", basePrice)}", fontSize = 13.sp, color = BrandDark, fontWeight = FontWeight.Bold)
                                        }

                                        selectedAddons.forEach { add ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("+ ${add.name}", fontSize = 12.sp, color = Color.Gray)
                                                Text("₱${String.format("%.2f", add.price)}", fontSize = 12.sp, color = Color.Gray)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Subtotal", fontSize = 13.sp, color = BrandDark)
                                            Text("₱${String.format("%.2f", subtotal)}", fontSize = 13.sp, color = BrandDark, fontWeight = FontWeight.Bold)
                                        }

                                        if (paymentMethod == "loyalty_points" && pointsDiscount > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Points Discount ($redeemedPoints PTS)", fontSize = 13.sp, color = Color(0xFF10B981))
                                                Text("-₱${String.format("%.2f", pointsDiscount.toDouble())}", fontSize = 13.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Divider(color = BrandDark.copy(alpha = 0.2f), thickness = 1.dp)
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Grand Total", fontSize = 14.sp, fontWeight = FontWeight.Black, color = BrandDark)
                                            Text("₱${String.format("%.2f", finalTotal)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = ZootopiaPrimary)
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                        Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Booking Details Block
                                        Text("APPOINTMENT SCHEDULE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Text("$selectedDate at $selectedTime", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("ASSIGNED PET", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        val finalPetName = selectedPet?.name ?: quickPetName
                                        val finalPetDetails = if (selectedPet != null) {
                                            "${selectedPet?.type?.uppercase()} • ${selectedPet?.breed ?: "Unknown Breed"}"
                                        } else {
                                            "${quickPetType.uppercase()}${if (quickPetBreed.isNotBlank()) " • $quickPetBreed" else ""}"
                                        }
                                        Text("$finalPetName ($finalPetDetails)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val petNameStr = selectedPet?.name ?: quickPetName
                                        presenter.bookAppointment(
                                            serviceName = selectedService?.name ?: "",
                                            addons = selectedAddons,
                                            date = selectedDate,
                                            time = selectedTime,
                                            petName = petNameStr,
                                            paymentMethod = paymentMethod,
                                            redeemedPoints = redeemedPoints
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Confirm Appointment Booking", color = Color.White, fontWeight = FontWeight.Bold)
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
