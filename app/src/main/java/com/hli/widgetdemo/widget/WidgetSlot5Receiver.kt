package com.hli.widgetdemo.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class WidgetSlot5Receiver : GlanceAppWidgetReceiver() {

    companion object {
        private const val TAG = "WidgetSlot5Receiver"
        private const val SLOT_INDEX = 5
    }

    override val glanceAppWidget: GlanceAppWidget = SlotWidget(SLOT_INDEX)

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled - First Slot 5 widget added")
        WidgetAnimationWorker.enqueue(context.applicationContext)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled - Last Slot 5 widget removed")
        checkAndStopAnimation(context)
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
        initializeSlotWidget(context, appWidgetIds, SLOT_INDEX)
    }
}
