package com.tconley.spaceinvaders

// class DefenceBrick(screenX: Int, screenY: Int, shelterNumber: Int, column: Int, row: Int) {
// }

import android.graphics.RectF

class DefenceBrick(row: Int, column: Int, shelterNumber: Int, screenX: Int, screenY: Int) {

    // The hitbox for collision detection
    private var rect: RectF

    // Whether the brick is still visible
    private var isVisible: Boolean = true

    init {
        val width = screenX / 90
        val height = screenY / 40

        // Sometimes a bullet slips through this padding. Set to 0 if needed.
        val brickPadding = 1

        // Number of shelters
        val shelterPadding = screenX / 9
        val startHeight = screenY - (screenY / 8 * 2)

        // Initialize the rectangle position based on row, column, and shelterNumber
        rect = RectF(
            (
                column * width + brickPadding +
                    (shelterPadding * shelterNumber) +
                    shelterPadding + shelterPadding * shelterNumber
                ).toFloat(),

            (row * height + brickPadding + startHeight).toFloat(),

            (
                column * width + width - brickPadding +
                    (shelterPadding * shelterNumber) +
                    shelterPadding + shelterPadding * shelterNumber
                ).toFloat(),

            (row * height + height - brickPadding + startHeight).toFloat()
        )
    }

    // --- Getters and Setters ---

    fun getRect(): RectF = rect

    fun setInvisible() {
        isVisible = false
    }

    fun getVisibility(): Boolean = isVisible
}
