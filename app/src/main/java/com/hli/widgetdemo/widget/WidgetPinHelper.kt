package com.hli.widgetdemo.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.hli.widgetdemo.R

object WidgetPinHelper {

    private const val TAG = "WidgetPinHelper"

    fun isPinWidgetSupported(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Log.d(TAG, "Pin widget not supported: API < 26")
            return false
        }
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val supported = appWidgetManager.isRequestPinAppWidgetSupported
        Log.d(TAG, "isPinWidgetSupported: $supported")
        return supported
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun requestPinWidget(
        context: Context,
        style: WidgetPreviewStyle
    ): Boolean {
        Log.d(TAG, "requestPinWidget: style=$style")
        val appWidgetManager = AppWidgetManager.getInstance(context)

        if (!appWidgetManager.isRequestPinAppWidgetSupported) {
            Log.w(TAG, "Pin widget not supported")
            return false
        }

        // Select the correct receiver based on the widget style
        val receiverClass = when (style) {
            WidgetPreviewStyle.STYLE_JERRY -> AnimatedWidgetJerryReceiver::class.java
            WidgetPreviewStyle.STYLE_STAR -> AnimatedWidgetStarReceiver::class.java
            WidgetPreviewStyle.STYLE_HEART -> AnimatedWidgetHeartReceiver::class.java
        }
        val widgetProvider = ComponentName(context, receiverClass)
        Log.d(TAG, "Widget provider: $widgetProvider")

        // Create preview RemoteViews with the selected style's preview image
        val previewViews = RemoteViews(context.packageName, R.layout.widget_preview_layout).apply {
            setImageViewResource(R.id.preview_image, style.previewResId)
        }

        val extras = Bundle().apply {
            putInt(EXTRA_WIDGET_STYLE, style.ordinal)
            // Set the preview image for the pin dialog
            putParcelable(AppWidgetManager.EXTRA_APPWIDGET_PREVIEW, previewViews)
        }

        val successCallback = createPinSuccessCallback(context, style)

        val result = appWidgetManager.requestPinAppWidget(
            widgetProvider,
            extras,
            successCallback
        )
        Log.d(TAG, "requestPinAppWidget result: $result")
        return result
    }

    private fun createPinSuccessCallback(
        context: Context,
        style: WidgetPreviewStyle
    ): PendingIntent {
        val intent = Intent(context, WidgetPinReceiver::class.java).apply {
            action = ACTION_WIDGET_PINNED
            putExtra(EXTRA_WIDGET_STYLE, style.ordinal)
        }

        return PendingIntent.getBroadcast(
            context,
            style.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    const val ACTION_WIDGET_PINNED = "com.hli.widgetdemo.WIDGET_PINNED"
    const val EXTRA_WIDGET_STYLE = "extra_widget_style"
}
