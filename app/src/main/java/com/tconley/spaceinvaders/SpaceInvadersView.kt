import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import com.tconley.spaceinvaders.R
import com.tconley.spaceinvaders.gameassets.Bullet
import com.tconley.spaceinvaders.gameassets.DefenceBrick
import com.tconley.spaceinvaders.gameassets.Invader
import com.tconley.spaceinvaders.gameassets.PlayerShip
import com.tconley.spaceinvaders.viewmodels.SpaceInvadersViewModel

class SpaceInvadersView(context: Context, private val viewModel: SpaceInvadersViewModel, private val screenX: Int, private val screenY: Int) : SurfaceView(context), Runnable {

    private val paint = Paint()

    @Volatile
    private var playing = false
    private var paused = true

    private var fps: Long = 0
    private var timeThisFrame: Long = 0

    private lateinit var playerShip: PlayerShip
    private lateinit var bullet: Bullet
    private val invadersBullets = Array(200) { Bullet(screenX, screenY) }
    private var nextBullet = 0
    private val maxInvaderBullets = 10

    private val invaders = Array(60) { Invader(context, 0, 0, screenX, screenY) }
    private var numInvaders = 0

    private val bricks = Array(400) { DefenceBrick(screenX, screenY, 0, 0, 0) }
    private var numBricks = 0

    // private var soundPool: SoundPool
    private var soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    // Load the sound file (replace "laser_sound" with your file name)
    private var soundId = soundPool.load(context, R.raw.shoot, 1)
    private var uhID = soundPool.load(context, R.raw.uh, 1)
    private var ohID = soundPool.load(context, R.raw.oh, 1)
    private var soundLoaded = false

    private var playerExplodeID = -1
    private var invaderExplodeID = -1
    private var shootID = -1
    private var damageShelterID = -1

    private var score = 0
    private var lives = 3

    private var menaceInterval: Long = 1000
    private var uhOrOh = false
    private var lastMenaceTime: Long = System.currentTimeMillis()

    private var gameOver = false
    private val pauseButton = RectF(screenX - 150f, 50f, screenX - 50f, 150f)
    private var isPaused = false

    // Joystick
//    private val joystickRadius = 200
//    private val joystickCenterX = 200f
//    private val joystickCenterY = (screenY - 250f)
//    private var joystickActive = false
//    private var joystickAngle = 0
//    private var joystickStrength = 0

