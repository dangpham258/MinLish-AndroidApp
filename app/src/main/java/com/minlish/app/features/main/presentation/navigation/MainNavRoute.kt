package com.minlish.app.features.main.presentation.navigation

sealed class MainNavRoute(val route: String) {
    object Home : MainNavRoute("home")
    object Settings : MainNavRoute("settings")
    object Profile : MainNavRoute("profile")
}
