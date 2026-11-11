package com.example.data.repository

import android.content.SharedPreferences
import com.example.data.local.dao.DailyLogDao
import com.example.data.local.dao.FinanceDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.JournalDao
import com.example.data.local.dao.TimeAllocationDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.DailyLogEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.UserEntity
import com.example.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * PHẦN 2.2: Unit Test cho HabitRepository sử dụng MockK & MainDispatcherRule
 * Tuân thủ nghiêm ngặt nguyên tắc AAA (Arrange - Act - Assert).
 */
class HabitRepositoryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userDao: UserDao
    private lateinit var habitDao: HabitDao
    private lateinit var dailyLogDao: DailyLogDao
    private lateinit var financeDao: FinanceDao
    private lateinit var journalDao: JournalDao
    private lateinit var timeAllocationDao: TimeAllocationDao
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var repository: HabitRepository

    @Before
    fun setUp() {
        userDao = mockk(relaxed = true)
        habitDao = mockk(relaxed = true)
        dailyLogDao = mockk(relaxed = true)
        financeDao = mockk(relaxed = true)
        journalDao = mockk(relaxed = true)
        timeAllocationDao = mockk(relaxed = true)
        sharedPreferences = mockk(relaxed = true)

        every { userDao.getUser() } returns flowOf(null)
        every { habitDao.getAllHabits() } returns flowOf(emptyList())
        every { financeDao.getAllTransactions() } returns flowOf(emptyList())
        every { financeDao.getTotalIncome() } returns flowOf(0L)
        every { financeDao.getTotalExpense() } returns flowOf(0L)
        every { financeDao.getTotalSavingsAllocated() } returns flowOf(0L)
        every { financeDao.getAllDebts() } returns flowOf(emptyList())
        every { financeDao.getAllSavingsGoals() } returns flowOf(emptyList())
        every { financeDao.getTotalGoalsSavedAmount() } returns flowOf(0L)
        every { journalDao.getAllJournals() } returns flowOf(emptyList())
        every { journalDao.getCompletedJournalCount() } returns flowOf(0)
        every { dailyLogDao.getCompletedLogsForHabit("reading_20min") } returns flowOf(emptyList())

        repository = HabitRepository(
            userDao = userDao,
            habitDao = habitDao,
            dailyLogDao = dailyLogDao,
            financeDao = financeDao,
            journalDao = journalDao,
            timeAllocationDao = timeAllocationDao
        )
    }

    // =========================================================================
    // 1. TEST LOGIC CHUỖI NGÀY ĐỌC SÁCH LIÊN TIẾP (READING STREAK)
    // =========================================================================

    @Test
    fun `calculateReadingStreak - returns consecutive days count when today and previous days are completed`() = runTest {
        // Arrange
        val today = 20770L
        val mockLogs = listOf(
            DailyLogEntity(id = 1, dateEpochDay = today, habitId = "reading_20min", isCompleted = true, progressValue = 20),
            DailyLogEntity(id = 2, dateEpochDay = today - 1, habitId = "reading_20min", isCompleted = true, progressValue = 20),
            DailyLogEntity(id = 3, dateEpochDay = today - 2, habitId = "reading_20min", isCompleted = true, progressValue = 25),
            DailyLogEntity(id = 4, dateEpochDay = today - 3, habitId = "reading_20min", isCompleted = true, progressValue = 20),
            // Gap at today - 4
            DailyLogEntity(id = 5, dateEpochDay = today - 5, habitId = "reading_20min", isCompleted = true, progressValue = 20)
        )
        every { dailyLogDao.getCompletedLogsForHabit("reading_20min") } returns flowOf(mockLogs)

        // Act
        val streak = repository.calculateReadingStreak(todayEpochDay = today)

        // Assert
        assertEquals(4, streak)
    }

    @Test
    fun `calculateReadingStreak - preserves active streak from yesterday when today is not yet completed`() = runTest {
        // Arrange
        val today = 20770L
        val mockLogs = listOf(
            DailyLogEntity(id = 1, dateEpochDay = today - 1, habitId = "reading_20min", isCompleted = true, progressValue = 20),
            DailyLogEntity(id = 2, dateEpochDay = today - 2, habitId = "reading_20min", isCompleted = true, progressValue = 20),
            DailyLogEntity(id = 3, dateEpochDay = today - 3, habitId = "reading_20min", isCompleted = true, progressValue = 20)
        )
        every { dailyLogDao.getCompletedLogsForHabit("reading_20min") } returns flowOf(mockLogs)

        // Act
        val streak = repository.calculateReadingStreak(todayEpochDay = today)

        // Assert
        assertEquals(3, streak)
    }

    @Test
    fun `calculateReadingStreak - returns 0 when neither today nor yesterday was completed`() = runTest {
        // Arrange
        val today = 20770L
        val mockLogs = listOf(
            DailyLogEntity(id = 1, dateEpochDay = today - 2, habitId = "reading_20min", isCompleted = true, progressValue = 20),
            DailyLogEntity(id = 2, dateEpochDay = today - 3, habitId = "reading_20min", isCompleted = true, progressValue = 20)
        )
        every { dailyLogDao.getCompletedLogsForHabit("reading_20min") } returns flowOf(mockLogs)

        // Act
        val streak = repository.calculateReadingStreak(todayEpochDay = today)

        // Assert
        assertEquals(0, streak)
    }

    // =========================================================================
    // 2. TEST LOGIC TÍNH SỐ NGÀY HIỆN TẠI DỰA TRÊN startDateEpochDay
    // =========================================================================

    @Test
    fun `calculateCurrentDayNumber - calculates exact (todayEpochDay - startDateEpochDay) for active challenge`() {
        // Arrange
        val startDateEpoch = 20755L
        val todayEpoch = 20770L // 15 days after startDate

        // Act
        val currentDay = repository.calculateCurrentDayNumber(
            todayEpochDay = todayEpoch,
            startDateEpochDay = startDateEpoch
        )
        val isCompleted = repository.isChallengeCompleted(
            todayEpochDay = todayEpoch,
            startDateEpochDay = startDateEpoch
        )

        // Assert
        assertEquals(15, currentDay)
        assertFalse(isCompleted)
    }

    @Test
    fun `calculateCurrentDayNumber and isChallengeCompleted - detects challenge completion when elapsed days exceed 90`() {
        // Arrange
        val startDateEpoch = 20670L
        val todayEpoch = 20770L // 100 days elapsed (> 90 days)

        // Act
        val currentDay = repository.calculateCurrentDayNumber(
            todayEpochDay = todayEpoch,
            startDateEpochDay = startDateEpoch
        )
        val isCompleted = repository.isChallengeCompleted(
            todayEpochDay = todayEpoch,
            startDateEpochDay = startDateEpoch
        )

        // Assert
        assertEquals(100, currentDay)
        assertTrue(isCompleted)
    }

    @Test
    fun `calculateCurrentDayNumber - returns 0 safely when system date is before startDateEpochDay`() {
        // Arrange
        val futureStartEpoch = 20800L
        val todayEpoch = 20770L // 30 days before start date

        // Act
        val currentDay = repository.calculateCurrentDayNumber(
            todayEpochDay = todayEpoch,
            startDateEpochDay = futureStartEpoch
        )
        val isCompleted = repository.isChallengeCompleted(
            todayEpochDay = todayEpoch,
            startDateEpochDay = futureStartEpoch
        )

        // Assert
        assertEquals(0, currentDay)
        assertFalse(isCompleted)
    }

    @Test
    fun `getEffectiveStartDateEpochDay - prioritizes SharedPreferences over UserDao when configured`() = runTest {
        // Arrange
        val prefStartEpoch = 20760L
        val dbStartEpoch = 20700L
        every { sharedPreferences.contains("start_date_epoch_day") } returns true
        every { sharedPreferences.getLong("start_date_epoch_day", any()) } returns prefStartEpoch
        every { userDao.getUser() } returns flowOf(
            UserEntity(id = 1, startDateEpochDay = dbStartEpoch, targetDateEpochDay = dbStartEpoch + 90)
        )

        // Act
        val resolvedStartEpoch = repository.getEffectiveStartDateEpochDay(prefs = sharedPreferences)

        // Assert
        assertEquals(prefStartEpoch, resolvedStartEpoch)
    }

    @Test
    fun `getEffectiveStartDateEpochDay - falls back to UserDao when SharedPreferences is not set`() = runTest {
        // Arrange
        val dbStartEpoch = 20710L
        every { sharedPreferences.contains("start_date_epoch_day") } returns false
        every { userDao.getUser() } returns flowOf(
            UserEntity(id = 1, startDateEpochDay = dbStartEpoch, targetDateEpochDay = dbStartEpoch + 90)
        )

        // Act
        val resolvedStartEpoch = repository.getEffectiveStartDateEpochDay(prefs = sharedPreferences)

        // Assert
        assertEquals(dbStartEpoch, resolvedStartEpoch)
    }

    // =========================================================================
    // 3. TEST TỰ ĐỘNG TRÍCH 10% LƯƠNG VÀO QUỸ TIẾT KIỆM MỤC TIÊU
    // =========================================================================

    @Test
    fun `addFinancialTransaction - automatically allocates 10 percent of salary to primary savings goal`() = runTest {
        // Arrange
        val primaryGoal = SavingsGoalEntity(
            id = 10L,
            title = "Quỹ Kỷ Luật 10%",
            targetAmount = 20_000_000L,
            currentAmount = 1_000_000L
        )
        every { financeDao.getAllSavingsGoals() } returns flowOf(listOf(primaryGoal))
        every { dailyLogDao.getLogForHabit(any(), "savings_10pct") } returns flowOf(null)

        // Act
        repository.addFinancialTransaction(
            amount = 15_000_000L,
            type = "INCOME",
            category = "Lương tháng",
            bankSource = "Techcombank",
            description = "Lương tháng 11",
            isSalary = true
        )

        // Assert: 10% of 15,000,000 = 1,500,000 allocated to goal #10 and user totals updated
        coVerify(exactly = 1) {
            financeDao.insertTransaction(
                match {
                    it.amount == 15_000_000L &&
                        it.savingsAllocated == 1_500_000L &&
                        it.type == "INCOME"
                }
            )
        }
        coVerify(exactly = 1) {
            userDao.updateFinanceTotals(savings = 1_500_000L, income = 15_000_000L, expense = 0L)
        }
        coVerify(exactly = 1) {
            financeDao.addAmountToSavingsGoal(goalId = 10L, amountDelta = 1_500_000L)
        }
        coVerify(exactly = 1) {
            dailyLogDao.upsertLog(
                match { it.habitId == "savings_10pct" && it.isCompleted && it.progressValue == 1 }
            )
        }
    }
}
