package com.example.screen.locker.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.screen.locker.domain.repository.HookStyle
import com.example.screen.locker.domain.repository.ZipperStyle
import kotlin.math.roundToInt

@Composable
fun ZipperUnlockView(
    onUnlock: () -> Unit,
    zipperStyle: ZipperStyle = ZipperStyle.SILVER,
    hookStyle: HookStyle = HookStyle.CLASSIC,
    wallpaper: ImageBitmap? = null,
    modifier: Modifier = Modifier
) {
    var offsetY by remember { mutableStateOf(0f) }
    var containerHeight by remember { mutableStateOf(0f) }
    val handleY by animateFloatAsState(targetValue = offsetY, label = "zipper")

    val (toothColor1, toothColor2) = when (zipperStyle) {
        ZipperStyle.GOLD -> Color(0xFFFFD700) to Color(0xFFDAA520)
        ZipperStyle.BLACK -> Color(0xFF333333) to Color(0xFF000000)
        ZipperStyle.RAINBOW -> Color.Cyan to Color.Magenta
        else -> Color(0xFFC0C0C0) to Color(0xFFA0A0A0)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                containerHeight = coordinates.size.height.toFloat()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (containerHeight > 0) {
                            offsetY = (offsetY + dragAmount.y).coerceIn(0f, containerHeight)
                        }
                    },
                    onDragEnd = {
                        if (containerHeight > 0 && offsetY >= containerHeight * 0.7f) {
                            onUnlock()
                        } else {
                            offsetY = 0f
                        }
                    }
                )
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val toothWidth = 24f
            val toothHeight = 12f
            val toothSpacing = 18f

            // 1. Draw Split Wallpaper
            wallpaper?.let { bitmap ->
                val canvasWidth = size.width
                val canvasHeight = size.height
                val bitmapWidth = bitmap.width.toFloat()
                val bitmapHeight = bitmap.height.toFloat()

                // Calculate source rect for center crop
                val canvasRatio = canvasWidth / canvasHeight
                val bitmapRatio = bitmapWidth / bitmapHeight

                val srcWidth: Float
                val srcHeight: Float
                val srcX: Float
                val srcY: Float

                if (bitmapRatio > canvasRatio) {
                    srcHeight = bitmapHeight
                    srcWidth = bitmapHeight * canvasRatio
                    srcX = (bitmapWidth - srcWidth) / 2
                    srcY = 0f
                } else {
                    srcWidth = bitmapWidth
                    srcHeight = bitmapWidth / canvasRatio
                    srcX = 0f
                    srcY = (bitmapHeight - srcHeight) / 2
                }

                // Left Side Wallpaper
                val leftPath = Path().apply {
                    moveTo(0f, 0f)
                    var y = 0f
                    while (y <= canvasHeight) {
                        val isBelowHandle = y > handleY
                        val spread = if (isBelowHandle) 0f else {
                            val distance = handleY - y
                            (distance / 400f).coerceIn(0f, 1f) * (centerX * 0.8f)
                        }
                        lineTo(centerX - spread, y)
                        y += toothSpacing
                    }
                    lineTo(0f, canvasHeight)
                    close()
                }

                clipPath(leftPath) {
                    drawImage(
                        image = bitmap,
                        srcOffset = IntOffset(srcX.roundToInt(), srcY.roundToInt()),
                        srcSize = IntSize(srcWidth.roundToInt(), srcHeight.roundToInt()),
                        dstOffset = IntOffset(0, 0),
                        dstSize = IntSize(canvasWidth.roundToInt(), canvasHeight.roundToInt())
                    )
                }

                // Right Side Wallpaper
                val rightPath = Path().apply {
                    moveTo(canvasWidth, 0f)
                    var y = 0f
                    while (y <= canvasHeight) {
                        val isBelowHandle = y > handleY
                        val spread = if (isBelowHandle) 0f else {
                            val distance = handleY - y
                            (distance / 400f).coerceIn(0f, 1f) * (centerX * 0.8f)
                        }
                        lineTo(centerX + spread, y)
                        y += toothSpacing
                    }
                    lineTo(canvasWidth, canvasHeight)
                    close()
                }

                clipPath(rightPath) {
                    drawImage(
                        image = bitmap,
                        srcOffset = IntOffset(srcX.roundToInt(), srcY.roundToInt()),
                        srcSize = IntSize(srcWidth.roundToInt(), srcHeight.roundToInt()),
                        dstOffset = IntOffset(0, 0),
                        dstSize = IntSize(canvasWidth.roundToInt(), canvasHeight.roundToInt())
                    )
                }
            }

            // 2. Draw Zipper Teeth
            var y = 0f
            while (y < size.height) {
                val isBelowHandle = y > handleY
                
                if (isBelowHandle) {
                    drawRoundRect(
                        color = toothColor1,
                        topLeft = Offset(centerX - toothWidth - 2f, y),
                        size = Size(toothWidth, toothHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = toothColor2,
                        topLeft = Offset(centerX + 2f, y + toothHeight / 2),
                        size = Size(toothWidth, toothHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                } else {
                    val distance = handleY - y
                    val progress = (distance / 400f).coerceIn(0f, 1f)
                    val spread = progress * (centerX * 0.8f)
                    
                    drawRoundRect(
                        color = toothColor1,
                        topLeft = Offset(centerX - toothWidth - spread - 5f, y),
                        size = Size(toothWidth, toothHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = toothColor2,
                        topLeft = Offset(centerX + spread + 5f, y + toothHeight / 2),
                        size = Size(toothWidth, toothHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
                y += toothSpacing
            }
        }

        ZipperHandle(
            yOffset = handleY.roundToInt(),
            style = zipperStyle,
            hook = hookStyle
        )
    }
}

@Composable
private fun ZipperHandle(yOffset: Int, style: ZipperStyle, hook: HookStyle) {
    val handleColors = when (style) {
        ZipperStyle.GOLD -> listOf(Color(0xFFFFF176), Color(0xFFFDD835), Color(0xFFF9A825))
        ZipperStyle.BLACK -> listOf(Color(0xFF424242), Color(0xFF212121), Color(0xFF000000))
        ZipperStyle.RAINBOW -> listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue, Color.Magenta)
        else -> listOf(Color(0xFFF9FAFB), Color(0xFF9CA3AF), Color(0xFF4B5563))
    }

    val shape = when (hook) {
        HookStyle.ROUND -> CircleShape
        HookStyle.SQUARE -> RoundedCornerShape(4.dp)
        HookStyle.HEART -> RoundedCornerShape(16.dp) // Simplified
        else -> RoundedCornerShape(16.dp)
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(0, yOffset) }
            .size(if (hook == HookStyle.ROUND) 80.dp else 70.dp, 110.dp)
            .clip(shape)
            .background(Brush.verticalGradient(handleColors)),
        contentAlignment = Alignment.Center
    ) {
        if (hook == HookStyle.HEART) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp, 50.dp)
                        .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}
