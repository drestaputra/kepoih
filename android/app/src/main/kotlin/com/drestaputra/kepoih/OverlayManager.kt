package com.drestaputra.kepoih

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Shader
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign
import kotlin.random.Random

class PrivacyOverlayView(context: Context) : View(context) {
    private val linePaint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val noisePaint = Paint().apply {
        style = Paint.Style.FILL
    }

    private var targetBlurIntensity: Float = 0f
    private var currentBlurIntensity: Float = 0f

    // Matrix of noise properties
    private val lineSpacing = 6f
    private val lineWidth = 3f

    private var lastDrawTime = 0L
    private var noiseShader: BitmapShader? = null

    init {
        createNoiseShader()
    }

    private fun createNoiseShader() {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val dots = (size * size) / 3 // Density of noise
        for (i in 0 until dots) {
            val x = Random.nextInt(size).toFloat()
            val y = Random.nextInt(size).toFloat()
            paint.color = Color.argb(Random.nextInt(50, 200), 0, 0, 0)
            canvas.drawPoint(x, y, paint)
            // Draw slightly larger blocks for some noise
            if (i % 5 == 0) {
                canvas.drawRect(x, y, x + 2f, y + 2f, paint)
            }
        }
        noiseShader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        noisePaint.shader = noiseShader
    }

    fun updateBlurIntensity(intensity: Float) {
        if (this.targetBlurIntensity != intensity) {
            this.targetBlurIntensity = intensity
            invalidate() // Request redraw
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val now = SystemClock.uptimeMillis()
        if (lastDrawTime > 0) {
            // max dt is 50ms to prevent large jumps
            val dt = min(now - lastDrawTime, 50L)
            if (currentBlurIntensity != targetBlurIntensity) {
                val diff = targetBlurIntensity - currentBlurIntensity
                // Transition duration: 120ms to cover a distance of 1.0 (or to cover any diff in 120ms?)
                // AGENTS.md: "blur transition duration: 120ms". Let's make the speed constant
                // so a diff of 1.0 takes 120ms. Speed = 1.0 / 120f per ms
                val step = (dt / 120f)
                if (abs(diff) <= step) {
                    currentBlurIntensity = targetBlurIntensity
                } else {
                    currentBlurIntensity += sign(diff) * step
                }
                invalidate() // Keep animating
            }
        } else {
            currentBlurIntensity = targetBlurIntensity
            if (currentBlurIntensity != 0f) invalidate()
        }
        lastDrawTime = now

        if (currentBlurIntensity <= 0f) return

        val blur = currentBlurIntensity.coerceIn(0f, 3f)

        // 1. Draw a dark tint based on intensity to simulate brightness reduction
        val alphaBase = (blur / 3f * 200).toInt().coerceIn(0, 200)
        canvas.drawColor(Color.argb(alphaBase, 0, 0, 0))

        // 2. Draw fine vertical lines to simulate privacy louvers
        val lineAlpha = (blur / 3f * 255).toInt().coerceIn(0, 255)
        linePaint.color = Color.argb(lineAlpha, 0, 0, 0)

        val w = width.toFloat()
        val h = height.toFloat()

        val currentLineWidth = lineWidth + (blur * 1.5f)

        var x = 0f
        while (x < w) {
            canvas.drawRect(x, 0f, x + currentLineWidth, h, linePaint)
            x += lineSpacing + currentLineWidth
        }

        // 3. Draw repeating static noise to obscure pixels
        val noiseAlpha = (blur / 3f * 255).toInt().coerceIn(0, 255)
        noisePaint.alpha = noiseAlpha
        canvas.drawRect(0f, 0f, w, h, noisePaint)
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
