package com.hli.widgetdemo.ui.screen

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hli.widgetdemo.widget.*
import kotlinx.coroutines.delay

@Composable
fun SlotManagementScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val slotManager = remember { WidgetSlotManager(context) }

    var slotConfigs by remember { mutableStateOf(slotManager.getAllSlots()) }
    var selectedSlot by remember { mutableStateOf<Int?>(null) }
    var showModelPicker by remember { mutableStateOf(false) }

    // 刷新槽位配置
    fun refreshSlots() {
        slotConfigs = slotManager.getAllSlots()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "Widget Slot Management",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // 使用情况
        val usedSlots = slotConfigs.count { it.isActive && it.modelId != null }
        Text(
            text = "Used: $usedSlots / ${slotManager.getMaxSlots()}",
            fontSize = 14.sp,
            color = if (usedSlots >= slotManager.getMaxSlots()) Color.Red else Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 说明
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "How to use:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Assign models to slots below\n2. Long press desktop → Widgets\n3. Select \"Widget Slot X\" to add",
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // 槽位网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(slotConfigs) { config ->
                SlotCard(
                    config = config,
                    onClick = {
                        selectedSlot = config.slotIndex
                        if (config.isActive && config.modelId != null) {
                            // 已分配，显示详情
                        } else {
                            // 未分配，显示模型选择器
                            showModelPicker = true
                        }
                    },
                    onClear = {
                        slotManager.clearSlot(config.slotIndex)
                        refreshSlots()
                        Toast.makeText(context, "Slot ${config.slotIndex} cleared", Toast.LENGTH_SHORT).show()
                    },
                    onPin = {
                        // Pin功能
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                                val receiverClass = when (config.slotIndex) {
                                    1 -> WidgetSlot1Receiver::class.java
                                    2 -> WidgetSlot2Receiver::class.java
                                    3 -> WidgetSlot3Receiver::class.java
                                    4 -> WidgetSlot4Receiver::class.java
                                    5 -> WidgetSlot5Receiver::class.java
                                    6 -> WidgetSlot6Receiver::class.java
                                    else -> return@SlotCard
                                }

                                val provider = ComponentName(context, receiverClass)
                                val success = appWidgetManager.requestPinAppWidget(provider, null, null)

                                if (success) {
                                    Toast.makeText(context, "Widget pinned!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to pin widget", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "Pin widget not supported", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Requires Android 8.0+", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // 模型选择对话框
    if (showModelPicker && selectedSlot != null) {
        ModelPickerDialog(
            onDismiss = {
                showModelPicker = false
                selectedSlot = null
            },
            onModelSelected = { style ->
                slotManager.assignModelToSlot(selectedSlot!!, style.name)
                refreshSlots()
                showModelPicker = false
                selectedSlot = null
                Toast.makeText(
                    context,
                    "${style.displayName} assigned to Slot $selectedSlot",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
fun SlotCard(
    config: WidgetSlotConfig,
    onClick: () -> Unit,
    onClear: () -> Unit,
    onPin: () -> Unit
) {
    var currentFrame by remember { mutableStateOf(0) }

    // 获取模型样式
    val style = if (config.modelId != null) {
        WidgetPreviewStyle.entries.find { it.name == config.modelId }
    } else null

    // 动画效果
    LaunchedEffect(style) {
        if (style != null) {
            while (true) {
                delay(100)
                currentFrame = (currentFrame + 1) % style.frameResIds.size
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (style != null) {
                // 显示动画
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(style.frameResIds[currentFrame]),
                        contentDescription = style.displayName,
                        modifier = Modifier
                            .size(80.dp)
                            .weight(1f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Slot ${config.slotIndex}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = style.displayName,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Pin按钮
                        IconButton(
                            onClick = onPin,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Pin",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // 清空按钮
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp),
                                tint = Color.Red
                            )
                        }
                    }
                }
            } else {
                // 空槽位
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Slot ${config.slotIndex}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap to assign",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ModelPickerDialog(
    onDismiss: () -> Unit,
    onModelSelected: (WidgetPreviewStyle) -> Unit
) {
    var selectedStyle by remember { mutableStateOf<WidgetPreviewStyle?>(null) }
    var currentFrame by remember { mutableStateOf(0) }

    // 动画效果
    LaunchedEffect(selectedStyle) {
        if (selectedStyle != null) {
            while (true) {
                delay(100)
                currentFrame = (currentFrame + 1) % (selectedStyle?.frameResIds?.size ?: 10)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Model",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 模型列表
                WidgetPreviewStyle.entries.forEach { style ->
                    val isSelected = selectedStyle == style

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { selectedStyle = style }
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                ) else Modifier
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 预览图
                            Image(
                                painter = painterResource(
                                    if (isSelected && selectedStyle != null)
                                        selectedStyle!!.frameResIds[currentFrame]
                                    else
                                        style.previewResId
                                ),
                                contentDescription = style.displayName,
                                modifier = Modifier.size(60.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = style.displayName,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            selectedStyle?.let { onModelSelected(it) }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedStyle != null
                    ) {
                        Text("Assign")
                    }
                }
            }
        }
    }
}
