package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.data.local.entity.HabitCategory
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.HabitRepository
import com.example.ui.theme.AppBackgroundPreset
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.DynamicThemeState
import com.example.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PHẦN 3: KIỂM THỬ VIEWMODEL (STATE & LOGIC TEST)
 * Sử dụng Turbine để hứng StateFlow, MainDispatcherRule để điều khiển ảo hóa thời gian (Virtual Time),
 * và tuân thủ nguyên tắc AAA (Arrange - Act - Assert).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Project90ViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var application: Application
    private lateinit var mockRepository: HabitRepository
    private val userFlow = MutableStateFlow<UserEntity?>(null)
    private val habitsFlow = MutableStateFlow<List<HabitEntity>>(emptyList())

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        // Xóa sạch SharedPreferences trước mỗi test để đảm bảo tính cô lập
        application.getSharedPreferences(Project90ViewModel.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        DynamicThemeState.activePreset = AppThemePreset.ECO_GREEN
        DynamicThemeState.activeBackground = AppBackgroundPreset.MISTY_BOTANICAL

        mockRepository = mockk(relaxed = true)
        every { mockRepository.userFlow } returns userFlow
        every { mockRepository.allHabitsFlow } returns habitsFlow
        every { mockRepository.getLogsForDate(any()) } returns flowOf(emptyList())
        every { mockRepository.getRecentStepLogs(any()) } returns flowOf(emptyList())
        every { mockRepository.getJournalForDate(any()) } returns flowOf(null)
        every { mockRepository.allJournalsFlow } returns flowOf(emptyList())
        every { mockRepository.completedJournalCountFlow } returns flowOf(0)
        every { mockRepository.completedReadingLogsFlow } returns flowOf(emptyList())
        every { mockRepository.allFinanceFlow } returns flowOf(emptyList())
        every { mockRepository.allDebtsFlow } returns flowOf(emptyList())
        every { mockRepository.allSavingsGoalsFlow } returns flowOf(emptyList())
        every { mockRepository.totalGoalsSavedFlow } returns flowOf(0L)
        every { mockRepository.totalIncomeFlow } returns flowOf(0L)
        every { mockRepository.totalExpenseFlow } returns flowOf(0L)
        every { mockRepository.totalSavingsFlow } returns flowOf(0L)
        every { mockRepository.getTimeAllocationsForDate(any()) } returns flowOf(emptyList())

        habitsFlow.value = listOf(
            HabitEntity(
                id = "water_2l",
                title = "Uống đủ 2L nước",
                category = HabitCategory.NUTRITION.name,
                targetValue = 2000,
                unit = "ml",
                description = "Uống nước",
                displayOrder = 1
            )
        )
    }

    // =========================================================================
    // 1. TEST ĐẾM NGƯỢC 90 NGÀY (NGÀY THỨ 15 & TRẠNG THÁI "HOÀN THÀNH THỬ THÁCH")
    // =========================================================================

    @Test
    fun `dashboardSummary - emits exact Day 15 when startDateEpochDay is 15 days before today`() = runTest {
        // Arrange
        val viewModel = Project90ViewModel(application, mockRepository)
        val simulatedStartDate = viewModel.todayEpochDay - 15L

        // Act & Assert with Turbine
        viewModel.dashboardSummary.test {
            // Bỏ qua hoặc nhận giá trị khởi tạo đầu tiên
            awaitItem()

            // Act: Cập nhật ngày bắt đầu lùi 15 ngày so với hôm nay
            viewModel.updateStartDate(simulatedStartDate)
            runCurrent()

            val updatedSummary = awaitItem()

            // Assert: (Ngày hiện tại - Ngày bắt đầu) = 15
            assertEquals(15, updatedSummary.dayNumber)
            assertEquals(75L, updatedSummary.daysRemainingToFeb)
            assertFalse(updatedSummary.isChallengeCompleted)
            assertFalse(updatedSummary.isBeforeStart)
            assertEquals(15f / 90f, updatedSummary.progressPercent, 0.001f)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dashboardSummary - emits Challenge Completed state when elapsed days exceed 90 days`() = runTest {
        // Arrange
        val viewModel = Project90ViewModel(application, mockRepository)
        val simulatedStartDateOver90 = viewModel.todayEpochDay - 95L // 95 ngày (> 90 ngày)

        // Act & Assert with Turbine
        viewModel.dashboardSummary.test {
            awaitItem()

            // Act: Giả lập ngày bắt đầu cách đây 95 ngày
            viewModel.updateStartDate(simulatedStartDateOver90)
            runCurrent()

            val completedSummary = awaitItem()

            // Assert: Vượt quá 90 ngày -> isChallengeCompleted = true, tiến độ đạt 100% (1.0f)
            assertEquals(95, completedSummary.dayNumber)
            assertEquals(0L, completedSummary.daysRemainingToFeb)
            assertTrue(completedSummary.isChallengeCompleted)
            assertFalse(completedSummary.isBeforeStart)
            assertEquals(1.0f, completedSummary.progressPercent, 0.001f)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // =========================================================================
    // 2. TEST ĐỒNG HỒ POMODORO (TICK TIMER, TỰ DỪNG KHI HẾT GIỜ & KHI TẮT FOCUS SHIELD)
    // =========================================================================

    @Test
    fun `pomodoroTimer - ticks down accurately every second and auto-stops when session finishes`() = runTest {
        // Arrange
        val viewModel = Project90ViewModel(application, mockRepository)
        viewModel.setPomodoroDuration(minutes = 1) // 60 giây để kiểm thử trọn chu kỳ

        viewModel.pomodoroRemainingSeconds.test {
            // Ban đầu: 60 giây
            assertEquals(60, awaitItem())

            // Act: Bắt đầu chạy đồng hồ Pomodoro
            viewModel.togglePomodoroTimer()
            assertTrue(viewModel.isPomodoroRunning.value)

            // Tiến thêm 3 giây
            advanceTimeBy(3_000L)
            runCurrent()

            // Assert: Đếm lùi qua 59 -> 58 -> 57
            assertEquals(59, awaitItem())
            assertEquals(58, awaitItem())
            assertEquals(57, awaitItem())

            // Act: Tua nhanh nốt 57 giây còn lại để kết thúc phiên Pomodoro
            advanceTimeBy(57_000L)
            runCurrent()

            cancelAndIgnoreRemainingEvents()
        }

        // Assert: Khi hết giờ, đồng hồ tự động dừng, tăng số phiên hoàn thành lên 1 và nạp lại 60s
        assertFalse(viewModel.isPomodoroRunning.value)
        assertEquals(1, viewModel.pomodoroCompletedSessions.value)
        assertEquals(60, viewModel.pomodoroRemainingSeconds.value)
    }

    @Test
    fun `pomodoroTimer - automatically stops countdown when Focus Shield is toggled off`() = runTest {
        // Arrange
        val viewModel = Project90ViewModel(application, mockRepository)
        viewModel.setPomodoroDuration(minutes = 25)
        assertTrue(viewModel.isFocusShieldActive.value)

        // Act: Khởi chạy Pomodoro và cho chạy 2 giây
        viewModel.togglePomodoroTimer()
        assertTrue(viewModel.isPomodoroRunning.value)
        advanceTimeBy(2_000L)
        runCurrent()
        assertEquals(25 * 60 - 2, viewModel.pomodoroRemainingSeconds.value)

        // Act: Tắt Focus Shield trong lúc Pomodoro đang chạy
        viewModel.isFocusShieldActive.test {
            assertEquals(true, awaitItem())

            viewModel.toggleFocusShield()

            // Assert: Focus Shield chuyển sang false và Pomodoro tự động dừng ngay lập tức
            assertEquals(false, awaitItem())
            assertFalse(viewModel.isPomodoroRunning.value)

            cancelAndIgnoreRemainingEvents()
        }

        // Assert: Thời gian không bị trừ thêm sau khi đã dừng
        val pausedSeconds = viewModel.pomodoroRemainingSeconds.value
        advanceTimeBy(5_000L)
        runCurrent()
        assertEquals(pausedSeconds, viewModel.pomodoroRemainingSeconds.value)
    }

    // =========================================================================
    // 3. TEST THEME & CÀI ĐẶT (ĐỔI THEME TỪ ECO-GREEN SANG OCEAN BLUE)
    // =========================================================================

    @Test
    fun `selectThemePreset - emits Ocean Blue preset and updates DynamicThemeState immediately`() = runTest {
        // Arrange
        val viewModel = Project90ViewModel(application, mockRepository)

        // Act & Assert with Turbine
        viewModel.themePreset.test {
            // Mặc định là ECO_GREEN
            val initialPreset = awaitItem()
            assertEquals(AppThemePreset.ECO_GREEN, initialPreset)
            assertEquals(Color(0xFF1B4324), initialPreset.primary)

            // Act: Người dùng chọn đổi sang Ocean Blue
            viewModel.selectThemePreset(AppThemePreset.OCEAN_BLUE)

            // Assert: StateFlow phát ra đúng giá trị màu Ocean Blue mới
            val updatedPreset = awaitItem()
            assertEquals(AppThemePreset.OCEAN_BLUE, updatedPreset)
            assertEquals(Color(0xFF0C4A6E), updatedPreset.primary)
            assertEquals(Color(0xFF0284C7), updatedPreset.accent)
            assertEquals(AppThemePreset.OCEAN_BLUE, DynamicThemeState.activePreset)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
