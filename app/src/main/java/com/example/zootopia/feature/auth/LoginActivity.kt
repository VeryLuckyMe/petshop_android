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
fun LoginActivity(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    presenter: LoginPresenter = viewModel()
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    val state by presenter.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error, state.isSuccess) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            presenter.clearError()
        }
        if (state.isSuccess) {
            onLoginSuccess()
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
                Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BrandDark)
                Text("Welcome back! Please enter your details.", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

                ZootopiaTextField(Modifier.fillMaxWidth(), emailInput, { email -> emailInput = email }, "Email Address", Icons.Default.Email)
                Spacer(modifier = Modifier.height(16.dp))
                ZootopiaTextField(Modifier.fillMaxWidth(), passwordInput, { pass -> passwordInput = pass }, "Password", Icons.Default.Lock, isPassword = true)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { presenter.login(emailInput, passwordInput) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005696)),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("Don't have an account? ", color = Color.Gray)
                    Text("Sign Up", color = Color(0xFF005696), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToRegister() })
                }
            }
        }
    }
}
