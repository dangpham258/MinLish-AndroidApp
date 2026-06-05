package com.minlish.app.presentation.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.minlish.app.presentation.learn.ContextScreen
import com.minlish.app.presentation.learn.LearnMode
import com.minlish.app.presentation.learn.LearnScreen
import com.minlish.app.presentation.learn.LearnViewModel


fun NavGraphBuilder.studyMethodNavGraph(navController: NavHostController) {

    composable(
        route = Screen.Flashcard.route,
        arguments = listOf(navArgument("deckId") { type = NavType.StringType })
    ) { backStackEntry ->
        val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
        val learnViewModel: LearnViewModel = hiltViewModel()

        LaunchedEffect(deckId) {
            learnViewModel.selectDeckById(deckId)
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
        val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
        val learnViewModel: LearnViewModel = hiltViewModel()

        LaunchedEffect(deckId) {
            learnViewModel.selectDeckById(deckId)
        }

        LearnScreen(
            viewModel = learnViewModel,
            mode = LearnMode.SRS,
            onNavigateBack = {
                if (deckId == "all_due") {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                } else {
                    navController.popBackStack()
                }
            }
        )
    }

    composable(
        route = Screen.ContextLearning.route,
        arguments = listOf(navArgument("deckId") { type = NavType.StringType })
    ) { backStackEntry ->
        val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
        val learnViewModel: LearnViewModel = hiltViewModel()

        // Đồng bộ dữ liệu real-world context từ tệp cấu trúc Firebase
        LaunchedEffect(deckId) {
            learnViewModel.selectDeckById(deckId)
        }

        ContextScreen(
            viewModel = learnViewModel,
            deckId = deckId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}