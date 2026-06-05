package com.minlish.app.presentation.navigation

import android.content.Intent
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
import com.minlish.app.presentation.auth.ui.ForgotPasswordScreen
import com.minlish.app.presentation.auth.ui.LoginScreen
import com.minlish.app.presentation.auth.ui.ResetPasswordScreen
import com.minlish.app.presentation.auth.ui.SignUpScreen
import com.minlish.app.presentation.auth.viewmodel.AuthViewModel
import com.minlish.app.presentation.main.MainScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel(),
    deepLinkIntent: Intent? = null
) {
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn == true) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Auth.route) { inclusive = true }
            }
        }
    }

    LaunchedEffect(deepLinkIntent) {
        deepLinkIntent?.data?.let { uri ->
            android.util.Log.d("NavGraph", "Deep link received: $uri")
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
        // AUTH GRAPH
        navigation(
            startDestination = Screen.Login.route,
            route = Screen.Auth.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.SignUp.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                )
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onResetSent = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ResetPassword.route,
                arguments = listOf(navArgument("oobCode") { type = NavType.StringType }),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "https://minlish.app/?mode=resetPassword&oobCode={oobCode}" },
                    navDeepLink { uriPattern = "https://minlish-1e2ec.firebaseapp.com/?mode=resetPassword&oobCode={oobCode}" },
                    navDeepLink { uriPattern = "minlish://reset_password/{oobCode}" }
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
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(route = Screen.Home.route) {
            MainScreen(navController = navController)
        }

        composable(route = Screen.Main.route) {
            MainScreen(navController = navController)
        }


        deckNavGraph(navController)

        studyMethodNavGraph(navController)
    }
}
