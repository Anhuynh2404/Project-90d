package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DailyLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs WHERE dateEpochDay = :dateEpochDay")
    fun getLogsForDate(dateEpochDay: Long): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay LIMIT 1")
    fun getLogForHabit(dateEpochDay: Long, habitId: String): Flow<DailyLogEntity?>

    @Query("SELECT * FROM daily_logs WHERE habitId = :habitId ORDER BY dateEpochDay DESC LIMIT :limit")
    fun getRecentLogsForHabit(habitId: String, limit: Int = 7): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE habitId = :habitId AND isCompleted = 1 ORDER BY dateEpochDay DESC")
    fun getCompletedLogsForHabit(habitId: String): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs ORDER BY dateEpochDay DESC")
    fun getAllLogs(): Flow<List<DailyLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: DailyLogEntity)

    @Query("UPDATE daily_logs SET isCompleted = :completed, progressValue = :progress, updatedAt = :updatedAt WHERE dateEpochDay = :dateEpochDay AND habitId = :habitId")
    suspend fun updateProgress(dateEpochDay: Long, habitId: String, completed: Boolean, progress: Int, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM daily_logs WHERE dateEpochDay = :dateEpochDay AND isCompleted = 1")
    fun getCompletedCountForDate(dateEpochDay: Long): Flow<Int>

    @Query("DELETE FROM daily_logs")
    suspend fun deleteAllLogs()
}
