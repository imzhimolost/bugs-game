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
    val drawableResId: Int,
    var health: Int = 1
) {
    companion object {
        fun createRandom(): Bug {
            val roll = Random.nextInt(100)
            return when {
                roll < 15 -> Bug(
                    x = Random.nextFloat() * 700 + 150,
                    y = Random.nextFloat() * 700 + 150,
                    speedX = (Random.nextFloat() * 4 - 2) * 2f,
                    speedY = (Random.nextFloat() * 4 - 2) * 2f,
                    size = 230f,
                    type = BugType.RARE,
                    points = 50,
                    drawableResId = R.drawable.bug_rare,
                    health = 3
                )
                roll < 50 -> Bug(
                    x = Random.nextFloat() * 700 + 150,
                    y = Random.nextFloat() * 700 + 150,
                    speedX = (if (Random.nextBoolean()) 1 else -1) * (Random.nextFloat() * 6 + 7),
                    speedY = (if (Random.nextBoolean()) 1 else -1) * (Random.nextFloat() * 6 + 7),
                    size = 130f,
                    type = BugType.FAST,
                    points = 25,
                    drawableResId = R.drawable.bug_fast,
                    health = 1
                )
                else -> Bug(
                    x = Random.nextFloat() * 700 + 150,
                    y = Random.nextFloat() * 700 + 150,
                    speedX = (if (Random.nextBoolean()) 1 else -1) * (Random.nextFloat() * 4 + 3),
                    speedY = (if (Random.nextBoolean()) 1 else -1) * (Random.nextFloat() * 4 + 3),
                    size = 180f,
                    type = BugType.COMMON,
                    points = 10,
                    drawableResId = R.drawable.bug_common,
                    health = 1
                )
            }
        }
    }
}