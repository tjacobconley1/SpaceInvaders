import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Canvas
import android.graphics.Paint
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

    private val invaders = Array(60) { Invader(screenX, screenY, 0, 0) }
    private var numInvaders = 0

    private val bricks = Array(400) { DefenceBrick(screenX, screenY, 0, 0, 0) }
    private var numBricks = 0

    private var soundPool: SoundPool
    private var playerExplodeID = -1
    private var invaderExplodeID = -1
    private var shootID = -1
    private var damageShelterID = -1
    private var uhID = -1
    private var ohID = -1

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

        prepareLevel()
    }

    private fun prepareLevel() {
        // Here we will initialize all the game objects

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
                invaders[numInvaders] = Invader(screenX, screenY, row, column)
                numInvaders++
            }
        }

        // Build the shelters
        numBricks = 0
        for (shelterNumber in 0 until 4) {
            for (column in 0 until 10) {
                for (row in 0 until 5) {
                    bricks[numBricks] = DefenceBrick(
                        screenX,
                        screenY,
                        shelterNumber,
                        column,
                        row
                    )
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
        if(bullet.getStatus()){
            bullet.update(fps)
        }

        // Has the player's bullet hit the top of the screen

        // Has an invader's bullet hit the bottom of the screen

        // Has the player's bullet hit an invader

        // Has an alien bullet hit a shelter brick

        // Has a player bullet hit a shelter brick

        // Has an invader bullet hit the player's ship
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

            // Draw the bricks if visible

            // Draw the player's bullet if active
            if(bullet.getStatus()){
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
                        soundPool.play(shootID, 1f, 1f, 0, 0, 1f)
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

}
