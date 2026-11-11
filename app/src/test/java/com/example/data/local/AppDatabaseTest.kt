package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.data.local.dao.DailyLogDao
import com.example.data.local.dao.FinanceDao
import com.example.data.local.dao.JournalDao
import com.example.data.local.dao.TimeAllocationDao
import com.example.data.local.database.AppDatabase
import com.example.data.local.entity.DailyJournalEntity
import com.example.data.local.entity.DailyLogEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.FinanceTransactionEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TimeAllocationEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PHẦN 2.1: Kiểm thử tích hợp Room Database (In-Memory Test)
 * Bao phủ: FinanceDao (Transactions, DebtEntity, SavingsGoalEntity),
 * TimeAllocationDao, DailyLogDao và JournalDao theo chuẩn AAA (Arrange - Act - Assert).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var financeDao: FinanceDao
    private lateinit var timeAllocationDao: TimeAllocationDao
    private lateinit var dailyLogDao: DailyLogDao
    private lateinit var journalDao: JournalDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        financeDao = database.financeDao()
        timeAllocationDao = database.timeAllocationDao()
        dailyLogDao = database.dailyLogDao()
        journalDao = database.journalDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // =========================================================================
    // 1. FINANCE DAO TESTS (Giao dịch, Công nợ DebtEntity, Quỹ tiết kiệm SavingsGoalEntity)
    // =========================================================================

    @Test
    fun `FinanceDao - insert and delete transactions calculates total income, expense and savings accurately`() = runTest {
        // Arrange
        val salaryTx = FinanceTransactionEntity(
            dateEpochDay = 20768L,
            amount = 20_000_000L,
            type = "INCOME",
            category = "Lương tháng 11",
            bankSource = "Vietcombank",
            savingsAllocated = 2_000_000L,
            description = "Nhận lương chính thức"
        )
        val expenseTx = FinanceTransactionEntity(
            dateEpochDay = 20768L,
            amount = 1_500_000L,
            type = "EXPENSE",
            category = "Ăn uống Clean Eating",
            bankSource = "Momo",
            savingsAllocated = 0L,
            description = "Mua ức gà và rau củ"
        )

        // Act
        financeDao.insertTransaction(salaryTx)
        financeDao.insertTransaction(expenseTx)

        // Assert
        financeDao.getAllTransactions().test {
            val list = awaitItem()
            assertEquals(2, list.size)
            cancelAndIgnoreRemainingEvents()
        }

        financeDao.getTotalIncome().test {
            assertEquals(20_000_000L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        financeDao.getTotalExpense().test {
            assertEquals(1_500_000L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        financeDao.getTotalSavingsAllocated().test {
            assertEquals(2_000_000L, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        // Act: Delete expense transaction
        financeDao.getAllTransactions().test {
            val expenseId = awaitItem().first { it.type == "EXPENSE" }.id
            financeDao.deleteTransaction(expenseId)
            val remaining = awaitItem()
            assertEquals(1, remaining.size)
            assertEquals("INCOME", remaining.first().type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `FinanceDao - DebtEntity CRUD and updateDebtStatus settles loan accurately`() = runTest {
        // Arrange
        val lendDebt = DebtEntity(
            type = "LEND",
            personName = "Nguyễn Văn A",
            amount = 5_000_000L,
            paidAmount = 0L,
            dueDateEpochDay = 20780L,
            createdDateEpochDay = 20768L,
            isSettled = false,
            note = "Cho mượn xoay vòng vốn"
        )
        val borrowDebt = DebtEntity(
            type = "BORROW",
            personName = "Trần Thị B",
            amount = 3_000_000L,
            paidAmount = 0L,
            dueDateEpochDay = 20775L,
            createdDateEpochDay = 20768L,
            isSettled = false,
            note = "Mượn đóng học phí"
        )

        // Act: Insert debts
        financeDao.insertDebt(lendDebt)
        financeDao.insertDebt(borrowDebt)

        // Assert initial state (unsettled, ordered by dueDateEpochDay ASC)
        var insertedLendId = 0L
        financeDao.getAllDebts().test {
            val debts = awaitItem()
            assertEquals(2, debts.size)
            // borrowDebt has dueDate 20775 < lendDebt 20780, so borrowDebt comes first
            assertEquals("Trần Thị B", debts[0].personName)
            assertEquals("Nguyễn Văn A", debts[1].personName)
            insertedLendId = debts[1].id
            cancelAndIgnoreRemainingEvents()
        }

        // Act: Mark lendDebt as settled (Đã tất toán)
        financeDao.updateDebtStatus(
            id = insertedLendId,
            isSettled = true,
            paidAmount = 5_000_000L
        )

        // Assert settled status and ordering (isSettled ASC puts unsettled first)
        financeDao.getAllDebts().test {
            val debtsAfterUpdate = awaitItem()
            val updatedLend = debtsAfterUpdate.first { it.id == insertedLendId }
            assertTrue(updatedLend.isSettled)
            assertEquals(5_000_000L, updatedLend.paidAmount)
            cancelAndIgnoreRemainingEvents()
        }

        // Act: Delete debt
        financeDao.deleteDebt(insertedLendId)

        // Assert deletion
        financeDao.getAllDebts().test {
            val remaining = awaitItem()
            assertEquals(1, remaining.size)
            assertEquals("BORROW", remaining.first().type)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `FinanceDao - SavingsGoalEntity insert and addAmountToSavingsGoal increments fund balance`() = runTest {
        // Arrange
        val carGoal = SavingsGoalEntity(
            title = "Quỹ Mua Xe Máy",
            targetAmount = 50_000_000L,
            currentAmount = 10_000_000L,
            categoryIcon = "CAR",
            note = "Mục tiêu cuối năm"
        )
        val studyGoal = SavingsGoalEntity(
            title = "Quỹ Học Tiếng Anh",
            targetAmount = 15_000_000L,
            currentAmount = 2_000_000L,
            categoryIcon = "SCHOOL",
            note = "Luyện thi IELTS"
        )

        // Act
        financeDao.insertSavingsGoal(carGoal)
        financeDao.insertSavingsGoal(studyGoal)

        // Assert count & initial sum
        assertEquals(2, financeDao.getSavingsGoalCount())
        var carGoalId = 0L
        financeDao.getAllSavingsGoals().test {
            val goals = awaitItem()
            assertEquals(2, goals.size)
            carGoalId = goals.first { it.title == "Quỹ Mua Xe Máy" }.id
            cancelAndIgnoreRemainingEvents()
        }

        // Act: Allocate 3,500,000 VND from income into carGoal
        financeDao.addAmountToSavingsGoal(goalId = carGoalId, amountDelta = 3_500_000L)

        // Assert updated balance and total saved across all goals
        financeDao.getAllSavingsGoals().test {
            val updatedCarGoal = awaitItem().first { it.id == carGoalId }
            assertEquals(13_500_000L, updatedCarGoal.currentAmount)
            cancelAndIgnoreRemainingEvents()
        }

        financeDao.getTotalGoalsSavedAmount().test {
            assertEquals(15_500_000L, awaitItem()) // 13.5M + 2.0M
            cancelAndIgnoreRemainingEvents()
        }
    }

    // =========================================================================
    // 2. TIME ALLOCATION DAO TESTS (Quỹ thời gian 24h)
    // =========================================================================

    @Test
    fun `TimeAllocationDao - insertAllAllocations and upsertAllocation enforces unique activityType per day`() = runTest {
        // Arrange
        val today = 20768L
        val allocations = listOf(
            TimeAllocationEntity(dateEpochDay = today, activityType = "SLEEP", hours = 7.5f),
            TimeAllocationEntity(dateEpochDay = today, activityType = "WORK", hours = 8.0f),
            TimeAllocationEntity(dateEpochDay = today, activityType = "STUDY", hours = 3.0f),
            TimeAllocationEntity(dateEpochDay = today, activityType = "REST", hours = 3.5f),
            TimeAllocationEntity(dateEpochDay = today, activityType = "PLAY", hours = 2.0f)
        )

        // Act
        timeAllocationDao.insertAllAllocations(allocations)

        // Assert initial 5 categories totaling 24.0h
        assertEquals(5, timeAllocationDao.getAllocationCountForDate(today))
        assertEquals(24.0f, timeAllocationDao.getTotalAllocatedHoursForDate(today) ?: 0f, 0.01f)

        // Act: Upsert STUDY from 3.0h to 4.5h on the same day (should REPLACE, not duplicate)
        timeAllocationDao.upsertAllocation(
            TimeAllocationEntity(
                dateEpochDay = today,
                activityType = "STUDY",
                hours = 4.5f,
                note = "Tăng giờ học Kotlin & Testing"
            )
        )

        // Assert still 5 records and STUDY is updated to 4.5h
        assertEquals(5, timeAllocationDao.getAllocationCountForDate(today))
        timeAllocationDao.getAllocationsForDate(today).test {
            val list = awaitItem()
            val studyItem = list.first { it.activityType == "STUDY" }
            assertEquals(4.5f, studyItem.hours, 0.01f)
            assertEquals("Tăng giờ học Kotlin & Testing", studyItem.note)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // =========================================================================
    // 3. DAILY LOG DAO TESTS (Theo dõi thói quen & chuỗi đọc sách)
    // =========================================================================

    @Test
    fun `DailyLogDao - upsertLog and getCompletedLogsForHabit filters and sorts accurately`() = runTest {
        // Arrange
        val day1 = 20765L
        val day2 = 20766L
        val day3 = 20767L

        val logDay1 = DailyLogEntity(dateEpochDay = day1, habitId = "reading_20min", isCompleted = true, progressValue = 20)
        val logDay2 = DailyLogEntity(dateEpochDay = day2, habitId = "reading_20min", isCompleted = false, progressValue = 10)
        val logDay3 = DailyLogEntity(dateEpochDay = day3, habitId = "reading_20min", isCompleted = true, progressValue = 25)

        // Act
        dailyLogDao.upsertLog(logDay1)
        dailyLogDao.upsertLog(logDay2)
        dailyLogDao.upsertLog(logDay3)

        // Assert getCompletedLogsForHabit only returns completed logs in DESC date order
        dailyLogDao.getCompletedLogsForHabit("reading_20min").test {
            val completedLogs = awaitItem()
            assertEquals(2, completedLogs.size)
            assertEquals(day3, completedLogs[0].dateEpochDay)
            assertEquals(day1, completedLogs[1].dateEpochDay)
            cancelAndIgnoreRemainingEvents()
        }

        // Act: Update logDay2 to completed
        val existingDay2 = dailyLogDao.getLogForHabit(day2, "reading_20min")
        assertNotNull(existingDay2)
        dailyLogDao.upsertLog(
            logDay2.copy(isCompleted = true, progressValue = 20)
        )

        // Assert all 3 days are now completed
        dailyLogDao.getCompletedLogsForHabit("reading_20min").test {
            val completedLogs = awaitItem()
            assertEquals(3, completedLogs.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // =========================================================================
    // 4. JOURNAL DAO TESTS (3 Dòng nhật ký tối cô đọng)
    // =========================================================================

    @Test
    fun `JournalDao - upsertJournal and getCompletedJournalCount counts only full 3-line journals`() = runTest {
        // Arrange
        val completeJournal = DailyJournalEntity(
            dateEpochDay = 20767L,
            line1GoodThings = "Hoàn thành 10.000 bước chân và uống đủ 2L nước",
            line2LessonLearned = "Kỷ luật nhỏ mỗi ngày tạo ra đà tâm lý lớn",
            line3TomorrowGoal = "Dậy sớm đọc sách 20 phút trước khi làm việc",
            readingSecondsCompleted = 1200
        )
        val incompleteJournal = DailyJournalEntity(
            dateEpochDay = 20768L,
            line1GoodThings = "Tập đủ 50 hít đất",
            line2LessonLearned = "", // Line 2 blank -> not counted as completed
            line3TomorrowGoal = "Giữ vững No Sugar",
            readingSecondsCompleted = 600
        )

        // Act
        journalDao.upsertJournal(completeJournal)
        journalDao.upsertJournal(incompleteJournal)

        // Assert: getJournalForDate returns exact entry
        journalDao.getJournalForDate(20767L).test {
            val entry = awaitItem()
            assertNotNull(entry)
            assertEquals("Kỷ luật nhỏ mỗi ngày tạo ra đà tâm lý lớn", entry?.line2LessonLearned)
            cancelAndIgnoreRemainingEvents()
        }

        journalDao.getJournalForDate(99999L).test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        // Assert: getCompletedJournalCount only counts the entry where all 3 lines are non-empty
        journalDao.getCompletedJournalCount().test {
            assertEquals(1, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        // Act: Complete line 2 of the second journal
        journalDao.upsertJournal(
            incompleteJournal.copy(line2LessonLearned = "Không xem điện thoại sau 22:00 giúp ngủ sâu hơn")
        )

        // Assert: Completed journal count increments to 2
        journalDao.getCompletedJournalCount().test {
            assertEquals(2, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
