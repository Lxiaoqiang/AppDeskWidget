package com.hli.widgetdemo.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class WidgetSlot4Receiver : GlanceAppWidgetReceiver() {

    companion object {
        private const val TAG = "WidgetSlot4Receiver"
        private const val SLOT_INDEX = 4
    }

    override val glanceAppWidget: GlanceAppWidget = SlotWidget(SLOT_INDEX)

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled - First Slot 4 widget added")
        WidgetAnimationWorker.enqueue(context.applicationContext)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "onDisabled - Last Slot 4 widget removed")
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
