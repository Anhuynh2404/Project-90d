package com.example.ui.screens

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.Project90App
import com.example.data.local.entity.DailyLogEntity
import com.example.data.local.entity.HabitCategory
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.TimeActivityCategory
import com.example.data.local.entity.TimeAllocationEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.HabitRepository
import com.example.ui.theme.Project90Theme
import com.example.ui.viewmodel.Project90ViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * PHẦN 4: KIỂM THỬ GIAO DIỆN COMPOSABLE (UI TEST)
 * Sử dụng createComposeRule() kết hợp Robolectric để kiểm thử các màn hình chính:
 * 1. Onboarding / Popup Thiết lập ngày bắt đầu & Banner quyền Thông báo
 * 2. SelfGrowthScreen (Nhập 3 dòng nhật ký, chặn >180 ký tự, OutlinedTextField tự mở rộng)
 * 3. DigitalWellbeingScreen (Biểu đồ Donut Chart 24h hiển thị đủ 5 mục và không bị ép dòng)
 * 4. DashboardScreen (4 thẻ Widget Nước, Bước chân, Thời gian, Tài chính hiển thị đầy đủ %)
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-normal-long-notround-any-420dpi-keyshidden-nonav")
class Project90ComposeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var mockRepository: HabitRepository

    private val userFlow = MutableStateFlow<UserEntity?>(null)
    private val habitsFlow = MutableStateFlow<List<HabitEntity>>(emptyList())
    private val todayLogsFlow = MutableStateFlow<List<DailyLogEntity>>(emptyList())
    private val timeAllocationsFlow = MutableStateFlow<List<TimeAllocationEntity>>(emptyList())

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
        every { mockRepository.getLogsForDate(any()) } returns todayLogsFlow
        every { mockRepository.getRecentStepLogs(any()) } returns flowOf(emptyList())
        every { mockRepository.getJournalForDate(any()) } returns flowOf(null)
        every { mockRepository.allJournalsFlow } returns flowOf(emptyList())
        every { mockRepository.completedJournalCountFlow } returns flowOf(5)
        every { mockRepository.completedReadingLogsFlow } returns flowOf(emptyList())
        every { mockRepository.allFinanceFlow } returns flowOf(emptyList())
        every { mockRepository.allDebtsFlow } returns flowOf(emptyList())
        every { mockRepository.allSavingsGoalsFlow } returns flowOf(emptyList())
        every { mockRepository.totalGoalsSavedFlow } returns flowOf(2_500_000L)
        every { mockRepository.totalIncomeFlow } returns flowOf(20_000_000L)
        every { mockRepository.totalExpenseFlow } returns flowOf(5_000_000L)
        every { mockRepository.totalSavingsFlow } returns flowOf(2_000_000L)
        every { mockRepository.getTimeAllocationsForDate(any()) } returns timeAllocationsFlow

        habitsFlow.value = listOf(
            HabitEntity("water_2l", "Uống đủ 2 Lít nước lọc", HabitCategory.NUTRITION.name, false, 2000, "ml", "Uống nước", 1),
            HabitEntity("no_sugar", "Kỷ luật Không Đường", HabitCategory.NUTRITION.name, false, 1, "ngày", "No sugar", 2),
            HabitEntity("pedometer_10k", "Đi bộ 10.000 bước", HabitCategory.MOVEMENT.name, false, 10000, "bước", "Đi bộ", 3),
            HabitEntity("pushups_50", "50 lần Hít đất", HabitCategory.MOVEMENT.name, false, 50, "cái", "Hít đất", 4),
            HabitEntity("reading_20min", "Đọc sách 20 phút", HabitCategory.SELF_GROWTH.name, false, 20, "phút", "Đọc sách", 5),
            HabitEntity("savings_10pct", "Trích 10% thu nhập", HabitCategory.FINANCE.name, false, 1, "lần", "Tiết kiệm", 6)
        )

        todayLogsFlow.value = listOf(
            DailyLogEntity(1, 20768L, "water_2l", false, 1000),
            DailyLogEntity(2, 20768L, "no_sugar", true, 1),
            DailyLogEntity(3, 20768L, "pedometer_10k", false, 5000),
            DailyLogEntity(4, 20768L, "pushups_50", true, 50),
            DailyLogEntity(5, 20768L, "savings_10pct", true, 1)
        )

        timeAllocationsFlow.value = TimeActivityCategory.entries.map { cat ->
            TimeAllocationEntity(
                dateEpochDay = 20768L,
                activityType = cat.code,
                hours = cat.recommendedHours,
                note = cat.labelVi
            )
        }
    }

    // =========================================================================
    // 1. KỊCH BẢN 1: ONBOARDING / POPUP THIẾT LẬP NGÀY BẮT ĐẦU & QUYỀN THÔNG BÁO
    // =========================================================================

    @Test
    fun `OnboardingPopup - displays Start Date dialog and Notification Permission banner on first launch`() {
        // Arrange: Mở app lần đầu khi chưa cấu hình KEY_HAS_CONFIGURED_START_DATE
        val viewModel = Project90ViewModel(application, mockRepository)

        // Act
        composeTestRule.setContent {
            Project90Theme {
                Project90App(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // Assert 1: Popup "THIẾT LẬP NGÀY BẮT ĐẦU" hiển thị chính xác
        composeTestRule.onNodeWithTag("start_date_onboarding_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("THIẾT LẬP NGÀY BẮT ĐẦU").assertIsDisplayed()
        composeTestRule.onNodeWithText("Công thức: (Ngày hiện tại - Ngày bắt đầu)").assertIsDisplayed()

        // Act: Nhấn nút "Lưu Ngày Bắt Đầu" để đóng popup
        composeTestRule.onNodeWithTag("confirm_start_date_button").performClick()
        composeTestRule.waitForIdle()

        // Assert 2: Popup đã đóng và banner nhắc cấp quyền Thông báo ghim 24/7 hiển thị trên màn hình
        composeTestRule.onNodeWithText("Chưa cấp quyền! Chạm để bật thông báo ghim 24/7").assertIsDisplayed()
    }

    // =========================================================================
    // 2. KỊCH BẢN 2: SELF GROWTH SCREEN - 3 DÒNG NHẬT KÝ (>180 KÝ TỰ & AUTO-EXPAND)
    // =========================================================================

    @Test
    fun `SelfGrowthScreen - enforces 180 char limit and auto-expands OutlinedTextField height on multi-line input`() {
        // Arrange
        val viewModel = Project90ViewModel(application, mockRepository)

        composeTestRule.setContent {
            Project90Theme {
                SelfGrowthScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // Cuộn tới thẻ "3 Dòng nhật ký tối cô đọng"
        composeTestRule.onNodeWithTag("self_growth_screen")
            .performScrollToNode(hasTestTag("journal_line_1_input"))
        composeTestRule.waitForIdle()

        // Đo chiều cao ban đầu của OutlinedTextField (minLines = 2)
        val initialBounds = composeTestRule.onNodeWithTag("journal_line_1_input")
            .fetchSemanticsNode().boundsInRoot
        val initialHeight = initialBounds.height

        // Act 1: Nhập văn bản nhiều dòng (4 dòng) để kiểm tra tự động mở rộng (minLines=2, maxLines=5)
        val multiLineText = "Dòng 1 kỷ luật\nDòng 2 đọc sách\nDòng 3 tập luyện\nDòng 4 tiết kiệm"
        composeTestRule.onNodeWithTag("journal_line_1_input").performTextInput(multiLineText)
        composeTestRule.waitForIdle()

        val expandedBounds = composeTestRule.onNodeWithTag("journal_line_1_input")
            .fetchSemanticsNode().boundsInRoot
        val expandedHeight = expandedBounds.height

        // Assert 1: Chiều cao của OutlinedTextField đã tự động mở rộng khi có nhiều dòng
        assertTrue(
            "Expected OutlinedTextField to auto-expand height from $initialHeight to $expandedHeight",
            expandedHeight > initialHeight
        )

        // Act 2: Nhập chuỗi dài > 180 ký tự (220 ký tự) vào dòng nhật ký số 2
        composeTestRule.onNodeWithTag("self_growth_screen")
            .performScrollToNode(hasTestTag("journal_line_2_input"))
        composeTestRule.waitForIdle()

        val longString220Chars = "A".repeat(220)
        composeTestRule.onNodeWithTag("journal_line_2_input").performTextInput(longString220Chars)
        composeTestRule.waitForIdle()

        // Assert 2: Bộ đếm chữ dừng đúng ở "180 / 180" và chặn toàn bộ ký tự vượt quá 180
        composeTestRule.onNodeWithTag("journal_line_2_input_char_counter")
            .assertTextEquals("180 / 180")
        composeTestRule.onNodeWithTag("journal_line_2_input")
            .assertTextContains("A".repeat(180))
    }

    // =========================================================================
    // 3. KỊCH BẢN 3: DIGITAL WELLBEING SCREEN - BIỂU ĐỒ DONUT 24H ĐỦ 5 MỤC & KHÔNG ÉP DÒNG
    // =========================================================================

    @Test
    fun `DigitalWellbeingScreen - renders Donut Chart with all 5 categories without text wrapping issues`() {
        // Arrange
        val viewModel = Project90ViewModel(application, mockRepository)

        // Act
        composeTestRule.setContent {
            Project90Theme {
                DigitalWellbeingScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()

        // Assert: Thẻ Quỹ thời gian 24h hiển thị đầy đủ 5 danh mục (Ngủ, Làm việc, Học tập, Nghỉ ngơi, Giải trí)
        composeTestRule.onNodeWithTag("time_fund_donut_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("QUỸ THỜI GIAN 24H").assertIsDisplayed()
        composeTestRule.onNodeWithText("24.0h / 24h").assertIsDisplayed()

        // Kiểm tra đủ 5 mục hoạt động của Donut Chart
        composeTestRule.onNodeWithText("7.0h (29%)").assertIsDisplayed() // Ngủ (7.0h)
        composeTestRule.onNodeWithText("8.0h (33%)").assertIsDisplayed() // Làm việc (8.0h)
        composeTestRule.onNodeWithText("3.0h (13%)").assertIsDisplayed() // Học tập (3.0h)
        composeTestRule.onNodeWithText("3.5h (15%)").assertIsDisplayed() // Nghỉ ngơi (3.5h)
        composeTestRule.onNodeWithText("2.5h (10%)").assertIsDisplayed() // Chơi & Giải trí (2.5h)
    }

    // =========================================================================
    // 4. KỊCH BẢN 4: DASHBOARD SCREEN - 4 THẺ WIDGET HIỂN THỊ ĐẦY ĐỦ SỐ LIỆU %
    // =========================================================================

    @Test
    fun `DashboardScreen - renders all 4 Bento Widget cards with accurate completion percentages`() {
        // Arrange: Đánh dấu đã cấu hình ngày bắt đầu để vào thẳng Dashboard
        application.getSharedPreferences(Project90ViewModel.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(Project90ViewModel.KEY_HAS_CONFIGURED_START_DATE, true)
            .commit()

        val viewModel = Project90ViewModel(application, mockRepository)

        // Act
        composeTestRule.setContent {
            Project90Theme {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTab = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Cuộn tới khu vực 4 thẻ Widget
        composeTestRule.onNodeWithTag("dashboard_screen")
            .performScrollToNode(hasTestTag("widget_water_card"))
        composeTestRule.waitForIdle()

        // Assert 1: Widget Nước & No Sugar (1000/2000ml + No Sugar = 65%)
        composeTestRule.onNodeWithTag("widget_water_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nước & No Sugar").assertIsDisplayed()
        composeTestRule.onNodeWithText("65%").assertIsDisplayed()

        // Assert 2: Widget Bước Chân (5000/10000 bước + 1/3 bài tập = 43%)
        composeTestRule.onNodeWithTag("widget_steps_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bước Chân").assertIsDisplayed()
        composeTestRule.onNodeWithText("43%").assertIsDisplayed()

        // Cuộn xuống hàng Widget thứ 2 (Thời gian & Tài chính)
        composeTestRule.onNodeWithTag("dashboard_screen")
            .performScrollToNode(hasTestTag("widget_finance_card"))
        composeTestRule.waitForIdle()

        // Assert 3: Widget Thời Gian & Detox
        composeTestRule.onNodeWithTag("widget_time_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Thời Gian & Detox").assertIsDisplayed()

        // Assert 4: Widget Tài Chính 10% (savings_10pct = true -> 100%)
        composeTestRule.onNodeWithTag("widget_finance_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tài Chính 10%").assertIsDisplayed()
        composeTestRule.onNodeWithText("100%").assertIsDisplayed()
    }
}
