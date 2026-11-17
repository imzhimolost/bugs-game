package com.games.bugs_game

import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    var score: Int = 0
        private set
    var hits: Int = 0
        private set
    var misses: Int = 0
        private set

    var remainingTimeSeconds: Int = -1
    var isGameOver: Boolean = false
    var isGameStarted: Boolean = false

    fun startNewGame(duration: Int) {
        score = 0
        hits = 0
        misses = 0
        remainingTimeSeconds = duration
        isGameOver = false
        isGameStarted = true
    }

    fun registerHit(points: Int) {
        if (isGameOver) return
        hits++
        score += points
    }

    fun registerMiss(penalty: Int) {
        if (isGameOver) return
        misses++
        score = maxOf(0, score - penalty)
    }

    fun updateTime(secondsLeft: Int) {
        remainingTimeSeconds = secondsLeft
    }

    fun calculateAccuracy(): Int {
        val total = hits + misses
        return if (total > 0) (hits * 100) / total else 0
    }
}