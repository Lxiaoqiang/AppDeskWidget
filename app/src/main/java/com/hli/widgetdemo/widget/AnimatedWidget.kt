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

class AnimatedWidget : GlanceAppWidget() {

    companion object {
        private const val TAG = "AnimatedWidget"
    }

    override val sizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*> = WidgetGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "provideGlance called for glanceId=$id")

        provideContent {
            val prefs = currentState<Preferences>()
            val frameIndex = prefs[WidgetGlanceStateDefinition.Keys.CURRENT_FRAME] ?: 0
            val styleOrdinal = prefs[WidgetGlanceStateDefinition.Keys.WIDGET_STYLE] ?: 0
            val widgetStyle = WidgetPreviewStyle.fromOrdinal(styleOrdinal)

            Log.d(TAG, "Rendering: frameIndex=$frameIndex, style=$widgetStyle")

            GlanceTheme {
                WidgetContent(
                    frameIndex = frameIndex,
                    style = widgetStyle
                )
            }
        }
    }

    @Composable
    private fun WidgetContent(frameIndex: Int, style: WidgetPreviewStyle) {
        val frameResId = style.frameResIds[frameIndex % style.frameResIds.size]

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color.Transparent)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(frameResId),
                contentDescription = "Animation",
                modifier = GlanceModifier.size(120.dp)
            )
        }
    }
}
