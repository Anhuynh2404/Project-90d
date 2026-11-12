package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.HabitNotificationHelper
import com.example.service.PersistentHabitNotificationService
import com.example.ui.screens.AlarmSettingsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DigitalWellbeingScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.MovementScreen
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.SelfGrowthScreen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.MintLight
import com.example.ui.theme.MintSurface
import com.example.ui.theme.Project90Theme
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.Project90ViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: Project90ViewModel = viewModel()
            val themePreset by viewModel.themePreset.collectAsStateWithLifecycle()
            val backgroundPreset by viewModel.backgroundPreset.collectAsStateWithLifecycle()

            Project90Theme(
                themePreset = themePreset,
                backgroundPreset = backgroundPreset
            ) {
                Project90App(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        HabitNotificationHelper.postOrUpdateNotification(this)
        PersistentHabitNotificationService.startOrUpdate(this)
    }

    override fun onPause() {
        super.onPause()
        HabitNotificationHelper.postOrUpdateNotification(this)
    }

    override fun onStop() {
        super.onStop()
        HabitNotificationHelper.postOrUpdateNotification(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        HabitNotificationHelper.postOrUpdateNotification(this)
    }
}

@Composable
fun Project90App(
    viewModel: Project90ViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val showStartDateDialog by viewModel.showStartDateDialog.collectAsStateWithLifecycle()
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()

    val appTheme = LocalAppTheme.current
    val activeTheme = appTheme.themePreset
    val activeBackground = appTheme.backgroundPreset

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var hasActivityRecognitionPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val runtimePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            result[Manifest.permission.POST_NOTIFICATIONS]?.let { hasNotificationPermission = it }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            result[Manifest.permission.ACTIVITY_RECOGNITION]?.let { granted ->
                hasActivityRecognitionPermission = granted
                if (granted) {
                    viewModel.onActivityRecognitionPermissionGranted()
                }
            }
        }
        try {
            HabitNotificationHelper.postOrUpdateNotification(context)
            PersistentHabitNotificationService.startOrUpdate(context)
        } catch (_: Exception) {}
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasActivityRecognitionPermission) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            try {
                runtimePermissionsLauncher.launch(permissionsToRequest.toTypedArray())
            } catch (_: Exception) {}
        }

        try {
            viewModel.onActivityRecognitionPermissionGranted()
            HabitNotificationHelper.postOrUpdateNotification(context)
            PersistentHabitNotificationService.startOrUpdate(context)
        } catch (_: Exception) {}
    }

    BackHandler(enabled = currentTab != AppTab.DASHBOARD) {
        viewModel.selectTab(AppTab.DASHBOARD)
    }

    // PHẦN 3: Hình nền tùy chỉnh (res/drawable hoặc Gradient) + Lớp phủ kính mờ (Glassmorphism Overlay)
    Box(modifier = Modifier.fillMaxSize()) {
        if (activeBackground.drawableRes != null) {
            Image(
                painter = painterResource(id = activeBackground.drawableRes),
                contentDescription = activeBackground.titleVi,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White,
                                activeTheme.containerLight,
                                activeTheme.borderTint
                            )
                        )
                    )
            )
        }

        // Frosted Glassmorphism Overlay ensuring high text contrast & readability across all screens
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            activeTheme.backgroundBase.copy(alpha = activeBackground.glassOverlayAlpha),
                            activeTheme.surfaceTint.copy(alpha = (activeBackground.glassOverlayAlpha + 0.06f).coerceAtMost(0.94f))
                        )
                    )
                )
        )

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            containerColor = Color.Transparent,
            bottomBar = {
                Project90BottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedVisibility(visible = !hasNotificationPermission || !hasActivityRecognitionPermission) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DangerCrimson)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Chưa cấp quyền! Chạm để bật thông báo ghim 24/7 & Đếm bước chân",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = {
                                val req = mutableListOf<String>()
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                    req.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !hasActivityRecognitionPermission) {
                                    req.add(Manifest.permission.ACTIVITY_RECOGNITION)
                                }
                                if (req.isNotEmpty()) {
                                    try {
                                        runtimePermissionsLauncher.launch(req.toTypedArray())
                                    } catch (_: Exception) {}
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Bật Ngay",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = currentTab, label = "tab_fade") { tab ->
                        when (tab) {
                            AppTab.DASHBOARD -> DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { viewModel.selectTab(it) }
                            )
                            AppTab.MOVEMENT -> MovementScreen(viewModel = viewModel)
                            AppTab.NUTRITION -> NutritionScreen(viewModel = viewModel)
                            AppTab.DIGITAL -> DigitalWellbeingScreen(viewModel = viewModel)
                            AppTab.FINANCE -> FinanceScreen(viewModel = viewModel)
                            AppTab.GROWTH -> SelfGrowthScreen(viewModel = viewModel)
                            AppTab.SETTINGS -> AlarmSettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // PHẦN 4: Màn hình Popup / Onboarding Thiết lập Ngày Bắt Đầu (Start Date Configuration)
        if (showStartDateDialog) {
            StartDateOnboardingDialog(
                currentStartEpochDay = summary.startDateEpochDay,
                todayEpochDay = viewModel.todayEpochDay,
                onConfirmStartDate = { newEpochDay ->
                    viewModel.updateStartDate(newEpochDay)
                },
                onDismiss = {
                    viewModel.dismissStartDateDialog()
                }
            )
        }
    }
}