    init {
        // Ask SurfaceView to set up our object
        this.setWillNotDraw(false)

        // Initialize SoundPool (updated version for API 21+)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load the sounds
        shootID = soundPool.load(context, R.raw.shoot, 1)
        invaderExplodeID = soundPool.load(context, R.raw.invaderexplode, 1)
        damageShelterID = soundPool.load(context, R.raw.damageshelter, 1)
        playerExplodeID = soundPool.load(context, R.raw.playerexplode, 1)
        uhID = soundPool.load(context, R.raw.uh, 1)
        ohID = soundPool.load(context, R.raw.oh, 1)

        // Ensure sound is loaded before playing
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                Log.d("SoundPool", "Sound loaded successfully: $sampleId")
                soundLoaded = true
            } else {
                Log.e("SoundPool", "Failed to load sound: $status")
            }
        }

        prepareLevel()
    }

    // PREPARE THE GAME LEVEL
    private fun prepareLevel() {
        // Here we will initialize all the game objects
        menaceInterval = 1000

        // Make a new player space ship
        playerShip = PlayerShip(context, screenX, screenY)

        // Prepare the player's bullet
        bullet = Bullet(screenX, screenY)

        // Initialize the invadersBullets array
        for (i in invadersBullets.indices) {
            invadersBullets[i] = Bullet(screenX, screenY)
        }

        // Build an army of invaders
        numInvaders = 0
        for (column in 0 until 6) {
            for (row in 0 until 5) {
                invaders[numInvaders] = Invader(context, row, column, screenX, screenY)
                numInvaders++
            }
        }

        // Build the shelters
        numBricks = 0
        for (shelterNumber in 0 until 4) {
            for (column in 0 until 10) {
                for (row in 0 until 5) {
                    bricks[numBricks] = DefenceBrick(row, column, shelterNumber, screenX, screenY)
                    numBricks++
                }
            }
        }
    }

    // --- PLACE THE GAME LOOP METHODS RIGHT AFTER prepareLevel() ---

    override fun run() {
        while (playing) {
            val startFrameTime = System.currentTimeMillis()

            if (!paused) {
                update()
            }

            draw()

            timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame > 0) {
                fps = 1000 / timeThisFrame
            }
            if (!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    // Play Uh or Oh sound
                    val soundID = if (uhOrOh) uhID else ohID
                    soundPool.play(soundID, 1f, 1f, 0, 0, 1f)

                    // Reset the last menace time
                    lastMenaceTime = System.currentTimeMillis()

                    // Toggle the Uh-Oh value
                    uhOrOh = !uhOrOh
                }
            }
        }
    }

    private fun update() {
        // Did an invader bump into the side of the screen
        var bumped = false

        // Has the player lost
        var lost = false

        // Move the player's ship based on joystick input
//        if (joystickStrength > 10) { // Ignore very small movements
//            when {
//                joystickAngle in 45..135 -> playerShip.setMovementState(PlayerShip.RIGHT)
//                joystickAngle in 225..315 -> playerShip.setMovementState(PlayerShip.STOPPED)
//                joystickAngle in 135..225 -> playerShip.setMovementState(PlayerShip.LEFT)
//                else -> playerShip.setMovementState(PlayerShip.RIGHT)
//            }
//        } else {
//            playerShip.setMovementState(PlayerShip.STOPPED)
//        }

        // Move the player's ship
        playerShip.update(fps)

        // Update the invaders if visible
        for (invader in invaders) {
            if (invader.getVisibility()) {
                // Move the invader
                invader.update(fps)

                // Check if the invader wants to take a shot
                if (invader.takeAim(playerShip.getX(), playerShip.getLength())) {
                    // Try to fire a bullet
                    if (invadersBullets[nextBullet].shoot(
                            invader.getX() + invader.getLength() / 2,
                            invader.getY(),
                            Bullet.DOWN
                        )
                    ) {
                        // Shot fired, prepare for the next shot
                        nextBullet++

                        // Loop back to the first bullet if we reach the last one
                        if (nextBullet == maxInvaderBullets) {
                            // Prevents firing another bullet until one completes its journey
                            // Because if bullet 0 is still active, shoot() returns false
                            nextBullet = 0
                        }
                    }
                }

                // Check if the invader has bumped into the screen edges
                if (invader.getX() > screenX - invader.getLength() || invader.getX() < 0) {
                    bumped = true
                }
            }
        }

        // Did an invader bump into the edge of the screen
        if (bumped) {
            // Move all the invaders down and change direction
            for (invader in invaders) {
                invader.dropDownAndReverse()

                // Check if the invaders have landed
                if (invader.getY() > screenY - screenY / 10) {
                    lost = true
                }
            }

            // Increase the menace level by making the sounds more frequent
            menaceInterval -= 80
        }

        // Update all the invaders bullets if active
        for (i in invadersBullets.indices) {
            if (invadersBullets[i].getStatus()) {
                invadersBullets[i].update(fps)
            }
        }

        // Did an invader bump into the edge of the screen

        if (lost) {
            prepareLevel()
        }

        // Update the player's bullet
        if (bullet.getStatus()) {
            bullet.update(fps)
        }

        // Has the player's bullet hit the top of the screen
        if (bullet.getImpactPointY() < 0) {
            bullet.setInactive()
        }

        // Has an invader's bullet hit the bottom of the screen
        for (bullet in invadersBullets) {
            if (bullet.getImpactPointY() > screenY) {
                bullet.setInactive()
            }
        }

        // Has the player's bullet hit an invader
        if (bullet.getStatus()) {
            for (invader in invaders) {
                if (invader.getVisibility() && RectF.intersects(bullet.getRect(), invader.rect)) {
                    invader.setInvisible()
                    soundPool.play(invaderExplodeID, 1f, 1f, 0, 0, 1f)
                    bullet.setInactive()
                    score += 10

                    // Has the player won?
                    if (score == numInvaders * 10) {
                        paused = true
                        score = 0
                        lives = 3
                        prepareLevel()
                    }
                }
            }
        }

        // Has an alien bullet hit a shelter brick
        for (bullet in invadersBullets) {
            if (bullet.getStatus()) {
                for (brick in bricks) {
                    if (brick.getVisibility() && RectF.intersects(bullet.getRect(), brick.getRect())) {
                        // A collision has occurred
                        bullet.setInactive()
                        brick.setInvisible()
                        soundPool.play(damageShelterID, 1f, 1f, 0, 0, 1f)
                    }
                }
            }
        }

        // Has a player bullet hit a shelter brick
        if (bullet.getStatus()) {
            for (brick in bricks) {
                if (brick.getVisibility() && RectF.intersects(bullet.getRect(), brick.getRect())) {
                    // A collision has occurred
                    bullet.setInactive()
                    brick.setInvisible()
                    soundPool.play(damageShelterID, 1f, 1f, 0, 0, 1f)
                }
            }
        }

        // Has an invader bullet hit the player's ship
        for (bullet in invadersBullets) {
            if (bullet.getStatus() && RectF.intersects(playerShip.getRect(), bullet.getRect())) {
                bullet.setInactive()
                lives--
                soundPool.play(playerExplodeID, 1f, 1f, 0, 0, 1f)

                // Is it game over?
                if (lives == 0) {
                    gameOver = true
                    paused = true
                    // TODO prompt player to enter their name
                    viewModel.InsertPlayerScore("Player", score)
                    lives = 3
                    score = 0
                    prepareLevel()
                }
            }
        }
    }

