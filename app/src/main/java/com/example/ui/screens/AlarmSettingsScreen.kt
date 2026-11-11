package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AppBackgroundPreset
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.MarineAqua
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintLight
import com.example.ui.theme.MintSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.Project90ViewModel

@Composable
fun AlarmSettingsScreen(
    viewModel: Project90ViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val todayLogs by viewModel.todayLogs.collectAsStateWithLifecycle()
    val selectedTheme by viewModel.themePreset.collectAsStateWithLifecycle()
    val selectedBackground by viewModel.backgroundPreset.collectAsStateWithLifecycle()
    val localThemeConfig = LocalAppTheme.current

    val logsMap = remember(todayLogs) { todayLogs.associateBy { it.habitId } }
    val pendingHabits = remember(habits, todayLogs) {
        habits.filter { !it.isWeekly && logsMap[it.id]?.isCompleted != true }
    }

    var hasNotifPermission by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    var isIgnoringBattery by remember {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
            } else true
        )
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
        if (granted) {
            viewModel.refreshPersistentNotification()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("alarm_settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "CÀI ĐẶT GIAO DIỆN & HỆ THỐNG 24/7",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    ),
                    color = ForestGreen
                )
                Text(
                    text = "Cá nhân hóa chủ đề Glassmorphism, hình nền, ngày bắt đầu và thông báo 24/7",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        // =========================================================================
        // PHẦN 3: CÁ NHÂN HÓA GIAO DIỆN (DYNAMIC THEME & CUSTOM BACKGROUND)
        // =========================================================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ForestGreen.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                    .testTag("theme_customization_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MintLight)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Dynamic Theme",
                                    tint = ForestGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MÀU SẮC CHỦ ĐỀ (DYNAMIC THEME)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Đang dùng: ${localThemeConfig.themePreset.titleVi} • Đổi màu tức thì",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ForestGreen,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MintLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "LIVE UI",
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2x2 Grid of 4 Theme Presets
                    val presets = AppThemePreset.entries
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        presets.chunked(2).forEach { rowItems ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowItems.forEach { preset ->
                                    val isSelected = selectedTheme == preset
                                    ThemePresetOptionCard(
                                        preset = preset,
                                        isSelected = isSelected,
                                        onClick = { viewModel.selectThemePreset(preset) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Custom Background Section Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MintLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = "Custom Wallpaper",
                                tint = ForestGreen,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "HÌNH NỀN TÙY CHỈNH & LỚP KÍNH MỜ",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Ảnh nền tĩnh (res/drawable) phủ kính mờ Glassmorphism bảo vệ độ tương phản",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val backgrounds = AppBackgroundPreset.entries
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        backgrounds.chunked(2).forEach { rowBg ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowBg.forEach { bgPreset ->
                                    val isSelected = selectedBackground == bgPreset
                                    BackgroundPresetOptionCard(
                                        preset = bgPreset,
                                        isSelected = isSelected,
                                        onClick = { viewModel.selectBackgroundPreset(bgPreset) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // PHẦN 4: THIẾT LẬP NGÀY BẮT ĐẦU (START DATE CONFIGURATION)
        // =========================================================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ForestGreen.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                    .testTag("start_date_settings_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MintLight)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Start Date",
                                    tint = ForestGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "NGÀY BẮT ĐẦU THỬ THÁCH 90 NGÀY",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Công thức: (Ngày hiện tại - Ngày bắt đầu)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (summary.isChallengeCompleted) SuccessGreen.copy(alpha = 0.15f) else MintLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (summary.isChallengeCompleted) "HOÀN THÀNH" else "NGÀY ${summary.dayNumber}/90",
                                color = if (summary.isChallengeCompleted) SuccessGreen else ForestGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MintSurface)
                            .border(1.dp, MintBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Mốc bắt đầu đã lưu:", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    text = summary.startDateFormatted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Trạng thái đếm ngược:", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    text = when {
                                        summary.isChallengeCompleted -> "Hoàn thành thử thách (${summary.dayNumber} ngày)"
                                        summary.isBeforeStart -> "Chưa tới ngày bắt đầu"
                                        else -> "Ngày ${summary.dayNumber} • Còn ${summary.daysRemainingToFeb} ngày"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.isChallengeCompleted) SuccessGreen else ForestGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.openStartDateDialog() },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_start_date_dialog_button")
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Thiết Lập / Đổi Ngày Bắt Đầu 90 Ngày", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 1: Non-dismissible Ongoing Notification Status & Preview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, ForestGreen.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MintLight)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Ongoing Notification",
                                    tint = ForestGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "THÔNG BÁO GHIM 24/7 (STATUS BAR)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Không thể vuốt tắt - Luôn hiển thị việc chưa làm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ForestGreen,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MintLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ONGOING",
                                color = ForestGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Xem trước thông báo đang ghim trên thanh trạng thái:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Notification Preview Box (Dashboard Mini Style)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MintSurface)
                            .border(1.dp, MintBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
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
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Notif",
                                        tint = ForestGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PROJECT 90 • COMMAND DASHBOARD",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = ForestGreen,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "🔥 ${summary.currentStreak}N Chuỗi",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmAmber,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier
                                        .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mini 4-Tile Grid
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val stepCount = logsMap["pedometer_10k"]?.progressValue ?: 0
                                val waterMl = logsMap["water_2l"]?.progressValue ?: 0
                                val noSugarDone = logsMap["no_sugar"]?.isCompleted == true

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(CardWhite, RoundedCornerShape(8.dp))
                                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                ) {
                                    Column {
                                        Text("👟 BƯỚC", fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("${stepCount}/10k", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(CardWhite, RoundedCornerShape(8.dp))
                                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                ) {
                                    Column {
                                        Text("💧 NƯỚC", fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("${waterMl}ml", fontSize = 11.sp, color = MarineAqua, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(CardWhite, RoundedCornerShape(8.dp))
                                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                ) {
                                    Column {
                                        Text("🏋️ THỂ LỰC", fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("50 Pushup", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(CardWhite, RoundedCornerShape(8.dp))
                                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                ) {
                                    Column {
                                        Text("🚫 NO SUGAR", fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text(if (noSugarDone) "ĐÃ ĐẠT" else "CHƯA", fontSize = 11.sp, color = if (noSugarDone) SuccessGreen else DangerCrimson, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (pendingHabits.isNotEmpty())
                                    "⚠️ CÒN ${pendingHabits.size} MỤC CHƯA XONG HÔM NAY:"
                                else
                                    "🔥 100% HOÀN TẤT TOÀN BỘ MỤC TIÊU HÔM NAY!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (pendingHabits.isNotEmpty()) WarmAmber else SuccessGreen
                            )

                            if (pendingHabits.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                pendingHabits.take(3).forEach { habit ->
                                    Text(
                                        text = "• ${habit.title}",
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .background(ForestGreen, RoundedCornerShape(6.dp))
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text("📋 MỞ DASHBOARD", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(MintLight, RoundedCornerShape(6.dp))
                                        .border(1.dp, MintBorder, RoundedCornerShape(6.dp))
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text("💧 +250ML", fontSize = 10.sp, color = ForestGreen, fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(MintLight, RoundedCornerShape(6.dp))
                                        .border(1.dp, MintBorder, RoundedCornerShape(6.dp))
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text("👟 +500 BƯỚC", fontSize = 10.sp, color = ForestGreen, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.refreshPersistentNotification() },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Đồng Bộ Lại Thông Báo Trên Thanh Trạng Thái", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        // Section 2: Permission Checklist & Shield
        item {
            Text(
                text = "TRUNG TÂM CẤP QUYỀN TOÀN DIỆN (CHỐNG BỊ CHẶN)",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = ForestGreen
            )
        }

        item {
            PermissionCard(
                title = "1. Quyền Thông Báo Đẩy (Post Notifications)",
                description = "Bắt buộc trên Android 13+ để ứng dụng hiển thị bảng thông báo ghim 24/7.",
                isGranted = hasNotifPermission,
                actionLabel = "Cấp Quyền",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }

        item {
            PermissionCard(
                title = "2. Chạy Nền Không Giới Hạn (Tắt Tối Ưu Hóa Pin)",
                description = "Ngăn chặn hệ điều hành Android tự ý dừng dịch vụ thông báo 24/7 và đếm bước chân khi tắt màn hình.",
                isGranted = isIgnoringBattery,
                actionLabel = "Bỏ Giới Hạn Pin",
                onAction = {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    } catch (_: Exception) {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        context.startActivity(intent)
                    }
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ThemePresetOptionCard(
    preset: AppThemePreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) preset.containerLight else CardWhite)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) preset.primary else CardBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("theme_preset_${preset.id}")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(preset.primary)
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(preset.accent)
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(preset.containerLight)
                            .border(1.dp, preset.primary.copy(alpha = 0.3f), CircleShape)
                    )
                }

                if (isSelected) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(preset.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = preset.titleVi,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                color = preset.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = preset.subtitleVi,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BackgroundPresetOptionCard(
    preset: AppBackgroundPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) MintLight else CardWhite)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ForestGreen else CardBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
            .testTag("bg_preset_${preset.id}")
    ) {
        Column {
            // Wallpaper Thumbnail Preview with Glassmorphic Pill Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            ) {
                if (preset.drawableRes != null) {
                    Image(
                        painter = painterResource(id = preset.drawableRes),
                        contentDescription = preset.titleVi,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(preset.previewColors))
                    )
                }

                // Mini frosted glass card inside preview
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.78f))
                        .border(0.8.dp, Color.White, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Kính mờ ${(preset.glassOverlayAlpha * 100).toInt()}%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (isSelected) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(ForestGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = preset.titleVi,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = preset.subtitleVi,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
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
                if (isGranted) SuccessGreen.copy(alpha = 0.5f) else DangerCrimson.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
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
                        .padding(end = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isGranted) SuccessGreen.copy(alpha = 0.15f)
                                else DangerCrimson.copy(alpha = 0.15f)
                            )
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Shield,
                            contentDescription = "Status",
                            tint = if (isGranted) SuccessGreen else DangerCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isGranted) SuccessGreen.copy(alpha = 0.15f)
                            else DangerCrimson.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isGranted) "ĐÃ CẤP" else "CHƯA CẤP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) SuccessGreen else DangerCrimson,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
