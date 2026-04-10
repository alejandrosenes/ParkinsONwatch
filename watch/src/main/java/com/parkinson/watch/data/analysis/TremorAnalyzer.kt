package com.parkinson.watch.data.analysis

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class TremorAnalysis(
    val tpiScore: Float,
    val dominantFrequency: Float,
    val bandPower3_7: Float,
    val bandPower8_12: Float,
    val rmsAmplitude: Float,
    val severity: Int
)

@Singleton
class TremorAnalyzer @Inject constructor() {

    private val sampleRate = 50f
    private val fftSize = 128
    private val pdFrequencyRange = 3f..7f
    private val posturalFrequencyRange = 8f..12f

    fun analyze(accelData: List<FloatArray>): TremorAnalysis {
        if (accelData.size < fftSize) {
            return TremorAnalysis(0f, 0f, 0f, 0f, 0f, 0)
        }

        val x = accelData.takeLast(fftSize).map { it[0] }.toFloatArray()
        val y = accelData.takeLast(fftSize).map { it[1] }.toFloatArray()
        val z = accelData.takeLast(fftSize).map { it[2] }.toFloatArray()

        val magnitude = FloatArray(fftSize) { i ->
            sqrt(x[i].pow(2) + y[i].pow(2) + z[i].pow(2)) - 9.81f
        }

        val rmsAmplitude = calculateRMS(magnitude)
        val fftResult = computeFFT(magnitude)
        val frequencies = computeFrequencies()

        val bandPower3_7 = calculateBandPower(fftResult, frequencies, pdFrequencyRange)
        val bandPower8_12 = calculateBandPower(fftResult, frequencies, posturalFrequencyRange)
        val dominantFrequency = findDominantFrequency(fftResult, frequencies)

        val tpiScore = calculateTPIScore(rmsAmplitude, bandPower3_7, bandPower8_12)
        val severity = calculateSeverity(tpiScore)

        return TremorAnalysis(
            tpiScore = tpiScore,
            dominantFrequency = dominantFrequency,
            bandPower3_7 = bandPower3_7,
            bandPower8_12 = bandPower8_12,
            rmsAmplitude = rmsAmplitude,
            severity = severity
        )
    }

    private fun calculateRMS(data: FloatArray): Float {
        val sumSquares = data.fold(0f) { acc, value -> acc + value * value }
        return sqrt(sumSquares / data.size)
    }

    private fun computeFFT(data: FloatArray): FloatArray {
        val n = data.size
        val real = data.copyOf()
        val imag = FloatArray(n)

        var size = n
        while (size > 1) {
            size /= 2
            for (i in 0 until n step n / size) {
                for (j in 0 until size) {
                    val idx1 = i + j
                    val idx2 = idx1 + size
                    val angle = -2.0 * PI * j / (2 * size)
                    val cos = cos(angle).toFloat()
                    val sin = sin(angle).toFloat()

                    val tReal = real[idx2] * cos - imag[idx2] * sin
                    val tImag = real[idx2] * sin + imag[idx2] * cos

                    real[idx2] = real[idx1] - tReal
                    imag[idx2] = imag[idx1] - tImag
                    real[idx1] = real[idx1] + tReal
                    imag[idx1] = imag[idx1] + tImag
                }
            }
        }

        return FloatArray(n / 2) { i ->
            sqrt(real[i].pow(2) + imag[i].pow(2))
        }
    }

    private fun computeFrequencies(): FloatArray {
        val n = fftSize
        return FloatArray(n / 2) { i ->
            i * sampleRate / n
        }
    }

    private fun calculateBandPower(
        fft: FloatArray,
        frequencies: FloatArray,
        range: ClosedFloatingPointRange<Float>
    ): Float {
        var power = 0f
        for (i in frequencies.indices) {
            if (frequencies[i] in range) {
                power += fft[i].pow(2)
            }
        }
        return power
    }

    private fun findDominantFrequency(fft: FloatArray, frequencies: FloatArray): Float {
        var maxIndex = 0
        var maxValue = 0f

        for (i in fft.indices) {
            if (fft[i] > maxValue) {
                maxValue = fft[i]
                maxIndex = i
            }
        }

        return if (frequencies.isNotEmpty() && maxIndex < frequencies.size) {
            frequencies[maxIndex]
        } else 0f
    }

    private fun calculateTPIScore(
        rms: Float,
        bandPower3_7: Float,
        bandPower8_12: Float
    ): Float {
        val noise = 0.1f
        val ratio = bandPower3_7 / (bandPower3_7 + bandPower8_12 + noise)
        val tpi = rms * ratio * 10f
        return tpi.coerceIn(0f, 10f)
    }

    private fun calculateSeverity(tpiScore: Float): Int = when {
        tpiScore < 2 -> 0
        tpiScore < 4 -> 1
        tpiScore < 6 -> 2
        tpiScore < 8 -> 3
        else -> 4
    }

    fun getDominantFrequency(): Float {
        return 0f
    }
}
