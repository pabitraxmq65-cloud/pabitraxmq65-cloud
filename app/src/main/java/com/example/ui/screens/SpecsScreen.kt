package com.example.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatteryState
import com.example.ui.theme.PolishPrimary

@Composable
fun SpecsScreen(
    batteryState: BatteryState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("specs_screen_root")
    ) {
        // Device Hardware Card
        SpecsSectionCard(
            title = "DEVICE & HARDWARE",
            icon = Icons.Default.PhoneAndroid,
            items = listOf(
                "Device Model" to "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
                "Android Version" to "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
                "Board / Hardware" to "${Build.BOARD} / ${Build.HARDWARE}",
                "Battery Present" to if (batteryState.isPresent) "Yes (Integrated)" else "No"
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Battery Chemistry & Capacity Card
        SpecsSectionCard(
            title = "BATTERY CELL & POWER PROFILE",
            icon = Icons.Default.BatteryFull,
            items = listOf(
                "Technology / Chemistry" to batteryState.technology,
                "Design Capacity" to "${batteryState.designCapacityMah} mAh",
                "Calculated Remaining" to "${batteryState.remainingCapacityMah} mAh",
                "Battery Level" to "${batteryState.levelPercent}%",
                "Health State" to batteryState.health.name.replace("_", " ")
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Electrical Specifications Card
        SpecsSectionCard(
            title = "ELECTRICAL SPECIFICATIONS",
            icon = Icons.Default.Power,
            items = listOf(
                "Instant Current" to "${batteryState.currentNowMa} mA",
                "Average Current" to "${batteryState.currentAvgMa} mA",
                "Peak Observed Current" to "${batteryState.maxCurrentMa} mA",
                "Minimum Observed Current" to "${batteryState.minCurrentMa} mA",
                "Current Voltage" to "${batteryState.voltageMv} mV (${(batteryState.voltageMv / 1000.0 * 100).toInt() / 100.0} V)",
                "Power Throughput" to "${batteryState.powerWatts} Watts",
                "Temperature" to "${batteryState.temperatureC} °C (${(batteryState.temperatureC * 9 / 5 + 32).toInt()} °F)",
                "Power Interface" to batteryState.powerSource.name
            )
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SpecsSectionCard(
    title: String,
    icon: ImageVector,
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PolishPrimary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = PolishPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            items.forEachIndexed { index, (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (index < items.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