// DRAW ==========================
    private fun draw() {
        // Make sure our drawing surface is valid, or we crash
        if (holder.surface.isValid) {
            // Lock the canvas ready to draw
            val canvas: Canvas = holder.lockCanvas()

            // Draw the background color
            canvas.drawColor(android.graphics.Color.argb(255, 26, 128, 182))

            // Choose the brush color for drawing
            paint.color = android.graphics.Color.argb(255, 255, 255, 255)

            // Draw the player spaceship
            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), (screenY - 50).toFloat(), paint)

            // Draw joystick
            // drawJoystick(canvas)

            // Draw the invaders
            for (invader in invaders) {
                if (invader.getVisibility()) {
                    val bitmap = if (uhOrOh) invader.getBitmap1() else invader.getBitmap2()
                    canvas.drawBitmap(bitmap, invader.getX(), invader.getY(), paint)
                }
            }

            // Draw the bricks if visible
            for (brick in bricks) {
                if (brick.getVisibility()) {
                    canvas.drawRect(brick.getRect(), paint)
                }
            }

            // Draw the player's bullet if active
            if (bullet.getStatus()) {
                canvas.drawRect(bullet.getRect(), paint)
            }

            // Draw the invaders' bullets if active
            for (i in invadersBullets.indices) {
                if (invadersBullets[i].getStatus()) {
                    canvas.drawRect(invadersBullets[i].getRect(), paint)
                }
            }

            // Draw the score and remaining lives
            paint.color = android.graphics.Color.argb(255, 249, 129, 0)
            paint.textSize = 50f // Ensure readable size
            paint.textAlign = Paint.Align.LEFT // Align text correctly

            val padding = 20f // Add padding to avoid clipping
            val textBaseline = padding + paint.textSize // Ensure proper baseline positioning

            canvas.drawText("Score: $score   Lives: $lives", padding, textBaseline, paint)

            // Draw the pause button
            paint.color = android.graphics.Color.GRAY
            canvas.drawRoundRect(pauseButton, 30f, 30f, paint) // Rounded corners

            // Draw the "||" with Thicker Strokes
            paint.color = android.graphics.Color.WHITE
            paint.strokeWidth = 10f
            paint.style = Paint.Style.FILL // Ensures solid bars

            // Define dimensions for the pause bars
            val barWidth = 15f // Width of each pause bar
            val barHeight = 60f // Height of each pause bar
            val centerX = pauseButton.centerX()
            val centerY = pauseButton.centerY()

            // Draw two thick vertical bars for the pause icon
            canvas.drawRect(centerX - barWidth - 5f, centerY - barHeight / 2, centerX - 5f, centerY + barHeight / 2, paint)
            canvas.drawRect(centerX + 5f, centerY - barHeight / 2, centerX + barWidth + 5f, centerY + barHeight / 2, paint)

            // If lives = 0 show game over overlay
            if (gameOver) {
                // Semi-transparent overlay
                paint.color = android.graphics.Color.argb(180, 0, 0, 0)
                canvas.drawRect(0f, 0f, screenX.toFloat(), screenY.toFloat(), paint)

                // Game Over Text
                paint.color = android.graphics.Color.WHITE
                paint.textSize = 80f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("GAME OVER", (screenX / 2).toFloat(), (screenY / 2 - 50).toFloat(), paint)

                // Restart Message
                paint.textSize = 50f
                canvas.drawText("Tap to Restart", (screenX / 2).toFloat(), (screenY / 2 + 50).toFloat(), paint)
            }

            // show pause overlay
            if (isPaused) {
                paint.color = android.graphics.Color.argb(180, 0, 0, 0)
                canvas.drawRect(0f, 0f, screenX.toFloat(), screenY.toFloat(), paint)

                paint.color = android.graphics.Color.WHITE
                paint.textSize = 80f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("PAUSED", (screenX / 2).toFloat(), (screenY / 2).toFloat(), paint)
            }

            // Draw everything to the screen
            holder.unlockCanvasAndPost(canvas)
        }
    }

    // Draw Joystick
    // val joystickRadius = 100  // Halved outer ring
