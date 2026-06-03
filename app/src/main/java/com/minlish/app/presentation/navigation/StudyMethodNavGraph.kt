package com.minlish.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.minlish.app.presentation.deck.DeckDetailScreen
import com.minlish.app.presentation.deck.DeckViewModel
import com.minlish.app.presentation.deck.ListOfDeckScreen
import com.minlish.app.presentation.learn.*
import androidx.navigation.NavGraphBuilder

fun NavGraphBuilder.studyMethodNavGraph(navController: NavHostController) {


        composable(
            route = Screen.Flashcard.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            val learnViewModel: LearnViewModel = hiltViewModel()

            // Đồng bộ cấu trúc dữ liệu mới bằng cách đẩy thẳng dữ liệu lấy từ Firebase Node
            // thông qua hàm điều phối selectDeckById của LearnViewModel
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

            // Khởi động thiết lập trạng thái ôn tập Spaced Repetition an toàn
            LaunchedEffect(deckId) {
                learnViewModel.selectDeckById(deckId)
            }

            LearnScreen(
                viewModel = learnViewModel,
                mode = LearnMode.SRS,
                onNavigateBack = {
                    if (deckId == "all_due") {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
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