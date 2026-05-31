package com.minlish.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.minlish.app.presentation.navigation.DeckRoute
import com.minlish.app.presentation.navigation.deckNavGraph
import com.minlish.app.ui.theme.MinLishAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
