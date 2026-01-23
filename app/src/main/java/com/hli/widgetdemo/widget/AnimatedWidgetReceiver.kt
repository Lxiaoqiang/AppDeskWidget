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
        Log.d(TAG, "onEnabled - First widget added, starting animation")
        WidgetAnimationWorker.enqueue(context.applicationContext)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled - Last widget removed, stopping animation")
        WidgetAnimationWorker.cancel(context.applicationContext)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "onDeleted - Widget IDs: ${appWidgetIds.joinToString()}")
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate - Widget IDs: ${appWidgetIds.joinToString()}")

        // Initialize widget state and start animation
        CoroutineScope(Dispatchers.IO).launch {
            val glanceManager = GlanceAppWidgetManager(context)

            appWidgetIds.forEach { appWidgetId ->
                try {
                    val glanceId = glanceManager.getGlanceIdBy(appWidgetId)

                    // Only initialize frame counter, not style
                    // Style is set by WidgetPinReceiver for app-pinned widgets
                    // For desktop-added widgets, AnimatedWidget defaults to JERRY (ordinal 0)
                    updateAppWidgetState(
                        context = context,
                        definition = WidgetGlanceStateDefinition,
                        glanceId = glanceId
                    ) { prefs ->
                        prefs.toMutablePreferences().apply {
                            if (!contains(WidgetGlanceStateDefinition.Keys.CURRENT_FRAME)) {
                                this[WidgetGlanceStateDefinition.Keys.CURRENT_FRAME] = 0
                            }
                            // Don't initialize WIDGET_STYLE here to avoid race condition
                            // with WidgetPinReceiver which sets the correct style
                        }
                    }

                    Log.d(TAG, "Widget $appWidgetId frame counter initialized")
                } catch (e: Exception) {
                    Log.e(TAG, "Error initializing widget $appWidgetId", e)
                }
            }

            // Start animation
            Log.d(TAG, "Starting animation from onUpdate")
            WidgetAnimationWorker.enqueue(context.applicationContext)
        }
    }
}
