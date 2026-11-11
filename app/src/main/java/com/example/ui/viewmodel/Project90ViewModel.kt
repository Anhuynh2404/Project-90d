package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.Project90Application
import com.example.data.local.entity.DailyJournalEntity
import com.example.data.local.entity.DailyLogEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.FinanceTransactionEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TimeAllocationEntity
import com.example.data.local.entity.UserEntity
import com.example.sensor.StepCounterManager
import com.example.service.HabitNotificationHelper
import com.example.service.PersistentHabitNotificationService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class AppTab(val title: String, val categoryTag: String) {
    DASHBOARD("Tổng Quan", "ALL"),
    MOVEMENT("Vận Động", "MOVEMENT"),
    NUTRITION("Dinh Dưỡng", "NUTRITION"),
    DIGITAL("Detox & 24h", "DIGITAL_WELLBEING"),
    FINANCE("Tài Chính", "FINANCE"),
    GROWTH("Phát Triển", "SELF_GROWTH"),
    SETTINGS("Cài Đặt", "SETTINGS")
}

data class DashboardSummary(
    val dayNumber: Int = 1,
    val totalDays: Int = 90,
    val daysRemainingToFeb: Long = 85,
    val progressPercent: Float = 0.1f,
    val completedTodayCount: Int = 0,
    val totalDailyHabitsCount: Int = 12,
    val streakWarning: Boolean = false,
    val currentStreak: Int = 0
)

data class PersonalGrowthSummary(
    val readingStreakDays: Int = 0,
    val readingStreakTarget: Int = 7,
    val totalReadingDaysCompleted: Int = 0,
    val totalJournalsWritten: Int = 0,
    val journalStreakDays: Int = 0,
    val journalGoalDays: Int = 90,
    val last7DaysReading: List<Pair<String, Boolean>> = emptyList()
)

