package com.example.edgecases

import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.data.local.entity.HabitCategory
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.HabitRepository
import com.example.service.PersistentHabitNotificationService
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.theme.Project90Theme
import com.example.ui.viewmodel.Project90ViewModel
import com.example.util.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

/**
 * PHẦN 5: KIỂM THỬ TRƯỜNG HỢP BIÊN (EDGE CASES)
 * 1. Pedometer Foreground Service bị hệ thống kill (kiểm tra START_STICKY và cơ chế tự hồi sinh).
 * 2. Nhập số tiền chi tiêu/nợ âm hoặc ký tự lạ vào FinanceScreen & ViewModel.
 * 3. Giả lập ngày hệ thống (System time) bị đổi lùi về quá khứ (trước startDateEpochDay).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class Project90EdgeCaseTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var mockRepository: HabitRepository

    private val userFlow = MutableStateFlow<UserEntity?>(null)
    private val habitsFlow = MutableStateFlow<List<HabitEntity>>(emptyList())

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        application.getSharedPreferences(Project90ViewModel.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

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
                id = "pedometer_10k",
                title = "Đi bộ 10.000 bước",
                category = HabitCategory.MOVEMENT.name,
                targetValue = 10000,
                unit = "bước",
                description = "Đi bộ",
                displayOrder = 1
            )
        )
    }

    // =========================================================================
    // 1. EDGE CASE 1: PEDOMETER FOREGROUND SERVICE BỊ KILL (START_STICKY & TỰ HỒI SINH)
    // =========================================================================

    @Test
    fun `PedometerForegroundService - returns START_STICKY on restart with null intent and auto-restarts when killed`() {
        // Arrange: Khởi tạo Service thông qua Robolectric ServiceController
        val controller = Robolectric.buildService(PersistentHabitNotificationService::class.java)
        val service = controller.create().get()

        // Act 1: Giả lập hệ thống Android tự khởi động lại Service sau khi bị kill (intent = null)
        val startResult = service.onStartCommand(null, Service.START_FLAG_REDELIVERY, 1)

        // Assert 1: Phải trả về START_STICKY và trạng thái isServiceRunning = true
        assertEquals(Service.START_STICKY, startResult)
        assertTrue(PersistentHabitNotificationService.isServiceRunning)

        // Act 2: Giả lập hệ thống hoặc người dùng kill task / destroy service
        Shadows.shadowOf(application).clearStartedServices()
        controller.destroy()

        // Assert 2: onDestroy tự động phát lại Intent khởi chạy lại PersistentHabitNotificationService
        val restartedServiceIntent: Intent? = Shadows.shadowOf(application).nextStartedService
        assertNotNull("Service must schedule an immediate restart Intent upon being killed", restartedServiceIntent)
        assertEquals(
            PersistentHabitNotificationService::class.java.name,
            restartedServiceIntent?.component?.className
        )
    }

    // =========================================================================
    // 2. EDGE CASE 2: NHẬP SỐ TIỀN CHI TIÊU / NỢ ÂM HOẶC KÝ TỰ LẠ VÀO FINANCE SCREEN
    // =========================================================================

    @Test
    fun `FinanceScreen - rejects negative numbers and special characters in Expense and Debt dialogs without crashing`() {
        // Arrange
        val viewModel = Project90ViewModel(application, mockRepository)

        composeTestRule.setContent {
            Project90Theme {
                FinanceScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // Act 1: Mở Dialog "Ghi Thu/Chi", nhập số âm "-500000" rồi bấm Lưu
        composeTestRule.onNodeWithTag("finance_primary_add_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("tx_amount_input").performTextInput("-500000")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tx_save_button").performClick()
        composeTestRule.waitForIdle()

        // Assert 1: Giao dịch số âm bị chặn, Dialog vẫn mở và repository KHÔNG bị gọi
        coVerify(exactly = 0) {
            mockRepository.addFinancialTransaction(any(), any(), any(), any(), any(), any())
        }

        // Act 2: Nhập chuỗi ký tự lạ "@#$%^&*abcXYZ!" vào ô số tiền rồi bấm Lưu
        composeTestRule.onNodeWithTag("tx_amount_input").performTextClearance()
        composeTestRule.onNodeWithTag("tx_amount_input").performTextInput("@#$%^&*abcXYZ!")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tx_save_button").performClick()
        composeTestRule.waitForIdle()

        // Assert 2: Không crash (NumberFormatException), không lưu giao dịch rác
        coVerify(exactly = 0) {
            mockRepository.addFinancialTransaction(any(), any(), any(), any(), any(), any())
        }

        // Đóng dialog Thu/Chi và chuyển sang tab "Quản Lý Nợ"
        composeTestRule.onNodeWithText("Hủy").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Quản Lý Nợ").performClick()
        composeTestRule.waitForIdle()

        // Act 3: Mở Dialog "Thêm Khoản Nợ", nhập tên người vay nhưng nhập số tiền nợ âm "-2000000"
        composeTestRule.onNodeWithTag("finance_primary_add_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("debt_person_input").performTextInput("Nguyễn Văn C")
        composeTestRule.onNodeWithTag("debt_amount_input").performTextInput("-2000000")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("debt_save_button").performClick()
        composeTestRule.waitForIdle()

        // Assert 3: Khoản nợ âm bị chặn hoàn toàn
        coVerify(exactly = 0) {
            mockRepository.addDebt(any(), any(), any(), any(), any())
        }

        // Act 4: Kiểm tra bảo vệ trực tiếp tại tầng ViewModel khi truyền số âm/0
        viewModel.addTransaction(-100_000L, "EXPENSE", "Ăn uống", "Tiền mặt", "Test âm", false)
        viewModel.addDebt("BORROW", "Người lạ", -50_000L, viewModel.todayEpochDay + 7, "Test nợ âm")

        coVerify(exactly = 0) {
            mockRepository.addFinancialTransaction(any(), any(), any(), any(), any(), any())
            mockRepository.addDebt(any(), any(), any(), any(), any())
        }
    }

    // =========================================================================
    // 3. EDGE CASE 3: NGÀY HỆ THỐNG BỊ ĐỔI LÙI VỀ QUÁ KHỨ (TRƯỚC startDateEpochDay)
    // =========================================================================

    @Test
    fun `SystemClockRollback - handles system date earlier than startDateEpochDay gracefully with KHỞI ĐỘNG state`() {
        // Arrange: Giả lập startDateEpochDay nằm ở tương lai (+30 ngày so với ngày hệ thống hiện tại)
        val viewModel = Project90ViewModel(application, mockRepository)
        val futureStartEpoch = viewModel.todayEpochDay + 30L

        composeTestRule.setContent {
            Project90Theme {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTab = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Act: Cập nhật startDateEpochDay ở tương lai (giả lập đồng hồ hệ thống bị lùi về quá khứ trước ngày bắt đầu)
        viewModel.updateStartDate(futureStartEpoch)
        composeTestRule.waitForIdle()

        // Assert 1: Kiểm tra trạng thái ViewModel không bị số ngày âm hay phần trăm âm
        val summary = viewModel.dashboardSummary.value
        assertEquals(0, summary.dayNumber)
        assertTrue(summary.isBeforeStart)
        assertFalse(summary.isChallengeCompleted)
        assertEquals(0f, summary.progressPercent, 0.001f)
        assertEquals(90L, summary.daysRemainingToFeb)
        assertFalse(summary.streakWarning)

        // Assert 2: Xác minh UI hiển thị "KHỞI ĐỘNG" thay vì số ngày âm
        composeTestRule.onNodeWithText("KHỞI ĐỘNG").assertIsDisplayed()
    }
}
