package com.hli.widgetdemo.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Slot Widget通用初始化函数
 */
fun initializeSlotWidget(context: Context, appWidgetIds: IntArray, slotIndex: Int) {
    CoroutineScope(Dispatchers.IO).launch {
        val slotManager = WidgetSlotManager(context)
        val slotConfig = slotManager.getSlotConfig(slotIndex)
        val glanceManager = GlanceAppWidgetManager(context)

        Log.d("SlotWidget$slotIndex", "Initializing ${appWidgetIds.size} widgets")

        appWidgetIds.forEach { appWidgetId ->
            try {
                val glanceId = glanceManager.getGlanceIdBy(appWidgetId)

                updateAppWidgetState(
                    context = context,
                    definition = SlotWidgetStateDefinition,
                    glanceId = glanceId
                ) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[SlotWidgetStateDefinition.Keys.SLOT_INDEX] = slotIndex
                        this[SlotWidgetStateDefinition.Keys.MODEL_ID] = slotConfig.modelId ?: ""
                        this[SlotWidgetStateDefinition.Keys.CURRENT_FRAME] = 0
                    }
                }

                Log.d(
                    "SlotWidget$slotIndex",
                    "Widget $appWidgetId initialized with model: ${slotConfig.modelId}"
                )
            } catch (e: Exception) {
                Log.e("SlotWidget$slotIndex", "Failed to initialize widget $appWidgetId", e)
            }
        }

        // 启动动画Worker
        WidgetAnimationWorker.enqueue(context.applicationContext)
    }
}

/**
 * 检查并停止动画Worker
 * 当所有widget都被移除时停止动画
 */
fun checkAndStopAnimation(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        val glanceManager = GlanceAppWidgetManager(context)

        // 检查所有槽位是否还有widget
        val totalWidgets = (1..6).sumOf { slotIndex ->
            glanceManager.getGlanceIds(SlotWidget(slotIndex)::class.java).size
        }

        if (totalWidgets == 0) {
            Log.d("SlotWidget", "No widgets remaining, stopping animation")
            WidgetAnimationWorker.cancel(context.applicationContext)
        } else {
            Log.d("SlotWidget", "Still have $totalWidgets widgets, keeping animation running")
        }
    }
}
