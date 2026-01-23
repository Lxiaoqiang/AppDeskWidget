package com.hli.widgetdemo.widget

import com.hli.widgetdemo.R

enum class WidgetPreviewStyle(
    val displayName: String,
    val previewResId: Int,
    val frameResIds: List<Int>
) {
    STYLE_JERRY(
        displayName = "Jerry Mouse",
        previewResId = R.drawable.jerry_frame_01,
        frameResIds = listOf(
            R.drawable.jerry_frame_01,
            R.drawable.jerry_frame_02,
            R.drawable.jerry_frame_03,
            R.drawable.jerry_frame_04,
            R.drawable.jerry_frame_05,
            R.drawable.jerry_frame_06,
            R.drawable.jerry_frame_07,
            R.drawable.jerry_frame_08,
            R.drawable.jerry_frame_09,
            R.drawable.jerry_frame_10
        )
    ),
    STYLE_STAR(
        displayName = "Twinkle Star",
        previewResId = R.drawable.star_frame_05,
        frameResIds = listOf(
            R.drawable.star_frame_01,
            R.drawable.star_frame_02,
            R.drawable.star_frame_03,
            R.drawable.star_frame_04,
            R.drawable.star_frame_05,
            R.drawable.star_frame_06,
            R.drawable.star_frame_07,
            R.drawable.star_frame_08,
            R.drawable.star_frame_09,
            R.drawable.star_frame_10
        )
    ),
    STYLE_HEART(
        displayName = "Beating Heart",
        previewResId = R.drawable.heart_frame_05,
        frameResIds = listOf(
            R.drawable.heart_frame_01,
            R.drawable.heart_frame_02,
            R.drawable.heart_frame_03,
            R.drawable.heart_frame_04,
            R.drawable.heart_frame_05,
            R.drawable.heart_frame_06,
            R.drawable.heart_frame_07,
            R.drawable.heart_frame_08,
            R.drawable.heart_frame_09,
            R.drawable.heart_frame_10
        )
    );

    companion object {
        fun fromOrdinal(ordinal: Int): WidgetPreviewStyle {
            return entries.getOrElse(ordinal) { STYLE_JERRY }
        }
    }
}
