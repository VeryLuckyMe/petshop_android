package com.example.zootopia.feature.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zootopia.core.theme.BrandDark
import com.example.zootopia.core.theme.BrandMedium
import kotlinx.coroutines.launch

@Composable
fun RegisterActivity(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    presenter: RegisterPresenter = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeToTerms by remember { mutableStateOf(false) }

    val state by presenter.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error, state.isSuccess) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            presenter.clearError()
        }
        if (state.isSuccess) {
            Toast.makeText(context, "Success! Check your email to verify and log in.", Toast.LENGTH_LONG).show()
            onRegisterSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
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
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false
                    )
                    .background(
                        color = Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(28.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Branding Logo & Text
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = BrandDark
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ZOOTOPIA",
                        color = BrandDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create your account to get started.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ZootopiaTextField(Modifier.weight(1f), firstName, { value -> firstName = value }, "First Name", Icons.Default.Person)
                        ZootopiaTextField(Modifier.weight(1f), lastName, { value -> lastName = value }, "Last Name", null)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ZootopiaTextField(Modifier.fillMaxWidth(), username, { value -> username = value }, "Username", Icons.Default.PersonOutline)
                    Spacer(modifier = Modifier.height(16.dp))
                    ZootopiaTextField(Modifier.fillMaxWidth(), emailInput, { value -> emailInput = value }, "Email Address", Icons.Default.Email)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ZootopiaTextField(Modifier.weight(1f), passwordInput, { value -> passwordInput = value }, "Password", Icons.Default.Lock, isPassword = true)
                        ZootopiaTextField(Modifier.weight(1f), confirmPassword, { value -> confirmPassword = value }, "Confirm Password", Icons.Default.History, isPassword = true)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Terms of Service and Privacy Policy checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { agreeToTerms = !agreeToTerms },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = agreeToTerms,
                            onCheckedChange = { agreeToTerms = it },
                            colors = CheckboxDefaults.colors(checkedColor = BrandMedium)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "I agree to the Terms of Service & Privacy Policy",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (!agreeToTerms) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("You must agree to the Terms of Service and Privacy Policy.")
                                }
                            } else if (passwordInput != confirmPassword) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Passwords do not match!")
                                }
                            } else {
                                presenter.signUp(emailInput, passwordInput, firstName, lastName, username)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandMedium),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account? ", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "Log in",
                            color = BrandMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onNavigateToLogin() }
                                .padding(horizontal = 2.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
