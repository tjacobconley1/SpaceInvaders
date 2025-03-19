package com.tconley.spaceinvaders

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF

class PlayerShip(context: Context, screenX: Int, screenY: Int) {

    // The player ship will be represented by a Bitmap
    private var bitmap: Bitmap

    // The player's hitbox (used for collision detection)
    private var rect: RectF

    // How long and high our ship will be
    private var length: Float = (screenX / 10).toFloat()
    private var height: Float = (screenY / 10).toFloat()

    // X is the far left of the rectangle which forms our ship
    private var x: Float = (screenX / 2).toFloat()

    // Y is the top coordinate
    private var y: Float = (screenY - 20).toFloat()

    // This will hold the pixels per second speed that the ship will move
    private var shipSpeed: Float = 350f

    // Which ways can the ship move
    companion object {
        const val STOPPED = 0
        const val LEFT = 1
        const val RIGHT = 2
    }

    // Is the ship moving and in which direction
    private var shipMoving: Int = STOPPED

    // Constructor logic
    init {
        // Initialize the rectangle representing the ship
        rect = RectF(x, y, x + length, y + height)

        // Load the bitmap and scale it to the correct size
        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.playership)
        bitmap = Bitmap.createScaledBitmap(bitmap, length.toInt(), height.toInt(), false)
    }

    // Getters for collision detection & rendering
    fun getRect(): RectF = rect

    fun getBitmap(): Bitmap = bitmap

    fun getX(): Float = x

    fun getLength(): Float = length

    // Setter for movement state
    fun setMovementState(state: Int) {
        shipMoving = state
    }

    fun update(fps: Long) {
        if (shipMoving == LEFT) {
            x -= shipSpeed / fps
        }

        if (shipMoving == RIGHT) {
            x += shipSpeed / fps
        }

        // Update rect which is used to detect hits
        rect.set(x, y, x + length, y + height)
    }
}
