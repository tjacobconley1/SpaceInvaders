package com.tconley.spaceinvaders.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tconley.spaceinvaders.screens.uicomponents.ScoreCard
import com.tconley.spaceinvaders.viewmodels.HighScoresScreenViewModel

@Composable
fun HighScoresScreen(
    navController: NavHostController,
    viewModel: HighScoresScreenViewModel
) {
    val highScores = viewModel.highScores.collectAsState(initial = emptyList())
    var sortedHighScores = highScores.value.sortedByDescending { it.score }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(text = "High Scores")
            Spacer(modifier = Modifier.height(16.dp))
            if (sortedHighScores.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(sortedHighScores.size) { index ->
                        ScoreCard(sortedHighScores[index].name, sortedHighScores[index].score)
                    }
                }
            } else {
                Text(
                    modifier = Modifier.align(CenterHorizontally),
                    text = "No high scores available"
                )
            }
        }
    }
}
