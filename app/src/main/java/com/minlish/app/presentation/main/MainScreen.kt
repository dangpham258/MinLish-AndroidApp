package com.minlish.app.presentation.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.minlish.app.data.UserSession
import com.minlish.app.presentation.common.BunnyBottomNavBar
import com.minlish.app.presentation.common.BunnyTab
import com.minlish.app.presentation.dashboard.ui.BunnyStatisticsScreen
import com.minlish.app.presentation.dashboard.viewmodel.StatisticsViewModel
import com.minlish.app.presentation.deck.viewmodel.DeckListViewModel
import com.minlish.app.presentation.deck.ui.ListOfDeckScreen
import com.minlish.app.presentation.navigation.Screen
import com.minlish.app.presentation.profile.ProfileScreen
import com.minlish.app.presentation.profile.ProfileViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val userSession: UserSession
) : ViewModel()

/**
 * Saver cho BunnyTab enum — cần thiết để rememberSaveable hoạt động với kiểu enum.
 * Lưu dưới dạng String (tên enum), khôi phục bằng valueOf().
 */
@Stable
private val BunnyTabSaver = Saver<BunnyTab, String>(
    save = { it.name },
    restore = { name -> BunnyTab.entries.firstOrNull { it.name == name } ?: BunnyTab.STATS }
)

// HomeScaffold — Wrapper duy nhất quản lý bottom navigation bar.
@Composable
fun HomeScaffold(
    navController: NavHostController,
    startTab: BunnyTab = BunnyTab.STATS,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    var currentTab by rememberSaveable(stateSaver = BunnyTabSaver) {
        mutableStateOf(startTab)
    }

    val deckViewModel: DeckListViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val statsViewModel: StatisticsViewModel = hiltViewModel()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    androidx.compose.runtime.LaunchedEffect(currentRoute) {
        if (currentRoute == Screen.Home.route || currentRoute == Screen.Main.route) {
            deckViewModel.loadDecks()
        }
    }

    Scaffold(
        bottomBar = {
            BunnyBottomNavBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentTab) {
            BunnyTab.STATS -> {
                BunnyStatisticsScreen(
                    viewModel = statsViewModel,
                    modifier = contentModifier,
                    onNavigateToSRS = { deckId ->
                        navController.navigate(Screen.SRS.createRoute(deckId))
                    }
                )
            }

            BunnyTab.LESSONS -> {
                ListOfDeckScreen(
                    viewModel = deckViewModel,
                    modifier = contentModifier,
                    onNavigateToDeckDetail = { deckId ->
                        navController.navigate(Screen.DeckDetail.createRoute(deckId))
                    },
                    onNavigateToCreateDeck = {
                        navController.navigate(Screen.CreateDeck.route)
                    }
                )
            }

            BunnyTab.PROFILE -> {
                ProfileScreen(
                    viewModel = profileViewModel,
                    modifier = contentModifier,
                    onLogout = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

// Alias giữ backward compat với NavGraph cũ (gọi MainScreen)
@Composable
fun MainScreen(
    navController: NavHostController,
    startTab: BunnyTab = BunnyTab.STATS
) {
    HomeScaffold(navController = navController, startTab = startTab)
}
