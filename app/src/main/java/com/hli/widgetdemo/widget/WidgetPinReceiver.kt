package com.hli.widgetdemo.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetPinReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "WidgetPinReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: action=${intent.action}")

        if (intent.action == WidgetPinHelper.ACTION_WIDGET_PINNED) {
            val styleOrdinal = intent.getIntExtra(WidgetPinHelper.EXTRA_WIDGET_STYLE, 0)
            val style = WidgetPreviewStyle.fromOrdinal(styleOrdinal)
            Log.d(TAG, "Widget pinned with style: $style (ordinal=$styleOrdinal)")

            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            Log.d(TAG, "AppWidgetId: $appWidgetId")

            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val glanceManager = GlanceAppWidgetManager(context)
                        val glanceId = glanceManager.getGlanceIdBy(appWidgetId)

                        // Save style using Glance state
                        updateAppWidgetState(
                            context = context,
                            definition = WidgetGlanceStateDefinition,
                            glanceId = glanceId
                        ) { prefs ->
                            prefs.toMutablePreferences().apply {
                                this[WidgetGlanceStateDefinition.Keys.WIDGET_STYLE] = styleOrdinal
                                this[WidgetGlanceStateDefinition.Keys.CURRENT_FRAME] = 0
                            }
                        }
                        Log.d(TAG, "Widget style saved to Glance state: $styleOrdinal")

                        // Update widget
                        AnimatedWidget().update(context, glanceId)
                        Log.d(TAG, "Widget updated")

                        // Start animation service
                        StartAnimationWorker.enqueue(context.applicationContext)
                        Log.d(TAG, "Animation service start requested via WorkManager")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in onReceive", e)
                    }
                }
            } else {
                Log.w(TAG, "Invalid appWidgetId, starting service anyway")
                StartAnimationWorker.enqueue(context.applicationContext)
            }
        }
    }
}
