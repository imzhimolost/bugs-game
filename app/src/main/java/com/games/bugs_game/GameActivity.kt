package com.games.bugs_game

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentActivity
import kotlin.math.atan2
import kotlin.math.hypot

class GameActivity : FragmentActivity() {

    private var score = 0
    private var hits = 0
    private var misses = 0
    private var isGameOver = false

    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var gameView: GameView
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        tvScore = findViewById(R.id.tvScore)
        tvTimer = findViewById(R.id.tvTimer)
        val container = findViewById<FrameLayout>(R.id.gameContainer)

        gameView = GameView(this)
        container.addView(gameView)

        startGame()
    }

    private fun startGame() {
        score = 0
        hits = 0
        misses = 0
        isGameOver = false
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
    }

    private fun updateScore() {
        tvScore.text = "Очки: $score"
    }

    private fun endGame() {
        isGameOver = true
        gameView.stopGame()

        val totalShots = hits + misses
        val accuracy = if (totalShots > 0) (hits * 100) / totalShots else 0

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
        gameView.stopGame()
    }

    inner class GameView(context: Context) : View(context) {

        private val bugs = mutableListOf<Bug>()
        private val handler = Handler(Looper.getMainLooper())
        private val missPenalty = 5

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
            repeat(GameSettings.maxBugs) {
                bugs.add(Bug.createRandom())
            }
            handler.removeCallbacks(gameLoop)
            handler.post(gameLoop)
        }

        fun stopGame() {
            handler.removeCallbacks(gameLoop)
        }

        private fun updatePhysics() {
            val speedFactor = GameSettings.speedMultiplier

            for (bug in bugs) {
                bug.x += bug.speedX * speedFactor
                bug.y += bug.speedY * speedFactor

                val bound = bug.size / 2
                if (bug.x <= bound || bug.x >= 1000 - bound) bug.speedX *= -1
                if (bug.y <= bound || bug.y >= 1000 - bound) bug.speedY *= -1
            }
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val scaleX = width / 1000f
            val scaleY = height / 1000f

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