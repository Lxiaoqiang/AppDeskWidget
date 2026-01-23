package com.hli.widgetdemo.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

/**
 * Slot Widget
 * 根据槽位配置动态显示分配的模型动画
 */
class SlotWidget(private val slotIndex: Int) : GlanceAppWidget() {

    companion object {
        private const val TAG = "SlotWidget"
    }

    override val sizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*> = SlotWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "provideGlance called for slot $slotIndex, glanceId=$id")

        provideContent {
            val prefs = currentState<Preferences>()
            val modelId = prefs[SlotWidgetStateDefinition.Keys.MODEL_ID] ?: ""
            val frameIndex = prefs[SlotWidgetStateDefinition.Keys.CURRENT_FRAME] ?: 0

            Log.d(TAG, "Slot $slotIndex rendering: modelId=$modelId, frameIndex=$frameIndex")

            GlanceTheme {
                WidgetContent(
                    slotIndex = slotIndex,
                    modelId = modelId,
                    frameIndex = frameIndex
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(slotIndex: Int, modelId: String, frameIndex: Int) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.Transparent)),
            contentAlignment = Alignment.Center
        ) {
            if (modelId.isNotEmpty()) {
                // 根据modelId获取对应的样式
                val style = WidgetPreviewStyle.entries.find { it.name == modelId }

                if (style != null) {
                    // 显示动画帧
                    val frameResId = style.frameResIds[frameIndex % style.frameResIds.size]

                    Image(
                        provider = ImageProvider(frameResId),
                        contentDescription = "Animation",
                        modifier = GlanceModifier.size(120.dp)
                    )
                } else {
                    // 模型ID无效
                    ErrorContent(slotIndex, "Invalid model")
                }
            } else {
                // 未分配模型
                EmptySlotContent(slotIndex)
            }
        }
    }

    @Composable
    private fun EmptySlotContent(slotIndex: Int) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.padding(16.dp)
        ) {
            Text(
                text = "Slot $slotIndex",
                style = TextStyle(
                    color = ColorProvider(Color.Gray),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Not assigned",
                style = TextStyle(
                    color = ColorProvider(Color.LightGray),
                    fontSize = 12.sp
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Open app to assign",
                style = TextStyle(
                    color = ColorProvider(Color.LightGray),
                    fontSize = 10.sp
                )
            )
        }
    }

    @Composable
    private fun ErrorContent(slotIndex: Int, message: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.padding(16.dp)
        ) {
            Text(
                text = "Slot $slotIndex",
                style = TextStyle(
                    color = ColorProvider(Color.Red),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = message,
                style = TextStyle(
                    color = ColorProvider(Color.Red),
                    fontSize = 10.sp
                )
            )
        }
    }
}
