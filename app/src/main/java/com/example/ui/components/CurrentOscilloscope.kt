package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryCurrentPoint
import com.example.ui.theme.PolishAmber
import com.example.ui.theme.PolishChargingGreen
import com.example.ui.theme.PolishPrimary

@Composable
fun CurrentOscilloscope(
    points: List<BatteryCurrentPoint>,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_pulse")
    val dotPulse by infiniteTransition.animateFloat(
        initialValue = 3.5f,
        targetValue = 7.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )

    val lineColor = if (isCharging) PolishPrimary else PolishAmber
    val gradientColor = if (isCharging) PolishChargingGreen else PolishAmber
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, outlineVariant, RoundedCornerShape(24.dp))
            .padding(16.dp)
            .testTag("current_oscilloscope_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(lineColor.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Waveform Chart",
                            tint = lineColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "LIVE CURRENT STREAM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (points.isNotEmpty()) {
                    val latest = points.last().currentMa
                    Text(
                        text = "${if (latest > 0) "+" else ""}$latest mA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = lineColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Waveform Area
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("oscilloscope_canvas")
            ) {
                val width = size.width
                val height = size.height

                // Draw subtle background grid
                val gridRows = 4
                val gridCols = 6
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)

                for (i in 1 until gridRows) {
                    val y = height * (i.toFloat() / gridRows)
                    drawLine(
                        color = outlineVariant.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }
                for (j in 1 until gridCols) {
                    val x = width * (j.toFloat() / gridCols)
                    drawLine(
                        color = outlineVariant.copy(alpha = 0.3f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                if (points.size < 2) {
                    drawLine(
                        color = lineColor.copy(alpha = 0.4f),
                        start = Offset(0f, height / 2f),
                        end = Offset(width, height / 2f),
                        strokeWidth = 2.dp.toPx()
                    )
                    return@Canvas
                }

                var minVal = points.minOf { it.currentMa }
                var maxVal = points.maxOf { it.currentMa }
                if (maxVal - minVal < 200) {
                    minVal -= 100
                    maxVal += 100
                }
                val range = (maxVal - minVal).toFloat().coerceAtLeast(10f)

                val stepX = width / (points.size - 1).coerceAtLeast(1)

                val strokePath = Path()
                val fillPath = Path()

                fillPath.moveTo(0f, height)

                val coordinates = points.mapIndexed { index, point ->
                    val x = index * stepX
                    val normalizedY = (point.currentMa - minVal) / range
                    val y = height - (normalizedY * (height - 20.dp.toPx()) + 10.dp.toPx())
                    Offset(x, y)
                }

                strokePath.moveTo(coordinates[0].x, coordinates[0].y)
                fillPath.lineTo(coordinates[0].x, coordinates[0].y)

                for (i in 0 until coordinates.size - 1) {
                    val p0 = coordinates[i]
                    val p1 = coordinates[i + 1]
                    val controlX = (p0.x + p1.x) / 2f
                    strokePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                }

                fillPath.lineTo(coordinates.last().x, height)
                fillPath.close()

                // Draw gradient under area
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradientColor.copy(alpha = 0.18f),
                            gradientColor.copy(alpha = 0.01f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw dynamic waveform line
                drawPath(
                    path = strokePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.5f),
                            lineColor
                        )
                    ),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw pulsating latest point dot
                val lastCoord = coordinates.last()
                drawCircle(
                    color = lineColor.copy(alpha = 0.3f),
                    radius = dotPulse.dp.toPx(),
                    center = lastCoord
                )
                drawCircle(
                    color = lineColor,
                    radius = 3.5.dp.toPx(),
                    center = lastCoord
                )
            }
        }
    }
}

