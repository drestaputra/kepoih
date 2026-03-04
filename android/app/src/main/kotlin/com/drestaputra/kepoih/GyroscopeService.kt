package com.drestaputra.kepoih

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.util.Log
import kotlin.math.abs

class GyroscopeService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private lateinit var overlayManager: OverlayManager
    private lateinit var motionAnalyzer: MotionAnalyzer

    companion object {
        const val CHANNEL_ID = "kepoih_channel"
        const val NOTIFICATION_ID = 1001

        // Settings static
        var isServiceRunning = false
    }

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        overlayManager = OverlayManager(this)
        motionAnalyzer = MotionAnalyzer()

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sensitivity = intent?.getFloatExtra("sensitivity", 1.2f) ?: 1.2f
        motionAnalyzer.setSensitivity(sensitivity)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("KepoIh Privacy Protection")
            .setContentText("Monitoring for screen peeking...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock) // Standard icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // 50Hz = 20,000 microseconds (SensorManager.SENSOR_DELAY_GAME)
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }

        overlayManager.showOverlay()
        isServiceRunning = true

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        overlayManager.hideOverlay()
        isServiceRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // --- SensorEventListener ---

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Moving walking/pocket spike filter (Section 37)
            val magnitude = abs(x) + abs(y) + abs(z)
            if (magnitude > 8f) {
                // Ignore event
                return
            }

            val blurIntensity = motionAnalyzer.analyze(x, y, z)

            // Only update if value actually changed significantly to save UI thread
            overlayManager.updateOverlay(blurIntensity)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    // --- Private ---

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "KepoIh Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}