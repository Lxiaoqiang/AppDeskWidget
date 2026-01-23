package com.hli.widgetdemo.widget

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters

class StartAnimationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "StartAnimationWorker"
        private const val WORK_NAME = "start_animation_work"

        fun enqueue(context: Context) {
            Log.d(TAG, "Enqueueing work to start animation service")
            val request = OneTimeWorkRequestBuilder<StartAnimationWorker>()
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork - Starting animation service")
        return try {
            WidgetAnimationService.start(applicationContext)
            Log.d(TAG, "Animation service started successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start animation service", e)
            Result.failure()
        }
    }
}
