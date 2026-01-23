package com.hli.widgetdemo.widget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.hli.widgetdemo.MainActivity
import com.hli.widgetdemo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WidgetAnimationService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentFrame = 0

    private val animationRunnable = object : Runnable {
        override fun run() {
            currentFrame = (currentFrame + 1) % TOTAL_FRAMES
            Log.d(TAG, "Animation frame: $currentFrame")
            updateWidgets()
            handler.postDelayed(this, FRAME_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        Log.d(TAG, "Service started foreground")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")
        when (intent?.action) {
            ACTION_START -> startAnimation()
            ACTION_STOP -> stopAnimation()
        }
        return START_STICKY
    }

    private fun startAnimation() {
        Log.d(TAG, "Starting animation")
        handler.removeCallbacks(animationRunnable)
        handler.post(animationRunnable)
    }

    private fun stopAnimation() {
        Log.d(TAG, "Stopping animation")
        handler.removeCallbacks(animationRunnable)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateWidgets() {
        serviceScope.launch {
            try {
                val manager = GlanceAppWidgetManager(this@WidgetAnimationService)
                val glanceIds = manager.getGlanceIds(AnimatedWidget::class.java)

                Log.d(TAG, "Updating ${glanceIds.size} widgets with frame: $currentFrame")

                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(
                        context = this@WidgetAnimationService,
                        definition = WidgetGlanceStateDefinition,
                        glanceId = glanceId
                    ) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[WidgetGlanceStateDefinition.Keys.CURRENT_FRAME] = currentFrame
                        }
                    }
                }

                AnimatedWidget().updateAll(this@WidgetAnimationService)
                Log.d(TAG, "Widget updated with frame: $currentFrame")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget", e)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Widget Animation",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps widget animation running"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Widget Animation")
            .setContentText("Animation is running")
            .setSmallIcon(R.drawable.ic_widget_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        handler.removeCallbacks(animationRunnable)
        super.onDestroy()
    }

    companion object {
        private const val TAG = "WidgetAnimationService"
        const val ACTION_START = "com.hli.widgetdemo.START_ANIMATION"
        const val ACTION_STOP = "com.hli.widgetdemo.STOP_ANIMATION"
        private const val CHANNEL_ID = "widget_animation_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TOTAL_FRAMES = 10
        private const val FRAME_INTERVAL_MS = 100L // 10fps = 100ms per frame

        fun start(context: Context) {
            Log.d(TAG, "start() called")
            val intent = Intent(context, WidgetAnimationService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d(TAG, "Service start intent sent")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting service", e)
            }
        }

        fun stop(context: Context) {
            Log.d(TAG, "stop() called")
            val intent = Intent(context, WidgetAnimationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
