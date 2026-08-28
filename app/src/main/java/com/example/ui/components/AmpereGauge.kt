package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryState
import com.example.data.ChargingSpeed
import com.example.ui.theme.PolishAmber
import com.example.ui.theme.PolishChargingGreen
import com.example.ui.theme.PolishPrimary
import com.example.ui.theme.PolishSecondary
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmpereGauge(
    batteryState: BatteryState,
    modifier: Modifier = Modifier
) {
    val isCharging = batteryState.isCharging
    val currentMa = batteryState.currentNowMa

    // Animated current value for smooth transition
    val animatedCurrent = remember { Animatable(0f) }
    LaunchedEffect(currentMa) {
        animatedCurrent.animateTo(
            targetValue = currentMa.toFloat(),
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "gauge_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate_gauge"
    )

    val primaryAccent = when {
        !isCharging -> PolishAmber
        batteryState.speedCategory == ChargingSpeed.TURBO -> PolishPrimary
        batteryState.speedCategory == ChargingSpeed.FAST -> PolishChargingGreen
        else -> PolishPrimary
    }

    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(270.dp)
            .testTag("ampere_gauge_component")
    ) {
        // Custom Canvas Dial
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - 18.dp.toPx()
            val trackRadius = radius - 8.dp.toPx()

            // Background Arc Track (240 degrees, from 150 to 390)
            val startAngle = 150f
            val sweepAngle = 240f

            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - trackRadius, center.y - trackRadius),
                size = Size(trackRadius * 2, trackRadius * 2),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )

            // Active Flow Arc
            val maxScaleMa = if (isCharging) 4000f else 1500f
            val progressFraction = (abs(animatedCurrent.value) / maxScaleMa).coerceIn(0f, 1f)
            val activeSweep = sweepAngle * progressFraction

            if (activeSweep > 0.5f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to primaryAccent.copy(alpha = 0.7f),
                        0.5f to primaryAccent,
                        1.0f to primaryAccent,
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - trackRadius, center.y - trackRadius),
                    size = Size(trackRadius * 2, trackRadius * 2),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Ticks
            val totalTicks = 24
            for (i in 0..totalTicks) {
                val tickFraction = i.toFloat() / totalTicks
                val angleDeg = startAngle + (sweepAngle * tickFraction)
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val isMajor = i % 4 == 0

                val innerR = radius + if (isMajor) 4.dp.toPx() else 8.dp.toPx()
                val outerR = radius + 14.dp.toPx()

                val tickColor = if (tickFraction <= progressFraction) {
                    primaryAccent.copy(alpha = 0.85f)
                } else {
                    trackColor.copy(alpha = 0.6f)
                }

                val startX = (center.x + innerR * cos(angleRad)).toFloat()
                val startY = (center.y + innerR * sin(angleRad)).toFloat()
                val endX = (center.x + outerR * cos(angleRad)).toFloat()
                val endY = (center.y + outerR * sin(angleRad)).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Subtle rotating ambient particle ring when charging
            if (isCharging) {
                rotate(degrees = rotationAngle, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0.0f to Color.Transparent,
                            0.4f to primaryAccent.copy(alpha = pulseAlpha * 0.3f),
                            0.5f to primaryAccent.copy(alpha = pulseAlpha * 0.7f),
                            0.6f to Color.Transparent,
                            center = center
                        ),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius - 14.dp.toPx(), center.y - radius - 14.dp.toPx()),
                        size = Size((radius + 14.dp.toPx()) * 2, (radius + 14.dp.toPx()) * 2),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Inner Digital Readout Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCharging) Icons.Default.Bolt else Icons.Default.ElectricMeter,
                    contentDescription = if (isCharging) "Charging" else "Discharging",
                    tint = primaryAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = if (isCharging) "FAST CHARGING" else "DISCHARGING",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = primaryAccent
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Ampere Big Display
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                if (currentMa > 0) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 6.dp, end = 2.dp)
                    )
                }
                Text(
                    text = "${abs(currentMa)}",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.5).sp,
                        fontSize = 52.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("gauge_current_text")
                )
                Text(
                    text = " mA",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Min / Max Subline
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Min: ${batteryState.minCurrentMa} mA",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "  •  ",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "Max: ${batteryState.maxCurrentMa} mA",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

