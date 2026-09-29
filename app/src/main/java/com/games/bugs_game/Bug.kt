package com.games.bugs_game

import kotlin.random.Random

enum class BugType {
    COMMON,
    FAST,
    RARE
}

data class Bug(
    val id: Long = System.nanoTime(),
    var x: Float,
    var y: Float,
    var speedX: Float,
    var speedY: Float,
    val size: Float,
    val type: BugType,
    val points: Int,
    val drawableResId: Int
) {
    companion object {
        fun createRandom(): Bug {
            val roll = Random.nextInt(100)
            return when {
                roll < 15 -> Bug(
                    x = Random.nextFloat() * 800 + 100,
                    y = Random.nextFloat() * 800 + 100,
                    speedX = (Random.nextFloat() * 4 - 2) * 2f,
                    speedY = (Random.nextFloat() * 4 - 2) * 2f,
                    size = 130f,
                    type = BugType.RARE,
                    points = 50,
                    drawableResId = R.drawable.bug_rare
                )
                roll < 50 -> Bug(
                    x = Random.nextFloat() * 800 + 100,
                    y = Random.nextFloat() * 800 + 100,
                    speedX = (if (Random.nextBoolean()) 1 else -1) * (Random.nextFloat() * 8 + 8),
                    speedY = (if (Random.nextBoolean()) 1 else -1) * (Random.nextFloat() * 8 + 8),
                    size = 70f,
                    type = BugType.FAST,
                    points = 25,
                    drawableResId = R.drawable.bug_fast
                )
                else -> Bug(
                    x = Random.nextFloat() * 800 + 100,
                    y = Random.nextFloat() * 800 + 100,
                    speedX = (if (Random.nextBoolean()) 1 else -1) * (Random.nextFloat() * 5 + 4),
                    speedY = (if (Random.nextBoolean()) 1 else -1) * (Random.nextFloat() * 5 + 4),
                    size = 100f,
                    type = BugType.COMMON,
                    points = 10,
                    drawableResId = R.drawable.bug_common
                )
            }
        }
    }
}