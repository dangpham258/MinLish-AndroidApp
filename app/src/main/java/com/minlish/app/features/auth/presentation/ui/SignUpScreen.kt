package com.minlish.app.features.auth.presentation.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minlish.app.features.auth.presentation.viewmodel.AuthViewModel
import com.minlish.app.ui_components.theme.*
import kotlinx.coroutines.flow.collectLatest

private const val CORNER_RADIUS_VALUE = 16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnSuccessListener { account ->
                val idToken = account.idToken
                viewModel.onGoogleSignInResult(idToken,
                    onSuccess = { onSignUpSuccess() },
                    onError = { }
                )
            }
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AuthViewModel.UiEvent.AuthSuccess -> {
                    onSignUpSuccess()
                }
                is AuthViewModel.UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Bunny English",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8FF), Color(0xFFF4F2FF))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Tạo tài khoản mới",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.W800,
                        color = OnSurfaceText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bắt đầu hành trình chinh phục tiếng Anh cùng Bunny ngay hôm nay!",
                        fontSize = 15.sp,
                        color = OnSurfaceVariant
                    )
                }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-24).dp)
                    ) {
                        Text(
                            text = "🐰",
                            fontSize = 48.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Họ và tên",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = { Text("Nguyễn Văn A", color = OutlineVariant) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = OnSurfaceVariant)
                        },
                        shape = RoundedCornerShape(CORNER_RADIUS_VALUE.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = OutlineVariant,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                Column {
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("email@vi-du.com", color = OutlineVariant) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = OnSurfaceVariant)
                        },
                        shape = RoundedCornerShape(CORNER_RADIUS_VALUE.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = OutlineVariant,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                Column {
                    Text(
                        text = "Mật khẩu",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", color = OutlineVariant) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = OnSurfaceVariant)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Ẩn/hiển thị mật khẩu",
                                    tint = OnSurfaceVariant
                                )
                            }
                        },
                        shape = RoundedCornerShape(CORNER_RADIUS_VALUE.dp),
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = OutlineVariant,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                Column {
                    Text(
                        text = "Xác nhận mật khẩu",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("••••••••", color = OutlineVariant) },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OnSurfaceVariant)
                        },
                        shape = RoundedCornerShape(CORNER_RADIUS_VALUE.dp),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = OutlineVariant,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreeToTerms,
                        onCheckedChange = { agreeToTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tôi đồng ý với Điều khoản sử dụng và Chính sách bảo mật của Bunny English.",
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }

                Button(
                    onClick = {
                        if (!agreeToTerms) {
                            return@Button
                        }
                        if (password != confirmPassword) {
                            return@Button
                        }
                        if (fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                            viewModel.onSignUp(fullName, email, password)
                        }
                    },
                    enabled = !isLoading && agreeToTerms && fullName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && password == confirmPassword,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(CORNER_RADIUS_VALUE.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đăng ký tài khoản",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                    Text(
                        text = "HOẶC",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = OutlineVariant)
                }

                OutlinedButton(
                    onClick = {
                        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                        )
                            .requestIdToken("607388926260-net4bpfn8pof9tv8l72q80e67d3g0unk.apps.googleusercontent.com")
                            .requestEmail()
                            .build()

                        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(CORNER_RADIUS_VALUE.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceVariant),
                    border = BorderStroke(1.5.dp, OutlineVariant)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Đăng ký với Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "Đã có tài khoản? Đăng nhập",
                    fontSize = 14.sp,
                    color = OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen()
}
