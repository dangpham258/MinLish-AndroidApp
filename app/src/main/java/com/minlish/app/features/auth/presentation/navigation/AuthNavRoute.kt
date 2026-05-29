package com.minlish.app.features.auth.presentation.navigation

sealed class AuthNavRoute(val route: String) {
    object Login : AuthNavRoute("login")
    object SignUp : AuthNavRoute("signup")
    object ForgotPassword : AuthNavRoute("forgot_password")
    object ResetPassword : AuthNavRoute("reset_password/{oobCode}") {
        fun createRoute(oobCode: String): String = "reset_password/$oobCode"
    }
}
