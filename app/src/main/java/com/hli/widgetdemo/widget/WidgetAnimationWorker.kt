package com.hli.widgetdemo.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.hli.widgetdemo.MainActivity
import com.hli.widgetdemo.R
import kotlinx.coroutines.delay

class WidgetAnimationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "WidgetAnimationWorker"
        private const val WORK_NAME = "widget_animation_work"
        private const val CHANNEL_ID = "widget_animation_channel"
        private const val NOTIFICATION_ID = 1001
        private const val TOTAL_FRAMES = 10
        private const val FRAME_INTERVAL_MS = 100L // 10fps = 100ms per frame
        private const val ANIMATION_DURATION_MS = 60_000L // Run for 1 minute then restart

        fun enqueue(context: Context) {
            Log.d(TAG, "Enqueueing animation work")
            val request = OneTimeWorkRequestBuilder<WidgetAnimationWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }

        fun cancel(context: Context) {
            Log.d(TAG, "Cancelling animation work")
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork - Starting animation loop")

        // Set as foreground work with notification
        setForeground(createForegroundInfo())

        var currentFrame = 0
        val startTime = System.currentTimeMillis()

        try {
            while (System.currentTimeMillis() - startTime < ANIMATION_DURATION_MS) {
                if (isStopped) {
                    Log.d(TAG, "Worker stopped")
                    break
                }

                currentFrame = (currentFrame + 1) % TOTAL_FRAMES
                updateWidgets(currentFrame)
                delay(FRAME_INTERVAL_MS)
            }

            // Re-enqueue to continue animation
            if (!isStopped) {
                Log.d(TAG, "Re-enqueueing animation work")
                enqueue(applicationContext)
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in animation loop", e)
            return Result.retry()
        }
    }

    private suspend fun updateWidgets(frameIndex: Int) {
        try {
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = manager.getGlanceIds(AnimatedWidget::class.java)

            if (glanceIds.isEmpty()) {
                Log.d(TAG, "No widgets found, stopping")
                return
            }

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(
                    context = applicationContext,
                    definition = WidgetGlanceStateDefinition,
                    glanceId = glanceId
                ) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[WidgetGlanceStateDefinition.Keys.CURRENT_FRAME] = frameIndex
                    }
                }
            }

            AnimatedWidget().updateAll(applicationContext)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widgets", e)
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createNotificationChannel()

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Widget Animation")
            .setContentText("Animation is running")
            .setSmallIcon(R.drawable.ic_widget_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
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
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }
}
