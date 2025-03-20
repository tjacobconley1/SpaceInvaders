package com.tconley.spaceinvaders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tconley.spaceinvaders.navigation.AppNavGraph
import com.tconley.spaceinvaders.ui.theme.SpaceInvadersTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpaceInvadersTheme {
                AppNavGraph()
            }
        }
    }
}
