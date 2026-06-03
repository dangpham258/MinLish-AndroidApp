package com.minlish.app.presentation.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.minlish.app.data.UserSession
import com.minlish.app.presentation.common.BunnyBottomNavBar
import com.minlish.app.presentation.common.BunnyTab
import com.minlish.app.presentation.navigation.Screen
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
    val userId by viewModel.userSession.currentUserId.collectAsState()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email

    Scaffold(
        bottomBar = {
            BunnyBottomNavBar(
                selectedTab = BunnyTab.PROFILE,
                onTabSelected = { tab ->
                    when (tab) {
                        BunnyTab.LESSONS -> {
                            navController.navigate(Screen.ListOfDeck.route) {
                                popUpTo(Screen.Main.route) { inclusive = true }
                            }
                        }
                        BunnyTab.STATS -> {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Main.route) { inclusive = true }
                            }
                        }
                        BunnyTab.PROFILE -> { /* Đã ở đây */ }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Trang chu MinLish",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "---",
                    fontSize = 14.sp
                )

                Text(
                    text = "User ID: ${userId ?: "Chua dang nhap"}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Email: ${userEmail ?: "Chua dang nhap"}",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
