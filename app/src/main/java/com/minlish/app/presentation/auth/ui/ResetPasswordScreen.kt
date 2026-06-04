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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
fun ResetPasswordScreen(
    oobCode: String,
    onResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = PrimaryBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isSuccess) {
                // ===== SUCCESS STATE =====
                Text(
                    text = "Password reset successful!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurfaceText,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You can now sign in with your new password",
                    fontSize = 15.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onResetSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(CORNER_RADIUS.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnPrimary
                    )
                }
            } else {
                // ===== PASSWORD RESET FORM =====
                Text(
                    text = "Set New Password",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurfaceText,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Enter a new password for your account",
                    fontSize = 15.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // Password
                Text(
                    text = "New Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    placeholder = { Text("Enter new password", color = OutlineVariant) },
                    leadingIcon = { Icon(Icons.Default.Lock, "Lock", tint = PrimaryBlue) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = OnSurfaceVariant
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CORNER_RADIUS.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password
                Text(
                    text = "Confirm Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        passwordError = null
                    },
                    placeholder = { Text("Re-enter your password", color = OutlineVariant) },
                    leadingIcon = { Icon(Icons.Default.Lock, "Lock", tint = PrimaryBlue) },
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                            Icon(
                                if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle password visibility",
                                tint = OnSurfaceVariant
                            )
                        }
                    },
                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(CORNER_RADIUS.dp),
                    singleLine = true,
                    isError = passwordError != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = OutlineVariant,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                AnimatedVisibility(
                    visible = passwordError != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Text(
                        text = passwordError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        when {
                            password.length < 6 -> {
                                passwordError = "Password must be at least 6 characters"
                            }
                            password != confirmPassword -> {
                                passwordError = "Passwords do not match"
                            }
                            else -> {
                                scope.launch {
                                    val result = viewModel.confirmPasswordReset(oobCode, password)
                                    if (result.isSuccess) {
                                        isSuccess = true
                                    } else {
                                        passwordError = result.exceptionOrNull()?.message ?: "An error occurred"
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading && password.isNotEmpty() && confirmPassword.isNotEmpty(),
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
                            text = "Reset Password",
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
