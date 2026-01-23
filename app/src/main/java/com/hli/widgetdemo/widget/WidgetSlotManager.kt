package com.hli.widgetdemo.widget

import android.content.Context
import android.util.Log
import com.google.gson.Gson

/**
 * Widget槽位管理器
 * 负责管理6个槽位的模型分配
 */
class WidgetSlotManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("widget_slots", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val TAG = "WidgetSlotManager"
        private const val MAX_SLOTS = 6
        private const val KEY_SLOT_CONFIG = "slot_config_"
    }

    /**
     * 获取所有槽位配置
     */
    fun getAllSlots(): List<WidgetSlotConfig> {
        return (1..MAX_SLOTS).map { slotIndex ->
            getSlotConfig(slotIndex)
        }
    }

    /**
     * 获取指定槽位配置
     */
    fun getSlotConfig(slotIndex: Int): WidgetSlotConfig {
        if (slotIndex !in 1..MAX_SLOTS) {
            Log.e(TAG, "Invalid slot index: $slotIndex")
            return WidgetSlotConfig(slotIndex, null, false, 0)
        }

        val key = "$KEY_SLOT_CONFIG$slotIndex"
        val json = prefs.getString(key, null)

        return if (json != null) {
            try {
                gson.fromJson(json, WidgetSlotConfig::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse slot config for slot $slotIndex", e)
                WidgetSlotConfig(slotIndex, null, false, 0)
            }
        } else {
            // 默认配置：未分配
            WidgetSlotConfig(
                slotIndex = slotIndex,
                modelId = null,
                isActive = false,
                assignedAt = 0
            )
        }
    }

    /**
     * 分配模型到槽位
     */
    fun assignModelToSlot(slotIndex: Int, modelId: String): Boolean {
        if (slotIndex !in 1..MAX_SLOTS) {
            Log.e(TAG, "Invalid slot index: $slotIndex")
            return false
        }

        val config = WidgetSlotConfig(
            slotIndex = slotIndex,
            modelId = modelId,
            isActive = true,
            assignedAt = System.currentTimeMillis()
        )

        val key = "$KEY_SLOT_CONFIG$slotIndex"
        val json = gson.toJson(config)
        prefs.edit().putString(key, json).apply()

        Log.d(TAG, "Assigned model $modelId to slot $slotIndex")
        return true
    }

    /**
     * 清空槽位
     */
    fun clearSlot(slotIndex: Int) {
        if (slotIndex !in 1..MAX_SLOTS) {
            Log.e(TAG, "Invalid slot index: $slotIndex")
            return
        }

        val config = WidgetSlotConfig(
            slotIndex = slotIndex,
            modelId = null,
            isActive = false,
            assignedAt = 0
        )

        val key = "$KEY_SLOT_CONFIG$slotIndex"
        val json = gson.toJson(config)
        prefs.edit().putString(key, json).apply()

        Log.d(TAG, "Cleared slot $slotIndex")
    }

    /**
     * 获取可用的空槽位
     */
    fun getAvailableSlot(): Int? {
        return (1..MAX_SLOTS).firstOrNull { slotIndex ->
            val config = getSlotConfig(slotIndex)
            !config.isActive || config.modelId == null
        }
    }

    /**
     * 获取已使用的槽位数量
     */
    fun getUsedSlotCount(): Int {
        return getAllSlots().count { it.isActive && it.modelId != null }
    }

    /**
     * 检查是否还有可用槽位
     */
    fun hasAvailableSlot(): Boolean {
        return getUsedSlotCount() < MAX_SLOTS
    }

    /**
     * 获取模型所在的槽位
     */
    fun getSlotForModel(modelId: String): Int? {
        return getAllSlots().firstOrNull {
            it.modelId == modelId && it.isActive
        }?.slotIndex
    }

    /**
     * 获取最大槽位数
     */
    fun getMaxSlots(): Int = MAX_SLOTS
}
