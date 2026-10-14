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

    fun bindSettings() {
        setupSpeed()
        setupMaxBugs()
        setupLinkedSettings()
    }

    private fun setupSpeed() {
        sbSpeed.max = 4
        sbSpeed.progress = (GameSettings.speedMultiplier - 1).toInt()
        tvSpeedLabel.text = "Скорость игры: ${GameSettings.speedMultiplier.toInt()}x"

        sbSpeed.setOnSeekBarChangeListener(simpleSeekBarListener { progress ->
            val speed = (progress + 1).toFloat()
            GameSettings.speedMultiplier = speed
            tvSpeedLabel.text = "Скорость игры: ${speed.toInt()}x"
        })
    }

    private fun setupMaxBugs() {
        sbMaxBugs.max = 13
        sbMaxBugs.progress = GameSettings.maxBugs - 2
        tvMaxBugsLabel.text = "Макс. тараканов на экране: ${GameSettings.maxBugs}"

        sbMaxBugs.setOnSeekBarChangeListener(simpleSeekBarListener { progress ->
            val bugs = progress + 2
            GameSettings.maxBugs = bugs
            tvMaxBugsLabel.text = "Макс. тараканов на экране: $bugs"
        })
    }

    private fun setupLinkedSettings() {
        updateDurationWindow()

        sbBonusInterval.max = 20
        sbBonusInterval.progress = GameSettings.bonusInterval - 5
        tvBonusIntervalLabel.text = "Интервал появления бонусов: ${GameSettings.bonusInterval} сек."

        sbBonusInterval.setOnSeekBarChangeListener(simpleSeekBarListener { progress ->
            val interval = progress + 5
            GameSettings.bonusInterval = interval
            tvBonusIntervalLabel.text = "Интервал появления бонусов: $interval сек."
            updateDurationWindow()
        })

        sbRoundDuration.setOnSeekBarChangeListener(simpleSeekBarListener { progress ->
            val minDuration = GameSettings.bonusInterval * 2
            val actualDuration = minDuration + progress
            GameSettings.roundDuration = actualDuration
            tvRoundDurationLabel.text = "Длительность раунда: $actualDuration сек."
        })
    }

    private fun updateDurationWindow() {
        val b = GameSettings.bonusInterval
        val minDuration = b * 2
        val maxDuration = b * 3

        sbRoundDuration.max = maxDuration - minDuration

        if (GameSettings.roundDuration < minDuration) GameSettings.roundDuration = minDuration
        if (GameSettings.roundDuration > maxDuration) GameSettings.roundDuration = maxDuration

        sbRoundDuration.progress = GameSettings.roundDuration - minDuration
        tvRoundDurationLabel.text = "Длительность раунда: ${GameSettings.roundDuration} сек."
    }

    private fun simpleSeekBarListener(onProgress: (Int) -> Unit): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, f: Boolean) = onProgress(p)
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        }
    }
}