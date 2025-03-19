package com.tconley.spaceinvaders

import android.graphics.RectF
import android.util.Log

class Bullet(screenX: Int, private val screenY: Int) {

    // Bullet position
    private var x: Float = 0f
    private var y: Float = 0f

    // Rectangle for drawing and collision detection
    private var rect: RectF = RectF()

    // Bullet direction constants
    companion object {
        const val UP = 0
        const val DOWN = 1
    }

    // Bullet properties
    private var heading: Int = -1 // Going nowhere initially
    private var speed: Float = 350f // Pixels per second
    private var width: Int = 1
    private var height: Int = screenY / 20
    private var isActive: Boolean = false

    // --- Getters & Setters ---

    fun getRect(): RectF = rect

    fun getStatus(): Boolean = isActive

    fun setInactive() {
        isActive = false
    }

    fun getImpactPointY(): Float {
        return if (heading == DOWN) {
            y + height
        } else {
            y
        }
    }

    // --- Shoot Method ---
    fun shoot(startX: Float, startY: Float, direction: Int): Boolean {
        return if (!isActive) {
            x = startX
            y = startY
            heading = direction
            isActive = true
            true
        } else {
            // Bullet already active
            false
        }
    }

    fun update(fps: Long) {
        // Move up or down based on heading
        y -= if (heading == UP) speed / fps else -speed / fps

        // Update rect (hitbox)
        rect.set(x, y, x + width, y + height)

        // Deactivate bullet if it moves off-screen
        if (heading == UP && y < 0) {
            isActive = false
            Log.d("Bullet", "Deactivated bullet: moved off top of screen")
        } else if (heading == DOWN && y > screenY) {
            isActive = false
            Log.d("Bullet", "Deactivated bullet: moved off bottom of screen")
        }
    }
}