@Composable
fun StartDateOnboardingDialog(
    currentStartEpochDay: Long,
    todayEpochDay: Long,
    onConfirmStartDate: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val initialDate = remember(currentStartEpochDay) {
        try {
            LocalDate.ofEpochDay(currentStartEpochDay)
        } catch (_: Exception) {
            LocalDate.of(2026, 11, 7)
        }
    }

    var selectedYear by remember(initialDate) { mutableIntStateOf(initialDate.year) }
    var selectedMonth by remember(initialDate) { mutableIntStateOf(initialDate.monthValue) }
    var selectedDay by remember(initialDate) { mutableIntStateOf(initialDate.dayOfMonth) }

    val candidateDate = remember(selectedYear, selectedMonth, selectedDay) {
        try {
            val maxDay = LocalDate.of(selectedYear, selectedMonth, 1).lengthOfMonth()
            LocalDate.of(selectedYear, selectedMonth, selectedDay.coerceIn(1, maxDay))
        } catch (_: Exception) {
            LocalDate.now()
        }
    }

    val candidateEpoch = candidateDate.toEpochDay()
    val previewDiffDays = (todayEpochDay - candidateEpoch).toInt()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, ForestGreen.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .testTag("start_date_onboarding_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
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
                            text = "THIẾT LẬP NGÀY BẮT ĐẦU",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = ForestGreen
                        )
                        Text(
                            text = "Thử thách 90 ngày kỷ luật thép",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Chọn nhanh mốc bắt đầu:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Preset Buttons
                val defaultNov7 = LocalDate.of(2026, 11, 7)
                val todayDate = LocalDate.ofEpochDay(todayEpochDay)
                val completedTestDate = todayDate.minusDays(95)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedYear = todayDate.year
                                selectedMonth = todayDate.monthValue
                                selectedDay = todayDate.dayOfMonth
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Hôm nay (${todayDate.format(formatter)})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                selectedYear = defaultNov7.year
                                selectedMonth = defaultNov7.monthValue
                                selectedDay = defaultNov7.dayOfMonth
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Mốc 07/11/2026",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            selectedYear = completedTestDate.year
                            selectedMonth = completedTestDate.monthValue
                            selectedDay = completedTestDate.dayOfMonth
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Thử nghiệm > 90 ngày (${completedTestDate.format(formatter)} • Hoàn thành)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Hoặc tùy chỉnh Ngày / Tháng / Năm:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Day / Month / Year Stepper Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DateStepperColumn(
                        label = "NGÀY",
                        value = candidateDate.dayOfMonth,
                        onDecrement = {
                            val maxD = LocalDate.of(selectedYear, selectedMonth, 1).lengthOfMonth()
                            selectedDay = if (selectedDay <= 1) maxD else selectedDay - 1
                        },
                        onIncrement = {
                            val maxD = LocalDate.of(selectedYear, selectedMonth, 1).lengthOfMonth()
                            selectedDay = if (selectedDay >= maxD) 1 else selectedDay + 1
                        },
                        modifier = Modifier.weight(1f)
                    )

                    DateStepperColumn(
                        label = "THÁNG",
                        value = selectedMonth,
                        onDecrement = {
                            selectedMonth = if (selectedMonth <= 1) 12 else selectedMonth - 1
                        },
                        onIncrement = {
                            selectedMonth = if (selectedMonth >= 12) 1 else selectedMonth + 1
                        },
                        modifier = Modifier.weight(1f)
                    )

                    DateStepperColumn(
                        label = "NĂM",
                        value = selectedYear,
                        onDecrement = { selectedYear -= 1 },
                        onIncrement = { selectedYear += 1 },
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Calculation Preview Box: (Ngày hiện tại - Ngày bắt đầu)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MintSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Công thức: (Ngày hiện tại - Ngày bắt đầu)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val statusText = when {
                            previewDiffDays > 90 -> "HOÀN THÀNH THỬ THÁCH ($previewDiffDays / 90 ngày)"
                            previewDiffDays < 0 -> "Chưa bắt đầu (Còn ${-previewDiffDays} ngày tới mốc)"
                            else -> "NGÀY $previewDiffDays / 90 ngày (Còn ${90 - previewDiffDays} ngày)"
                        }
                        Text(
                            text = "Kết quả: $statusText",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                            color = if (previewDiffDays > 90) SuccessGreen else ForestGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Đóng", color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirmStartDate(candidateEpoch) },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("confirm_start_date_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lưu Ngày Bắt Đầu",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateStepperColumn(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MintSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(CardWhite)
                    .border(1.dp, CardBorder, CircleShape)
                    .clickable { onDecrement() }
            ) {
                Text("-", fontWeight = FontWeight.Black, color = ForestGreen, fontSize = 14.sp)
            }
            Text(
                text = value.toString().padStart(2, '0'),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(CardWhite)
                    .border(1.dp, CardBorder, CircleShape)
                    .clickable { onIncrement() }
            ) {
                Text("+", fontWeight = FontWeight.Black, color = ForestGreen, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun Project90BottomBar(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        color = CardWhite.copy(alpha = 0.94f),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder)
            .testTag("project90_bottom_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                AppTab.DASHBOARD to ("Tổng Quan" to Icons.Default.Dashboard),
                AppTab.MOVEMENT to ("Vận Động" to Icons.Default.DirectionsRun),
                AppTab.NUTRITION to ("Dinh Dưỡng" to Icons.Default.LocalDrink),
                AppTab.DIGITAL to ("Detox" to Icons.Default.Nightlight),
                AppTab.FINANCE to ("Tài Chính" to Icons.Default.Savings),
                AppTab.GROWTH to ("Phát Triển" to Icons.Default.Book),
                AppTab.SETTINGS to ("Cài Đặt" to Icons.Default.Settings)
            )

            tabs.forEach { (tab, meta) ->
                val (label, icon) = meta
                val isSelected = currentTab == tab

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isSelected) ForestGreen else Color.Transparent)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color.White else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
