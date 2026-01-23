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

class AnimatedWidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        private const val TAG = "AnimatedWidgetReceiver"
    }

    override val glanceAppWidget: GlanceAppWidget = AnimatedWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled - First widget added, starting animation via WorkManager")
        StartAnimationWorker.enqueue(context.applicationContext)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled - Last widget removed, stopping animation service")
        WidgetAnimationService.stop(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "onDeleted - Widget IDs: ${appWidgetIds.joinToString()}")
        CoroutineScope(Dispatchers.IO).launch {
            appWidgetIds.forEach { widgetId ->
                WidgetStateManager.removeWidgetStyle(context, widgetId)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate - Widget IDs: ${appWidgetIds.joinToString()}")

        // Initialize widget state and start animation service
        CoroutineScope(Dispatchers.IO).launch {
            val glanceManager = GlanceAppWidgetManager(context)

            appWidgetIds.forEach { appWidgetId ->
                try {
                    val glanceId = glanceManager.getGlanceIdBy(appWidgetId)

                    // Initialize state for this widget
                    updateAppWidgetState(
                        context = context,
                        definition = WidgetGlanceStateDefinition,
                        glanceId = glanceId
                    ) { prefs ->
                        prefs.toMutablePreferences().apply {
                            if (!contains(WidgetGlanceStateDefinition.Keys.CURRENT_FRAME)) {
                                this[WidgetGlanceStateDefinition.Keys.CURRENT_FRAME] = 0
                            }
                            if (!contains(WidgetGlanceStateDefinition.Keys.WIDGET_STYLE)) {
                                this[WidgetGlanceStateDefinition.Keys.WIDGET_STYLE] = 0
                            }
                        }
                    }

                    Log.d(TAG, "Widget $appWidgetId state initialized")
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing widget $appWidgetId", e)
                }
            }

            // Start animation service via WorkManager
            Log.d(TAG, "Starting animation service via WorkManager from onUpdate")
            StartAnimationWorker.enqueue(context.applicationContext)
        }
    }
}
