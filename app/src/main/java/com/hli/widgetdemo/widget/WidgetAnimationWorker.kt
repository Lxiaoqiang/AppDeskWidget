package com.hli.widgetdemo.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class WidgetAnimationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "WidgetAnimationWorker"
        private const val WORK_NAME = "widget_animation_work"
        private const val TOTAL_FRAMES = 10
        private const val FRAME_INTERVAL_MS = 100L // 10fps = 100ms per frame
        private const val ANIMATION_DURATION_MS = 60_000L // Run for 1 minute then restart

        fun enqueue(context: Context) {
            Log.d(TAG, "Enqueueing animation work")
            val request = OneTimeWorkRequestBuilder<WidgetAnimationWorker>()
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
}