package com.drestaputra.kepoih

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.view.View
import android.view.WindowManager
import kotlin.random.Random

class PrivacyOverlayView(context: Context) : View(context) {
    private val paint = Paint()
    private var blurIntensity: Float = 0f

    // Matrix of noise properties
    private val noiseSize = 20f

    fun updateBlurIntensity(intensity: Float) {
        if (this.blurIntensity != intensity) {
            this.blurIntensity = intensity
            invalidate() // Request redraw
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (blurIntensity <= 0) return

        // 1. Draw a dark tint based on intensity
        // Mapping blur intensity (0 to 3) to alpha (0 to 220)
        val alphaBase = (blurIntensity / 3f * 220).toInt().coerceIn(0, 220)
        canvas.drawColor(Color.argb(alphaBase, 0, 0, 0))

        // 2. Draw random noise mask (Mode 2)
        // More intensity = more noisy blocks
        paint.color = Color.BLACK

        val width = width.toFloat()
        val height = height.toFloat()

        val numNoises = (blurIntensity * 500).toInt()

        for (i in 0 until numNoises) {
            val x = Random.nextFloat() * width
            val y = Random.nextFloat() * height
            val alphaNoise = Random.nextInt(100, 255)

            paint.alpha = alphaNoise
            canvas.drawRect(x, y, x + noiseSize, y + noiseSize, paint)
        }
    }
}

class OverlayManager(private val context: Context) {
    private var windowManager: WindowManager? = null
    private var overlayView: PrivacyOverlayView? = null

    init {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    fun showOverlay() {
        if (overlayView != null) return

        overlayView = PrivacyOverlayView(context)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        windowManager?.addView(overlayView, layoutParams)
    }

    fun updateOverlay(blurIntensity: Float) {
        overlayView?.updateBlurIntensity(blurIntensity)
    }

    fun hideOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }
}
