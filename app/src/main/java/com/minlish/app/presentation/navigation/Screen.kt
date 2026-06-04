package com.minlish.app.presentation.navigation

sealed class Screen(val route: String) {
    // Auth flow
    object Auth : Screen("auth")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{oobCode}") {
        fun createRoute(oobCode: String) = "reset_password/$oobCode"
    }

    // Home (bottom nav graph — chứa 3 tab chính)
    object Home : Screen("home")

    // 3 Tab chính trong bottom nav
    object Dashboard : Screen("dashboard")
    object ListOfDeck : Screen("list_of_deck")
    object Profile : Screen("profile")

    // Detail screens (không có bottom bar)
    object CreateDeck : Screen("create_deck")
    object DeckDetail : Screen("deck_detail/{deckId}") {
        fun createRoute(deckId: String) = "deck_detail/$deckId"
    }
    object AddUpdateWord : Screen("add_update_word/{deckId}?wordId={wordId}") {
        fun createRoute(deckId: String, wordId: String? = null): String {
            return if (wordId != null) {
                "add_update_word/$deckId?wordId=$wordId"
            } else {
                "add_update_word/$deckId"
            }
        }
    }
    object Flashcard : Screen("flashcard/{deckId}") {
        fun createRoute(deckId: String) = "flashcard/$deckId"
    }
    object SRS : Screen("srs/{deckId}") {
        fun createRoute(deckId: String) = "srs/$deckId"
    }
    object ContextLearning : Screen("context_learning/{deckId}") {
        fun createRoute(deckId: String) = "context_learning/$deckId"
    }

    // Legacy — giữ lại để tránh crash nếu còn route cũ trong back stack
    object Main : Screen("main")
}
