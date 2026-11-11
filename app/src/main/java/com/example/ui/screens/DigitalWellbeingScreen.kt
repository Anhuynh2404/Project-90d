package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.NoCell
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TimeActivityCategory
import com.example.data.local.entity.TimeAllocationEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.EcoBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.MarineAqua
import com.example.ui.theme.MintLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.Project90ViewModel
import kotlin.math.roundToInt

@Composable
fun DigitalWellbeingScreen(
    viewModel: Project90ViewModel,
    modifier: Modifier = Modifier
) {
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val timeAllocations by viewModel.todayTimeAllocations.collectAsStateWithLifecycle()

    val pomodoroDurationMinutes by viewModel.pomodoroDurationMinutes.collectAsStateWithLifecycle()
    val pomodoroRemainingSeconds by viewModel.pomodoroRemainingSeconds.collectAsStateWithLifecycle()
    val isPomodoroRunning by viewModel.isPomodoroRunning.collectAsStateWithLifecycle()
    val pomodoroCompletedSessions by viewModel.pomodoroCompletedSessions.collectAsStateWithLifecycle()
    val isFocusShieldActive by viewModel.isFocusShieldActive.collectAsStateWithLifecycle()

    val logsMap = remember(todayLogs) { todayLogs.associateBy { it.habitId } }

    val caffeineDone = logsMap["caffeine_curfew"]?.isCompleted == true
    val screenLimitDone = logsMap["screen_limit_22"]?.isCompleted == true
    val bedtimeDone = logsMap["bedtime_2230"]?.isCompleted == true

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EcoBackground)
            .testTag("digital_wellbeing_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "QUỸ THỜI GIAN 24H & DETOX",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    ),
                    color = ForestGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Biểu đồ Donut 24h • Khóa tập trung Pomodoro • Báo cáo Screen-Time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 1. 24-Hour Time Fund Management Card with Donut Chart
        item {
            TimeFund24hDonutCard(
                allocations = timeAllocations,
                onUpdateHours = { activityCode, newHours ->
                    viewModel.updateTimeAllocation(activityCode, newHours)
                }
            )
        }

        // 2. Pomodoro Deep Focus Lock Card
        item {
            PomodoroFocusBlockerCard(
                durationMinutes = pomodoroDurationMinutes,
                remainingSeconds = pomodoroRemainingSeconds,
                isRunning = isPomodoroRunning,
                completedSessions = pomodoroCompletedSessions,
                isFocusShieldActive = isFocusShieldActive,
                onSelectDuration = { viewModel.setPomodoroDuration(it) },
                onToggleStartPause = { viewModel.togglePomodoroTimer() },
                onReset = { viewModel.resetPomodoroTimer() },
                onToggleShield = { viewModel.toggleFocusShield() }
            )
        }

        // 3. Visual Screen-Time Report Card
        item {
            VisualScreenTimeReportCard(
                timeAllocations = timeAllocations,
                pomodoroSessions = pomodoroCompletedSessions,
                screenLimitDone = screenLimitDone
            )
        }

        // 4. Biological Curfew Checklist Header
        item {
            Text(
                text = "3 MỐC GIỚI NGHIÊM SINH HỌC BUỔI TỐI",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.6.sp
                ),
                color = ForestGreen
            )
        }

        // Curfew 1: Caffeine Curfew 14:00
        item {
            DetoxCurfewCard(
                title = "NGỪNG CAFFEINE SAU 14:00",
                subtitle = "Bảo vệ thụ thể Adenosine tự nhiên cho giấc ngủ sâu",
                description = "Thời gian bán rã của caffeine kéo dài 6-8 tiếng. Ngừng uống cafe sau 14h giúp não bộ đạt 100% pha ngủ sóng chậm.",
                icon = Icons.Default.Coffee,
                accentColor = WarmAmber,
                isDone = caffeineDone,
                onToggle = {
                    viewModel.toggleHabit("caffeine_curfew", caffeineDone, if (caffeineDone) 1 else 0, 1)
                }
            )
        }

        // Curfew 2: Screen Limit after 22:00
        item {
            DetoxCurfewCard(
                title = "KHÓA MÀN HÌNH SAU 22:00",
                subtitle = "Chặn ánh sáng xanh ức chế hormone Melatonin",
                description = "Tắt toàn bộ mạng xã hội và thông báo giải trí sau 22:00 để hệ thần kinh hạ nhiệt kích thích trước giờ ngủ.",
                icon = Icons.Default.NoCell,
                accentColor = MarineAqua,
                isDone = screenLimitDone,
                onToggle = {
                    viewModel.toggleHabit("screen_limit_22", screenLimitDone, if (screenLimitDone) 1 else 0, 1)
                }
            )
        }

        // Curfew 3: Bedtime before 22:30
        item {
            DetoxCurfewCard(
                title = "LÊN GIƯỜNG NGỦ TRƯỚC 22:30",
                subtitle = "Đảm bảo 7 tiếng phục hồi toàn diện thể chất & trí não",
                description = "Khung giờ vàng từ 22:30 đến 02:00 sáng là lúc cơ thể tiết Hormone Tăng Trưởng (HGH) mạnh nhất.",
                icon = Icons.Default.DarkMode,
                accentColor = ForestGreen,
                isDone = bedtimeDone,
                onToggle = {
                    viewModel.toggleHabit("bedtime_2230", bedtimeDone, if (bedtimeDone) 1 else 0, 1)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TimeFund24hDonutCard(
    allocations: List<TimeAllocationEntity>,
    onUpdateHours: (String, Float) -> Unit
) {
    val mapByType = remember(allocations) {
        allocations.associateBy { it.activityType }
    }

    val itemsData = remember(mapByType) {
        TimeActivityCategory.entries.map { cat ->
            val hrs = mapByType[cat.code]?.hours ?: cat.recommendedHours
            val color = when (cat) {
                TimeActivityCategory.SLEEP -> ForestGreen
                TimeActivityCategory.WORK -> MarineAqua
                TimeActivityCategory.STUDY -> LeafGreen
                TimeActivityCategory.REST -> WarmAmber
                TimeActivityCategory.PLAY -> Color(0xFF8E6CEF)
            }
            Triple(cat, hrs, color)
        }
    }

    val totalHours = remember(itemsData) {
        itemsData.sumOf { it.second.toDouble() }.toFloat()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = ForestGreen.copy(alpha = 0.14f))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .testTag("time_fund_donut_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Card Header with proper weight(1f) & single-line badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MintLight)
                    ) {
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "QUỸ THỜI GIAN 24H",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Phân bổ: Ngủ • Làm • Học • Nghỉ • Chơi",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (totalHours in 23.5f..24.5f) SuccessGreen.copy(alpha = 0.14f)
                            else WarmAmber.copy(alpha = 0.16f)
                        )
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = String.format("%.1fh / 24h", totalHours),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (totalHours in 23.5f..24.5f) SuccessGreen else WarmAmber,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Donut Chart + Legend Side-by-Side
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Hollow Center Donut Chart Canvas
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(134.dp)
                ) {
                    Canvas(modifier = Modifier.size(122.dp)) {
                        val strokeWidth = 20.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val safeTotal = totalHours.coerceAtLeast(1f)

                        var currentStartAngle = -90f
                        itemsData.forEach { (_, hrs, color) ->
                            val sweep = (hrs / safeTotal) * 360f
                            if (sweep > 1f) {
                                drawArc(
                                    color = color,
                                    startAngle = currentStartAngle,
                                    sweepAngle = (sweep - 2.5f).coerceAtLeast(1f),
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(diameter, diameter),
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                                currentStartAngle += sweep
                            }
                        }
                    }

                    // Hollow Center Text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.1fh", totalHours),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = ForestGreenDark,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = if (totalHours in 23.5f..24.5f) "Cân bằng" else "Điều chỉnh",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalHours in 23.5f..24.5f) SuccessGreen else WarmAmber,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Percentage Legend with proper weight(1f) & non-wrapping value
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsData.forEach { (cat, hrs, color) ->
                        val pct = if (totalHours > 0f) ((hrs / totalHours) * 100f).roundToInt() else 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.labelVi,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "${String.format("%.1f", hrs)}h ($pct%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = color,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Log Controls for Ngủ, Làm, Học, Nghỉ, Chơi
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsData.forEach { (cat, hrs, color) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MintLight.copy(alpha = 0.55f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cat.labelVi,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(CardWhite)
                                    .border(1.dp, CardBorder, CircleShape)
                                    .clickable {
                                        val next = (hrs - 0.5f).coerceAtLeast(0f)
                                        onUpdateHours(cat.code, next)
                                    }
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Giảm", tint = ForestGreen, modifier = Modifier.size(15.dp))
                            }

                            Text(
                                text = String.format("%.1f giờ", hrs),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = color,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(64.dp)
                            )

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(CardWhite)
                                    .border(1.dp, CardBorder, CircleShape)
                                    .clickable {
                                        val next = (hrs + 0.5f).coerceAtMost(16f)
                                        onUpdateHours(cat.code, next)
                                    }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tăng", tint = ForestGreen, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PomodoroFocusBlockerCard(
    durationMinutes: Int,
    remainingSeconds: Int,
    isRunning: Boolean,
    completedSessions: Int,
    isFocusShieldActive: Boolean,
    onSelectDuration: (Int) -> Unit,
    onToggleStartPause: () -> Unit,
    onReset: () -> Unit,
    onToggleShield: () -> Unit
) {
    val totalSeconds = (durationMinutes * 60).coerceAtLeast(1)
    val progress = 1f - (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "pomodoroArc"
    )

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", mins, secs)

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = ForestGreen.copy(alpha = 0.14f))
            .border(
                1.5.dp,
                if (isRunning) ForestGreen else CardBorder,
                RoundedCornerShape(24.dp)
            )
            .testTag("pomodoro_focus_card")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row with weight(1f) & single-line badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MintLight)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ĐỒNG HỒ POMODORO",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Khóa tập trung • Chặn thông báo",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MintLight)
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$completedSessions phiên",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = ForestGreen,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clean 3-Option Segmented Duration Selector (Never wraps)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    25 to "25 phút • Chuẩn",
                    45 to "45 phút • Sâu",
                    60 to "60 phút • Cực đại"
                ).forEach { (m, label) ->
                    val isSelected = durationMinutes == m
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ForestGreen else MintLight.copy(alpha = 0.7f))
                            .border(
                                1.dp,
                                if (isSelected) ForestGreen else CardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectDuration(m) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else ForestGreenDark,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pomodoro Circular Timer Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                Canvas(modifier = Modifier.size(168.dp)) {
                    val stroke = 12.dp.toPx()
                    drawCircle(
                        color = Color(0xFFE5EFE7),
                        radius = size.minDimension / 2f - stroke / 2f,
                        style = Stroke(stroke)
                    )
                    drawArc(
                        color = if (isRunning) ForestGreen else LeafGreen,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isFocusShieldActive) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DoNotDisturbOn,
                                contentDescription = null,
                                tint = DangerCrimson,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CHẶN THÔNG BÁO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = DangerCrimson,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    Text(
                        text = timeText,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = ForestGreenDark,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = if (isRunning) "Đang khóa tập trung..." else "Sẵn sàng bắt đầu",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Focus Shield DND Toggle Strip with proper weight(1f) on inner Column
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MintLight.copy(alpha = 0.7f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Khiên Chặn Xao Nhãng (Focus Shield)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Tắt âm báo mạng xã hội khi chạy Pomodoro",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Switch(
                    checked = isFocusShieldActive,
                    onCheckedChange = { onToggleShield() },
                    colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = MintLight)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onToggleStartPause,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) WarningAmber else ForestGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRunning) "Tạm Dừng Phiên" else "Bắt Đầu Khóa Tập Trung",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Đặt lại", tint = ForestGreen, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun VisualScreenTimeReportCard(
    timeAllocations: List<TimeAllocationEntity>,
    pomodoroSessions: Int,
    screenLimitDone: Boolean
) {
    val mapByType = remember(timeAllocations) { timeAllocations.associateBy { it.activityType } }
    val playHours = mapByType[TimeActivityCategory.PLAY.code]?.hours ?: 2.0f
    val studyHours = mapByType[TimeActivityCategory.STUDY.code]?.hours ?: 3.0f
    val deepWorkHours = remember(pomodoroSessions, studyHours) {
        studyHours + (pomodoroSessions * 0.4f)
    }

    val totalScreenHours = (playHours + studyHours * 0.6f).coerceIn(0.5f, 12f)
    val productiveRatio = (deepWorkHours / (deepWorkHours + playHours).coerceAtLeast(0.5f)).coerceIn(0f, 1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row with weight(1f) & single-line badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MarineAqua.copy(alpha = 0.14f))
                    ) {
                        Icon(Icons.Default.Smartphone, contentDescription = null, tint = MarineAqua, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BÁO CÁO SCREEN-TIME",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Học tập & Làm việc sâu vs Giải trí",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = String.format("%.1fh màn hình", totalScreenHours),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (totalScreenHours <= 3.5f) SuccessGreen else WarmAmber,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .background(MintLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Productive vs Entertainment Screen Bar with non-wrapping right values
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tập trung Học tập & Làm việc sâu",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "${(productiveRatio * 100).roundToInt()}% (${String.format("%.1fh", deepWorkHours)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = ForestGreen,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { productiveRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = ForestGreen,
                        trackColor = MintLight,
                        strokeCap = StrokeCap.Round
                    )
                }

                Column {
                    val entertainmentRatio = (1f - productiveRatio).coerceIn(0f, 1f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Giải trí & Mạng xã hội",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmAmber,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = "${(entertainmentRatio * 100).roundToInt()}% (${String.format("%.1fh", playHours)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = WarmAmber,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { entertainmentRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (playHours > 3f) DangerCrimson else WarmAmber,
                        trackColor = MintLight,
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (screenLimitDone) {
                    "Tuyệt vời: Đã kích hoạt khóa màn hình sau 22:00 hôm nay!"
                } else {
                    "Lưu ý: Hãy giữ thời lượng giải trí dưới 2.5h/ngày và khóa màn hình đúng 22:00."
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (screenLimitDone) SuccessGreen else TextSecondary
            )
        }
    }
}

@Composable
private fun DetoxCurfewCard(
    title: String,
    subtitle: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isDone: Boolean,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDone) SuccessGreen.copy(alpha = 0.5f) else CardBorder,
                RoundedCornerShape(18.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDone) SuccessGreen else MintLight
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (isDone) "Đã Đạt" else "Xác nhận",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDone) Color.White else ForestGreen,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                lineHeight = 17.sp
            )
        }
    }
}
