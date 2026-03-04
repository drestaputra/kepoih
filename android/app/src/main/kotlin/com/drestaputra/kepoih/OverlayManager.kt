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
    private val paint = Paint().apply {
        style = Paint.Style.FILL
    }
    private var blurIntensity: Float = 0f

    // Matrix of noise properties
    private val lineSpacing = 6f
    private val lineWidth = 3f

    fun updateBlurIntensity(intensity: Float) {
        if (this.blurIntensity != intensity) {
            this.blurIntensity = intensity
            invalidate() // Request redraw
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (blurIntensity <= 0) return

        // To simulate a physical privacy screen (like Samsung's), we can draw a fine
        // grid or interleaved lines that obscure the screen. When viewed straight,
        // it acts like a mild tint. When viewed from an angle (blurIntensity > 0),
        // the lines become thicker or darker to block out more light.

        // 1. Draw a dark tint based on intensity to simulate brightness reduction
        val alphaBase = (blurIntensity / 3f * 200).toInt().coerceIn(0, 200)
        canvas.drawColor(Color.argb(alphaBase, 0, 0, 0))

        // 2. Draw fine vertical lines to simulate privacy louvers
        // The opacity and thickness of lines increase with blurIntensity
        val lineAlpha = (blurIntensity / 3f * 255).toInt().coerceIn(0, 255)
        paint.color = Color.argb(lineAlpha, 0, 0, 0)

        val width = width.toFloat()
        val height = height.toFloat()

        // Increase line thickness based on intensity
        val currentLineWidth = lineWidth + (blurIntensity * 1.5f)

        var x = 0f
        while (x < width) {
            canvas.drawRect(x, 0f, x + currentLineWidth, height, paint)
            x += lineSpacing + currentLineWidth
        }

        // 3. Draw random noise to further scatter pixels and obscure text
        val numNoises = (blurIntensity * 1000).toInt()
        val noiseSize = 10f
        paint.color = Color.BLACK

        for (i in 0 until numNoises) {
            val nx = Random.nextFloat() * width
            val ny = Random.nextFloat() * height
            val alphaNoise = Random.nextInt(150, 255)

            paint.alpha = alphaNoise
            canvas.drawRect(nx, ny, nx + noiseSize, ny + noiseSize, paint)
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
