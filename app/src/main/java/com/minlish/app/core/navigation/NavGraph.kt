package com.minlish.app.core.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.minlish.app.features.auth.presentation.ui.ForgotPasswordScreen
import com.minlish.app.features.auth.presentation.ui.LoginScreen
import com.minlish.app.features.auth.presentation.ui.ResetPasswordScreen
import com.minlish.app.features.auth.presentation.ui.SignUpScreen
import com.minlish.app.features.auth.presentation.viewmodel.AuthViewModel
import com.minlish.app.features.main.presentation.MainScreen

sealed class Screen(val route: String) {
    object SplashScreen : Screen("splash")
    object Auth : Screen("auth")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{oobCode}") {
        fun createRoute(oobCode: String): String {
            return "reset_password/$oobCode"
        }
    }
    object Main : Screen("main")
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel(),
    deepLinkIntent: Intent? = null
) {
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    // Xử lý deep link khi app được mở từ link reset password
    LaunchedEffect(deepLinkIntent) {
        deepLinkIntent?.data?.let { uri ->
            android.util.Log.d("NavGraph", "Deep link received: $uri")
            
            // Kiểm tra nếu là link reset password
            // Format: https://minlish.app/action?mode=resetPassword&oobCode=xxx&apiKey=xxx
            if (uri.path?.contains("resetPassword") == true || uri.host?.contains("google") == true) {
                val mode = uri.getQueryParameter("mode")
                val oobCode = uri.getQueryParameter("oobCode")
                
                if (mode == "resetPassword" && !oobCode.isNullOrEmpty()) {
                    navController.navigate(Screen.ResetPassword.createRoute(oobCode))
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        navigation(
            startDestination = Screen.Login.route,
            route = Screen.Auth.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onSignUpSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onResetSent = {
                        // Firebase sẽ gửi email
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.ResetPassword.route,
                arguments = listOf(
                    navArgument("oobCode") { type = NavType.StringType }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "https://minlish.app/?mode=resetPassword&oobCode={oobCode}"
                    },
                    navDeepLink {
                        uriPattern = "https://minlish-1e2ec.firebaseapp.com/?mode=resetPassword&oobCode={oobCode}"
                    },
                    navDeepLink {
                        uriPattern = "minlish://reset_password/{oobCode}"
                    }
                )
            ) { backStackEntry ->
                val oobCode = backStackEntry.arguments?.getString("oobCode") ?: ""
                ResetPasswordScreen(
                    oobCode = oobCode,
                    onResetSuccess = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.Main.route) {
            MainScreen()
        }
    }
}
