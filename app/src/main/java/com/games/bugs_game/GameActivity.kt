package com.games.bugs_game

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentActivity
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.random.Random

class GameActivity : FragmentActivity(), SensorEventListener {

    private var score = 0
    private var hits = 0
    private var misses = 0
    private var isGameOver = false

    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var gameView: GameView
    private var countDownTimer: CountDownTimer? = null

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var tiltX = 0f
    private var tiltY = 0f

    private var mediaPlayer: MediaPlayer? = null

    private var isGravityBonusActive = false
    private val bonusHandler = Handler(Looper.getMainLooper())
    private val gravityDurationHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        tvScore = findViewById(R.id.tvScore)
        tvTimer = findViewById(R.id.tvTimer)
        val container = findViewById<FrameLayout>(R.id.gameContainer)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        gameView = GameView(this)
        container.addView(gameView)

        startGame()
    }

    private fun startGame() {
        score = 0
        hits = 0
        misses = 0
        isGameOver = false
        isGravityBonusActive = false
        gravityDurationHandler.removeCallbacksAndMessages(null)
        updateScore()

        gameView.resetBugs()

        val duration = GameSettings.roundDuration
        tvTimer.text = "Время: $duration"

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer((duration * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "Время: ${millisUntilFinished / 1000}"
            }
            override fun onFinish() {
                tvTimer.text = "Время: 0"
                endGame()
            }
        }.start()

        scheduleBonusSpawn()
    }

    private fun scheduleBonusSpawn() {
        bonusHandler.removeCallbacksAndMessages(null)

        val delayMillis = (GameSettings.bonusInterval * 1000).toLong()

        bonusHandler.postDelayed({
            if (!isGameOver) {
                gameView.spawnBonus()
            }
        }, delayMillis)
    }

    private fun activateGravityBonus() {
        isGravityBonusActive = true
        Toast.makeText(this, "ГРАВИТАЦИЯ! Наклоняйте телефон!", Toast.LENGTH_SHORT).show()

        playScreamSound()

        gravityDurationHandler.removeCallbacksAndMessages(null)

        gravityDurationHandler.postDelayed({
            isGravityBonusActive = false
            Toast.makeText(this, "Гравитация выключена!", Toast.LENGTH_SHORT).show()
        }, 5000)
    }

    private fun playScreamSound() {
        try {
            mediaPlayer?.release()
            val soundId = resources.getIdentifier("bug_scream", "raw", packageName)
            if (soundId != 0) {
                mediaPlayer = MediaPlayer.create(this, soundId)
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            tiltX = -event.values[0]
            tiltY = event.values[1]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun updateScore() {
        tvScore.text = "Очки: $score"
    }

    private fun endGame() {
        isGameOver = true
        bonusHandler.removeCallbacksAndMessages(null)
        gameView.stopGame()

        val totalShots = hits + misses
        val accuracy = if (totalShots > 0) (hits * 100) / totalShots else 0

        val currentDate = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val db = AppDatabase.getDatabase(this)
        db.appDao().insertRecord(
            RecordEntity(
                playerName = GameSettings.currentPlayerName,
                score = score,
                difficulty = GameSettings.speedMultiplier.toInt(),
                date = currentDate,
                accuracy = accuracy
            )
        )

        AlertDialog.Builder(this)
            .setTitle("Раунд завершен!")
            .setMessage(
                """
                ИТОГОВЫЙ СЧЕТ: $score
                -------------------------
                Попаданий: $hits
                Промахов: $misses
                Точность: $accuracy%
                """.trimIndent()
            )
            .setCancelable(false)
            .setPositiveButton("Играть снова") { _, _ -> startGame() }
            .setNegativeButton("В меню") { _, _ -> finish() }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        bonusHandler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        gameView.stopGame()
        gravityDurationHandler.removeCallbacksAndMessages(null)
    }

    inner class GameView(context: Context) : View(context) {

        private val bugs = mutableListOf<Bug>()
        private val handler = Handler(Looper.getMainLooper())
        private val missPenalty = 5

        private var bonusX = 500f
        private var bonusY = 500f
        private val bonusSize = 150f
        private var isBonusOnScreen = false

        private val gameLoop = object : Runnable {
            override fun run() {
                if (!isGameOver) {
                    updatePhysics()
                    invalidate()
                    handler.postDelayed(this, 30)
                }
            }
        }

        init {
            resetBugs()
        }

        fun resetBugs() {
            bugs.clear()
            isBonusOnScreen = false
            repeat(GameSettings.maxBugs) {
                bugs.add(Bug.createRandom())
            }
            handler.removeCallbacks(gameLoop)
            handler.post(gameLoop)
        }

        fun stopGame() {
            handler.removeCallbacks(gameLoop)
        }

        fun spawnBonus() {
            bonusX = Random.nextFloat() * 700 + 150
            bonusY = Random.nextFloat() * 700 + 150
            isBonusOnScreen = true
            invalidate()

            bonusHandler.postDelayed({
                if (isBonusOnScreen && !isGameOver) {
                    isBonusOnScreen = false
                    scheduleBonusSpawn()
                    invalidate()
                }
            }, 7000)
        }

        private fun updatePhysics() {
            val speedFactor = GameSettings.speedMultiplier

            for (bug in bugs) {
                if (isGravityBonusActive) {
                    bug.speedX += tiltX * 1.5f
                    bug.speedY += tiltY * 1.5f

                    bug.speedX *= 0.95f
                    bug.speedY *= 0.95f
                }

                bug.x += bug.speedX * speedFactor
                bug.y += bug.speedY * speedFactor

                val bound = bug.size / 2
                if (bug.x <= bound) {
                    bug.x = bound;
                    bug.speedX = kotlin.math.abs(bug.speedX)
                }
                if (bug.x >= 1000 - bound) {
                    bug.x = 1000 - bound;
                    bug.speedX = -kotlin.math.abs(bug.speedX)
                }
                if (bug.y <= bound) {
                    bug.y = bound;
                    bug.speedY = kotlin.math.abs(bug.speedY)
                }
                if (bug.y >= 1000 - bound) {
                    bug.y = 1000 - bound;
                    bug.speedY = -kotlin.math.abs(bug.speedY)
                }
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val scaleX = width / 1000f
            val scaleY = height / 1000f

            if (isBonusOnScreen) {
                val bx = bonusX * scaleX
                val by = bonusY * scaleY
                val bSize = bonusSize * scaleX

                val bonusDrawable = AppCompatResources.getDrawable(context, android.R.drawable.ic_menu_compass)
                bonusDrawable?.let {
                    it.setBounds(
                        (bx - bSize / 2).toInt(),
                        (by - bSize / 2).toInt(),
                        (bx + bSize / 2).toInt(),
                        (by + bSize / 2).toInt()
                    )
                    it.draw(canvas)
                }
            }

            for (bug in bugs) {
                val px = bug.x * scaleX
                val py = bug.y * scaleY
                val pSize = bug.size * scaleX

                val drawable = AppCompatResources.getDrawable(context, bug.drawableResId)
                drawable?.let {
                    it.setBounds(
                        (px - pSize / 2).toInt(),
                        (py - pSize / 2).toInt(),
                        (px + pSize / 2).toInt(),
                        (py + pSize / 2).toInt()
                    )

                    val angle = Math.toDegrees(
                        atan2(bug.speedY.toDouble(), bug.speedX.toDouble())
                    ).toFloat() + 90f

                    canvas.save()
                    canvas.rotate(angle, px, py)
                    it.draw(canvas)
                    canvas.restore()
                }
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN && !isGameOver) {
                val scaleX = width / 1000f
                val scaleY = height / 1000f

                val touchLogicalX = event.x / scaleX
                val touchLogicalY = event.y / scaleY

                if (isBonusOnScreen) {
                    val distToBonus = hypot(touchLogicalX - bonusX, touchLogicalY - bonusY)
                    if (distToBonus <= bonusSize / 1.2f) {
                        isBonusOnScreen = false
                        activateGravityBonus()
                        scheduleBonusSpawn()
                        invalidate()
                        return true
                    }
                }

                var hitBug: Bug? = null
                for (bug in bugs.reversed()) {
                    val distance = hypot(touchLogicalX - bug.x, touchLogicalY - bug.y)
                    if (distance <= bug.size / 1.5f) {
                        hitBug = bug
                        break
                    }
                }

                if (hitBug != null) {
                    hits++
                    hitBug.health--
                    if (hitBug.health <= 0) {
                        score += hitBug.points
                        bugs.remove(hitBug)
                        bugs.add(Bug.createRandom())
                    } else {
                        hitBug.speedX *= 1.3f
                        hitBug.speedY *= 1.3f
                    }
                } else {
                    misses++
                    score = maxOf(0, score - missPenalty)
                }

                updateScore()
                invalidate()
                return true
            }
            return super.onTouchEvent(event)
        }
    }
}