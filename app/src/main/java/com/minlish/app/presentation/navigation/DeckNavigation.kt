package com.minlish.app.presentation.navigation

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.minlish.app.presentation.deck.AddUpdateWordScreen
import com.minlish.app.presentation.deck.CreateNewDeckScreen
import com.minlish.app.presentation.deck.DeckDetailScreen
import com.minlish.app.presentation.deck.DeckViewModel
import com.minlish.app.presentation.deck.ListOfDeckScreen

const val DECK_GRAPH_ROUTE = "deck_graph"

fun NavGraphBuilder.deckNavGraph(navController: NavHostController) {
    // Bọc tất cả deck screens trong một nested navigation graph
    // để tất cả màn hình deck dùng chung một DeckViewModel instance.
    navigation(
        startDestination = Screen.ListOfDeck.route,
        route = DECK_GRAPH_ROUTE
    ) {
        composable(route = Screen.ListOfDeck.route) { backStackEntry ->
            // Lấy ViewModel từ NavBackStackEntry của graph cha (DECK_GRAPH_ROUTE)
            // để tất cả màn hình deck dùng chung một instance
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(DECK_GRAPH_ROUTE)
            }
            val viewModel: DeckViewModel = hiltViewModel(parentEntry)
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

        composable(route = Screen.CreateDeck.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(DECK_GRAPH_ROUTE)
            }
            val viewModel: DeckViewModel = hiltViewModel(parentEntry)
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
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(DECK_GRAPH_ROUTE)
            }
            val viewModel: DeckViewModel = hiltViewModel(parentEntry)

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
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(DECK_GRAPH_ROUTE)
            }
            val viewModel: DeckViewModel = hiltViewModel(parentEntry)

            AddUpdateWordScreen(
                viewModel = viewModel,
                deckId = deckId,
                wordId = wordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
