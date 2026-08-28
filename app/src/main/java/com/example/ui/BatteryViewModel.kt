package com.example.ui

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BatteryAlertSettings
import com.example.data.BatteryCurrentPoint
import com.example.data.BatteryRepository
import com.example.data.BatteryState
import com.example.data.BatteryStatus
import com.example.data.BenchmarkResult
import com.example.data.local.BatteryDatabase
import com.example.data.local.BatterySessionEntity
import com.example.service.BatteryManagerHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

    private val batteryHelper = BatteryManagerHelper(application)
    private val database = BatteryDatabase.getDatabase(application)
    private val repository = BatteryRepository(database.batteryDao())

    private val _batteryState = MutableStateFlow(batteryHelper.getBatteryState())
    val batteryState: StateFlow<BatteryState> = _batteryState.asStateFlow()

    private val _rollingSamples = mutableListOf<BatteryCurrentPoint>()

    val sessionHistory: StateFlow<List<BatterySessionEntity>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _benchmarkState = MutableStateFlow(BenchmarkResult())
    val benchmarkState: StateFlow<BenchmarkResult> = _benchmarkState.asStateFlow()

    private val _alertSettings = MutableStateFlow(BatteryAlertSettings())
    val alertSettings: StateFlow<BatteryAlertSettings> = _alertSettings.asStateFlow()

    private val _activeAlertMessage = MutableStateFlow<String?>(null)
    val activeAlertMessage: StateFlow<String?> = _activeAlertMessage.asStateFlow()

    // Session tracking
    private var currentSessionStartTime: Long = System.currentTimeMillis()
    private var currentSessionStartLevel: Int = _batteryState.value.levelPercent
    private var currentSessionStartTemp: Float = _batteryState.value.temperatureC
    private var currentSessionIsCharging: Boolean = _batteryState.value.isCharging
    private val currentSessionSamples = mutableListOf<Int>()

    private var monitoringJob: Job? = null
    private var benchmarkJob: Job? = null

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            while (isActive) {
                val state = batteryHelper.getBatteryState()
                val now = System.currentTimeMillis()

                // Update rolling buffer for waveform graph
                _rollingSamples.add(BatteryCurrentPoint(now, state.currentNowMa))
                if (_rollingSamples.size > 50) {
                    _rollingSamples.removeAt(0)
                }

                val updatedState = state.copy(sampleHistory = _rollingSamples.toList())
                _batteryState.value = updatedState

                // Track active session for history
                handleSessionTracking(updatedState)

                // Check alert triggers
                checkBatteryAlerts(updatedState)

                delay(1000)
            }
        }
    }

    private fun handleSessionTracking(state: BatteryState) {
        currentSessionSamples.add(state.currentNowMa)

        if (state.isCharging != currentSessionIsCharging) {
            // Charging state changed, save the completed session
            val endTime = System.currentTimeMillis()
            val durationMs = endTime - currentSessionStartTime

            if (durationMs > 10000 && currentSessionSamples.isNotEmpty()) { // Minimum 10 seconds to avoid bounce
                val avgMa = currentSessionSamples.average().toInt()
                val maxMa = if (currentSessionIsCharging) {
                    currentSessionSamples.maxOrNull() ?: 0
                } else {
                    currentSessionSamples.minOrNull() ?: 0
                }

                val levelDelta = abs(state.levelPercent - currentSessionStartLevel)
                val estimatedMah = (state.designCapacityMah * (levelDelta / 100.0)).toInt()

                val sessionEntity = BatterySessionEntity(
                    sessionType = if (currentSessionIsCharging) "CHARGING" else "DISCHARGING",
                    startTime = currentSessionStartTime,
                    endTime = endTime,
                    startLevel = currentSessionStartLevel,
                    endLevel = state.levelPercent,
                    avgCurrentMa = avgMa,
                    maxCurrentMa = maxMa,
                    startTempC = currentSessionStartTemp,
                    endTempC = state.temperatureC,
                    powerSource = state.powerSource.name,
                    estimatedMah = estimatedMah
                )

                viewModelScope.launch {
                    repository.saveSession(sessionEntity)
                }
            }

            // Reset new session tracker
            currentSessionStartTime = endTime
            currentSessionStartLevel = state.levelPercent
            currentSessionStartTemp = state.temperatureC
            currentSessionIsCharging = state.isCharging
            currentSessionSamples.clear()
            batteryHelper.resetSessionMinMax()
        }
    }

    private fun checkBatteryAlerts(state: BatteryState) {
        val settings = _alertSettings.value

        if (settings.highTempAlertEnabled && state.temperatureC >= settings.highTempThresholdC) {
            triggerAlert("High Battery Temperature: ${state.temperatureC}°C! Consider cooling down.")
        } else if (settings.fullChargeAlertEnabled && state.isCharging && state.levelPercent >= settings.fullChargeThresholdPercent) {
            triggerAlert("Battery reached ${state.levelPercent}% target level! Unplug to preserve longevity.")
        } else if (settings.lowBatteryAlertEnabled && !state.isCharging && state.levelPercent <= settings.lowBatteryThresholdPercent) {
            triggerAlert("Low Battery: ${state.levelPercent}% remaining. Please connect charger.")
        }
    }

    private fun triggerAlert(message: String) {
        if (_activeAlertMessage.value == message) return // Don't repeat identical active alert
        _activeAlertMessage.value = message

        val settings = _alertSettings.value
        if (settings.soundEnabled) {
            try {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
            } catch (_: Exception) {}
        }
        if (settings.vibrateEnabled) {
            try {
                val vibrator = getApplication<Application>().getSystemService(Application.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } catch (_: Exception) {}
        }
    }

    fun dismissAlert() {
        _activeAlertMessage.value = null
    }

    fun updateAlertSettings(settings: BatteryAlertSettings) {
        _alertSettings.value = settings
    }

    fun resetMinMax() {
        batteryHelper.resetSessionMinMax()
        val current = _batteryState.value
        _batteryState.value = current.copy(
            minCurrentMa = current.currentNowMa,
            maxCurrentMa = current.currentNowMa
        )
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteSession(id)
        }
    }

    fun clearSessionHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun startBenchmark(durationSeconds: Int = 15) {
        if (_benchmarkState.value.isRunning) return

        benchmarkJob?.cancel()
        benchmarkJob = viewModelScope.launch {
            _benchmarkState.value = BenchmarkResult(
                isRunning = true,
                progress = 0f,
                secondsLeft = durationSeconds,
                completed = false
            )

            val currentSamples = mutableListOf<Int>()
            val voltageSamples = mutableListOf<Int>()
            val totalSteps = durationSeconds * 2 // 2 samples per second

            for (step in 1..totalSteps) {
                delay(500)
                val state = batteryHelper.getBatteryState()
                currentSamples.add(state.currentNowMa)
                voltageSamples.add(state.voltageMv)

                val progress = step.toFloat() / totalSteps
                val secondsLeft = ((totalSteps - step) * 0.5f).toInt()

                _benchmarkState.value = _benchmarkState.value.copy(
                    progress = progress,
                    secondsLeft = secondsLeft,
                    sampleCount = currentSamples.size
                )
            }

            // Benchmark complete - compute analytical rating
            val avgMa = currentSamples.average().toInt()
            val peakMa = currentSamples.maxOrNull() ?: 0
            val minMa = currentSamples.minOrNull() ?: 0
            val avgVoltage = voltageSamples.average().toInt()
            val avgWatts = (avgVoltage / 1000.0) * (abs(avgMa) / 1000.0)

            // Stability based on standard deviation
            val mean = avgMa.toDouble()
            val variance = currentSamples.map { (it - mean) * (it - mean) }.average()
            val stdDev = sqrt(variance)
            val stability = (100 - (stdDev / (abs(avgMa) + 1.0) * 100)).toInt().coerceIn(40, 99)

            val score = when {
                avgMa >= 3000 -> "A+ (Ultra Fast)"
                avgMa >= 2000 -> "A (Fast Charging)"
                avgMa >= 1200 -> "B (Standard Quick)"
                avgMa >= 700 -> "C (Standard USB)"
                avgMa > 0 -> "D (Slow / Weak Cable)"
                else -> "N/A (Discharging)"
            }

            val verdict = when {
                avgMa >= 2500 -> "High wattage power delivery detected. Charger & cable have excellent throughput with stable current."
                avgMa >= 1500 -> "Good charging rate. Suitable for steady daily charging without excessive heat generation."
                avgMa >= 700 -> "Moderate speed. Could be a standard 5V USB port or older cable resistance."
                avgMa > 0 -> "Very slow charging current. Check if cable is damaged or power adapter rating is low."
                else -> "Device is not connected to a power source. Connect charger to test cable speed."
            }

            _benchmarkState.value = BenchmarkResult(
                isRunning = false,
                progress = 1f,
                secondsLeft = 0,
                sampleCount = currentSamples.size,
                avgCurrentMa = avgMa,
                peakCurrentMa = peakMa,
                minCurrentMa = minMa,
                avgVoltageMv = avgVoltage,
                avgWatts = (avgWatts * 100).toInt() / 100.0,
                stabilityPercent = stability,
                scoreRating = score,
                speedVerdict = verdict,
                completed = true
            )
        }
    }

    fun cancelBenchmark() {
        benchmarkJob?.cancel()
        _benchmarkState.value = BenchmarkResult(isRunning = false)
    }

    override fun onCleared() {
        super.onCleared()
        monitoringJob?.cancel()
        benchmarkJob?.cancel()
    }
}
