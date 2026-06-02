package com.minlish.app.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.minlish.app.presentation.navigation.DeckRoute
import com.minlish.app.presentation.navigation.deckNavGraph
import androidx.compose.runtime.Composable
import com.minlish.app.presentation.navigation.StudyMethodNavGraph
import com.minlish.app.ui.theme.MinLishAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MinLishAppTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = DeckRoute.DeckList.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    deckNavGraph(navController)
                }
            }
        }
    }
}

@Composable
fun MinLishApp() {
    val navController = rememberNavController()
    StudyMethodNavGraph(navController = navController)
}
