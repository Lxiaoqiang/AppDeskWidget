package com.hli.widgetdemo.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.RemoteViews
import com.hli.widgetdemo.R
import java.io.File

/**
 * Widget provider using ViewFlipper for GIF animation.
 * Frames should be pre-extracted before adding widget.
 */
class ViewFlipperWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "ViewFlipperWidget"
        private const val PREFS_NAME = "viewflipper_widget_prefs"
        private const val FRAME_CACHE_DIR = "gif_frames"

        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        fun saveGifIdForWidget(context: Context, appWidgetId: Int, gifId: String) {
            getPrefs(context).edit()
                .putString("widget_$appWidgetId", gifId)
                .apply()
        }

        fun getGifIdForWidget(context: Context, appWidgetId: Int): String? {
            return getPrefs(context).getString("widget_$appWidgetId", null)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Handle pin callback
        if (intent.action == WidgetPinHelper.ACTION_VIEWFLIPPER_PINNED) {
            val gifId = intent.getStringExtra(WidgetPinHelper.EXTRA_GIF_ID)
            Log.d(TAG, "onReceive: VIEWFLIPPER_PINNED, gifId=$gifId")

            // Save gifId for later use when onUpdate is called
            if (gifId != null) {
                getPrefs(context).edit()
                    .putString("pending_gif_id", gifId)
                    .apply()
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate: ${appWidgetIds.size} widgets")

        // Get pending gifId (set during pin callback)
        val pendingGifId = getPrefs(context).getString("pending_gif_id", null)

        for (appWidgetId in appWidgetIds) {
            // Try to get gifId for this widget, or use pending one
            var gifId = getGifIdForWidget(context, appWidgetId)

            if (gifId == null && pendingGifId != null) {
                // This is a new widget, save the pending gifId
                gifId = pendingGifId
                saveGifIdForWidget(context, appWidgetId, gifId)
                Log.d(TAG, "Saved gifId=$gifId for widget=$appWidgetId")
            }

            if (gifId != null) {
                updateWidgetWithCachedFrames(context, appWidgetManager, appWidgetId, gifId)
            } else {
                Log.w(TAG, "No gifId for widget $appWidgetId")
            }
        }

        // Clear pending gifId
        if (pendingGifId != null) {
            getPrefs(context).edit()
                .remove("pending_gif_id")
                .apply()
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        Log.d(TAG, "onDeleted: ${appWidgetIds.size} widgets removed")
        // Clean up preferences for deleted widgets
        val editor = getPrefs(context).edit()
        for (appWidgetId in appWidgetIds) {
            editor.remove("widget_$appWidgetId")
        }
        editor.apply()
    }

    private fun updateWidgetWithCachedFrames(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        gifId: String
    ) {
        val cacheDir = File(context.filesDir, "$FRAME_CACHE_DIR/$gifId")
        val frameFiles = cacheDir.listFiles()
            ?.filter { it.extension == "png" }
            ?.sortedBy { it.name }
            ?: emptyList()

        Log.d(TAG, "updateWidgetWithCachedFrames: gifId=$gifId, frames=${frameFiles.size}")

        if (frameFiles.isEmpty()) {
            Log.w(TAG, "No cached frames found for gifId=$gifId")
            return
        }

        val views = RemoteViews(context.packageName, R.layout.widget_viewflipper_layout)

        // Clear existing views in ViewFlipper
        views.removeAllViews(R.id.widget_flipper)

        // Add frame images to ViewFlipper
        for (frameFile in frameFiles) {
            val bitmap = BitmapFactory.decodeFile(frameFile.absolutePath)
            if (bitmap != null) {
                val frameView = RemoteViews(context.packageName, R.layout.widget_frame_image)
                frameView.setImageViewBitmap(R.id.frame_image, bitmap)
                views.addView(R.id.widget_flipper, frameView)
            }
        }

        Log.d(TAG, "Added ${frameFiles.size} frames to ViewFlipper for widget $appWidgetId")

        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}