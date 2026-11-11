package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.TimeAllocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeAllocationDao {
    @Query("SELECT * FROM time_allocations WHERE dateEpochDay = :dateEpochDay")
    fun getAllocationsForDate(dateEpochDay: Long): Flow<List<TimeAllocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllocation(allocation: TimeAllocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAllocations(allocations: List<TimeAllocationEntity>)

    @Query("SELECT COUNT(*) FROM time_allocations WHERE dateEpochDay = :dateEpochDay")
    suspend fun getAllocationCountForDate(dateEpochDay: Long): Int

    @Query("SELECT SUM(hours) FROM time_allocations WHERE dateEpochDay = :dateEpochDay")
    suspend fun getTotalAllocatedHoursForDate(dateEpochDay: Long): Float?
}
