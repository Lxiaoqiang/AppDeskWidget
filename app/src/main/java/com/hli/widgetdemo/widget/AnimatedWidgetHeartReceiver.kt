package com.hli.widgetdemo.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnimatedWidgetHeartReceiver : GlanceAppWidgetReceiver() {

    companion object {
        private const val TAG = "WidgetHeartReceiver"
    }

    override val glanceAppWidget: GlanceAppWidget = AnimatedWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled - Heart widget added, starting animation")
        WidgetAnimationWorker.enqueue(context.applicationContext)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled - Last Heart widget removed")
        checkAndStopAnimation(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "onDeleted - Heart Widget IDs: ${appWidgetIds.joinToString()}")
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate - Heart Widget IDs: ${appWidgetIds.joinToString()}")

        // Initialize widget state with Heart style
        CoroutineScope(Dispatchers.IO).launch {
            val glanceManager = GlanceAppWidgetManager(context)

            appWidgetIds.forEach { appWidgetId ->
                try {
                    val glanceId = glanceManager.getGlanceIdBy(appWidgetId)

                    updateAppWidgetState(
                        context = context,
                        definition = WidgetGlanceStateDefinition,
                        glanceId = glanceId
                    ) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[WidgetGlanceStateDefinition.Keys.CURRENT_FRAME] = 0
                            this[WidgetGlanceStateDefinition.Keys.WIDGET_STYLE] = WidgetPreviewStyle.STYLE_HEART.ordinal
                        }
                    }

                    Log.d(TAG, "Heart widget $appWidgetId initialized")
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing Heart widget $appWidgetId", e)
                }
            }

            // Start animation
            Log.d(TAG, "Starting animation from onUpdate")
            WidgetAnimationWorker.enqueue(context.applicationContext)
        }
    }

    private fun checkAndStopAnimation(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val glanceManager = GlanceAppWidgetManager(context)
            val totalWidgets = glanceManager.getGlanceIds(AnimatedWidget::class.java).size

            if (totalWidgets == 0) {
                Log.d(TAG, "No widgets remaining, stopping animation")
                WidgetAnimationWorker.cancel(context.applicationContext)
            }
        }
    }
}
