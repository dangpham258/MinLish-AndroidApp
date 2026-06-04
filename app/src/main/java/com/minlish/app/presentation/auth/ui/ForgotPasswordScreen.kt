package com.minlish.app.presentation.auth.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minlish.app.presentation.auth.viewmodel.AuthViewModel
import com.minlish.app.presentation.theme.*
import kotlinx.coroutines.launch

private const val CORNER_RADIUS = 16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onResetSent: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var isEmailSent by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun validateEmail(target: String): Boolean {
        val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        return when {
            target.isBlank() -> {
                emailError = "Please enter your email"
                false
            }
            !target.matches(emailPattern) -> {
                emailError = "Invalid email format"
                false
            }
            else -> {
                emailError = null
                true
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bunny English",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryBlue
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryLightContainer)
                            .border(1.5.dp, PrimaryBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐰", fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(PrimaryLightContainer.copy(alpha = 0.3f), Color.Transparent)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isEmailSent) Icons.Default.CheckCircle else Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = PrimaryBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEmailSent) {
                // ===== SUCCESS STATE =====
                Text(
                    text = "Email sent!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurfaceText,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "We've sent password reset instructions to\n$email",
                    fontSize = 15.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryLightContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Instructions:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Open the email from Bunny English\n2. Click the 'Reset Password' link\n3. Enter your new password on Firebase\n4. Sign in with your new password",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant,
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = {
                        isEmailSent = false
                        email = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(CORNER_RADIUS.dp)
                ) {
                    Text("Send to another email", color = PrimaryBlue)
                }
            } else {
                // ===== INPUT STATE =====
                Text(
                    text = "Forgot Password?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurfaceText,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Enter your registered email and we'll send you password reset instructions",
                    fontSize = 15.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // Email Input
                Text(
                    text = "Email",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) validateEmail(it)
                    },
                    placeholder = { Text("Enter your email", color = OutlineVariant) },
                    leadingIcon = { Icon(Icons.Default.Email, "Email", tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CORNER_RADIUS.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    isError = emailError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                AnimatedVisibility(
                    visible = emailError != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Text(
                        text = emailError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (validateEmail(email)) {
                            scope.launch {
                                val result = viewModel.sendPasswordResetEmail(email)
                                if (result.isSuccess) {
                                    isEmailSent = true
                                } else {
                                    snackbarHostState.showSnackbar(
                                        result.exceptionOrNull()?.message ?: "An error occurred"
                                    )
                                }
                            }
                        }
                    },
                    enabled = !isLoading && email.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(CORNER_RADIUS.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Send Instructions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnPrimary
                        )
                    }
                }
            }
        }
    }
}
