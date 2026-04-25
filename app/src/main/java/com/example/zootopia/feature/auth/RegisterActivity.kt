package com.example.zootopia.feature.auth

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zootopia.core.theme.BrandDark

@Composable
fun RegisterActivity(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    presenter: RegisterPresenter = viewModel()
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val state by presenter.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error, state.isSuccess) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            presenter.clearError()
        }
        if (state.isSuccess) {
            onRegisterSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            // Branding
            Box(
                modifier = Modifier.fillMaxWidth().background(BrandDark).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.1f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Pets, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ZOOTOPIA", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
            }

            // Form
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Create your account", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                Text("Enter your details to get started.", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

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

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { 
                        if (passwordInput == confirmPassword) {
                            presenter.signUp(emailInput, passwordInput, firstName, lastName, username)
                        } else {
                            // Local error handling
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005696)),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("Already have an account? ", color = Color.Gray)
                    Text("Log in", color = Color(0xFF005696), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToLogin() })
                }
            }
        }
    }
}
