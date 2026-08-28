package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_sessions")
data class BatterySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionType: String, // "CHARGING" or "DISCHARGING"
    val startTime: Long,
    val endTime: Long,
    val startLevel: Int,
    val endLevel: Int,
    val avgCurrentMa: Int,
    val maxCurrentMa: Int,
    val startTempC: Float,
    val endTempC: Float,
    val powerSource: String,
    val estimatedMah: Int
)
