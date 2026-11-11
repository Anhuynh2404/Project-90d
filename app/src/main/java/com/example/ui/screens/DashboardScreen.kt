package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.HabitCategory
import com.example.ui.components.Circular90DaysProgress
import com.example.ui.components.HabitChecklistItem
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.EcoBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.MarineAqua
import com.example.ui.theme.MintLight
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.Project90ViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    viewModel: Project90ViewModel,
    onNavigateToTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val readingTimerSeconds by viewModel.readingTimerSeconds.collectAsStateWithLifecycle()
    val todayJournal by viewModel.todayJournal.collectAsStateWithLifecycle()
    val totalSavings by viewModel.totalSavings.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()

    var selectedCategoryFilter by remember { mutableStateOf<String?>("ALL") }

    val logsMap = remember(todayLogs) {
        todayLogs.associateBy { it.habitId }
    }

    val filteredHabits = remember(habits, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL" || selectedCategoryFilter == null) {
            habits
        } else {
            habits.filter { it.category == selectedCategoryFilter }
        }
    }

    // Calculate daily completion fraction
    val dailyCompletionFraction = remember(summary.completedTodayCount, summary.totalDailyHabitsCount) {
        if (summary.totalDailyHabitsCount > 0) {
            (summary.completedTodayCount.toFloat() / summary.totalDailyHabitsCount.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    // Module 1: Water & Nutrition metrics
    val waterMl = logsMap["water_2l"]?.progressValue ?: 0
    val noSugarDone = logsMap["no_sugar"]?.isCompleted == true
    val waterFraction = (waterMl / 2000f).coerceIn(0f, 1f)
    val nutritionPercent = remember(waterFraction, noSugarDone) {
        ((waterFraction * 0.7f + (if (noSugarDone) 0.3f else 0f)) * 100f).roundToInt().coerceIn(0, 100)
    }

    // Module 2: Steps & Movement metrics
    val stepCount = logsMap["pedometer_10k"]?.progressValue ?: 0
    val pushupsDone = logsMap["pushups_50"]?.isCompleted == true
    val squatsDone = logsMap["squats_100"]?.isCompleted == true
    val coldShowerDone = logsMap["cold_shower"]?.isCompleted == true
    val stepsFraction = (stepCount / 10000f).coerceIn(0f, 1f)
    val bodyWorkoutDoneCount = listOf(pushupsDone, squatsDone, coldShowerDone).count { it }
    val movementPercent = remember(stepsFraction, bodyWorkoutDoneCount) {
        ((stepsFraction * 0.55f + (bodyWorkoutDoneCount / 3f) * 0.45f) * 100f).roundToInt().coerceIn(0, 100)
    }

    // Module 3: Time, Focus & Detox metrics
    val read20mDone = logsMap["reading_20min"]?.isCompleted == true
    val journalDone = logsMap["journal_3lines"]?.isCompleted == true || todayJournal != null
    val noCaffeineDone = logsMap["caffeine_curfew"]?.isCompleted == true
    val screenOffDone = logsMap["screen_limit_22"]?.isCompleted == true
    val sleepDone = logsMap["bedtime_2230"]?.isCompleted == true
    val elapsedReadMinutes = remember(readingTimerSeconds, read20mDone) {
        if (read20mDone) 20 else ((20 * 60 - readingTimerSeconds) / 60).coerceIn(0, 20)
    }
    val timeCompletedItems = listOf(read20mDone, journalDone, noCaffeineDone, screenOffDone, sleepDone).count { it }
    val timeFocusPercent = remember(timeCompletedItems, elapsedReadMinutes) {
        val baseFraction = timeCompletedItems / 5f
        val readBonus = if (!read20mDone) (elapsedReadMinutes / 20f) * 0.2f else 0f
        ((baseFraction + readBonus).coerceIn(0f, 1f) * 100f).roundToInt()
    }

    // Module 4: Finance & 10% Savings metrics
    val safeSavings = totalSavings ?: 0L
    val safeIncome = totalIncome ?: 0L
    val save10PercentDone = logsMap["savings_10pct"]?.isCompleted == true
    val savingsTargetRatio = remember(safeSavings, safeIncome) {
        if (safeIncome > 0L) ((safeSavings.toDouble() / (safeIncome.toDouble() * 0.10)).toFloat()).coerceIn(0f, 1f) else 0f
    }
    val financePercent = remember(save10PercentDone, safeSavings, savingsTargetRatio) {
        when {
            save10PercentDone -> 100
            safeSavings > 0L -> (50 + (savingsTargetRatio * 40f).roundToInt()).coerceIn(50, 95)
            else -> 0
        }
    }

    val currencyFormatter = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Top Command Header (Eco-Botanical v2.0)
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(LeafGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PROJECT 90 • BẢNG ĐIỀU KHIỂN 2.0",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.1.sp
                                ),
                                color = LeafGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "TRUNG TÂM KỶ LUẬT",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            ),
                            color = ForestGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Settings Button with soft shadow
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(6.dp, CircleShape, ambientColor = ForestGreen.copy(alpha = 0.15f))
                                .clip(CircleShape)
                                .background(CardWhite)
                                .border(1.dp, CardBorder, CircleShape)
                                .clickable { onNavigateToTab(AppTab.SETTINGS) }
                                .testTag("dashboard_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Cài đặt hệ thống 24/7",
                                tint = ForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Version 2.0 Eco-Botanical Pill
                        Box(
                            modifier = Modifier
                                .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = ForestGreen.copy(alpha = 0.15f))
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(ForestGreen, LeafGreen)
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${(dailyCompletionFraction * 100).roundToInt()}% HÔM NAY",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Glassmorphic Eco Inspiration Strip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = ForestGreen.copy(alpha = 0.08f)
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    CardWhite.copy(alpha = 0.92f),
                                    MintLight.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(LeafGreen.copy(alpha = 0.16f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = "Eco Botanical",
                            tint = ForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (summary.isChallengeCompleted) {
                            "Chúc mừng! Bạn đã chinh phục hoàn toàn thử thách 90 ngày kỷ luật thép!"
                        } else {
                            "Mỗi hành động nhỏ hôm nay tạo nên sự bứt phá phi thường ngày mai."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ForestGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Hero Glassmorphic 90-Day Countdown Centerpiece
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = ForestGreen.copy(alpha = 0.18f),
                        spotColor = ForestGreen.copy(alpha = 0.22f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                CardWhite.copy(alpha = 0.94f),
                                MintSurface.copy(alpha = 0.88f),
                                MintLight.copy(alpha = 0.65f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White,
                                CardBorder,
                                LeafGreen.copy(alpha = 0.35f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Start Date Config Pill inside Hero Card
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardWhite.copy(alpha = 0.85f))
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                            .clickable { viewModel.openStartDateDialog() }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                            .testTag("dashboard_start_date_button")
                    ) {
                        Text(
                            text = "Ngày bắt đầu: ${summary.startDateFormatted}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (summary.isChallengeCompleted) "HOÀN THÀNH THỬ THÁCH • Đổi ngày" else "Đổi ngày bắt đầu",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (summary.isChallengeCompleted) SuccessGreen else ForestGreen,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Circular90DaysProgress(
                        dayNumber = summary.dayNumber,
                        totalDays = summary.totalDays,
                        daysRemainingToFeb = summary.daysRemainingToFeb,
                        currentStreak = summary.currentStreak,
                        dailyCompletionFraction = dailyCompletionFraction,
                        isChallengeCompleted = summary.isChallengeCompleted,
                        isBeforeStart = summary.isBeforeStart,
                        startDateLabel = summary.startDateFormatted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Glassmorphic Bottom Telemetry Bar inside Hero Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(CardWhite.copy(alpha = 0.82f))
                            .border(1.dp, CardBorder.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeroMetricItem(
                            label = "CHECKLIST NGÀY",
                            value = "${summary.completedTodayCount}/${summary.totalDailyHabitsCount}",
                            accentColor = ForestGreen
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(CardBorder)
                        )
                        HeroMetricItem(
                            label = "TRẠNG THÁI",
                            value = if (summary.isChallengeCompleted) "HOÀN THÀNH" else "${(dailyCompletionFraction * 100).roundToInt()}%",
                            accentColor = if (summary.isChallengeCompleted) SuccessGreen else LeafGreen
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(CardBorder)
                        )
                        HeroMetricItem(
                            label = "TIẾN ĐỘ 90 NGÀY",
                            value = if (summary.isChallengeCompleted) "100%" else "${((summary.dayNumber.toFloat() / summary.totalDays.toFloat()) * 100).roundToInt().coerceIn(0, 100)}%",
                            accentColor = MarineAqua
                        )
                    }
                }
            }
        }

        // 3. 2x2 Bento Widget Grid: Tóm tắt 4 Mô-đun Cốt Lõi (Nước, Bước chân, Thời gian, Tài chính)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WIDGET CHỈ SỐ TRONG NGÀY",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Chạm vào từng thẻ để mở mô-đun chi tiết hoặc thao tác nhanh",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 1: Nước (Water) & Bước chân (Steps)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Widget 1: NƯỚC & DINH DƯỠNG
                    DashboardModuleWidgetCard(
                        title = "Nước & No Sugar",
                        subtitle = if (noSugarDone) "Cam kết No Sugar: Đạt" else "Chưa đánh dấu No Sugar",
                        primaryValue = "${currencyFormatter.format(waterMl)} ml",
                        targetLabel = "/ 2.000 ml mục tiêu",
                        completionPercent = nutritionPercent,
                        icon = Icons.Default.LocalDrink,
                        accentColor = MarineAqua,
                        quickActionText = "+250 ml",
                        onQuickAction = { viewModel.addWater(250) },
                        onCardClick = { onNavigateToTab(AppTab.NUTRITION) },
                        testTag = "widget_water_card",
                        modifier = Modifier.weight(1f)
                    )

                    // Widget 2: BƯỚC CHÂN & VẬN ĐỘNG
                    DashboardModuleWidgetCard(
                        title = "Bước Chân",
                        subtitle = "Thể lực: $bodyWorkoutDoneCount/3 bài tập",
                        primaryValue = currencyFormatter.format(stepCount),
                        targetLabel = "/ 10.000 bước ngày",
                        completionPercent = movementPercent,
                        icon = Icons.Default.DirectionsRun,
                        accentColor = ForestGreen,
                        quickActionText = "+500 bước",
                        onQuickAction = { viewModel.addManualSteps(500) },
                        onCardClick = { onNavigateToTab(AppTab.MOVEMENT) },
                        testTag = "widget_steps_card",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 2: Thời gian (Time/Focus/Detox) & Tài chính (Finance)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Widget 3: THỜI GIAN & TẬP TRUNG
                    DashboardModuleWidgetCard(
                        title = "Thời Gian & Detox",
                        subtitle = "Detox & Ngủ: $timeCompletedItems/5 mục đạt",
                        primaryValue = "$elapsedReadMinutes phút",
                        targetLabel = "/ 20 phút đọc sách",
                        completionPercent = timeFocusPercent,
                        icon = Icons.Default.Timer,
                        accentColor = WarmAmber,
                        quickActionText = "Đọc & Detox",
                        onQuickAction = { onNavigateToTab(AppTab.DIGITAL) },
                        onCardClick = { onNavigateToTab(AppTab.GROWTH) },
                        testTag = "widget_time_card",
                        modifier = Modifier.weight(1f)
                    )

                    // Widget 4: TÀI CHÍNH KỶ LUẬT
                    DashboardModuleWidgetCard(
                        title = "Tài Chính 10%",
                        subtitle = if (save10PercentDone) "Đã trích 10% hôm nay" else "Chưa trích 10% hôm nay",
                        primaryValue = "${currencyFormatter.format(safeSavings)} đ",
                        targetLabel = "Quỹ tiết kiệm tích lũy",
                        completionPercent = financePercent,
                        icon = Icons.Default.Savings,
                        accentColor = LeafGreen,
                        quickActionText = "Sổ thu chi",
                        onQuickAction = { onNavigateToTab(AppTab.FINANCE) },
                        onCardClick = { onNavigateToTab(AppTab.FINANCE) },
                        testTag = "widget_finance_card",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. Streak Status Banner (Soft Glassmorphic Alert)
        item {
            if (summary.streakWarning) {
                val remainingHabits = (summary.totalDailyHabitsCount - summary.completedTodayCount).coerceAtLeast(0)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = DangerCrimson.copy(alpha = 0.15f)
                        )
                        .border(1.dp, DangerCrimson.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                        .testTag("streak_warning_card")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        DangerCrimson.copy(alpha = 0.08f),
                                        CardWhite
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(DangerCrimson)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Cảnh báo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BẢO VỆ CHUỖI KỶ LUẬT!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = DangerCrimson
                            )
                            Text(
                                text = "Còn $remainingHabits mục chưa hoàn thành hôm nay. Hoàn thành đủ 100% trước 23:59 để giữ vững chuỗi ${summary.currentStreak} ngày!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardWhite),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = ForestGreen.copy(alpha = 0.15f)
                        )
                        .border(1.dp, ForestGreen.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(MintLight, CardWhite)
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ForestGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Hoàn hảo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "100% MỤC TIÊU NGÀY ĐÃ HOÀN TẤT!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = ForestGreen
                            )
                            Text(
                                text = "Toàn bộ 12 hạng mục kỷ luật hôm nay đã đạt chuẩn. Chuỗi ngày chiến thắng được bảo toàn!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 5. Daily Checklist Header & Category Filter Chips (No 5AM Alarm Category)
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CHECKLIST KỶ LUẬT HÔM NAY",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = TextPrimary
                    )

                    Surface(
                        color = if (summary.completedTodayCount >= summary.totalDailyHabitsCount) {
                            MintLight
                        } else {
                            WarmAmber.copy(alpha = 0.14f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${summary.completedTodayCount} / ${summary.totalDailyHabitsCount} Đạt",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (summary.completedTodayCount >= summary.totalDailyHabitsCount) {
                                SuccessGreen
                            } else {
                                WarmAmber
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterOptions = listOf(
                        "ALL" to "Tất cả",
                        HabitCategory.MOVEMENT.name to "Vận Động",
                        HabitCategory.NUTRITION.name to "Dinh Dưỡng",
                        HabitCategory.DIGITAL_WELLBEING.name to "Detox & Ngủ",
                        HabitCategory.FINANCE.name to "Tài Chính",
                        HabitCategory.SELF_GROWTH.name to "Phát Triển"
                    )

                    items(filterOptions) { (key, label) ->
                        val isSelected = selectedCategoryFilter == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = key },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen,
                                selectedLabelColor = Color.White,
                                containerColor = CardWhite,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) ForestGreen else CardBorder
                            )
                        )
                    }
                }
            }
        }

        // 6. Interactive Checklist Items
        items(filteredHabits, key = { it.id }) { habit ->
            val log = logsMap[habit.id]
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 6 }
            ) {
                HabitChecklistItem(
                    habit = habit,
                    log = log,
                    onToggle = {
                        viewModel.toggleHabit(
                            habitId = habit.id,
                            currentCompleted = log?.isCompleted == true,
                            currentProgress = log?.progressValue ?: 0,
                            target = habit.targetValue
                        )
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HeroMetricItem(
    label: String,
    value: String,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            ),
            color = accentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = TextSecondary
        )
    }
}

@Composable
private fun DashboardModuleWidgetCard(
    title: String,
    subtitle: String,
    primaryValue: String,
    targetLabel: String,
    completionPercent: Int,
    icon: ImageVector,
    accentColor: Color,
    quickActionText: String,
    onQuickAction: () -> Unit,
    onCardClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (completionPercent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "widgetProgress_$title"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = accentColor.copy(alpha = 0.16f),
                spotColor = accentColor.copy(alpha = 0.20f)
            )
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White,
                        accentColor.copy(alpha = 0.28f),
                        CardBorder
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onCardClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CardWhite,
                            accentColor.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            // Top Row: Module Icon + % Completion Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // % Completion Badge in Day
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (completionPercent >= 100) SuccessGreen.copy(alpha = 0.15f)
                            else accentColor.copy(alpha = 0.12f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (completionPercent >= 100) SuccessGreen.copy(alpha = 0.35f)
                            else accentColor.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$completionPercent%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        ),
                        color = if (completionPercent >= 100) SuccessGreen else accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Module Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Main Metric Value
            Text(
                text = primaryValue,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                ),
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = targetLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Smooth Animated Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (completionPercent >= 100) SuccessGreen else accentColor,
                trackColor = accentColor.copy(alpha = 0.14f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Interactive Pill Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f))
                    .clickable { onQuickAction() }
                    .padding(vertical = 7.dp, horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = if (quickActionText.startsWith("+")) Icons.Default.Add else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = quickActionText,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = quickActionText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    ),
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
