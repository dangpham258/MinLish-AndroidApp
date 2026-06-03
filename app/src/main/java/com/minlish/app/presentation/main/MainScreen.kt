package com.minlish.app.presentation.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.minlish.app.data.UserSession
import com.minlish.app.presentation.common.component.BunnyBottomNavBar
import com.minlish.app.presentation.common.component.BunnyTab
import com.minlish.app.presentation.deck.DeckViewModel
import com.minlish.app.presentation.deck.ListOfDeckScreen
import com.minlish.app.presentation.profile.ProfileScreen
import com.minlish.app.presentation.profile.ProfileViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val userSession: UserSession
) : ViewModel()

@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel = hiltViewModel()
) {
    var currentTab by remember { mutableStateOf(BunnyTab.LESSONS) }
    val deckViewModel: DeckViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            BunnyBottomNavBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        when (currentTab) {
            BunnyTab.LESSONS -> {
                ListOfDeckScreen(
                    viewModel = deckViewModel,
                    onNavigateToDeckDetail = { deckId ->
                        navController.navigate("deck_detail/$deckId")
                    },
                    onNavigateToCreateDeck = {
                        navController.navigate("create_deck")
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            BunnyTab.STATS -> {
                StatsContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            BunnyTab.PROFILE -> {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun StatsContent(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier,
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text("Thống kê - Sắp ra mắt!")
    }
}
