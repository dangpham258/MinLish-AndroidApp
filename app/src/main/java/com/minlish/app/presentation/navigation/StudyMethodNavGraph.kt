package com.minlish.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.minlish.app.core.di.ServiceLocator
import com.minlish.app.presentation.deck.DeckDetailScreen
import com.minlish.app.presentation.deck.DeckViewModel
import com.minlish.app.presentation.deck.DeckViewModelFactory
import com.minlish.app.presentation.learn.*

@Composable
fun StudyMethodNavGraph(navController: NavHostController) {
    val repository = ServiceLocator.getRepository(LocalContext.current)
    
    // Ensure initial data is loaded
    androidx.compose.runtime.LaunchedEffect(Unit) {
        repository.prepopulateInitialData()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.DeckDetail.route
    ) {
        composable(
            route = Screen.DeckDetail.route,
            arguments = listOf(navArgument("deckId") {
                type = NavType.StringType
                defaultValue = "1"
            })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: "1"
            val deckViewModel: DeckViewModel = viewModel(
                factory = DeckViewModelFactory(repository)
            )
            DeckDetailScreen(
                viewModel = deckViewModel,
                deckId = deckId,
                onNavigateBack = { /* Handle back or exit app */ },
                onNavigateToAddWord = { /* Navigate to add word */ },
                onNavigateToUpdateWord = { _, _ -> /* Navigate to update word */ },
                onNavigateToFlashcard = { dId -> 
                    navController.navigate(Screen.Flashcard.createRoute(dId)) 
                },
                onNavigateToSRS = { dId ->
                    navController.navigate(Screen.SRS.createRoute(dId))
                },
                onNavigateToContext = { dId -> 
                    navController.navigate(Screen.ContextLearning.createRoute(dId)) 
                }
            )
        }

        composable(
            route = Screen.Flashcard.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: "1"
            val learnViewModel: LearnViewModel = viewModel(
                factory = LearnViewModelFactory(repository)
            )
            
            // Reset progress and select deck
            androidx.compose.runtime.LaunchedEffect(deckId) {
                learnViewModel.resetProgress()
                repository.getDeckById(deckId.toIntOrNull() ?: 1)?.let {
                    learnViewModel.selectDeck(it)
                }
            }

            LearnScreen(
                viewModel = learnViewModel,
                mode = LearnMode.FLASHCARD,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SRS.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: "1"
            val learnViewModel: LearnViewModel = viewModel(
                factory = LearnViewModelFactory(repository)
            )

            // Reset progress and select deck
            androidx.compose.runtime.LaunchedEffect(deckId) {
                learnViewModel.resetProgress()
                repository.getDeckById(deckId.toIntOrNull() ?: 1)?.let {
                    learnViewModel.selectDeck(it)
                }
            }

            LearnScreen(
                viewModel = learnViewModel,
                mode = LearnMode.SRS,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ContextLearning.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: "1"
            val learnViewModel: LearnViewModel = viewModel(
                factory = LearnViewModelFactory(repository)
            )
            
            // Reset progress and select deck
            androidx.compose.runtime.LaunchedEffect(deckId) {
                learnViewModel.resetProgress()
                repository.getDeckById(deckId.toIntOrNull() ?: 1)?.let {
                    learnViewModel.selectDeck(it)
                }
            }

            ContextScreen(
                viewModel = learnViewModel,
                deckId = deckId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
