package com.hli.widgetdemo.widget

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.state.GlanceStateDefinition
import java.io.File

object WidgetGlanceStateDefinition : GlanceStateDefinition<Preferences> {

    private const val TAG = "WidgetGlanceState"
    private const val DATA_STORE_FILENAME = "widget_glance_state"

    private val Context.dataStore by preferencesDataStore(name = DATA_STORE_FILENAME)

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<Preferences> {
        Log.d(TAG, "getDataStore called for fileKey: $fileKey")
        return context.dataStore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return File(context.applicationContext.filesDir, "datastore/$DATA_STORE_FILENAME.preferences_pb")
    }

    object Keys {
        val CURRENT_FRAME = intPreferencesKey("current_frame")
        val WIDGET_STYLE = intPreferencesKey("widget_style")
    }
}
