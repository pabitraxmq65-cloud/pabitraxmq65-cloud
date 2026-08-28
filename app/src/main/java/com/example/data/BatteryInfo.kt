package com.example.data

enum class BatteryStatus {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN
}

enum class PowerSource {
    UNPLUGGED,
    AC,
    USB,
    WIRELESS,
    FAST_CHARGER
}

enum class BatteryHealthStatus {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED_FAILURE,
    COLD,
    UNKNOWN
}

enum class ChargingSpeed {
    SLOW,
    STANDARD,
    FAST,
    TURBO,
    DISCHARGING,
    FULL
}

data class BatteryCurrentPoint(
    val timestamp: Long,
    val currentMa: Int
)

data class BatteryState(
    val currentNowMa: Int = 0,
    val currentAvgMa: Int = 0,
    val minCurrentMa: Int = 0,
    val maxCurrentMa: Int = 0,
    val levelPercent: Int = 0,
    val voltageMv: Int = 0,
    val temperatureC: Float = 0f,
    val health: BatteryHealthStatus = BatteryHealthStatus.GOOD,
    val status: BatteryStatus = BatteryStatus.DISCHARGING,
    val powerSource: PowerSource = PowerSource.UNPLUGGED,
    val technology: String = "Li-ion",
    val designCapacityMah: Int = 5000,
    val remainingCapacityMah: Int = 0,
    val isPresent: Boolean = true,
    val powerWatts: Double = 0.0,
    val estimatedTimeMinutes: Int? = null,
    val isCharging: Boolean = false,
    val speedCategory: ChargingSpeed = ChargingSpeed.DISCHARGING,
    val sampleHistory: List<BatteryCurrentPoint> = emptyList()
)

data class BenchmarkResult(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val secondsLeft: Int = 0,
    val sampleCount: Int = 0,
    val avgCurrentMa: Int = 0,
    val peakCurrentMa: Int = 0,
    val minCurrentMa: Int = 0,
    val avgVoltageMv: Int = 0,
    val avgWatts: Double = 0.0,
    val stabilityPercent: Int = 0,
    val scoreRating: String = "",
    val speedVerdict: String = "",
    val completed: Boolean = false
)

data class BatteryAlertSettings(
    val highTempAlertEnabled: Boolean = true,
    val highTempThresholdC: Float = 42.0f,
    val fullChargeAlertEnabled: Boolean = true,
    val fullChargeThresholdPercent: Int = 80,
    val lowBatteryAlertEnabled: Boolean = true,
    val lowBatteryThresholdPercent: Int = 20,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true
)
