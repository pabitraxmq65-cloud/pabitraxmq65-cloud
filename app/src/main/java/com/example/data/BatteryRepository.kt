package com.example.data

import com.example.data.local.BatteryDao
import com.example.data.local.BatterySessionEntity
import kotlinx.coroutines.flow.Flow

class BatteryRepository(private val batteryDao: BatteryDao) {
    val allSessions: Flow<List<BatterySessionEntity>> = batteryDao.getAllSessions()

    suspend fun saveSession(session: BatterySessionEntity): Long {
        return batteryDao.insertSession(session)
    }

    suspend fun deleteSession(id: Long) {
        batteryDao.deleteSessionById(id)
    }

    suspend fun clearHistory() {
        batteryDao.clearAllSessions()
    }
}