//    private val knobRadius = 60        // Larger knob
//    private val joystickSensitivity = 1.5f  // Increase movement sensitivity

//    private fun drawJoystick(canvas: Canvas) {
//        val joystickRadius = 100
//        // Draw the outer circle (joystick base)
//        paint.color = android.graphics.Color.DKGRAY
//        canvas.drawCircle(joystickCenterX, joystickCenterY, joystickRadius.toFloat(), paint)
//
//        // Calculate new joystick knob position with increased sensitivity
//        val adjustedStrength = (joystickStrength / 100f) * joystickRadius * joystickSensitivity
//        val knobX = joystickCenterX + adjustedStrength * Math.cos(Math.toRadians(joystickAngle.toDouble())).toFloat()
//        val knobY = joystickCenterY + adjustedStrength * Math.sin(Math.toRadians(joystickAngle.toDouble())).toFloat()
//
//        // Draw the larger joystick knob
//        paint.color = android.graphics.Color.LTGRAY
//        canvas.drawCircle(knobX, knobY, knobRadius.toFloat(), paint)
//    }

    // --- LIFECYCLE METHODS (Pause & Resume) ---

    fun resume() {
        playing = true
        Thread(this).start()
    }

    fun pause() {
        playing = false
        try {
            Thread.sleep(16)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // --- TOUCH EVENT HANDLING ---

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        when (motionEvent.action and MotionEvent.ACTION_MASK) {
            // Player has touched the screen
            MotionEvent.ACTION_DOWN -> {
                if (gameOver) {
                    gameOver = false
                    score = 0
                    lives = 3
                    prepareLevel()
                    paused = false
                } else if (isPaused) {
                    // If paused, tap anywhere to resume
                    isPaused = false
                    paused = false
                } else if (pauseButton.contains(motionEvent.x, motionEvent.y)) {
                    isPaused = !isPaused
                    paused = isPaused
                } else if (!isPaused) {
                    paused = false
//                    val dx = motionEvent.x - joystickCenterX
//                    val dy = motionEvent.y - joystickCenterY
//                    val distance = Math.sqrt((dx * dx + dy * dy).toDouble())

//                    if (distance < joystickRadius) {
//                        joystickActive = true
//                        joystickAngle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toInt()
//                        if (joystickAngle < 0) joystickAngle += 360
//                        joystickStrength = ((distance / joystickRadius) * 100).toInt()
//                    } else {
//                        joystickActive = false
//                        joystickStrength = 0
//                    }
                    if (motionEvent.y > screenY - screenY / 8) {
                        playerShip.setMovementState(
                            if (motionEvent.x > screenX / 2) PlayerShip.RIGHT else PlayerShip.LEFT
                        )
                    }
                    if (motionEvent.y < screenY - screenY / 8) {
                        if (bullet.shoot(playerShip.getX() + playerShip.getLength() / 2, screenY.toFloat(), Bullet.UP)) {
                            playSound()
                        }
                    }
                }
            }

            // Player has removed finger from screen
            MotionEvent.ACTION_UP -> {
                // Handle lift-off event
//                joystickActive = false
//                joystickStrength = 0
                if (motionEvent.y > screenY - screenY / 10) {
                    playerShip.setMovementState(PlayerShip.STOPPED)
                }
            }
        }
        return true
    }

    // Call this function when you want to play the sound
    private fun playSound() {
        if (soundLoaded) {
            soundPool.play(shootID, 1f, 1f, 1, 0, 1f)
            Log.d("SoundPool", "Playing sound: $shootID")
        } else {
            Log.w("SoundPool", "Sound not ready yet")
        }
    }
}
