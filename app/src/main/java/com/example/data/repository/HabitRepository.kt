package com.example.data.repository

import com.example.data.local.dao.DailyLogDao
import com.example.data.local.dao.FinanceDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.JournalDao
import com.example.data.local.dao.TimeAllocationDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.DailyJournalEntity
import com.example.data.local.entity.DailyLogEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.FinanceTransactionEntity
import com.example.data.local.entity.HabitCategory
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TimeActivityCategory
import com.example.data.local.entity.TimeAllocationEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

class HabitRepository(
    private val userDao: UserDao,
    private val habitDao: HabitDao,
    private val dailyLogDao: DailyLogDao,
    private val financeDao: FinanceDao,
    private val journalDao: JournalDao,
    private val timeAllocationDao: TimeAllocationDao
) {
    val userFlow: Flow<UserEntity?> = userDao.getUser()
    val allHabitsFlow: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allFinanceFlow: Flow<List<FinanceTransactionEntity>> = financeDao.getAllTransactions()
    val totalIncomeFlow: Flow<Long?> = financeDao.getTotalIncome()
    val totalExpenseFlow: Flow<Long?> = financeDao.getTotalExpense()
    val totalSavingsFlow: Flow<Long?> = financeDao.getTotalSavingsAllocated()

    val allDebtsFlow: Flow<List<DebtEntity>> = financeDao.getAllDebts()
    val allSavingsGoalsFlow: Flow<List<SavingsGoalEntity>> = financeDao.getAllSavingsGoals()
    val totalGoalsSavedFlow: Flow<Long?> = financeDao.getTotalGoalsSavedAmount()

    val allJournalsFlow: Flow<List<DailyJournalEntity>> = journalDao.getAllJournals()
    val completedJournalCountFlow: Flow<Int> = journalDao.getCompletedJournalCount()
    val completedReadingLogsFlow: Flow<List<DailyLogEntity>> =
        dailyLogDao.getCompletedLogsForHabit("reading_20min")

    fun getLogsForDate(dateEpochDay: Long): Flow<List<DailyLogEntity>> =
        dailyLogDao.getLogsForDate(dateEpochDay)

    fun getRecentStepLogs(limit: Int = 7): Flow<List<DailyLogEntity>> =
        dailyLogDao.getRecentLogsForHabit("pedometer_10k", limit)

    fun getJournalForDate(dateEpochDay: Long): Flow<DailyJournalEntity?> =
        journalDao.getJournalForDate(dateEpochDay)

    fun getTimeAllocationsForDate(dateEpochDay: Long): Flow<List<TimeAllocationEntity>> =
        timeAllocationDao.getAllocationsForDate(dateEpochDay)

    suspend fun initializeDefaultsIfNeeded() {
        if (habitDao.getHabitCount() == 0) {
            val defaultHabits = listOf(
                HabitEntity(
                    id = "pedometer_10k",
                    title = "Đi bộ 10.000 bước mỗi ngày",
                    category = HabitCategory.MOVEMENT.name,
                    targetValue = 10000,
                    unit = "bước",
                    description = "Đốt mỡ thụ động, cải thiện trao đổi chất và tăng sức bền",
                    displayOrder = 1
                ),
                HabitEntity(
                    id = "pushups_50",
                    title = "50 lần Hít đất (Push-ups)",
                    category = HabitCategory.MOVEMENT.name,
                    targetValue = 50,
                    unit = "cái",
                    description = "Xây dựng cơ ngực, vai và bắp tay sau rắn rỏi",
                    displayOrder = 2
                ),
                HabitEntity(
                    id = "squats_100",
                    title = "100 lần Squats",
                    category = HabitCategory.MOVEMENT.name,
                    targetValue = 100,
                    unit = "cái",
                    description = "Gia tăng testosterone tự nhiên, săn chắc thân dưới",
                    displayOrder = 3
                ),
                HabitEntity(
                    id = "cold_shower",
                    title = "Tắm nước lạnh sảng khoái",
                    category = HabitCategory.MOVEMENT.name,
                    targetValue = 1,
                    unit = "lần",
                    description = "Thức tỉnh hệ thần kinh giao cảm, tăng dopamine bền vững",
                    displayOrder = 4
                ),
                HabitEntity(
                    id = "workout_weekly",
                    title = "2 buổi tập Gym / Kháng lực",
                    category = HabitCategory.MOVEMENT.name,
                    isWeekly = true,
                    targetValue = 2,
                    unit = "buổi/tuần",
                    description = "Kích thích phì đại cơ bắp toàn thân với tạ nặng",
                    displayOrder = 5
                ),
                HabitEntity(
                    id = "football_weekly",
                    title = "1 trận đá bóng đối kháng",
                    category = HabitCategory.MOVEMENT.name,
                    isWeekly = true,
                    targetValue = 1,
                    unit = "trận/tuần",
                    description = "Tập luyện tim mạch cường độ cao và phản xạ đồng đội",
                    displayOrder = 6
                ),
                HabitEntity(
                    id = "no_sugar",
                    title = "Kỷ luật Tuyệt Đối: Không Đường (No Sugar)",
                    category = HabitCategory.NUTRITION.name,
                    targetValue = 1,
                    unit = "ngày",
                    description = "Cắt đứt đồ ngọt, trà sữa, nước có gas. Giải phóng gan nhiễm mỡ",
                    displayOrder = 7
                ),
                HabitEntity(
                    id = "water_2l",
                    title = "Uống đủ 2 Lít nước lọc",
                    category = HabitCategory.NUTRITION.name,
                    targetValue = 2000,
                    unit = "ml",
                    description = "Thanh lọc cơ thể, tối ưu hoạt động tế bào não và cơ bắp",
                    displayOrder = 8
                ),
                HabitEntity(
                    id = "caffeine_curfew",
                    title = "Ngừng nạp Caffeine sau 14:00",
                    category = HabitCategory.DIGITAL_WELLBEING.name,
                    targetValue = 1,
                    unit = "ngày",
                    description = "Giữ thụ thể Adenosine tự nhiên giúp đi vào giấc ngủ sâu",
                    displayOrder = 9
                ),
                HabitEntity(
                    id = "screen_limit_22",
                    title = "Khóa màn hình điện thoại sau 22:00",
                    category = HabitCategory.DIGITAL_WELLBEING.name,
                    targetValue = 1,
                    unit = "ngày",
                    description = "Chặn ánh sáng xanh ức chế Melatonin, chuẩn bị phục hồi",
                    displayOrder = 10
                ),
                HabitEntity(
                    id = "bedtime_2230",
                    title = "Lên giường ngủ trước 22:30",
                    category = HabitCategory.DIGITAL_WELLBEING.name,
                    targetValue = 1,
                    unit = "ngày",
                    description = "Tối đa hóa pha ngủ sâu (Deep Sleep) và sóng delta",
                    displayOrder = 11
                ),
                HabitEntity(
                    id = "savings_10pct",
                    title = "Tự động trích 10% thu nhập vào quỹ tiết kiệm",
                    category = HabitCategory.FINANCE.name,
                    targetValue = 1,
                    unit = "lần",
                    description = "Nguyên tắc 'Pay Yourself First' trước khi chi tiêu bất kỳ khoản nào",
                    displayOrder = 12
                ),
                HabitEntity(
                    id = "reading_20min",
                    title = "Đọc sách 20 phút (Khóa tập trung)",
                    category = HabitCategory.SELF_GROWTH.name,
                    targetValue = 20,
                    unit = "phút",
                    description = "Nâng cấp tư duy, mở rộng vốn từ và khả năng duy trì tập trung",
                    displayOrder = 13
                ),
                HabitEntity(
                    id = "journal_3lines",
                    title = "3 dòng nhật ký tối cô đọng",
                    category = HabitCategory.SELF_GROWTH.name,
                    targetValue = 3,
                    unit = "dòng",
                    description = "1. Việc làm tốt - 2. Bài học rút ra - 3. Mục tiêu ngày mai",
                    displayOrder = 14
                )
            )
            habitDao.insertAll(defaultHabits)
        } else {
            habitDao.deleteHabitById("alarm_5am_camera")
        }

        val officialStartDate = LocalDate.of(2026, 11, 7)
        val targetDate = officialStartDate.plusDays(90)

        val existingUser = userDao.getUser().firstOrNull()
        if (existingUser == null) {
            val initialUser = UserEntity(
                id = 1,
                name = "Kẻ Kỷ Luật Thép",
                startDateEpochDay = officialStartDate.toEpochDay(),
                targetDateEpochDay = targetDate.toEpochDay(),
                currentStreak = 0,
                longestStreak = 0,
                lastCompletedDateEpochDay = -1L,
                totalSavings = 0L,
                totalIncome = 0L,
                totalExpense = 0L
            )
            userDao.insertOrUpdate(initialUser)
        }

        // Seed default Savings Goal funds if empty
        if (financeDao.getSavingsGoalCount() == 0) {
            financeDao.insertSavingsGoal(
                SavingsGoalEntity(
                    title = "Quỹ Kỷ Luật 10% (Tự Động)",
                    targetAmount = 20_000_000L,
                    currentAmount = 0L,
                    categoryIcon = "SAVINGS",
                    note = "Tự động trích 10% mỗi khi có thu nhập lương"
                )
            )
            financeDao.insertSavingsGoal(
                SavingsGoalEntity(
                    title = "Quỹ Mua Xe / Phương Tiện",
                    targetAmount = 50_000_000L,
                    currentAmount = 0L,
                    categoryIcon = "CAR",
                    note = "Tích lũy nâng cấp phương tiện đi lại"
                )
            )
            financeDao.insertSavingsGoal(
                SavingsGoalEntity(
                    title = "Quỹ Đóng Học & Kỹ Năng",
                    targetAmount = 15_000_000L,
                    currentAmount = 0L,
                    categoryIcon = "SCHOOL",
                    note = "Đầu tư khóa học, sách và chứng chỉ chuyên môn"
                )
            )
        }

        // Seed default 24h Time Allocation for today if empty
        val todayEpoch = LocalDate.now().toEpochDay()
        ensureTimeAllocationsForDate(todayEpoch)
    }

    suspend fun ensureTimeAllocationsForDate(dateEpochDay: Long) {
        if (timeAllocationDao.getAllocationCountForDate(dateEpochDay) == 0) {
            val defaults = TimeActivityCategory.entries.map { cat ->
                TimeAllocationEntity(
                    dateEpochDay = dateEpochDay,
                    activityType = cat.code,
                    hours = cat.recommendedHours,
                    note = "Phân bổ chuẩn: ${cat.labelVi}"
                )
            }
            timeAllocationDao.insertAllAllocations(defaults)
        }
    }

    suspend fun updateTimeAllocation(
        dateEpochDay: Long,
        activityType: String,
        hours: Float,
        note: String = ""
    ) {
        timeAllocationDao.upsertAllocation(
            TimeAllocationEntity(
                dateEpochDay = dateEpochDay,
                activityType = activityType,
                hours = hours.coerceIn(0f, 24f),
                note = note,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun setHabitProgress(dateEpochDay: Long, habitId: String, completed: Boolean, progressVal: Int) {
        val existing = dailyLogDao.getLogForHabit(dateEpochDay, habitId).firstOrNull()
        dailyLogDao.upsertLog(
            DailyLogEntity(
                id = existing?.id ?: 0L,
                dateEpochDay = dateEpochDay,
                habitId = habitId,
                isCompleted = completed,
                progressValue = progressVal,
                updatedAt = System.currentTimeMillis()
            )
        )
        evaluateStreak(dateEpochDay)
    }

    suspend fun addWater(dateEpochDay: Long, amountMl: Int) {
        val currentLog = dailyLogDao.getLogForHabit(dateEpochDay, "water_2l").firstOrNull()
        val currentVal = currentLog?.progressValue ?: 0
        val newVal = (currentVal + amountMl).coerceAtLeast(0)
        val isCompleted = newVal >= 2000

        dailyLogDao.upsertLog(
            DailyLogEntity(
                id = currentLog?.id ?: 0L,
                dateEpochDay = dateEpochDay,
                habitId = "water_2l",
                isCompleted = isCompleted,
                progressValue = newVal,
                updatedAt = System.currentTimeMillis()
            )
        )
        evaluateStreak(dateEpochDay)
    }

    suspend fun updateSteps(dateEpochDay: Long, steps: Int) {
        val currentLog = dailyLogDao.getLogForHabit(dateEpochDay, "pedometer_10k").firstOrNull()
        val isCompleted = steps >= 10000

        dailyLogDao.upsertLog(
            DailyLogEntity(
                id = currentLog?.id ?: 0L,
                dateEpochDay = dateEpochDay,
                habitId = "pedometer_10k",
                isCompleted = isCompleted,
                progressValue = steps,
                updatedAt = System.currentTimeMillis()
            )
        )
        evaluateStreak(dateEpochDay)
    }

    suspend fun addFinancialTransaction(
        amount: Long,
        type: String,
        category: String,
        bankSource: String,
        description: String,
        isSalary: Boolean
    ) {
        val todayEpoch = LocalDate.now().toEpochDay()
        val savings = if (type == "INCOME" && (isSalary || category.contains("Lương", ignoreCase = true))) {
            (amount * 10) / 100 // 10% auto allocation
        } else {
            0L
        }

        val tx = FinanceTransactionEntity(
            dateEpochDay = todayEpoch,
            amount = amount,
            type = type,
            category = category,
            bankSource = bankSource,
            savingsAllocated = savings,
            description = description
        )
        financeDao.insertTransaction(tx)

        val incomeDelta = if (type == "INCOME") amount else 0L
        val expenseDelta = if (type == "EXPENSE") amount else 0L
        userDao.updateFinanceTotals(savings = savings, income = incomeDelta, expense = expenseDelta)

        if (savings > 0L) {
            val goals = financeDao.getAllSavingsGoals().firstOrNull().orEmpty()
            val primaryGoal = goals.firstOrNull()
            if (primaryGoal != null) {
                financeDao.addAmountToSavingsGoal(primaryGoal.id, savings)
            }
            setHabitProgress(todayEpoch, "savings_10pct", true, 1)
        }
    }

    suspend fun deleteTransaction(id: Long) {
        financeDao.deleteTransaction(id)
    }

    // Debt Management (Cho vay / Đi mượn)
    suspend fun addDebt(
        type: String,
        personName: String,
        amount: Long,
        dueDateEpochDay: Long,
        note: String
    ) {
        financeDao.insertDebt(
            DebtEntity(
                type = type,
                personName = personName,
                amount = amount,
                paidAmount = 0L,
                dueDateEpochDay = dueDateEpochDay,
                createdDateEpochDay = LocalDate.now().toEpochDay(),
                isSettled = false,
                note = note
            )
        )
    }

    suspend fun toggleDebtSettled(debt: DebtEntity) {
        val newSettled = !debt.isSettled
        val newPaid = if (newSettled) debt.amount else 0L
        financeDao.updateDebtStatus(debt.id, newSettled, newPaid)
    }

    suspend fun deleteDebt(id: Long) {
        financeDao.deleteDebt(id)
    }

    // Savings Goal Management (Quỹ tiết kiệm mục tiêu)
    suspend fun addSavingsGoal(
        title: String,
        targetAmount: Long,
        categoryIcon: String,
        note: String
    ) {
        financeDao.insertSavingsGoal(
            SavingsGoalEntity(
                title = title,
                targetAmount = targetAmount,
                currentAmount = 0L,
                categoryIcon = categoryIcon,
                note = note
            )
        )
    }

    suspend fun allocateToSavingsGoal(
        goalId: Long,
        goalTitle: String,
        amount: Long,
        recordTransaction: Boolean = true
    ) {
        if (amount <= 0L) return
        val todayEpoch = LocalDate.now().toEpochDay()
        financeDao.addAmountToSavingsGoal(goalId, amount)
        userDao.updateFinanceTotals(savings = amount, income = 0L, expense = 0L)

        if (recordTransaction) {
            financeDao.insertTransaction(
                FinanceTransactionEntity(
                    dateEpochDay = todayEpoch,
                    amount = amount,
                    type = "INCOME",
                    category = "Trích Quỹ: $goalTitle",
                    bankSource = "Chuyển Quỹ",
                    savingsAllocated = amount,
                    description = "Trích tiền vào quỹ mục tiêu: $goalTitle"
                )
            )
        }
        setHabitProgress(todayEpoch, "savings_10pct", true, 1)
    }

    suspend fun deleteSavingsGoal(id: Long) {
        financeDao.deleteSavingsGoal(id)
    }

    suspend fun saveJournal(dateEpochDay: Long, l1: String, l2: String, l3: String) {
        val current = journalDao.getJournalForDate(dateEpochDay).firstOrNull()
        val completed = l1.isNotBlank() && l2.isNotBlank() && l3.isNotBlank()

        journalDao.upsertJournal(
            DailyJournalEntity(
                dateEpochDay = dateEpochDay,
                line1GoodThings = l1,
                line2LessonLearned = l2,
                line3TomorrowGoal = l3,
                readingSecondsCompleted = current?.readingSecondsCompleted ?: 0,
                updatedAt = System.currentTimeMillis()
            )
        )

        val existingLog = dailyLogDao.getLogForHabit(dateEpochDay, "journal_3lines").firstOrNull()
        dailyLogDao.upsertLog(
            DailyLogEntity(
                id = existingLog?.id ?: 0L,
                dateEpochDay = dateEpochDay,
                habitId = "journal_3lines",
                isCompleted = completed,
                progressValue = if (completed) 3 else (listOf(l1, l2, l3).count { it.isNotBlank() }),
                updatedAt = System.currentTimeMillis()
            )
        )
        evaluateStreak(dateEpochDay)
    }

    suspend fun saveReadingSeconds(dateEpochDay: Long, seconds: Int) {
        val current = journalDao.getJournalForDate(dateEpochDay).firstOrNull()
        journalDao.upsertJournal(
            DailyJournalEntity(
                dateEpochDay = dateEpochDay,
                line1GoodThings = current?.line1GoodThings ?: "",
                line2LessonLearned = current?.line2LessonLearned ?: "",
                line3TomorrowGoal = current?.line3TomorrowGoal ?: "",
                readingSecondsCompleted = seconds,
                updatedAt = System.currentTimeMillis()
            )
        )

        val minutes = seconds / 60
        val isCompleted = minutes >= 20
        val existingLog = dailyLogDao.getLogForHabit(dateEpochDay, "reading_20min").firstOrNull()
        dailyLogDao.upsertLog(
            DailyLogEntity(
                id = existingLog?.id ?: 0L,
                dateEpochDay = dateEpochDay,
                habitId = "reading_20min",
                isCompleted = isCompleted,
                progressValue = minutes,
                updatedAt = System.currentTimeMillis()
            )
        )
        evaluateStreak(dateEpochDay)
    }

    suspend fun resetOfficialStartDate(newStartEpochDay: Long) {
        val existingUser = userDao.getUser().firstOrNull()
        val updatedUser = (existingUser ?: UserEntity(
            id = 1,
            name = "Kẻ Kỷ Luật Thép",
            startDateEpochDay = newStartEpochDay,
            targetDateEpochDay = newStartEpochDay + 90L
        )).copy(
            startDateEpochDay = newStartEpochDay,
            targetDateEpochDay = newStartEpochDay + 90L
        )
        userDao.insertOrUpdate(updatedUser)
    }

    private suspend fun evaluateStreak(dateEpochDay: Long) {
        val user = userDao.getUser().firstOrNull() ?: return
        val dailyHabits = habitDao.getAllHabits().firstOrNull()?.filter { !it.isWeekly } ?: return
        val todayLogs = dailyLogDao.getLogsForDate(dateEpochDay).firstOrNull() ?: return

        val completedCount = todayLogs.count { it.isCompleted }
        val allDone = completedCount >= dailyHabits.size && dailyHabits.isNotEmpty()

        if (allDone && user.lastCompletedDateEpochDay != dateEpochDay) {
            val newStreak = user.currentStreak + 1
            val longest = maxOf(newStreak, user.longestStreak)
            userDao.updateStreak(newStreak, longest, dateEpochDay)
        }
    }
}
