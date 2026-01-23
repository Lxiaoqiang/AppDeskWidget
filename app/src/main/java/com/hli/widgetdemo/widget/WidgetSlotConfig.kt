package com.hli.widgetdemo.widget

/**
 * Widget槽位配置
 * 记录每个槽位分配的模型信息
 */
data class WidgetSlotConfig(
    val slotIndex: Int,                // 槽位索引 (1-6)
    val modelId: String?,              // 分配的模型ID（对应WidgetPreviewStyle的name）
    val isActive: Boolean,             // 是否激活
    val assignedAt: Long               // 分配时间戳
)
