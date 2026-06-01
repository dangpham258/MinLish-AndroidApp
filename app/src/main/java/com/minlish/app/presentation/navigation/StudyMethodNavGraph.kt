package com.minlish.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.minlish.app.presentation.deck.ListOfDeckScreen
import com.minlish.app.presentation.learn.*

@Composable
fun StudyMethodNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val repository = remember(context) { ServiceLocator.getRepository(context) }

    // Đảm bảo dữ liệu ban đầu được cấu hình sẵn sàng từ Firebase
    LaunchedEffect(Unit) {
        repository.prepopulateInitialData()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.ListOfDeck.route
    ) {
        composable(route = Screen.ListOfDeck.route) {
            val deckViewModel: DeckViewModel = viewModel(
                factory = DeckViewModelFactory(repository)
            )
            ListOfDeckScreen(
                viewModel = deckViewModel,
                onNavigateToDeckDetail = { deckId ->
                    navController.navigate(Screen.DeckDetail.createRoute(deckId))
                },
                onNavigateToCreateDeck = { /* Điều phối màn hình tạo bộ từ vựng */ }
            )
        }

        composable(
            route = Screen.DeckDetail.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            val deckViewModel: DeckViewModel = viewModel(
                factory = DeckViewModelFactory(repository)
            )
            DeckDetailScreen(
                viewModel = deckViewModel,
                deckId = deckId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddWord = { /* Điều hướng thêm từ vựng */ },
                onNavigateToUpdateWord = { _, _ -> /* Điều hướng chỉnh sửa từ vựng */ },
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
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            val learnViewModel: LearnViewModel = viewModel(
                factory = LearnViewModelFactory(repository)
            )

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
            val learnViewModel: LearnViewModel = viewModel(
                factory = LearnViewModelFactory(repository)
            )

            // Khởi động thiết lập trạng thái ôn tập Spaced Repetition an toàn
            LaunchedEffect(deckId) {
                learnViewModel.selectDeckById(deckId)
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
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            val learnViewModel: LearnViewModel = viewModel(
                factory = LearnViewModelFactory(repository)
            )

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
}