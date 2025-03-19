package com.tconley.spaceinvaders

import SpaceInvadersView
import android.graphics.Point
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity

class SpaceInvadersActivity : ComponentActivity() {

    private lateinit var spaceInvadersView: SpaceInvadersView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get a Display object to access screen details
        val display: Display = windowManager.defaultDisplay
        // Load the resolution into a Point object
        val size = Point()
        display.getSize(size)

        // Initialize spaceInvadersView and set it as the view
        spaceInvadersView = SpaceInvadersView(this, size.x, size.y)
        setContentView(spaceInvadersView)
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
