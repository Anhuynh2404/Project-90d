package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NoDrinks
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailyLogEntity
import com.example.data.local.entity.HabitCategory
import com.example.data.local.entity.HabitEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldenStreak
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber

@Composable
fun HabitChecklistItem(
    habit: HabitEntity,
    log: DailyLogEntity?,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = log?.isCompleted == true
    val progress = log?.progressValue ?: 0

    val borderColor by animateColorAsState(
        targetValue = if (isDone) SuccessGreen.copy(alpha = 0.5f) else CardBorder,
        label = "border_color"
    )

    val icon: ImageVector = when (habit.category) {
        HabitCategory.MOVEMENT.name -> if (habit.id.contains("water") || habit.id.contains("cold")) Icons.Default.LocalDrink else Icons.Default.FitnessCenter
        HabitCategory.NUTRITION.name -> if (habit.id.contains("sugar")) Icons.Default.NoDrinks else Icons.Default.LocalDrink
        HabitCategory.DIGITAL_WELLBEING.name -> Icons.Default.Nightlight
        HabitCategory.FINANCE.name -> Icons.Default.Savings
        HabitCategory.SELF_GROWTH.name -> Icons.Default.Book
        else -> Icons.Default.DirectionsRun
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_item_${habit.id}")
            .clip(RoundedCornerShape(18.dp))
            .background(CardWhite.copy(alpha = 0.94f))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable { onToggle() }
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Category Icon Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDone) SuccessGreen.copy(alpha = 0.15f)
                        else MintLight
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = habit.title,
                    tint = if (isDone) SuccessGreen else ForestGreen,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Habit Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDone) TextSecondary else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (habit.isWeekly) {
                        Text(
                            text = "Hàng tuần",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            modifier = Modifier
                                .background(MintLight, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Progress Bar if target > 1
                if (habit.targetValue > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { (progress.toFloat() / habit.targetValue.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isDone) SuccessGreen else ForestGreen,
                            trackColor = Color(0xFFE8EFE9)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$progress / ${habit.targetValue} ${habit.unit}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDone) SuccessGreen else WarmAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Checkbox Circle Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDone) SuccessGreen else MintLight
                    )
                    .border(
                        1.5.dp,
                        if (isDone) SuccessGreen else MintBorder,
                        CircleShape
                    )
            ) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Hoàn thành",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
