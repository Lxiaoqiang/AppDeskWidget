package com.hli.widgetdemo.widget

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.state.GlanceStateDefinition
import java.io.File

object WidgetGlanceStateDefinition : GlanceStateDefinition<Preferences> {

    private const val TAG = "WidgetGlanceState"
    private const val DATA_STORE_FILENAME_PREFIX = "widget_state_"

    // Cache for DataStore instances per fileKey
    private val dataStoreCache = mutableMapOf<String, DataStore<Preferences>>()

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<Preferences> {
        Log.d(TAG, "getDataStore called for fileKey: $fileKey")

        // Return cached DataStore or create a new one for this fileKey
        return synchronized(dataStoreCache) {
            dataStoreCache.getOrPut(fileKey) {
                Log.d(TAG, "Creating new DataStore for fileKey: $fileKey")
                PreferenceDataStoreFactory.create {
                    getLocation(context, fileKey)
                }
            }
        }
    }

    override fun getLocation(context: Context, fileKey: String): File {
        // Each widget gets its own file based on fileKey (which is derived from glanceId)
        val fileName = "$DATA_STORE_FILENAME_PREFIX$fileKey.preferences_pb"
        return File(context.applicationContext.filesDir, "datastore/$fileName")
    }

    object Keys {
        val CURRENT_FRAME = intPreferencesKey("current_frame")
        val WIDGET_STYLE = intPreferencesKey("widget_style")
    }
}
