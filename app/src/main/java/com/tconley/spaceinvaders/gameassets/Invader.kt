package com.tconley.spaceinvaders.gameassets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import com.tconley.spaceinvaders.R
import kotlin.random.Random

class Invader(context: Context, row: Int, column: Int, screenX: Int, screenY: Int) {

    // The hitbox for collision detection
    var rect: RectF = RectF()

    // The invader's two animation frames (arms up and arms down)
    private var bitmap1: Bitmap
    private var bitmap2: Bitmap

    // How long and high our invader will be
    private var length: Float = (screenX / 20).toFloat()
    private var height: Float = (screenY / 20).toFloat()

    // X is the far left of the rectangle which forms our invader
    private var x: Float

    // Y is the top coordinate
    private var y: Float

    // This will hold the pixels per second speed that the invader will move
    private var shipSpeed: Float = 40f

    companion object {
        const val LEFT = 1
        const val RIGHT = 2
    }

    // Is the ship moving and in which direction
    private var shipMoving: Int = RIGHT

    var isVisible: Boolean = true

    init {
        // Determine spacing between invaders
        val padding = screenX / 25

        // Position invader based on row and column
        x = column * (length + padding)
        y = row * (length + padding / 4)

        // Load bitmaps for invader animation (arms up and arms down)
        bitmap1 = BitmapFactory.decodeResource(context.resources, R.drawable.invader1)
        bitmap2 = BitmapFactory.decodeResource(context.resources, R.drawable.invader2)

        // Scale bitmaps to match invader size
        bitmap1 = Bitmap.createScaledBitmap(bitmap1, length.toInt(), height.toInt(), false)
        bitmap2 = Bitmap.createScaledBitmap(bitmap2, length.toInt(), height.toInt(), false)

        // Initialize the rectangle hitbox
        rect.set(x, y, x + length, y + height)
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

    fun dropDownAndReverse() {
        shipMoving = if (shipMoving == LEFT) RIGHT else LEFT

        y += height
        shipSpeed *= 1.18f
    }

    fun takeAim(playerShipX: Float, playerShipLength: Float): Boolean {
        // If near the player, increase firing chance
        if ((playerShipX + playerShipLength > x && playerShipX + playerShipLength < x + length) ||
            (playerShipX > x && playerShipX < x + length)
        ) {
            // A 1 in 50 chance to shoot
            if (Random.Default.nextInt(50) == 0) {
                return true
            }
        }

        // If firing randomly (not near the player), a 1 in 1000 chance
        return Random.Default.nextInt(20) == 0
    }

    // --- Getters, Setters, and Helper Methods ---

    fun setInvisible() {
        isVisible = false
    }

    fun getVisibility(): Boolean = isVisible

    // Getters for animation frames
    fun getBitmap1(): Bitmap = bitmap1
    fun getBitmap2(): Bitmap = bitmap2

    fun getX(): Float = x

    fun getY(): Float = y

    fun getLength(): Float = length
}
