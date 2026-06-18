package com.example.screen.locker.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

@Composable
fun PatternLockView(
    onPatternComplete: (List<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val dots = remember { (0 until 9).toList() }
    var connectedDots by remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }

    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val sizePx = constraints.maxWidth.toFloat()
        val cellSize = sizePx / 3

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            connectedDots = emptyList()
                            currentTouchPoint = offset
                        },
                        onDrag = { change, _ ->
                            currentTouchPoint = change.position
                            val row = (change.position.y / cellSize).toInt()
                            val col = (change.position.x / cellSize).toInt()
                            if (row in 0..2 && col in 0..2) {
                                val dot = row * 3 + col
                                val centerX = col * cellSize + cellSize / 2
                                val centerY = row * cellSize + cellSize / 2
                                val dist = sqrt((change.position.x - centerX).let { it * it } + (change.position.y - centerY).let { it * it })
                                if (dist < cellSize / 2.5f && dot !in connectedDots) {
                                    connectedDots = connectedDots + dot
                                }
                            }
                        },
                        onDragEnd = {
                            if (connectedDots.isNotEmpty()) {
                                onPatternComplete(connectedDots)
                            }
                            currentTouchPoint = null
                            connectedDots = emptyList()
                        }
                    )
                }
        ) {
            // Draw lines between dots
            if (connectedDots.isNotEmpty()) {
                for (i in 0 until connectedDots.size - 1) {
                    val startDot = connectedDots[i]
                    val endDot = connectedDots[i + 1]
                    drawLine(
                        color = Color.Cyan,
                        start = getDotOffset(startDot, cellSize),
                        end = getDotOffset(endDot, cellSize),
                        strokeWidth = 12f,
                        cap = StrokeCap.Round
                    )
                }
                currentTouchPoint?.let { touch ->
                    drawLine(
                        color = Color.Cyan.copy(alpha = 0.5f),
                        start = getDotOffset(connectedDots.last(), cellSize),
                        end = touch,
                        strokeWidth = 12f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw dots
            dots.forEach { dot ->
                val center = getDotOffset(dot, cellSize)
                val isConnected = dot in connectedDots
                
                // Outer glow for connected dots
                if (isConnected) {
                    drawCircle(
                        color = Color.Cyan.copy(alpha = 0.2f),
                        radius = cellSize / 3,
                        center = center
                    )
                }

                // Main dot
                drawCircle(
                    color = if (isConnected) Color.Cyan else Color.White.copy(alpha = 0.3f),
                    radius = if (isConnected) 20f else 12f,
                    center = center
                )
                
                // Dot border
                drawCircle(
                    color = if (isConnected) Color.Cyan else Color.White.copy(alpha = 0.1f),
                    radius = cellSize / 4,
                    center = center,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

private fun getDotOffset(dot: Int, cellSize: Float): Offset {
    val row = dot / 3
    val col = dot % 3
    return Offset(
        x = col * cellSize + cellSize / 2,
        y = row * cellSize + cellSize / 2
    )
}
