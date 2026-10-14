package com.games.bugs_game

import android.widget.SeekBar
import android.widget.TextView

class SettingsManager(
    private val sbSpeed: SeekBar,
    private val tvSpeedLabel: TextView,
    private val sbMaxBugs: SeekBar,
    private val tvMaxBugsLabel: TextView,
    private val sbBonusInterval: SeekBar,
    private val tvBonusIntervalLabel: TextView,
    private val sbRoundDuration: SeekBar,
    private val tvRoundDurationLabel: TextView
) {

    private val absoluteMinDuration = 10
    private val absoluteMaxDuration = 75

    fun bindSettings() {
        setupSpeed()
        setupMaxBugs()
        setupTwoWayLinkedSettings()
    }

    private fun setupSpeed() {
        sbSpeed.max = 4
        sbSpeed.progress = (GameSettings.speedMultiplier - 1).toInt()
        tvSpeedLabel.text = "Скорость игры: ${GameSettings.speedMultiplier.toInt()}x"

        sbSpeed.setOnSeekBarChangeListener(simpleSeekBarListener { progress, _ ->
            val speed = (progress + 1).toFloat()
            GameSettings.speedMultiplier = speed
            tvSpeedLabel.text = "Скорость игры: ${speed.toInt()}x"
        })
    }

    private fun setupMaxBugs() {
        sbMaxBugs.max = 13
        sbMaxBugs.progress = GameSettings.maxBugs - 2
        tvMaxBugsLabel.text = "Макс. тараканов на экране: ${GameSettings.maxBugs}"

        sbMaxBugs.setOnSeekBarChangeListener(simpleSeekBarListener { progress, _ ->
            val bugs = progress + 2
            GameSettings.maxBugs = bugs
            tvMaxBugsLabel.text = "Макс. тараканов на экране: $bugs"
        })
    }

    private fun setupTwoWayLinkedSettings() {
        sbRoundDuration.max = absoluteMaxDuration - absoluteMinDuration
        sbBonusInterval.max = 20

        sbBonusInterval.progress = GameSettings.bonusInterval - 5
        sbRoundDuration.progress = GameSettings.roundDuration - absoluteMinDuration
        updateLabels()

        sbBonusInterval.setOnSeekBarChangeListener(simpleSeekBarListener { progress, fromUser ->
            if (fromUser) {
                val interval = progress + 5
                GameSettings.bonusInterval = interval

                val windowMin = interval * 2
                val windowMax = minOf(interval * 3, absoluteMaxDuration)

                GameSettings.roundDuration = GameSettings.roundDuration.coerceIn(windowMin, windowMax)
                sbRoundDuration.progress = GameSettings.roundDuration - absoluteMinDuration

                updateLabels()
            }
        })

        sbRoundDuration.setOnSeekBarChangeListener(simpleSeekBarListener { progress, fromUser ->
            if (fromUser) {
                val requestedDuration = absoluteMinDuration + progress
                val currentB = GameSettings.bonusInterval

                if (requestedDuration > currentB * 3) {
                    val newB = ((requestedDuration + 2) / 3).coerceIn(5, 25)
                    updateBonusSliderProgrammatically(newB)
                }
                else if (requestedDuration < currentB * 2) {
                    val newB = (requestedDuration / 2).coerceIn(5, 25)
                    updateBonusSliderProgrammatically(newB)
                }

                GameSettings.roundDuration = requestedDuration
                updateLabels()
            }
        })
    }

    private fun updateBonusSliderProgrammatically(newBonusInterval: Int) {
        GameSettings.bonusInterval = newBonusInterval
        sbBonusInterval.progress = newBonusInterval - 5
    }

    private fun updateLabels() {
        val b = GameSettings.bonusInterval
        val windowMin = b * 2
        val windowMax = minOf(b * 3, absoluteMaxDuration)

        tvBonusIntervalLabel.text = "Интервал появления бонусов: $b сек."
        tvRoundDurationLabel.text = "Длительность раунда: ${GameSettings.roundDuration} сек."
    }

    private fun simpleSeekBarListener(onProgress: (Int, Boolean) -> Unit): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) = onProgress(p, fromUser)
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        }
    }
}