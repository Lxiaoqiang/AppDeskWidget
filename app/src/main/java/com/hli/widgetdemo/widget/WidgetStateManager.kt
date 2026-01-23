package com.hli.widgetdemo.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "widget_preferences"
)

object WidgetStateManager {

    private val CURRENT_FRAME_KEY = intPreferencesKey("current_frame")

    private fun widgetStyleKey(widgetId: Int) =
        intPreferencesKey("widget_style_$widgetId")

    suspend fun getCurrentFrame(context: Context): Int {
        return context.widgetDataStore.data.map { preferences ->
            preferences[CURRENT_FRAME_KEY] ?: 0
        }.first()
    }

    suspend fun setCurrentFrame(context: Context, frame: Int) {
        context.widgetDataStore.edit { preferences ->
            preferences[CURRENT_FRAME_KEY] = frame
        }
    }

    suspend fun getWidgetStyle(context: Context, glanceId: GlanceId): WidgetPreviewStyle {
        val manager = GlanceAppWidgetManager(context)
        val appWidgetId = manager.getAppWidgetId(glanceId)
        return getWidgetStyleById(context, appWidgetId)
    }

    suspend fun getWidgetStyleById(context: Context, appWidgetId: Int): WidgetPreviewStyle {
        return context.widgetDataStore.data.map { preferences ->
            val styleOrdinal = preferences[widgetStyleKey(appWidgetId)] ?: 0
            WidgetPreviewStyle.fromOrdinal(styleOrdinal)
        }.first()
    }

    suspend fun setWidgetStyle(context: Context, appWidgetId: Int, style: WidgetPreviewStyle) {
        context.widgetDataStore.edit { preferences ->
            preferences[widgetStyleKey(appWidgetId)] = style.ordinal
        }
    }

    suspend fun removeWidgetStyle(context: Context, appWidgetId: Int) {
        context.widgetDataStore.edit { preferences ->
            preferences.remove(widgetStyleKey(appWidgetId))
        }
    }
}
