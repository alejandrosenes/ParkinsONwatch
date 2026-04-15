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
    
    // Reusable buffers to avoid allocations
    private val magnitudeBuffer = FloatArray(fftSize)
    private val realBuffer = FloatArray(fftSize)
    private val imagBuffer = FloatArray(fftSize)
    private val frequencyBuffer = FloatArray(fftSize / 2)
    private val xBuffer = FloatArray(fftSize)
    private val yBuffer = FloatArray(fftSize)
    private val zBuffer = FloatArray(fftSize)

    fun analyze(accelData: List<FloatArray>): TremorAnalysis {
        if (accelData.size < fftSize) {
            return TremorAnalysis(0f, 0f, 0f, 0f, 0f, 0)
        }

        // Pre-fill coordinate buffers
        val size = accelData.lastIndex
        for (i in 0 until fftSize) {
            val data = accelData[size - fftSize + i + 1]
            xBuffer[i] = data[0]
            yBuffer[i] = data[1]
            zBuffer[i] = data[2]
        }

        // Calculate magnitude with gravity compensation
        for (i in 0 until fftSize) {
            magnitudeBuffer[i] = sqrt(xBuffer[i] * xBuffer[i] + yBuffer[i] * yBuffer[i] + zBuffer[i] * zBuffer[i]) - 9.81f
        }

        val rmsAmplitude = calculateRMS(magnitudeBuffer)
        computeFFT(magnitudeBuffer, realBuffer, imagBuffer)
        computeFrequencies(frequencyBuffer)

        val bandPower3_7 = calculateBandPower(realBuffer, imagBuffer, frequencyBuffer, pdFrequencyRange)
        val bandPower8_12 = calculateBandPower(realBuffer, imagBuffer, frequencyBuffer, posturalFrequencyRange)
        val dominantFrequency = findDominantFrequency(realBuffer, imagBuffer, frequencyBuffer)

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
        var sumSquares = 0f
        val size = data.size
        for (i in 0 until size) {
            val value = data[i]
            sumSquares += value * value
        }
        return sqrt(sumSquares / size)
    }

    private fun computeFFT(data: FloatArray, real: FloatArray, imag: FloatArray) {
        val n = data.size
        System.arraycopy(data, 0, real, 0, n)
        
        // Zero out imaginary buffer
        for (i in 0 until n) {
            imag[i] = 0f
        }

        var size = n
        while (size > 1) {
            size /= 2
            val step = n / size
            val halfSize = size * 2
            
            for (i in 0 until n step step) {
                for (j in 0 until size) {
                    val idx1 = i + j
                    val idx2 = idx1 + size
                    val angle = -2.0 * PI * j / halfSize
                    
                    val cosVal = cos(angle).toFloat()
                    val sinVal = sin(angle).toFloat()

                    val tReal = real[idx2] * cosVal - imag[idx2] * sinVal
                    val tImag = real[idx2] * sinVal + imag[idx2] * cosVal

                    real[idx2] = real[idx1] - tReal
                    imag[idx2] = imag[idx1] - tImag
                    real[idx1] = real[idx1] + tReal
                    imag[idx1] = imag[idx1] + tImag
                }
            }
        }
    }

    private fun computeFrequencies(buffer: FloatArray) {
        val n = fftSize
        val invN = sampleRate / n
        for (i in buffer.indices) {
            buffer[i] = i * invN
        }
    }

    private fun calculateBandPower(
        real: FloatArray,
        imag: FloatArray,
        frequencies: FloatArray,
        range: ClosedFloatingPointRange<Float>
    ): Float {
        var power = 0f
        val size = frequencies.size
        for (i in 0 until size) {
            val freq = frequencies[i]
            if (freq >= range.start && freq <= range.endInclusive) {
                val magnitude = real[i] * real[i] + imag[i] * imag[i]
                power += magnitude
            }
        }
        return power
    }

    private fun findDominantFrequency(real: FloatArray, imag: FloatArray, frequencies: FloatArray): Float {
        var maxIndex = 0
        var maxValue = 0f

        val size = real.size
        for (i in 0 until size) {
            val magnitude = real[i] * real[i] + imag[i] * imag[i]
            if (magnitude > maxValue) {
                maxValue = magnitude
                maxIndex = i
            }
        }

        return if (maxIndex < frequencies.size) {
            frequencies[maxIndex]
        } else 0f
    }

    private fun calculateTPIScore(
        rms: Float,
        bandPower3_7: Float,
        bandPower8_12: Float
    ): Float {
        val noise = 0.1f
        val denominator = bandPower3_7 + bandPower8_12 + noise
        val ratio = bandPower3_7 / denominator
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
