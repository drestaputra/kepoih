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

    // Dynamic noise variables
    private var lastNoiseIntensity: Float = -1f
    private val noiseBitmapSize = 128
    private var noiseBitmap: Bitmap = Bitmap.createBitmap(noiseBitmapSize, noiseBitmapSize, Bitmap.Config.ARGB_8888)

    init {
        updateNoiseShader(0f)
    }

    private fun updateNoiseShader(intensity: Float) {
        if (abs(lastNoiseIntensity - intensity) < 0.1f) return
        lastNoiseIntensity = intensity

        val pixels = IntArray(noiseBitmapSize * noiseBitmapSize)
        val baseAlpha = (intensity / 3f * 255).toInt().coerceIn(100, 255)

        // Probability of a pixel being noise vs clear (0.0 to 0.8 max density)
        val density = (intensity / 3f * 0.8f).coerceIn(0f, 0.8f)

        for (i in pixels.indices) {
            if (Random.nextFloat() < density) {
                // Generate a noise pixel (dark/opaque to block view)
                val noiseAlpha = Random.nextInt(baseAlpha, 255)
                val colorOffset = Random.nextInt(-20, 20)
                val c = (50 + colorOffset).coerceIn(0, 255) // Dark gray/black noise
                pixels[i] = Color.argb(noiseAlpha, c, c, c)
            } else {
                // Clear pixel to show underlying content
                pixels[i] = Color.TRANSPARENT
            }
        }

        noiseBitmap.setPixels(pixels, 0, noiseBitmapSize, 0, 0, noiseBitmapSize, noiseBitmapSize)
        noiseShader = BitmapShader(noiseBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
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

        // 3. Draw dynamic pixel mask (noise) to obscure pixels
        updateNoiseShader(blur)
        // Reset paint alpha since shader handles alpha per pixel
        noisePaint.alpha = 255
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