class Project90ViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as Project90Application).repository
    val stepCounterManager = StepCounterManager(application)

    val todayEpochDay = LocalDate.now().toEpochDay()
    private val _selectedDate = MutableStateFlow(todayEpochDay)
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    val user: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val habits: StateFlow<List<HabitEntity>> = repository.allHabitsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLogs: StateFlow<List<DailyLogEntity>> = repository.getLogsForDate(todayEpochDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentStepLogs: StateFlow<List<DailyLogEntity>> = repository.getRecentStepLogs(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayJournal: StateFlow<DailyJournalEntity?> = repository.getJournalForDate(todayEpochDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allJournals: StateFlow<List<DailyJournalEntity>> = repository.allJournalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedJournalCount: StateFlow<Int> = repository.completedJournalCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedReadingLogs: StateFlow<List<DailyLogEntity>> = repository.completedReadingLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<FinanceTransactionEntity>> = repository.allFinanceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = repository.allDebtsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.allSavingsGoalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalGoalsSaved: StateFlow<Long?> = repository.totalGoalsSavedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalIncome: StateFlow<Long?> = repository.totalIncomeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalExpense: StateFlow<Long?> = repository.totalExpenseFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalSavings: StateFlow<Long?> = repository.totalSavingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val todayTimeAllocations: StateFlow<List<TimeAllocationEntity>> =
        repository.getTimeAllocationsForDate(todayEpochDay)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 20-Minute Reading Timer
    private val _readingTimerSeconds = MutableStateFlow(20 * 60)
    val readingTimerSeconds: StateFlow<Int> = _readingTimerSeconds.asStateFlow()

    private val _isReadingRunning = MutableStateFlow(false)
    val isReadingRunning: StateFlow<Boolean> = _isReadingRunning.asStateFlow()

    private var timerJob: Job? = null

    // Pomodoro Focus Lock Timer (Detox Upgrade)
    private val _pomodoroDurationMinutes = MutableStateFlow(25)
    val pomodoroDurationMinutes: StateFlow<Int> = _pomodoroDurationMinutes.asStateFlow()

    private val _pomodoroRemainingSeconds = MutableStateFlow(25 * 60)
    val pomodoroRemainingSeconds: StateFlow<Int> = _pomodoroRemainingSeconds.asStateFlow()

    private val _isPomodoroRunning = MutableStateFlow(false)
    val isPomodoroRunning: StateFlow<Boolean> = _isPomodoroRunning.asStateFlow()

    private val _pomodoroCompletedSessions = MutableStateFlow(0)
    val pomodoroCompletedSessions: StateFlow<Int> = _pomodoroCompletedSessions.asStateFlow()

    private val _isFocusShieldActive = MutableStateFlow(true)
    val isFocusShieldActive: StateFlow<Boolean> = _isFocusShieldActive.asStateFlow()

    private var pomodoroJob: Job? = null

    // Personal Growth Summary combining reading logs & journals
    val personalGrowthSummary: StateFlow<PersonalGrowthSummary> = combine(
        completedReadingLogs,
        allJournals,
        completedJournalCount
    ) { readLogs, journals, journalCount ->
        val readEpochSet = readLogs.filter { it.isCompleted }.map { it.dateEpochDay }.toSet()

        // Calculate consecutive reading streak ending today or yesterday
        var readingStreak = 0
        var checkDay = if (todayEpochDay in readEpochSet) todayEpochDay else todayEpochDay - 1
        while (checkDay in readEpochSet) {
            readingStreak++
            checkDay--
        }

        val nextTarget = when {
            readingStreak < 7 -> 7
            readingStreak < 14 -> 14
            readingStreak < 30 -> 30
            readingStreak < 60 -> 60
            else -> 90
        }

        // Calculate consecutive journal streak
        val validJournalSet = journals.filter {
            it.line1GoodThings.isNotBlank() &&
                it.line2LessonLearned.isNotBlank() &&
                it.line3TomorrowGoal.isNotBlank()
        }.map { it.dateEpochDay }.toSet()

        var jStreak = 0
        var jCheckDay = if (todayEpochDay in validJournalSet) todayEpochDay else todayEpochDay - 1
        while (jCheckDay in validJournalSet) {
            jStreak++
            jCheckDay--
        }

        val last7Days = (6 downTo 0).map { offset ->
            val day = todayEpochDay - offset
            val date = LocalDate.ofEpochDay(day)
            val label = if (offset == 0) "H.Nay" else "${date.dayOfMonth}/${date.monthValue}"
            label to (day in readEpochSet)
        }

        PersonalGrowthSummary(
            readingStreakDays = readingStreak,
            readingStreakTarget = nextTarget,
            totalReadingDaysCompleted = readEpochSet.size,
            totalJournalsWritten = journalCount,
            journalStreakDays = jStreak,
            journalGoalDays = 90,
            last7DaysReading = last7Days
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PersonalGrowthSummary())

    val dashboardSummary: StateFlow<DashboardSummary> = combine(user, habits, todayLogs) { u, hList, logs ->
        val dailyHabits = hList.filter { !it.isWeekly }
        val dailyHabitIds = dailyHabits.map { it.id }.toSet()
        val completedCount = logs.count { it.isCompleted && (dailyHabitIds.isEmpty() || it.habitId in dailyHabitIds) }
        val totalDaily = if (dailyHabits.isNotEmpty()) dailyHabits.size else 12

        val officialStartEpoch = LocalDate.of(2026, 11, 7).toEpochDay()
        val startDay = u?.startDateEpochDay ?: officialStartEpoch
        val targetDay = u?.targetDateEpochDay ?: (startDay + 90)

        val diffFromStart = (todayEpochDay - startDay).toInt()
        val daysIntoJourney = if (diffFromStart < 0) {
            0
        } else {
            (diffFromStart + 1).coerceIn(1, 90)
        }
        val remaining = (targetDay - todayEpochDay).coerceAtLeast(0)
        val progress = if (daysIntoJourney <= 0) 0f else (daysIntoJourney / 90f).coerceIn(0f, 1f)

        val hasStreakRisk = daysIntoJourney > 0 && completedCount < totalDaily

        DashboardSummary(
            dayNumber = daysIntoJourney,
            totalDays = 90,
            daysRemainingToFeb = remaining,
            progressPercent = progress,
            completedTodayCount = completedCount,
            totalDailyHabitsCount = totalDaily,
            streakWarning = hasStreakRisk,
            currentStreak = u?.currentStreak ?: 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    init {
        stepCounterManager.startListening()

        viewModelScope.launch {
            repository.ensureTimeAllocationsForDate(todayEpochDay)
        }

        // Sync step sensor to Room
        viewModelScope.launch {
            stepCounterManager.stepCount.collect { steps ->
                if (steps > 0) {
                    repository.updateSteps(todayEpochDay, steps)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepCounterManager.stopListening()
        timerJob?.cancel()
        pomodoroJob?.cancel()
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun toggleHabit(habitId: String, currentCompleted: Boolean, currentProgress: Int, target: Int) {
        viewModelScope.launch {
            val newCompleted = !currentCompleted
            val newProgress = if (newCompleted) target else 0
            repository.setHabitProgress(todayEpochDay, habitId, newCompleted, newProgress)
            HabitNotificationHelper.postOrUpdateNotification(getApplication())
        }
    }

    fun updateHabitProgress(habitId: String, newProgress: Int, target: Int) {
        viewModelScope.launch {
            val isDone = newProgress >= target
            repository.setHabitProgress(todayEpochDay, habitId, isDone, newProgress)
            HabitNotificationHelper.postOrUpdateNotification(getApplication())
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(todayEpochDay, amountMl)
            HabitNotificationHelper.postOrUpdateNotification(getApplication())
        }
    }

    fun addManualSteps(steps: Int) {
        val existingLogSteps = todayLogs.value.firstOrNull { it.habitId == "pedometer_10k" }?.progressValue ?: 0
        val newTotal = existingLogSteps + steps
        stepCounterManager.setStepCount(newTotal)
        viewModelScope.launch {
            repository.updateSteps(todayEpochDay, newTotal)
            HabitNotificationHelper.postOrUpdateNotification(getApplication())
        }
    }

    // Finance: Transactions
    fun addTransaction(
        amount: Long,
        type: String,
        category: String,
        bankSource: String,
        description: String,
        isSalary: Boolean
    ) {
        viewModelScope.launch {
            repository.addFinancialTransaction(
                amount = amount,
                type = type,
                category = category,
                bankSource = bankSource,
                description = description,
                isSalary = isSalary
            )
            HabitNotificationHelper.postOrUpdateNotification(getApplication())
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    // Finance: Debts (Cho vay / Đi mượn)
    fun addDebt(
        type: String,
        personName: String,
        amount: Long,
        dueDateEpochDay: Long,
        note: String
    ) {
        viewModelScope.launch {
            repository.addDebt(type, personName, amount, dueDateEpochDay, note)
        }
    }

    fun toggleDebtSettled(debt: DebtEntity) {
        viewModelScope.launch {
            repository.toggleDebtSettled(debt)
        }
    }

    fun deleteDebt(id: Long) {
        viewModelScope.launch {
            repository.deleteDebt(id)
        }
    }

    // Finance: Savings Goals (Quỹ tiết kiệm mục tiêu)
    fun addSavingsGoal(
        title: String,
        targetAmount: Long,
        categoryIcon: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.addSavingsGoal(title, targetAmount, categoryIcon, note)
        }
    }

    fun allocateToSavingsGoal(goalId: Long, goalTitle: String, amount: Long) {
        viewModelScope.launch {
            repository.allocateToSavingsGoal(goalId, goalTitle, amount, recordTransaction = true)
            HabitNotificationHelper.postOrUpdateNotification(getApplication())
        }
    }

    fun deleteSavingsGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(id)
        }
    }

    // 24h Time Allocation
    fun updateTimeAllocation(activityType: String, hours: Float, note: String = "") {
        viewModelScope.launch {
            repository.updateTimeAllocation(todayEpochDay, activityType, hours, note)
        }
    }

    fun refreshPersistentNotification() {
        try {
            HabitNotificationHelper.postOrUpdateNotification(getApplication())
            PersistentHabitNotificationService.startOrUpdate(getApplication())
        } catch (_: Exception) {}
    }

    // Reading Timer
    fun toggleReadingTimer() {
        if (_isReadingRunning.value) {
            timerJob?.cancel()
            _isReadingRunning.value = false
        } else {
            _isReadingRunning.value = true
            timerJob = viewModelScope.launch {
                while (_readingTimerSeconds.value > 0 && _isReadingRunning.value) {
                    delay(1000)
                    _readingTimerSeconds.value -= 1
                    val elapsedSeconds = (20 * 60) - _readingTimerSeconds.value
                    repository.saveReadingSeconds(todayEpochDay, elapsedSeconds)
                }
                _isReadingRunning.value = false
                HabitNotificationHelper.postOrUpdateNotification(getApplication())
            }
        }
    }

    fun addReadingMinutesQuick(minutesToAdd: Int) {
        val deductSec = minutesToAdd * 60
        _readingTimerSeconds.value = (_readingTimerSeconds.value - deductSec).coerceAtLeast(0)
        val elapsedSeconds = (20 * 60) - _readingTimerSeconds.value
        viewModelScope.launch {
            repository.saveReadingSeconds(todayEpochDay, elapsedSeconds)
            HabitNotificationHelper.postOrUpdateNotification(getApplication())
        }
    }

    fun resetReadingTimer() {
        timerJob?.cancel()
        _isReadingRunning.value = false
        _readingTimerSeconds.value = 20 * 60
    }

    // Pomodoro Deep Focus Timer (Detox Upgrade)
    fun setPomodoroDuration(minutes: Int) {
        pomodoroJob?.cancel()
        _isPomodoroRunning.value = false
        _pomodoroDurationMinutes.value = minutes
        _pomodoroRemainingSeconds.value = minutes * 60
    }

    fun togglePomodoroTimer() {
        if (_isPomodoroRunning.value) {
            pomodoroJob?.cancel()
            _isPomodoroRunning.value = false
        } else {
            _isPomodoroRunning.value = true
            pomodoroJob = viewModelScope.launch {
                while (_pomodoroRemainingSeconds.value > 0 && _isPomodoroRunning.value) {
                    delay(1000)
                    _pomodoroRemainingSeconds.value -= 1
                }
                if (_pomodoroRemainingSeconds.value == 0) {
                    _pomodoroCompletedSessions.value += 1
                    _pomodoroRemainingSeconds.value = _pomodoroDurationMinutes.value * 60
                }
                _isPomodoroRunning.value = false
            }
        }
    }

    fun resetPomodoroTimer() {
        pomodoroJob?.cancel()
        _isPomodoroRunning.value = false
        _pomodoroRemainingSeconds.value = _pomodoroDurationMinutes.value * 60
    }

    fun toggleFocusShield() {
        _isFocusShieldActive.value = !_isFocusShieldActive.value
    }

    fun saveJournal(l1: String, l2: String, l3: String) {
        viewModelScope.launch {
            repository.saveJournal(todayEpochDay, l1, l2, l3)
            HabitNotificationHelper.postOrUpdateNotification(getApplication())
        }
    }
}
