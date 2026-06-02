package com.minlish.app.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.minlish.app.presentation.deck.AddUpdateWordScreen
import com.minlish.app.presentation.deck.CreateNewDeckScreen
import com.minlish.app.presentation.deck.DeckDetailScreen
import com.minlish.app.presentation.deck.DeckViewModel
import com.minlish.app.presentation.deck.ListOfDeckScreen

sealed class DeckRoute(val route: String) {
    object DeckList : DeckRoute("deck_list")
    object CreateDeck : DeckRoute("create_deck")
    object DeckDetail : DeckRoute("deck_detail/{deckId}") {
        fun createRoute(deckId: String) = "deck_detail/$deckId"
    }
    object AddUpdateWord : DeckRoute("add_update_word/{deckId}?wordId={wordId}") {
        fun createRoute(deckId: String, wordId: String? = null): String {
            return if (wordId != null) {
                "add_update_word/$deckId?wordId=$wordId"
            } else {
                "add_update_word/$deckId"
            }
        }
    }
}

fun NavGraphBuilder.deckNavGraph(navController: NavHostController) {
    composable(route = DeckRoute.DeckList.route) {
        val viewModel: DeckViewModel = hiltViewModel()
        ListOfDeckScreen(
            viewModel = viewModel,
            onNavigateToDeckDetail = { deckId ->
                navController.navigate(DeckRoute.DeckDetail.createRoute(deckId))
            },
            onNavigateToCreateDeck = {
                navController.navigate(DeckRoute.CreateDeck.route)
            }
        )
    }

    composable(route = DeckRoute.CreateDeck.route) {
        val viewModel: DeckViewModel = hiltViewModel()
        CreateNewDeckScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = DeckRoute.DeckDetail.route,
        arguments = listOf(navArgument("deckId") { type = NavType.StringType })
    ) { backStackEntry ->
        val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
        val viewModel: DeckViewModel = hiltViewModel()

        DeckDetailScreen(
            viewModel = viewModel,
            deckId = deckId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAddWord = { id ->
                navController.navigate(DeckRoute.AddUpdateWord.createRoute(id))
            },
            onNavigateToUpdateWord = { id, wordId ->
                navController.navigate(DeckRoute.AddUpdateWord.createRoute(id, wordId))
            },
            onNavigateToFlashcard = { id ->
                navController.navigate(Screen.Flashcard.createRoute(id))
            },
            onNavigateToSRS = { id ->
                navController.navigate(Screen.SRS.createRoute(id))
            },
            onNavigateToContext = { id ->
                navController.navigate(Screen.ContextLearning.createRoute(id))
            }
        )
    }

    composable(
        route = DeckRoute.AddUpdateWord.route,
        arguments = listOf(
            navArgument("deckId") { type = NavType.StringType },
            navArgument("wordId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
        val wordId = backStackEntry.arguments?.getString("wordId")
        val viewModel: DeckViewModel = hiltViewModel()

        AddUpdateWordScreen(
            viewModel = viewModel,
            deckId = deckId,
            wordId = wordId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
