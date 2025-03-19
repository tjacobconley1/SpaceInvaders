import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.tconley.spaceinvaders.Bullet
import com.tconley.spaceinvaders.DefenceBrick
import com.tconley.spaceinvaders.Invader
import com.tconley.spaceinvaders.PlayerShip
import com.tconley.spaceinvaders.R
import java.io.IOException

class SpaceInvadersView(context: Context, private val screenX: Int, private val screenY: Int) : SurfaceView(context), Runnable {

    // This is our thread
    private var gameThread: Thread? = null
    private val ourHolder: SurfaceHolder = holder
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

        try {
            val assetManager: AssetManager = context.assets
            var descriptor: AssetFileDescriptor

            descriptor = assetManager.openFd("shoot.ogg")
            shootID = soundPool.load(descriptor, 1)

            descriptor = assetManager.openFd("invaderexplode.ogg")
            invaderExplodeID = soundPool.load(descriptor, 1)

            descriptor = assetManager.openFd("damageshelter.ogg")
            damageShelterID = soundPool.load(descriptor, 1)

            descriptor = assetManager.openFd("playerexplode.ogg")
            playerExplodeID = soundPool.load(descriptor, 1)

            descriptor = assetManager.openFd("uh.ogg")
            uhID = soundPool.load(descriptor, 1)

            descriptor = assetManager.openFd("oh.ogg")
            ohID = soundPool.load(descriptor, 1)
        } catch (e: IOException) {
            Log.e("error", "Failed to load sound files")
        }

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
//        numBricks = 0
//        for (shelterNumber in 0 until 4) {
//            for (column in 0 until 10) {
//                for (row in 0 until 5) {
//                    bricks[numBricks] = DefenceBrick(
//                        screenX,
//                        screenY,
//                        shelterNumber,
//                        column,
//                        row
//                    )
//                    numBricks++
//                }
//            }
//        }
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
                    paused = true
                    lives = 3
                    score = 0
                    prepareLevel()
                }
            }
        }
    }

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
            paint.textSize = 40f
            canvas.drawText("Score: $score   Lives: $lives", 10f, 50f, paint)

            // Draw everything to the screen
            holder.unlockCanvasAndPost(canvas)
        }
    }

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
                // Handle touch event (e.g., move spaceship, fire bullet)
                paused = false

                if (motionEvent.y > screenY - screenY / 8) {
                    playerShip.setMovementState(
                        if (motionEvent.x > screenX / 2) PlayerShip.RIGHT else PlayerShip.LEFT
                    )
                }

                if (motionEvent.y < screenY - screenY / 8) {
                    // Shots fired
                    // TODO converted screenY to float here to avoid integer division
                    if (bullet.shoot(playerShip.getX() + playerShip.getLength() / 2, screenY.toFloat(), Bullet.UP)) {
                        playSound()
//                        soundPool.play(shootID, 1f, 1f, 0, 0, 1f)
                    }
                }
            }

            // Player has removed finger from screen
            MotionEvent.ACTION_UP -> {
                // Handle lift-off event
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
