package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.GoldenStreak
import com.example.ui.theme.GoldenStreakLight
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.MarineAqua
import com.example.ui.theme.MintLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Circular90DaysProgress(
    dayNumber: Int,
    totalDays: Int = 90,
    daysRemainingToFeb: Long,
    currentStreak: Int,
    dailyCompletionFraction: Float = 0f,
    modifier: Modifier = Modifier
) {
    val targetProgress = if (dayNumber <= 0) 0.02f else (dayNumber.toFloat() / totalDays.toFloat()).coerceIn(0.02f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "90_days_spring_progress"
    )

    val animatedDailyProgress by animateFloatAsState(
        targetValue = dailyCompletionFraction.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "daily_ring_progress"
    )

    // Subtle breathing aura animation
    val infiniteTransition = rememberInfiniteTransition(label = "halo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag("circular_90_days_progress")
            .size(256.dp)
    ) {
        Canvas(modifier = Modifier.size(248.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerStrokeWidth = 15.dp.toPx()
            val innerStrokeWidth = 7.dp.toPx()

            // 1. Soft breathing botanical aura halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LeafGreen.copy(alpha = pulseAlpha),
                        MarineAqua.copy(alpha = pulseAlpha * 0.45f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = (size.minDimension / 2f) * pulseScale
                ),
                radius = (size.minDimension / 2f) * pulseScale,
                center = center
            )

            // 2. Outer 90-Day Track
            val outerDiameter = size.minDimension - outerStrokeWidth * 1.8f
            val outerTopLeft = Offset((size.width - outerDiameter) / 2f, (size.height - outerDiameter) / 2f)
            val outerArcSize = Size(outerDiameter, outerDiameter)

            drawArc(
                color = Color(0xFFDCEBE0).copy(alpha = 0.75f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = outerTopLeft,
                size = outerArcSize,
                style = Stroke(width = outerStrokeWidth, cap = StrokeCap.Round)
            )

            // Subtle tick marks along the outer ring
            drawArc(
                color = ForestGreen.copy(alpha = 0.08f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = outerTopLeft,
                size = outerArcSize,
                style = Stroke(
                    width = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 14f), 0f)
                )
            )

            // 3. Outer 90-Day Active Gradient Arc
            val sweepAngle = 360f * animatedProgress
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to ForestGreen,
                    0.45f to LeafGreen,
                    0.8f to MarineAqua,
                    1.0f to GoldenStreak
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = outerTopLeft,
                size = outerArcSize,
                style = Stroke(width = outerStrokeWidth, cap = StrokeCap.Round)
            )

            // Glowing tip orb on outer arc
            val angleRad = Math.toRadians((-90f + sweepAngle).toDouble())
            val outerRadius = outerDiameter / 2f
            val orbCenter = Offset(
                x = center.x + (outerRadius * cos(angleRad)).toFloat(),
                y = center.y + (outerRadius * sin(angleRad)).toFloat()
            )
            drawCircle(
                color = LeafGreen.copy(alpha = 0.35f),
                radius = outerStrokeWidth * 0.85f,
                center = orbCenter
            )
            drawCircle(
                color = Color.White,
                radius = outerStrokeWidth * 0.32f,
                center = orbCenter
            )

            // 4. Inner Daily Completion Ring
            val innerGap = 14.dp.toPx()
            val innerDiameter = outerDiameter - (outerStrokeWidth + innerStrokeWidth) - innerGap
            val innerTopLeft = Offset((size.width - innerDiameter) / 2f, (size.height - innerDiameter) / 2f)
            val innerArcSize = Size(innerDiameter, innerDiameter)

            drawArc(
                color = Color(0xFFE5F0E8).copy(alpha = 0.8f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = innerTopLeft,
                size = innerArcSize,
                style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round)
            )

            if (animatedDailyProgress > 0f) {
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(MarineAqua, SuccessGreen, GoldenStreak)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedDailyProgress,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerArcSize,
                    style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // 5. Frosted Glass Inner Medallion (Glassmorphism)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(166.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.92f),
                            Color(0xFFF2FAF5).copy(alpha = 0.82f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            LeafGreen.copy(alpha = 0.28f),
                            MarineAqua.copy(alpha = 0.22f)
                        )
                    ),
                    shape = CircleShape
                )
                .padding(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Streak badge (Warm amber glass pill)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(GoldenStreakLight.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                        .border(0.8.dp, WarmAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Chuỗi ngày",
                        tint = WarmAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$currentStreak Ngày Chuỗi",
                        color = WarmAmber,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                if (dayNumber == 0) {
                    Text(
                        text = "KHỞI ĐỘNG",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            fontSize = 20.sp
                        ),
                        color = ForestGreenDark
                    )
                    Text(
                        text = "Bắt đầu 07/11",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = ForestGreen,
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = "NGÀY $dayNumber",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            fontSize = 24.sp
                        ),
                        color = ForestGreenDark
                    )
                    Text(
                        text = "trên $totalDays ngày • Còn ${daysRemainingToFeb}N",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 10.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Daily % Badge inside the ring
                val dailyPercentInt = (dailyCompletionFraction * 100).toInt().coerceIn(0, 100)
                Box(
                    modifier = Modifier
                        .background(MintLight.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                        .border(0.8.dp, ForestGreen.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Hôm nay: $dailyPercentInt% Hoàn thành",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dailyPercentInt == 100) SuccessGreen else ForestGreen
                    )
                }
            }
        }
    }
}
