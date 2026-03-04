package com.drestaputra.kepoih

import android.os.SystemClock
import kotlin.math.sqrt
import kotlin.math.max

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

    // Smart Delay (Cooldown)
    private val cooldownDurationMs = 3000L
    private var lastTriggerTime = 0L
    private var isPrivacyActive = false

    fun setSensitivity(level: Float) {
        sensitivityMultiplier = level
    }

    fun analyze(x: Float, y: Float, z: Float): Float {
        // Low pass filter
        filteredX = filteredX * (1 - alpha) + x * alpha
        filteredY = filteredY * (1 - alpha) + y * alpha

        // Deviation Calculation (section 24)
        val deviation = sqrt(filteredX * filteredX + filteredY * filteredY)

        // Dynamic Blur Calculation (section 28 & 33)
        var blurIntensity = deviation * sensitivityMultiplier

        val now = SystemClock.uptimeMillis()

        // Activation/Deactivation threshold (section 30)
        if (deviation > 0.4f) {
            // Significant deviation detected, trigger privacy mode
            isPrivacyActive = true
            lastTriggerTime = now
            if (blurIntensity < 1f) {
                blurIntensity = 1f
            }
        } else if (deviation < 0.2f) {
            // User holding phone straight
            if (isPrivacyActive) {
                // Check if cooldown has finished
                if (now - lastTriggerTime > cooldownDurationMs) {
                    isPrivacyActive = false
                    blurIntensity = 0f
                } else {
                    // Maintain a minimum blur during cooldown even if deviation drops
                    blurIntensity = max(blurIntensity, 1f)
                }
            } else {
                blurIntensity = 0f
            }
        } else {
            // Between 0.2 and 0.4, maintain minimum blur if active
            if (isPrivacyActive) {
                if (now - lastTriggerTime > cooldownDurationMs) {
                    isPrivacyActive = false
                    // Don't force to 0 if we are between 0.2 and 0.4, just use calculated
                } else {
                    lastTriggerTime = now // Keep cooldown active while in this middle zone
                    blurIntensity = max(blurIntensity, 1f)
                }
            } else {
                blurIntensity = 0f
            }
        }

        // Mapping 0 to 3
        return blurIntensity.coerceIn(0f, 3f)
    }
}