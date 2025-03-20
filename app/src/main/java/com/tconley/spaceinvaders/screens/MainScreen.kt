package com.tconley.spaceinvaders.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tconley.spaceinvaders.SpaceInvadersActivity

@Composable
fun MainScreen(
    navController: NavHostController
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome to Space Invaders!")

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                // Start SpaceInvadersActivity
                val intent = Intent(context, SpaceInvadersActivity::class.java)
                context.startActivity(intent)
            }) {
                Text(text = "Start Game")
            }

            Spacer(modifier = Modifier.height(100.dp))

            Button(onClick = {
                navController.navigate("high_scores_screen")
            }) {
                Text(text = "High Scores")
            }
        }
    }
}
