package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.example.data.local.entity.DailyLogEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.EcoBackground
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.MarineAqua
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.Project90ViewModel
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun MovementScreen(
    viewModel: Project90ViewModel,
    modifier: Modifier = Modifier
) {
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val recentStepLogs by viewModel.recentStepLogs.collectAsStateWithLifecycle()
    val hasHardwareSensor by viewModel.stepCounterManager.hasHardwareSensor.collectAsStateWithLifecycle()

    val logsMap = remember(todayLogs) { todayLogs.associateBy { it.habitId } }

    val stepLog = logsMap["pedometer_10k"]
    val stepCount = stepLog?.progressValue ?: 0
    val stepGoal = 10000
    val stepPercent = (stepCount.toFloat() / stepGoal.toFloat()).coerceIn(0f, 1f)

    val animatedArcProgress by animateFloatAsState(
        targetValue = stepPercent,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "curvedStepArc"
    )

    val distanceKm = remember(stepCount) { String.format("%.2f", stepCount * 0.00075f) }
    val caloriesBurned = remember(stepCount) { (stepCount * 0.045f).roundToInt() }

    val pushupCount = logsMap["pushups_50"]?.progressValue ?: 0
    val squatCount = logsMap["squats_100"]?.progressValue ?: 0
    val coldShowerDone = logsMap["cold_shower"]?.isCompleted == true
    val workoutCount = logsMap["workout_weekly"]?.progressValue ?: 0
    val footballDone = logsMap["football_weekly"]?.isCompleted == true

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(EcoBackground)
            .testTag("movement_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "MÔ-ĐUN VẬN ĐỘNG & PEDOMETER",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    ),
                    color = ForestGreen
                )
                Text(
                    text = "Cảm biến TYPE_STEP_COUNTER chạy ngầm 24/7 & Kỷ luật thể lực",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // 1. Curved Progress Arc Gauge Card (Thanh tiến độ cong Pedometer)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = ForestGreen.copy(alpha = 0.14f))
                    .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(18.dp)
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
                                .padding(end = 10.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MintLight)
                            ) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PEDOMETER TỰ ĐỘNG 24/7",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Chạy ngầm 24/7 • Cảm biến bước chân",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(MintLight, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = "Sensor",
                                tint = if (hasHardwareSensor) SuccessGreen else ForestGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasHardwareSensor) "Sensor Live" else "Cơ bản",
                                fontSize = 10.sp,
                                color = if (hasHardwareSensor) SuccessGreen else ForestGreen,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Curved Arc Gauge (240 degree horseshoe arc)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(220.dp)
                    ) {
                        Canvas(modifier = Modifier.size(200.dp)) {
                            val strokeWidth = 18.dp.toPx()
                            val arcDiameter = size.minDimension - strokeWidth
                            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

                            // Background track arc (startAngle = 150f, sweep = 240f)
                            drawArc(
                                color = Color(0xFFE5EFE7),
                                startAngle = 150f,
                                sweepAngle = 240f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcDiameter, arcDiameter),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Foreground curved progress arc
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = listOf(ForestGreen, LeafGreen, MarineAqua, ForestGreen)
                                ),
                                startAngle = 150f,
                                sweepAngle = 240f * animatedArcProgress,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcDiameter, arcDiameter),
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(stepPercent * 100).roundToInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LeafGreen
                            )
                            Text(
                                text = String.format("%,d", stepCount),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = if (stepCount >= stepGoal) SuccessGreen else ForestGreenDark
                            )
                            Text(
                                text = "/ 10.000 bước mục tiêu",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                    }

                    // Telemetry pills: Km & Kcal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MintLight.copy(alpha = 0.7f))
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("$distanceKm km", fontWeight = FontWeight.Black, fontSize = 14.sp, color = ForestGreenDark)
                                Text("Quãng đường", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                        Box(modifier = Modifier.width(1.dp).height(26.dp).background(CardBorder))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = WarmAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("$caloriesBurned kcal", fontWeight = FontWeight.Black, fontSize = 14.sp, color = ForestGreenDark)
                                Text("Năng lượng đốt", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick step increment buttons (for treadmill / indoor walking)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.addManualSteps(500) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Text("+500", fontSize = 12.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.addManualSteps(1000) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Text("+1.000", fontSize = 12.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.addManualSteps(2500) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                        ) {
                            Text("+2.500", fontSize = 12.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. 7-Day Daily Step Bar Chart Card (Biểu đồ cột bước chân theo ngày)
        item {
            DailyStepsBarChartCard(
                recentStepLogs = recentStepLogs,
                todayStepCount = stepCount,
                todayEpochDay = viewModel.todayEpochDay
            )
        }

        // 3. Daily Movement Checklist
        item {
            Text(
                text = "CHECKLIST VẬN ĐỘNG HÀNG NGÀY",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = TextPrimary
            )
        }

        item {
            ExerciseTrackerCard(
                title = "50 Lần Hít đất (Push-ups)",
                sub = "Cơ ngực & vai trước săn chắc",
                icon = Icons.Default.FitnessCenter,
                current = pushupCount,
                target = 50,
                unit = "cái",
                onIncrement = {
                    val next = (pushupCount + 10).coerceAtMost(50)
                    viewModel.updateHabitProgress("pushups_50", next, 50)
                },
                onComplete = {
                    viewModel.toggleHabit("pushups_50", pushupCount >= 50, pushupCount, 50)
                }
            )
        }

        item {
            ExerciseTrackerCard(
                title = "100 Lần Squats",
                sub = "Tăng sức bật, cơ đùi và testosterone",
                icon = Icons.Default.DirectionsRun,
                current = squatCount,
                target = 100,
                unit = "cái",
                onIncrement = {
                    val next = (squatCount + 20).coerceAtMost(100)
                    viewModel.updateHabitProgress("squats_100", next, 100)
                },
                onComplete = {
                    viewModel.toggleHabit("squats_100", squatCount >= 100, squatCount, 100)
                }
            )
        }

        item {
            SimpleToggleCard(
                title = "Tắm nước lạnh buổi sáng",
                sub = "Giải phóng adrenaline tự nhiên, tỉnh táo tuyệt đối",
                icon = Icons.Default.LocalDrink,
                isDone = coldShowerDone,
                color = MarineAqua,
                onToggle = {
                    viewModel.toggleHabit("cold_shower", coldShowerDone, if (coldShowerDone) 1 else 0, 1)
                }
            )
        }

        // Weekly Checklist
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "CHECKLIST HÀNG TUẦN (WEEKLY GOALS)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = ForestGreen
            )
        }

        item {
            ExerciseTrackerCard(
                title = "2 Buổi tập luyện thể thao / Gym",
                sub = "Kích thích cơ bắp toàn thân với cường độ cao",
                icon = Icons.Default.FitnessCenter,
                current = workoutCount,
                target = 2,
                unit = "buổi",
                onIncrement = {
                    val next = (workoutCount + 1).coerceAtMost(2)
                    viewModel.updateHabitProgress("workout_weekly", next, 2)
                },
                onComplete = {
                    viewModel.toggleHabit("workout_weekly", workoutCount >= 2, workoutCount, 2)
                }
            )
        }

        item {
            SimpleToggleCard(
                title = "1 Trận đá bóng đối kháng",
                sub = "Cardio bứt tốc, nâng cao phản xạ và tinh thần chiến binh",
                icon = Icons.Default.SportsSoccer,
                isDone = footballDone,
                color = LeafGreen,
                onToggle = {
                    viewModel.toggleHabit("football_weekly", footballDone, if (footballDone) 1 else 0, 1)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DailyStepsBarChartCard(
    recentStepLogs: List<DailyLogEntity>,
    todayStepCount: Int,
    todayEpochDay: Long
) {
    val logByDate = remember(recentStepLogs) {
        recentStepLogs.associateBy { it.dateEpochDay }
    }

    val daysData = remember(logByDate, todayStepCount, todayEpochDay) {
        (6 downTo 0).map { offset ->
            val day = todayEpochDay - offset
            val date = LocalDate.ofEpochDay(day)
            val label = if (offset == 0) "H.Nay" else "${date.dayOfMonth}/${date.monthValue}"
            val steps = if (offset == 0) todayStepCount else (logByDate[day]?.progressValue ?: 0)
            label to steps
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    Text(
                        text = "BIỂU ĐỒ BƯỚC CHÂN 7 NGÀY",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = ForestGreen,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "So sánh với mốc kỷ luật 10.000 bước/ngày",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "Mục tiêu: 10k",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .background(MintLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxSteps = 12000f
                    val chartHeight = size.height - 8.dp.toPx()
                    val slotWidth = size.width / daysData.size
                    val barWidth = (slotWidth * 0.45f).coerceAtMost(26.dp.toPx())

                    // 10k Goal dashed/solid line
                    val goalY = chartHeight * (1f - (10000f / maxSteps))
                    drawLine(
                        color = WarmAmber.copy(alpha = 0.65f),
                        start = Offset(0f, goalY),
                        end = Offset(size.width, goalY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    daysData.forEachIndexed { idx, (_, steps) ->
                        val centerX = idx * slotWidth + slotWidth / 2f
                        val ratio = (steps.toFloat() / maxSteps).coerceIn(0.05f, 1f)
                        val barH = chartHeight * ratio
                        val isGoalReached = steps >= 10000

                        drawRoundRect(
                            brush = if (isGoalReached) {
                                Brush.verticalGradient(listOf(SuccessGreen, ForestGreen))
                            } else {
                                Brush.verticalGradient(listOf(LeafGreen, ForestGreen.copy(alpha = 0.75f)))
                            },
                            topLeft = Offset(centerX - barWidth / 2f, chartHeight - barH),
                            size = Size(barWidth, barH),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysData.forEach { (label, steps) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (steps >= 1000) String.format("%.1fk", steps / 1000f) else "$steps",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (steps >= 10000) SuccessGreen else ForestGreen
                        )
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseTrackerCard(
    title: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    current: Int,
    target: Int,
    unit: String,
    onIncrement: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = current >= target
    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDone) SuccessGreen.copy(alpha = 0.5f) else CardBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDone) SuccessGreen.copy(alpha = 0.15f) else MintLight)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isDone) SuccessGreen else ForestGreen,
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
                            text = sub,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isDone) SuccessGreen else MintLight)
                        .border(1.dp, if (isDone) SuccessGreen else MintBorder, CircleShape)
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$current / $target $unit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) SuccessGreen else WarmAmber
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onIncrement,
                        enabled = !isDone,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = ForestGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tăng", fontSize = 12.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDone) Color(0xFFE2EBE4) else ForestGreen
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (isDone) "Hủy" else "Xong",
                            fontSize = 12.sp,
                            color = if (isDone) TextSecondary else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleToggleCard(
    title: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDone: Boolean,
    color: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDone) SuccessGreen.copy(alpha = 0.5f) else CardBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.12f))
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
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
                        text = sub,
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
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isDone) "Đã Đạt" else "Hoàn thành",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) Color.White else ForestGreen,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
