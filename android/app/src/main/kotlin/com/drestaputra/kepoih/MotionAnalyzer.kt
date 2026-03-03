package com.drestaputra.kepoih

import kotlin.math.sqrt

class MotionAnalyzer {
    // Threshold and Sensitivity mappings
    // deviation = sqrt(x² + y²)

    // Smooth filtered values
    private var filteredX = 0f
    private var filteredY = 0f

    // Low pass filter factor
    private val alpha = 0.2f

    // Sensitivities map
    // High sensitivity = higher blur mapping
    private var sensitivityMultiplier = 1.0f

    fun setSensitivity(level: Float) {
        sensitivityMultiplier = level
    }

    fun analyze(x: Float, y: Float, z: Float): Float {
        // Low pass filter
        filteredX = filteredX * (1 - alpha) + x * alpha
        filteredY = filteredY * (1 - alpha) + y * alpha

        // Deviation Calculation (section 24)
        val deviation = sqrt(filteredX * filteredX + filteredY * filteredY)

        // Dynamic Blur Calculation (section 28)
        var blurIntensity = deviation * sensitivityMultiplier

        // Activation/Deactivation threshold (section 30)
        if (deviation < 0.2f) {
            blurIntensity = 0f
        } else if (deviation > 0.4f && blurIntensity < 1f) {
            blurIntensity = 1f
        }

        // Clamp 0 to 3
        return blurIntensity.coerceIn(0f, 3f)
    }
}