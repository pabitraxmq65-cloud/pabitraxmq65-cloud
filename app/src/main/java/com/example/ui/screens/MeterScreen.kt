package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryHealthStatus
import com.example.data.BatteryState
import com.example.data.PowerSource
import com.example.ui.components.AmpereGauge
import com.example.ui.components.CurrentOscilloscope
import com.example.ui.components.StatCard
import com.example.ui.theme.PolishAmber
import com.example.ui.theme.PolishChargingGreen
import com.example.ui.theme.PolishCoral
import com.example.ui.theme.PolishPrimary

@Composable
fun MeterScreen(
    batteryState: BatteryState,
    activeAlert: String?,
    onDismissAlert: () -> Unit,
    onResetMinMax: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val isCharging = batteryState.isCharging
    val primaryColor = if (isCharging) PolishPrimary else PolishAmber
    val tempF = (batteryState.temperatureC * 9 / 5 + 32).toInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("meter_screen_root"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active In-App Alert Banner
        AnimatedVisibility(
            visible = activeAlert != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (activeAlert != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PolishCoral.copy(alpha = 0.12f))
                        .border(1.dp, PolishCoral.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("active_alert_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = PolishCoral,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = activeAlert,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = onDismissAlert,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Top Status Header Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Power Source Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCharging) Icons.Default.Power else Icons.Default.BatteryFull,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (batteryState.powerSource) {
                            PowerSource.AC -> "AC CHARGER"
                            PowerSource.USB -> "USB PORT"
                            PowerSource.WIRELESS -> "WIRELESS PAD"
                            PowerSource.FAST_CHARGER -> "FAST CHARGER"
                            PowerSource.UNPLUGGED -> "BATTERY POWER"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Battery Level Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (batteryState.levelPercent > 20) PolishChargingGreen.copy(alpha = 0.12f)
                        else PolishCoral.copy(alpha = 0.12f)
                    )
                    .border(
                        1.dp,
                        if (batteryState.levelPercent > 20) PolishChargingGreen.copy(alpha = 0.4f)
                        else PolishCoral.copy(alpha = 0.4f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (batteryState.levelPercent > 20) PolishChargingGreen
                                else PolishCoral
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${batteryState.levelPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = if (batteryState.levelPercent > 20) PolishChargingGreen else PolishCoral
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Container Card (Design HTML style rounded-[32px] with Ampere Gauge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    RoundedCornerShape(32.dp)
                )
                .padding(vertical = 16.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                AmpereGauge(
                    batteryState = batteryState,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Battery Level Progress Bar in Hero
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(top = 4.dp, bottom = 8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { batteryState.levelPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PolishPrimary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    )
                }

                // Reset Min/Max Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onResetMinMax() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("reset_min_max_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset peak",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset Peaks",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Real-Time Current Stream Waveform
        CurrentOscilloscope(
            points = batteryState.sampleHistory,
            isCharging = isCharging
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Instrument Metrics Grid (2 columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Voltage",
                value = "${(batteryState.voltageMv / 1000.0 * 100).toInt() / 100.0} V",
                subtitle = "${batteryState.voltageMv} mV",
                icon = Icons.Default.FlashOn,
                accentColor = PolishPrimary,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Temperature",
                value = "${batteryState.temperatureC} °C",
                subtitle = "$tempF °F",
                icon = Icons.Default.Thermostat,
                accentColor = if (batteryState.temperatureC > 40f) PolishCoral else PolishAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Power Flow",
                value = "${batteryState.powerWatts} W",
                subtitle = if (isCharging) "Charging In" else "Discharge Rate",
                icon = Icons.Default.ElectricBolt,
                accentColor = PolishPrimary,
                modifier = Modifier.weight(1f)
            )

            val timeDisplay = when {
                batteryState.estimatedTimeMinutes == null -> "--"
                batteryState.estimatedTimeMinutes == 0 -> "Full"
                batteryState.estimatedTimeMinutes < 60 -> "${batteryState.estimatedTimeMinutes} min"
                else -> "${batteryState.estimatedTimeMinutes / 60}h ${batteryState.estimatedTimeMinutes % 60}m"
            }

            StatCard(
                title = if (isCharging) "Time to Full" else "Time to Empty",
                value = timeDisplay,
                subtitle = if (isCharging) "Calculated rate" else "Remaining battery",
                icon = Icons.Default.AccessTime,
                accentColor = PolishChargingGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Battery Health",
                value = batteryState.health.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                subtitle = batteryState.technology,
                icon = Icons.Default.Favorite,
                accentColor = if (batteryState.health == BatteryHealthStatus.GOOD) PolishChargingGreen else PolishCoral,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Capacity",
                value = "${batteryState.remainingCapacityMah} mAh",
                subtitle = "of ${batteryState.designCapacityMah} mAh",
                icon = Icons.Default.BatteryFull,
                accentColor = PolishPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

