package com.minlish.app.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.minlish.app.presentation.deck.ui.AddUpdateWordScreen
import com.minlish.app.presentation.deck.viewmodel.CreateDeckViewModel
import com.minlish.app.presentation.deck.ui.CreateNewDeckScreen
import com.minlish.app.presentation.deck.ui.DeckDetailScreen
import com.minlish.app.presentation.deck.viewmodel.DeckDetailViewModel
import com.minlish.app.presentation.deck.viewmodel.DeckListViewModel
import com.minlish.app.presentation.deck.ui.ListOfDeckScreen
import com.minlish.app.presentation.deck.viewmodel.WordEditorViewModel

const val DECK_GRAPH_ROUTE = "deck_graph"


fun NavGraphBuilder.deckNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.ListOfDeck.route,
        route = DECK_GRAPH_ROUTE
    ) {
        composable(route = Screen.ListOfDeck.route) {
            val viewModel: DeckListViewModel = hiltViewModel()
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
            val viewModel: CreateDeckViewModel = hiltViewModel()
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
            val viewModel: DeckDetailViewModel = hiltViewModel()

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
            val viewModel: WordEditorViewModel = hiltViewModel()

            AddUpdateWordScreen(
                viewModel = viewModel,
                deckId = deckId,
                wordId = wordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
