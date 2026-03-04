package com.drestaputra.kepoih

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.drestaputra.kepoih/privacy"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            when (call.method) {
                "startService" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                        result.error("PERMISSION_DENIED", "System Alert Window permission not granted", null)
                        return@setMethodCallHandler
                    }

                    val sensitivity = call.argument<Double>("sensitivity")?.toFloat() ?: 1.2f
                    val intent = Intent(this, GyroscopeService::class.java).apply {
                        putExtra("sensitivity", sensitivity)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent)
                    } else {
                        startService(intent)
                    }
                    result.success(true)
                }
                "stopService" -> {
                    val intent = Intent(this, GyroscopeService::class.java)
                    stopService(intent)
                    result.success(true)
                }
                "isServiceRunning" -> {
                    result.success(GyroscopeService.isServiceRunning)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }
}
