package com.minlish.app.presentation.navigation

sealed class Screen(val route: String) {
    object ListOfDeck : Screen("list_of_deck")

    object DeckDetail : Screen("deck_detail/{deckId}") {
        fun createRoute(deckId: String) = "deck_detail/$deckId"
    }
    object Flashcard : Screen("flashcard/{deckId}") {
        fun createRoute(deckId: String) = "flashcard/$deckId"
    }
    object SRS : Screen("srs/{deckId}") {
        fun createRoute(deckId: String) = "srs/$deckId"
    }
    object ContextLearning : Screen("context/{deckId}") {
        fun createRoute(deckId: String) = "context/$deckId"
    }
}
