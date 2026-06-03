package com.minlish.app.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minlish.app.presentation.common.component.BunnyAppBar
import com.minlish.app.presentation.common.component.BunnyBottomNavBar
import com.minlish.app.presentation.common.component.BunnyTab
import com.minlish.app.presentation.profile.ProfileScreen
import com.minlish.app.presentation.profile.ProfileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent() {
    var currentTab by remember { mutableStateOf(BunnyTab.PROFILE) } 
    val profileViewModel: ProfileViewModel = viewModel()

    Scaffold(
        topBar = {
            BunnyAppBar(
                title = "Bunny English",
                onBackClick = { /* Xử lý khi nhấn nút quay lại */ }
            )
        },
        bottomBar = {
            BunnyBottomNavBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                BunnyTab.PROFILE -> {
                    ProfileScreen(viewModel = profileViewModel)
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bạn đang ở màn hình: ${currentTab.title}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
