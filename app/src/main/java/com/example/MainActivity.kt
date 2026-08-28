package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BatteryViewModel
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.BenchmarkScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MeterScreen
import com.example.ui.screens.SpecsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PolishAmber
import com.example.ui.theme.PolishChargingGreen
import com.example.ui.theme.PolishPrimary

enum class AppNavTab(val title: String, val icon: ImageVector, val tag: String) {
    METER("Meter", Icons.Default.ElectricMeter, "nav_meter"),
    HISTORY("History", Icons.Default.History, "nav_history"),
    BENCHMARK("Speed Test", Icons.Default.Cable, "nav_benchmark"),
    SPECS("Specs", Icons.Default.Info, "nav_specs"),
    ALERTS("Alerts", Icons.Default.NotificationsActive, "nav_alerts")
}

class MainActivity : ComponentActivity() {
    private val viewModel: BatteryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AmpereApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmpereApp(
    viewModel: BatteryViewModel,
    modifier: Modifier = Modifier
) {
    val batteryState by viewModel.batteryState.collectAsStateWithLifecycle()
    val sessionHistory by viewModel.sessionHistory.collectAsStateWithLifecycle()
    val benchmarkState by viewModel.benchmarkState.collectAsStateWithLifecycle()
    val alertSettings by viewModel.alertSettings.collectAsStateWithLifecycle()
    val activeAlert by viewModel.activeAlertMessage.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(AppNavTab.METER) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ampere",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " Meter",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = PolishPrimary
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Live Current Tag
                        val tagColor = if (batteryState.isCharging) PolishChargingGreen else PolishAmber
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(tagColor.copy(alpha = 0.12f))
                                .border(
                                    1.dp,
                                    tagColor.copy(alpha = 0.35f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${if (batteryState.currentNowMa > 0) "+" else ""}${batteryState.currentNowMa} mA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = tagColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 3.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                AppNavTab.values().forEach { tab ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { tab ->
                when (tab) {
                    AppNavTab.METER -> MeterScreen(
                        batteryState = batteryState,
                        activeAlert = activeAlert,
                        onDismissAlert = { viewModel.dismissAlert() },
                        onResetMinMax = { viewModel.resetMinMax() }
                    )
                    AppNavTab.HISTORY -> HistoryScreen(
                        sessions = sessionHistory,
                        onDeleteSession = { viewModel.deleteSession(it) },
                        onClearAll = { viewModel.clearSessionHistory() }
                    )
                    AppNavTab.BENCHMARK -> BenchmarkScreen(
                        benchmarkResult = benchmarkState,
                        batteryState = batteryState,
                        onStartBenchmark = { duration -> viewModel.startBenchmark(duration) },
                        onCancelBenchmark = { viewModel.cancelBenchmark() }
                    )
                    AppNavTab.SPECS -> SpecsScreen(
                        batteryState = batteryState
                    )
                    AppNavTab.ALERTS -> AlertsScreen(
                        alertSettings = alertSettings,
                        onUpdateSettings = { viewModel.updateAlertSettings(it) }
                    )
                }
            }
        }
    }
}

