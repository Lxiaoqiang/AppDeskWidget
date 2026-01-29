package com.hli.widgetdemo.ui.screen

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hli.widgetdemo.widget.WidgetPinHelper
import com.hli.widgetdemo.widget.WidgetPreviewStyle
import com.hli.widgetdemo.widget.gif.GifFrameExtractor
import kotlinx.coroutines.launch

@Composable
fun WidgetPreviewScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedStyle by remember { mutableStateOf(WidgetPreviewStyle.STYLE_JERRY) }

    // GIF related state
    //https://wishwish-dev.s3.us-east-2.amazonaws.com/collection/image/did:privy:cmhwopy3k00h4l10d4q5v8wkz/yot034w13ogs45ch.gif
    var gifUrl by remember { mutableStateOf("https://raw.githubusercontent.com/nicehorse06/gif-test/main/test.gif") }
    var isLoading by remember { mutableStateOf(false) }
    var framesReady by remember { mutableStateOf(false) }
    var frameCount by remember { mutableStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "GIF Widget Test",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // GIF URL input
        OutlinedTextField(
            value = gifUrl,
            onValueChange = {
                gifUrl = it
                framesReady = false
                errorMessage = null
            },
            label = { Text("GIF URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Download button
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    framesReady = false

                    val extractor = GifFrameExtractor(context)
                    val gifId = gifUrl.hashCode().toString()

                    val result = extractor.extractFrames(
                        gifUrl = gifUrl,
                        gifId = gifId,
                        maxFrames = 30,
                        maxDimension = 200
                    )

                    result.onSuccess { extractionResult ->
                        frameCount = extractionResult.frameCount
                        framesReady = true
                        Toast.makeText(
                            context,
                            "Extracted ${extractionResult.frameCount} frames",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.onFailure { error ->
                        errorMessage = error.message ?: "Download failed"
                    }

                    isLoading = false
                }
            },
            enabled = !isLoading && gifUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Download & Extract Frames")
            }
        }

        // Status display
        if (errorMessage != null) {
            Text(
                text = "Error: $errorMessage",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (framesReady) {
            Text(
                text = "Ready: $frameCount frames extracted",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Add to home screen button
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (WidgetPinHelper.isPinWidgetSupported(context)) {
                        val gifId = gifUrl.hashCode().toString()
                        val success = WidgetPinHelper.requestPinViewFlipperWidget(context, gifId)
                        if (!success) {
                            Toast.makeText(
                                context,
                                "Failed to request widget pin",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Widget pinning not supported on this device",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Widget pinning requires Android 8.0+",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            enabled = framesReady,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Add to Home Screen",
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "GIF Animation Widget (ViewFlipper)",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WidgetPreviewCard(
    style: WidgetPreviewStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = style.previewResId),
                contentDescription = style.displayName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = style.displayName,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun SelectedWidgetPreview(style: WidgetPreviewStyle) {
    Card(
        modifier = Modifier.size(200.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = style.previewResId),
                contentDescription = "Selected Widget Preview",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
