package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.DailyJournalEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.EcoBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.MarineAqua
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintLight
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.PersonalGrowthSummary
import com.example.ui.viewmodel.Project90ViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

const val MAX_JOURNAL_LINE_CHARS = 180

@Composable
fun SelfGrowthScreen(
    viewModel: Project90ViewModel,
    modifier: Modifier = Modifier
) {
    val timerSeconds by viewModel.readingTimerSeconds.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isReadingRunning.collectAsStateWithLifecycle()
    val todayJournal by viewModel.todayJournal.collectAsStateWithLifecycle()
    val allJournals by viewModel.allJournals.collectAsStateWithLifecycle()
    val growthSummary by viewModel.personalGrowthSummary.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()

    val isReadingCompletedToday = remember(todayLogs, timerSeconds) {
        timerSeconds == 0 || todayLogs.any { it.habitId == "reading_20min" && it.isCompleted }
    }

    var line1 by remember { mutableStateOf("") }
    var line2 by remember { mutableStateOf("") }
    var line3 by remember { mutableStateOf("") }
    var savedSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(todayJournal) {
        todayJournal?.let {
            if (line1.isEmpty() && it.line1GoodThings.isNotBlank()) line1 = it.line1GoodThings
            if (line2.isEmpty() && it.line2LessonLearned.isNotBlank()) line2 = it.line2LessonLearned
            if (line3.isEmpty() && it.line3TomorrowGoal.isNotBlank()) line3 = it.line3TomorrowGoal
        }
    }

    val totalSeconds = 20 * 60
    val readingProgress = if (isReadingCompletedToday) {
        1f
    } else {
        ((totalSeconds - timerSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    }

    val animatedReadingArc by animateFloatAsState(
        targetValue = readingProgress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "readingArcAnim"
    )

    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val completedLinesToday = listOf(line1, line2, line3).count { it.trim().isNotEmpty() }

    val completedJournalsList = remember(allJournals) {
        allJournals.filter {
            it.line1GoodThings.isNotBlank() ||
                it.line2LessonLearned.isNotBlank() ||
                it.line3TomorrowGoal.isNotBlank()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EcoBackground)
            .testTag("self_growth_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "PHÁT TRIỂN BẢN THÂN 2.0",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    ),
                    color = ForestGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Theo dõi Streak Đọc Sách • Nhật Ký 3 Dòng Tự Mở Rộng",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 2. Visual Progress Bars Card: Reading Streak & Total Journals Written
        item {
            GrowthStreakAndJournalProgressCard(
                summary = growthSummary,
                isReadingCompletedToday = isReadingCompletedToday,
                completedLinesToday = completedLinesToday
            )
        }

        // 3. Restructured 20-Minute Focus Reading Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = ForestGreen.copy(alpha = 0.14f))
                    .border(
                        1.5.dp,
                        if (isReadingCompletedToday) SuccessGreen.copy(alpha = 0.5f)
                        else if (isTimerRunning) ForestGreen
                        else CardBorder,
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("reading_timer_card")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(18.dp)
                ) {
                    // Header Row (non-wrapping)
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
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = "Reading",
                                    tint = ForestGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ĐỌC SÁCH 20 PHÚT TẬP TRUNG",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Sách giấy hoặc Kindle • Không xao nhãng",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    if (isReadingCompletedToday) SuccessGreen.copy(alpha = 0.15f) else MintLight,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = if (isReadingCompletedToday) Icons.Default.CheckCircle else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isReadingCompletedToday) SuccessGreen else ForestGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isReadingCompletedToday) "Đã Đạt 20p" else "${(readingProgress * 100).roundToInt()}%",
                                fontSize = 11.sp,
                                color = if (isReadingCompletedToday) SuccessGreen else ForestGreen,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Circular Countdown Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(176.dp)
                    ) {
                        Canvas(modifier = Modifier.size(164.dp)) {
                            val stroke = 12.dp.toPx()
                            drawCircle(
                                color = Color(0xFFE5EFE7),
                                radius = size.minDimension / 2f - stroke / 2f,
                                style = Stroke(stroke)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(ForestGreen, LeafGreen, MarineAqua, ForestGreen)
                                ),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedReadingArc,
                                useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isReadingCompletedToday && timerSeconds == totalSeconds) "00:00" else timeFormatted,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isReadingCompletedToday) SuccessGreen else ForestGreenDark,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = when {
                                    isReadingCompletedToday -> "HOÀN THÀNH 20 PHÚT!"
                                    isTimerRunning -> "Đang đọc tập trung..."
                                    else -> "Sẵn sàng đọc sách"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isReadingCompletedToday) SuccessGreen else WarmAmber,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Start/Pause & Reset Controls
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.toggleReadingTimer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTimerRunning) WarningAmber else ForestGreen
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Start/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTimerRunning) "Tạm Dừng" else "Bắt Đầu Đọc",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.addReadingMinutesQuick(5) },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Text(
                                text = "+5 phút",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetReadingTimer() },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = ForestGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // 4. Upgraded 3-Line Evening Journal Card (Auto-expanding TextFields with Character Limit & Progress Bar)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = ForestGreen.copy(alpha = 0.14f))
                    .border(
                        1.5.dp,
                        if (completedLinesToday == 3) SuccessGreen.copy(alpha = 0.5f) else CardBorder,
                        RoundedCornerShape(24.dp)
                    )
                    .testTag("three_line_journal_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header Row
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
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = "Journal",
                                    tint = ForestGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "3 DÒNG NHẬT KÝ TỐI CÔ ĐỌNG",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Tự động mở rộng dòng • Tối đa $MAX_JOURNAL_LINE_CHARS ký tự/mục",
                                    style = MaterialTheme.typography.bodySmall,
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
                                    if (completedLinesToday == 3) SuccessGreen.copy(alpha = 0.15f) else MintLight
                                )
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "$completedLinesToday / 3 dòng",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (completedLinesToday == 3) SuccessGreen else ForestGreen,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Line 1: Auto-expanding TextField
                    AutoExpandingJournalField(
                        stepNumber = "1",
                        label = "Thành tựu / Việc làm tốt nhất hôm nay",
                        placeholder = "VD: Hoàn thành 10.000 bước, đọc trọn 20 phút sách và giữ vững cam kết No Sugar...",
                        value = line1,
                        onValueChange = {
                            line1 = it.take(MAX_JOURNAL_LINE_CHARS)
                            savedSuccess = false
                        },
                        accentColor = ForestGreen,
                        testTag = "journal_line_1_input"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Line 2: Auto-expanding TextField
                    AutoExpandingJournalField(
                        stepNumber = "2",
                        label = "Bài học đúc kết từ thử thách / sai lầm",
                        placeholder = "VD: Khi mất tập trung lúc chiều, chỉ cần đứng dậy uống 250ml nước là tỉnh táo lại ngay...",
                        value = line2,
                        onValueChange = {
                            line2 = it.take(MAX_JOURNAL_LINE_CHARS)
                            savedSuccess = false
                        },
                        accentColor = WarmAmber,
                        testTag = "journal_line_2_input"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Line 3: Auto-expanding TextField
                    AutoExpandingJournalField(
                        stepNumber = "3",
                        label = "Mục tiêu trọng tâm số 1 ngày mai",
                        placeholder = "VD: Hoàn thành 2 phiên Pomodoro buổi sáng và trích 10% thu nhập vào quỹ mục tiêu...",
                        value = line3,
                        onValueChange = {
                            line3 = it.take(MAX_JOURNAL_LINE_CHARS)
                            savedSuccess = false
                        },
                        accentColor = MarineAqua,
                        testTag = "journal_line_3_input"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.saveJournal(line1.trim(), line2.trim(), line3.trim())
                            savedSuccess = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (completedLinesToday == 3) ForestGreen else LeafGreen
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_journal_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Save", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (completedLinesToday == 3) "Lưu Trọn Vẹn 3 Dòng Nhật Ký" else "Lưu Bản Nháp Nhật Ký ($completedLinesToday/3)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    AnimatedVisibility(
                        visible = savedSuccess,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SuccessGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Đã lưu nhật ký thành công! Chuỗi phát triển bản thân đã được cập nhật.",
                                fontSize = 11.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. Recent Journals History Section
        if (completedJournalsList.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SỔ LƯU TRỮ NHẬT KÝ ĐÃ VIẾT (${completedJournalsList.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = ForestGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            items(completedJournalsList.take(10), key = { it.dateEpochDay }) { entry ->
                PastJournalEntryCard(entry = entry)
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun GrowthStreakAndJournalProgressCard(
    summary: PersonalGrowthSummary,
    isReadingCompletedToday: Boolean,
    completedLinesToday: Int
) {
    val readingStreakRatio = remember(summary.readingStreakDays, summary.readingStreakTarget) {
        if (summary.readingStreakTarget > 0) {
            (summary.readingStreakDays.toFloat() / summary.readingStreakTarget.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    val animatedReadingStreakBar by animateFloatAsState(
        targetValue = readingStreakRatio,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "readingStreakBar"
    )

    val journalProgressRatio = remember(summary.totalJournalsWritten, summary.journalGoalDays) {
        if (summary.journalGoalDays > 0) {
            (summary.totalJournalsWritten.toFloat() / summary.journalGoalDays.toFloat()).coerceIn(0f, 1f)
        } else 0f
    }

    val animatedJournalBar by animateFloatAsState(
        targetValue = journalProgressRatio,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "journalCountBar"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = ForestGreen.copy(alpha = 0.15f))
            .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            .testTag("growth_progress_overview_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Card Header
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
                            .background(WarmAmber.copy(alpha = 0.16f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = WarmAmber,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TIẾN ĐỘ ĐỌC SÁCH & NHẬT KÝ",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Theo dõi chuỗi ngày đọc liên tục & tổng nhật ký",
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
                        text = "${summary.totalReadingDaysCompleted} ngày đọc",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = ForestGreen,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Progress Bar: Chuỗi ngày đọc sách liên tục (Reading Streak)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MintSurface)
                    .border(1.dp, MintBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chuỗi ngày đọc sách liên tục",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${summary.readingStreakDays} / ${summary.readingStreakTarget} ngày 🔥",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = WarmAmber,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { animatedReadingStreakBar },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = ForestGreen,
                    trackColor = CardBorder.copy(alpha = 0.6f),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 7-Day Mini Reading Chain Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    summary.last7DaysReading.forEach { (dayLabel, isDone) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) SuccessGreen else CardWhite)
                                    .border(
                                        1.dp,
                                        if (isDone) SuccessGreen else CardBorder,
                                        CircleShape
                                    )
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = dayLabel,
                                fontSize = 9.sp,
                                fontWeight = if (dayLabel == "H.Nay") FontWeight.Black else FontWeight.Medium,
                                color = if (isDone) ForestGreen else TextSecondary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Progress Bar: Số lượng nhật ký đã viết (Total Journals Written)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MintSurface)
                    .border(1.dp, MintBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HistoryEdu,
                            contentDescription = null,
                            tint = MarineAqua,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Số lượng nhật ký đã viết",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "${summary.totalJournalsWritten} / ${summary.journalGoalDays} trang",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MarineAqua,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { animatedJournalBar },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MarineAqua,
                    trackColor = CardBorder.copy(alpha = 0.6f),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Chuỗi viết liên tiếp: ${summary.journalStreakDays} ngày",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Hôm nay: $completedLinesToday/3 dòng",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (completedLinesToday == 3) SuccessGreen else ForestGreen,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun AutoExpandingJournalField(
    stepNumber: String,
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    accentColor: Color,
    testTag: String
) {
    val charCount = value.length
    val charRatio = (charCount.toFloat() / MAX_JOURNAL_LINE_CHARS.toFloat()).coerceIn(0f, 1f)
    val isNearLimit = charCount >= 100
    val isCompleted = value.trim().isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MintSurface.copy(alpha = 0.65f))
            .border(
                1.dp,
                if (isCompleted) accentColor.copy(alpha = 0.45f) else CardBorder,
                RoundedCornerShape(16.dp)
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .padding(12.dp)
    ) {
        // Label & Character Counter Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) accentColor else accentColor.copy(alpha = 0.16f))
                ) {
                    Text(
                        text = stepNumber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isCompleted) Color.White else accentColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "$charCount / $MAX_JOURNAL_LINE_CHARS",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = when {
                    charCount >= MAX_JOURNAL_LINE_CHARS -> DangerCrimson
                    isNearLimit -> WarmAmber
                    isCompleted -> accentColor
                    else -> TextSecondary
                },
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.testTag("${testTag}_char_counter")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Auto-expanding OutlinedTextField (minLines = 2, maxLines = 5)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 12.sp,
                    color = TextSecondary.copy(alpha = 0.7f),
                    lineHeight = 17.sp
                )
            },
            trailingIcon = if (value.isNotEmpty()) {
                {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Xóa dòng",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else null,
            singleLine = false,
            minLines = 2,
            maxLines = 5,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = TextPrimary,
                lineHeight = 20.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = CardBorder,
                focusedContainerColor = CardWhite,
                unfocusedContainerColor = CardWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .testTag(testTag)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Character Capacity Mini Progress Bar
        LinearProgressIndicator(
            progress = { charRatio },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = when {
                charCount >= MAX_JOURNAL_LINE_CHARS -> DangerCrimson
                isNearLimit -> WarmAmber
                else -> accentColor
            },
            trackColor = CardBorder.copy(alpha = 0.4f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
private fun PastJournalEntryCard(entry: DailyJournalEntity) {
    val dateFormatted = remember(entry.dateEpochDay) {
        LocalDate.ofEpochDay(entry.dateEpochDay).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Nhật ký ngày $dateFormatted",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = ForestGreen
                )
                if (entry.readingSecondsCompleted >= 1200) {
                    Text(
                        text = "Đã đọc 20p",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier
                            .background(SuccessGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (entry.line1GoodThings.isNotBlank()) {
                Text(
                    text = "1. Thành tựu: ${entry.line1GoodThings}",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    lineHeight = 17.sp
                )
            }
            if (entry.line2LessonLearned.isNotBlank()) {
                Text(
                    text = "2. Bài học: ${entry.line2LessonLearned}",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    lineHeight = 17.sp
                )
            }
            if (entry.line3TomorrowGoal.isNotBlank()) {
                Text(
                    text = "3. Mục tiêu: ${entry.line3TomorrowGoal}",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
