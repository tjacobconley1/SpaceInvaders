package com.tconley.spaceinvaders

import SpaceInvadersView
import android.app.AlertDialog
import android.graphics.Point
import android.os.Bundle
import android.view.Display
import android.widget.EditText
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.tconley.spaceinvaders.viewmodels.SpaceInvadersViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpaceInvadersActivity : ComponentActivity(), SpaceInvadersView.GameOverCallback {

    private lateinit var spaceInvadersView: SpaceInvadersView
    val spaceInvadersViewModel: SpaceInvadersViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get a Display object to access screen details
        val display: Display = windowManager.defaultDisplay
        // Load the resolution into a Point object
        val size = Point()
        display.getSize(size)

        // Initialize spaceInvadersView and set it as the view
        spaceInvadersView = SpaceInvadersView(this, spaceInvadersViewModel, size.x, size.y)
        spaceInvadersView.setGameOverCallback(this)
        setContentView(spaceInvadersView)
    }

    override fun onGameOver(score: Int) {
        runOnUiThread {
            val editText = EditText(this)
            AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("Enter your name:")
                .setView(editText)
                .setCancelable(false)
                .setPositiveButton("Submit") { _, _ ->
                    val name = editText.text.toString().ifBlank { "Player" }
                    spaceInvadersViewModel.InsertPlayerScore(name, score)

                    // Reset game
                    spaceInvadersView.resetGame()
                }
                .show()
        }
    }
    override fun onResume() {
        super.onResume()
        // Tell the gameView resume method to execute
        spaceInvadersView.resume()
    }

    override fun onPause() {
        super.onPause()
        // Tell the gameView pause method to execute
        spaceInvadersView.pause()
    }
}
