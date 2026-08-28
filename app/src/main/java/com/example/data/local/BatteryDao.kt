package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {
    @Query("SELECT * FROM battery_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<BatterySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: BatterySessionEntity): Long

    @Query("DELETE FROM battery_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM battery_sessions")
    suspend fun clearAllSessions()
}
