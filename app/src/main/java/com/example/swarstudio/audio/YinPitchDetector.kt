package com.example.swarstudio.audio

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.sqrt

/** Monophonic fundamental-frequency estimator using the YIN difference function. */
class YinPitchDetector(
    private val sampleRate: Int,
    private val minHz: Float = 65f,
    private val maxHz: Float = 1000f,
    private val threshold: Float = 0.15f
) {
    fun detect(samples: ShortArray): Float? {
        if (samples.size < 1024) return null
        val x = FloatArray(samples.size) { samples[it] / 32768f }
        val minTau = (sampleRate / maxHz).toInt().coerceAtLeast(2)
        val maxTau = (sampleRate / minHz).toInt().coerceAtMost(x.size / 2)
        val diff = FloatArray(maxTau + 1)
        for (tau in 1..maxTau) {
            var sum = 0f
            val limit = x.size - tau
            var i = 0
            while (i < limit) {
                val d = x[i] - x[i + tau]
                sum += d * d
                i++
            }
            diff[tau] = sum
        }
        val cmnd = FloatArray(maxTau + 1)
        cmnd[0] = 1f
        var running = 0f
        for (tau in 1..maxTau) {
            running += diff[tau]
            cmnd[tau] = if (running > 0f) diff[tau] * tau / running else 1f
        }
        var tau = minTau
        while (tau <= maxTau) {
            if (cmnd[tau] < threshold) {
                while (tau + 1 <= maxTau && cmnd[tau + 1] < cmnd[tau]) tau++
                val betterTau = parabolic(cmnd, tau)
                return (sampleRate / betterTau).toFloat().takeIf { it in minHz..maxHz }
            }
            tau++
        }
        return null
    }

    private fun parabolic(values: FloatArray, tau: Int): Float {
        if (tau <= 0 || tau >= values.lastIndex) return tau.toFloat()
        val s0 = values[tau - 1]; val s1 = values[tau]; val s2 = values[tau + 1]
        val denom = 2f * (2f * s1 - s2 - s0)
        return if (abs(denom) < 1e-9f) tau.toFloat() else tau + (s2 - s0) / denom
    }
}

data class PitchReading(val frequencyHz: Float, val note: String, val cents: Float)

fun frequencyToReading(hz: Float): PitchReading {
    val midi = (69.0 + 12.0 * (ln(hz / 440.0) / ln(2.0))).toInt()
    val names = arrayOf("C", "C♯", "D", "D♯", "E", "F", "F♯", "G", "G♯", "A", "A♯", "B")
    val note = names[Math.floorMod(midi, 12)] + (midi / 12 - 1)
    val exact = 440.0 * Math.pow(2.0, (midi - 69) / 12.0)
    val cents = (1200.0 * (ln(hz / exact) / ln(2.0))).toFloat()
    return PitchReading(hz, note, cents)
}
