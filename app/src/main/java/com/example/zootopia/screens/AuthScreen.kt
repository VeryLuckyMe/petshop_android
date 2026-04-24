package com.example.zootopia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zootopia.ui.theme.BrandDark
import com.example.zootopia.ui.theme.BrandMedium
import com.example.zootopia.ui.theme.ZootopiaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var isSignUp by remember { mutableStateOf(false) }
    
    // Form States
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // --- Branding Section ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandDark)
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Paw Icon in Circle
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "ZOOTOPIA",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "ANIMAL CARE",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    letterSpacing = 4.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = if (isSignUp) 
                        "Professional Care for Your Best Friends." 
                    else 
                        "\"Professional care for every creature, great and small. Dedicated to the well-being of your animal companions.\"",
                    color = Color.White,
                    fontSize = if (isSignUp) 24.sp else 14.sp,
                    fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                if (isSignUp) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SignUpCard(Modifier.weight(1f), Icons.Default.MedicalServices, "Health Tracking")
                        SignUpCard(Modifier.weight(1f), Icons.Default.CalendarToday, "Easy Scheduling")
                    }
                }
            }
        }

        // --- Form Section ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = if (isSignUp) "Create your account" else "Login",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = BrandDark
            )
            Text(
                text = if (isSignUp) 
                    "Enter your details to get started with Zootopia Animal Care."
                else 
                    "Welcome back! Please enter your details.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (isSignUp) {
                // Two column names
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ZootopiaTextField(Modifier.weight(1f), firstName, { firstName = it }, "First Name", Icons.Default.Person)
                    ZootopiaTextField(Modifier.weight(1f), lastName, { lastName = it }, "Last Name", null)
                }
                Spacer(modifier = Modifier.height(16.dp))
                ZootopiaTextField(Modifier.fillMaxWidth(), username, { username = it }, "Username", Icons.Default.PersonOutline)
                Spacer(modifier = Modifier.height(16.dp))
            }

            ZootopiaTextField(Modifier.fillMaxWidth(), email, { email = it }, "Email Address", Icons.Default.Email)
            
            Spacer(modifier = Modifier.height(16.dp))

            if (isSignUp) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ZootopiaTextField(Modifier.weight(1f), password, { password = it }, "Password", Icons.Default.Lock, isPassword = true)
                    ZootopiaTextField(Modifier.weight(1f), confirmPassword, { confirmPassword = it }, "Confirm Password", Icons.Default.History, isPassword = true)
                }
            } else {
                ZootopiaTextField(Modifier.fillMaxWidth(), password, { password = it }, "Password", Icons.Default.Lock, isPassword = true)
            }

            if (!isSignUp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = true, onCheckedChange = {})
                        Text("Remember for 30 days", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text("Forgot Password?", fontSize = 12.sp, color = BrandMedium, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = true, onCheckedChange = {})
                    Text("I agree to the Terms of Service and Privacy Policy", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onLoginSuccess() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005696))
            ) {
                Text(
                    text = if (isSignUp) "Create Account" else "LOGIN",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(
                    text = if (isSignUp) "Already have an account? " else "Don't have an account? ",
                    color = Color.Gray
                )
                Text(
                    text = if (isSignUp) "Log in" else "Sign Up",
                    color = Color(0xFF005696),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { isSignUp = !isSignUp }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZootopiaTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector?,
    isPassword: Boolean = false
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandDark, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(label, color = Color.LightGray) },
            leadingIcon = if (icon != null) { { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = BrandMedium) } } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedBorderColor = BrandMedium
            )
        )
    }
}

@Composable
fun SignUpCard(modifier: Modifier, icon: ImageVector, text: String) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
