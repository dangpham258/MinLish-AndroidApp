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

fun NavGraphBuilder.deckNavGraph(navController: NavHostController) {
    composable(route = Screen.ListOfDeck.route) {
        val viewModel: DeckViewModel = hiltViewModel()
        ListOfDeckScreen(
            viewModel = viewModel,
            onNavigateToDeckDetail = { deckId ->
                navController.navigate(Screen.DeckDetail.createRoute(deckId))
            },
            onNavigateToCreateDeck = {
                navController.navigate(Screen.CreateDeck.route)
            }
        )
    }

    composable(route = Screen.CreateDeck.route) {
        val viewModel: DeckViewModel = hiltViewModel()
        CreateNewDeckScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.DeckDetail.route,
        arguments = listOf(navArgument("deckId") { type = NavType.StringType })
    ) { backStackEntry ->
        val deckId = backStackEntry.arguments?.getString("deckId") ?: return@composable
        val viewModel: DeckViewModel = hiltViewModel()

        DeckDetailScreen(
            viewModel = viewModel,
            deckId = deckId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAddWord = { id ->
                navController.navigate(Screen.AddUpdateWord.createRoute(id))
            },
            onNavigateToUpdateWord = { id, wordId ->
                navController.navigate(Screen.AddUpdateWord.createRoute(id, wordId))
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
        route = Screen.AddUpdateWord.route,
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
