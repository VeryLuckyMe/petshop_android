package com.example.zootopia.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.ZootopiaPrimary
import com.example.zootopia.feature.auth.ZootopiaTextField
import com.example.zootopia.core.utils.NetworkUtils
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch

data class FAQCategory(
    val id: String,
    val label: String,
    val items: List<FAQItem>
)

data class FAQItem(
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpActivity(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val faqData = remember {
        listOf(
            FAQCategory(
                id = "orders",
                label = "Orders & Shipping",
                items = listOf(
                    FAQItem("How long does standard shipping take?", "Standard shipping takes 3–5 business days within Metro Manila and 5–7 days for provincial addresses."),
                    FAQItem("Can I change my shipping address after ordering?", "You can update your address if the order is not yet packed. Contact support immediately with your order number."),
                    FAQItem("How do I track my order?", "Go to your profile order history and open the order details page to see courier updates and tracking number."),
                    FAQItem("What if my order is lost in transit?", "If tracking is stalled, we will investigate with the courier and either resend your order or process a full refund.")
                )
            ),
            FAQCategory(
                id = "grooming",
                label = "Grooming Services",
                items = listOf(
                    FAQItem("How do I book a grooming appointment?", "Open the Services page, select your package, choose a date and time, then confirm your booking."),
                    FAQItem("What grooming packages are available?", "We offer Full Grooming, Bath & Dry, Nail Trim, Ear Cleaning, and optional wellness add-ons."),
                    FAQItem("Can I reschedule my appointment?", "Yes, appointments can be rescheduled up to 24 hours before your slot through your Account Appointments section."),
                    FAQItem("What should I bring to the grooming session?", "Bring your pet with a leash or carrier, updated vaccination details, and any grooming preferences you have.")
                )
            ),
            FAQCategory(
                id = "returns",
                label = "Returns & Refunds",
                items = listOf(
                    FAQItem("What is your return policy?", "Unused items in original packaging can be returned within 7 days of delivery, subject to inspection."),
                    FAQItem("How do I initiate a return?", "Submit a return request from your order history or contact support with photos and reason for return."),
                    FAQItem("When will I receive my refund?", "Refunds are processed within 3–7 business days after your return is approved and item quality is verified.")
                )
            ),
            FAQCategory(
                id = "payments",
                label = "Payments",
                items = listOf(
                    FAQItem("What payment methods are accepted?", "We accept major credit/debit cards, select e-wallets, and cash on delivery for eligible locations."),
                    FAQItem("How do loyalty points work?", "You earn points with every purchase. Every 10 points gives you a ₱50 discount redeemable at checkout."),
                    FAQItem("Why was my payment declined?", "Declines can happen due to insufficient funds, expired card, or bank security flags. Try another method or contact your bank.")
                )
            ),
            FAQCategory(
                id = "account",
                label = "Account & Profile",
                items = listOf(
                    FAQItem("How do I change my password?", "Go to Profile → Change Password, enter your new password twice, and click Update Password."),
                    FAQItem("How do I update my profile photo?", "Click the camera icon on your profile avatar, upload a new photo, and click Save."),
                    FAQItem("Can I add multiple delivery addresses?", "Yes. Go to Profile → Addresses and click Add New to save multiple addresses and set a default.")
                )
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("orders") }
    var searchQuery by remember { mutableStateOf("") }
    var expandedFaqIndex by remember { mutableStateOf<Int?>(null) }

    // Contact form fields
    var contactName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactTopic by remember { mutableStateOf("") }
    var contactMessage by remember { mutableStateOf("") }
    var isFormSent by remember { mutableStateOf(false) }

    // Pre-populate email if logged in
    LaunchedEffect(Unit) {
        val user = NetworkUtils.client.auth.currentUserOrNull()
        if (user != null) {
            contactEmail = user.email ?: ""
        }
    }

    val activeCategory = faqData.find { it.id == selectedCategory }
    val filteredFaqs = activeCategory?.items?.filter {
        it.question.contains(searchQuery, ignoreCase = true) ||
        it.answer.contains(searchQuery, ignoreCase = true)
    } ?: emptyList()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Help & Support", fontWeight = FontWeight.Black, color = BrandDark, fontSize = 20.sp) },
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Hero Banner
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = Color(0xFFFFB74D), // Soft Orange
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "How can we help?",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Find answers to common questions or message support.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Search bar
                            ZootopiaTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = "Search FAQs...",
                                icon = Icons.Default.Search,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Category selection row
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(faqData) { category ->
                            val isSelected = selectedCategory == category.id
                            val containerColor = if (isSelected) ZootopiaPrimary else Color.White.copy(alpha = 0.75f)
                            val contentColor = if (isSelected) Color.White else BrandDark

                            Surface(
                                modifier = Modifier.clickable { 
                                    selectedCategory = category.id
                                    expandedFaqIndex = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = containerColor,
                                border = BorderStroke(1.dp, if (isSelected) ZootopiaPrimary else Color.LightGray.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = category.label,
                                    color = contentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                // FAQ Title
                item {
                    Text(
                        "Frequently Asked Questions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandDark
                    )
                }

                // FAQ Items List
                if (filteredFaqs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = Color.Gray.copy(alpha = 0.4f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No matching FAQs found",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredFaqs.size) { index ->
                        val item = filteredFaqs[index]
                        val isExpanded = expandedFaqIndex == index
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedFaqIndex = if (isExpanded) null else index }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.question,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BrandDark,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.answer,
                                        fontSize = 13.sp,
                                        color = BrandDark.copy(alpha = 0.75f),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Contact Us Card Form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Contact Support",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = BrandDark
                            )
                            Text(
                                "SEND US A MESSAGE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (isFormSent) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Message Sent!",
                                        fontWeight = FontWeight.Bold,
                                        color = BrandDark,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        "We will respond within 24 hours.",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { isFormSent = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Send another message", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            } else {
                                ZootopiaTextField(
                                    value = contactName,
                                    onValueChange = { contactName = it },
                                    label = "Your Name",
                                    icon = Icons.Default.Person,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                                ZootopiaTextField(
                                    value = contactEmail,
                                    onValueChange = { contactEmail = it },
                                    label = "Email Address",
                                    icon = Icons.Default.Email,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                                ZootopiaTextField(
                                    value = contactTopic,
                                    onValueChange = { contactTopic = it },
                                    label = "Topic / Question",
                                    icon = Icons.Default.QuestionAnswer,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                                ZootopiaTextField(
                                    value = contactMessage,
                                    onValueChange = { contactMessage = it },
                                    label = "Describe your issue in detail...",
                                    icon = Icons.Default.Message,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (contactName.isBlank() || contactEmail.isBlank() || contactTopic.isBlank() || contactMessage.isBlank()) {
                                            coroutineScope.launch { snackbarHostState.showSnackbar("All fields are required.") }
                                        } else {
                                            isFormSent = true
                                            contactName = ""
                                            contactTopic = ""
                                            contactMessage = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandDark),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Text("Send Message", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Support Hours Panel
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Support Hours",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            val hours = listOf(
                                "Mon – Fri" to "8:00 AM – 8:00 PM",
                                "Saturday" to "9:00 AM – 6:00 PM",
                                "Sunday" to "10:00 AM – 4:00 PM"
                            )
                            hours.forEach { (days, hrs) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(days, fontSize = 13.sp, color = BrandDark.copy(alpha = 0.6f))
                                    Text(hrs, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
