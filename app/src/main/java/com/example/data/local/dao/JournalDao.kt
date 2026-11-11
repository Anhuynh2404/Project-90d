package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DailyJournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM daily_journals WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    fun getJournalForDate(dateEpochDay: Long): Flow<DailyJournalEntity?>

    @Query("SELECT * FROM daily_journals ORDER BY dateEpochDay DESC")
    fun getAllJournals(): Flow<List<DailyJournalEntity>>

    @Query("SELECT COUNT(*) FROM daily_journals WHERE TRIM(line1GoodThings) != '' AND TRIM(line2LessonLearned) != '' AND TRIM(line3TomorrowGoal) != ''")
    fun getCompletedJournalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertJournal(journal: DailyJournalEntity)

    @Query("UPDATE daily_journals SET readingSecondsCompleted = :seconds, updatedAt = :updatedAt WHERE dateEpochDay = :dateEpochDay")
    suspend fun updateReadingProgress(dateEpochDay: Long, seconds: Int, updatedAt: Long)
}
