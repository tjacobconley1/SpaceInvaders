package com.tconley.spaceinvaders.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tconley.spaceinvaders.screens.HighScoresScreen
import com.tconley.spaceinvaders.screens.MainScreen
import com.tconley.spaceinvaders.viewmodels.HighScoresScreenViewModel

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    // ViewModels
    val highScoresViewModel: HighScoresScreenViewModel = viewModel()

    NavHost(navController = navController, startDestination = "main_screen") {
        // MAIN SCREEN
        composable(route = "main_screen") {
            Spacer(modifier = Modifier.height(20.dp))
            MainScreen(
                navController = navController
            )
        }

        // HIGH SCORES SCREEN
        composable(route = "high_scores_screen") {
            Spacer(modifier = Modifier.height(20.dp))
            HighScoresScreen(
                navController = navController,
                highScoresViewModel
            )
        }
    }
}
