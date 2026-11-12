package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.Project90Application
import com.example.R
import com.example.sensor.StepCounterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

/**
 * PHẦN 5: Nâng cấp Giao diện Thanh Thông Báo Đẩy (Push Notification UI)
 * - Hỗ trợ cả Thông báo Foreground Service Đếm bước chân 24/7 (RemoteViews bo góc hiện đại + BigTextStyle)
 *   VÀ Thông báo Đồng hồ Pomodoro Khóa tập trung (ProgressBar đếm ngược trực tiếp + BigTextStyle).
 * - Tích hợp các nút bấm nhanh (Action Buttons): "💧 Nạp 250ml nước" và "👟 +500 bước" ngay trên thanh thông báo.
 */
object HabitNotificationHelper {

    const val CHANNEL_ID = "project90_dashboard_fixed_channel"
    const val CHANNEL_NAME = "Project 90 Ongoing Habit & Pedometer 24/7"
    const val NOTIFICATION_ID = 9001
    const val POMODORO_NOTIFICATION_ID = 9002

    @Volatile
    private var activePomodoroText: String? = null

    fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Bảng Dashboard theo dõi bước chân 24/7, đồng hồ Pomodoro, nước uống và kỷ luật 90 ngày"
                    setShowBadge(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    enableVibration(false)
                    enableLights(false)
                }

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.createNotificationChannel(channel)
            }
        } catch (_: Exception) {}
    }

    /**
     * PHẦN 5: Cập nhật thông báo đếm ngược trực tiếp của Đồng hồ Pomodoro (Khóa tập trung)
     */
    fun postOrCancelPomodoroNotification(
        context: Context,
        isRunning: Boolean,
        remainingSeconds: Int,
        durationMinutes: Int,
        completedSessions: Int
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) return
            }

            val manager = NotificationManagerCompat.from(context)
            if (!isRunning) {
                activePomodoroText = null
                manager.cancel(POMODORO_NOTIFICATION_ID)
                postOrUpdateNotification(context)
                return
            }

            createNotificationChannel(context)
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            val formattedTime = String.format(Locale.US, "%02d:%02d", mins, secs)
            activePomodoroText = "⏱️ $formattedTime"

            val totalSecs = (durationMinutes * 60).coerceAtLeast(1)
            val elapsedSecs = (totalSecs - remainingSeconds).coerceIn(0, totalSecs)

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                201,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val addWaterIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_ADD_WATER
            }
            val addWaterPendingIntent = PendingIntent.getBroadcast(
                context,
                202,
                addWaterIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_flame)
                .setContentTitle("⏱️ Khóa Tập Trung Pomodoro • Còn $formattedTime")
                .setContentText("Đang bật Khiên Chặn Thông Báo • Đã hoàn tất $completedSessions phiên sâu")
                .setSubText("Deep Work Mode")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle("⏱️ ĐỒNG HỒ POMODORO • CÒN $formattedTime")
                        .bigText(
                            "🛡️ Khiên tập trung đang kích hoạt (Phiên ${durationMinutes} phút).\n" +
                                    "🔥 Số phiên hoàn thành hôm nay: $completedSessions phiên.\n" +
                                    "💧 Đừng quên bổ sung nước trong lúc làm việc sâu!"
                        )
                )
                .setProgress(totalSecs, elapsedSecs, false)
                .addAction(R.drawable.ic_stat_flame, "💧 Nạp 250ml nước", addWaterPendingIntent)
                .setContentIntent(openAppPendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .build()

            manager.notify(POMODORO_NOTIFICATION_ID, notif)
        } catch (_: Exception) {}
    }

    fun buildDashboardNotification(
        context: Context,
        dayNumber: Int,
        totalDays: Int,
        daysRemainingToFeb: Long,
        currentStreak: Int,
        completedCount: Int,
        totalCount: Int,
        stepCount: Int,
        waterMl: Int,
        pushupDone: Boolean,
        squatDone: Boolean,
        noSugarDone: Boolean,
        pendingHabitsText: String,
        isCompletedAll: Boolean
    ): Notification {
        createNotificationChannel(context)

        val percent = if (totalCount > 0) ((completedCount.toFloat() / totalCount.toFloat()) * 100).toInt().coerceIn(0, 100) else 0
        val numberFormat = NumberFormat.getInstance(Locale("vi", "VN"))

        // Open App PendingIntent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick Action 1: Nạp 250ml nước
        val addWaterIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_ADD_WATER
        }
        val addWaterPendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            addWaterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Quick Action 2: Cộng nhanh +500 bước
        val addStepsIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_ADD_STEPS
        }
        val addStepsPendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            addStepsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tự động ghim lại nếu bị xóa
        val deleteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_REPOST
        }
        val deletePendingIntent = PendingIntent.getBroadcast(
            context,
            103,
            deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val collapsedTitle = if (dayNumber == 0) {
            "🌿 PROJECT 90 • KHỞI ĐỘNG • 👟 ${numberFormat.format(stepCount)} bước"
        } else {
            "🌿 PROJECT 90 • NGÀY $dayNumber/$totalDays • 👟 ${numberFormat.format(stepCount)}"
        }
        val expandedTitle = if (dayNumber == 0) {
            "🌿 PROJECT 90 • KHỞI ĐỘNG HÀNH TRÌNH"
        } else {
            "🌿 PROJECT 90 • NGÀY $dayNumber / $totalDays"
        }
        val countdownText = if (dayNumber == 0) {
            "Sẵn sàng bứt phá 90 ngày kỷ luật thép"
        } else {
            "Còn $daysRemainingToFeb ngày • Tiến độ hôm nay: $percent%"
        }

        val fallbackTitle = if (isCompletedAll) {
            "🔥 Project 90 (Ngày $dayNumber/$totalDays): Hoàn tất 100% mục tiêu!"
        } else {
            "🌿 Project 90 (Ngày $dayNumber/$totalDays) • 👟 ${numberFormat.format(stepCount)} bước • 💧 ${numberFormat.format(waterMl)}ml"
        }

        val bigTextSummary = buildString {
            append("👟 Bước chân: ${numberFormat.format(stepCount)} / 10.000 bước  |  💧 Nước: ${numberFormat.format(waterMl)} / 2.000 ml\n")
            append("📊 Hoàn thành: $completedCount/$totalCount mục ($percent%) • 🔥 Chuỗi $currentStreak ngày\n")
            append(pendingHabitsText)
        }

        val pomodoroOrWorkoutLabel = activePomodoroText ?: if (pushupDone && squatDone) {
            "50/50 • 100/100 ✅"
        } else {
            "50 Hít • 100 Sq"
        }

        return try {
            // 1. Collapsed RemoteViews (Bo góc hiện đại)
            val collapsedViews = RemoteViews(context.packageName, R.layout.notification_dashboard_collapsed).apply {
                setTextViewText(R.id.notif_collapsed_title, collapsedTitle)
                setTextViewText(R.id.notif_collapsed_streak, "🔥 ${currentStreak}N")
                setProgressBar(R.id.notif_collapsed_progressbar, 100, percent, false)
                setTextViewText(R.id.notif_collapsed_progress_text, "$completedCount/$totalCount ($percent%)")
                if (isCompletedAll) {
                    setTextViewText(R.id.notif_collapsed_status, "🔥 100% Hoàn thành! 💧 ${numberFormat.format(waterMl)}ml • 👟 ${numberFormat.format(stepCount)} bước")
                    setTextColor(R.id.notif_collapsed_status, android.graphics.Color.parseColor("#68D391"))
                } else {
                    val pomoPrefix = if (activePomodoroText != null) "$activePomodoroText • " else ""
                    setTextViewText(R.id.notif_collapsed_status, "${pomoPrefix}💧 ${numberFormat.format(waterMl)}/2000ml • 👟 ${numberFormat.format(stepCount)}/10k bước")
                    setTextColor(R.id.notif_collapsed_status, android.graphics.Color.parseColor("#E2F2E8"))
                }
            }

            // 2. Expanded RemoteViews (Command Center Dashboard)
            val expandedViews = RemoteViews(context.packageName, R.layout.notification_dashboard_expanded).apply {
                setTextViewText(R.id.notif_expanded_day, expandedTitle)
                setTextViewText(R.id.notif_expanded_countdown, countdownText)
                setTextViewText(R.id.notif_expanded_streak, "🔥 $currentStreak Ngày Chuỗi")

                setTextViewText(R.id.notif_expanded_progress_label, "$completedCount / $totalCount Mục ($percent%)")
                setProgressBar(R.id.notif_expanded_progressbar, 100, percent, false)

                // 4 Mini Dashboard Tiles
                setTextViewText(R.id.notif_stat_steps, "${numberFormat.format(stepCount)} / 10k")
                setTextViewText(R.id.notif_stat_water, "${numberFormat.format(waterMl)} / 2L")
                setTextViewText(R.id.notif_stat_workout, pomodoroOrWorkoutLabel)
                setTextViewText(
                    R.id.notif_stat_sugar,
                    if (noSugarDone) "ĐÃ ĐẠT ✅" else "CHƯA ĐẠT ⚠️"
                )
                setTextColor(
                    R.id.notif_stat_sugar,
                    if (noSugarDone) android.graphics.Color.parseColor("#48BB78") else android.graphics.Color.parseColor("#FFA726")
                )

                if (isCompletedAll) {
                    setTextViewText(R.id.notif_expanded_summary_title, "🔥 XUẤT SẮC! HOÀN THÀNH 100% TOÀN BỘ MỤC TIÊU HÔM NAY")
                    setTextViewText(R.id.notif_expanded_summary_body, "Kỷ luật thép đã được giữ vững! Chuỗi $currentStreak ngày liên tiếp không một ngày lơ là.")
                    setTextColor(R.id.notif_expanded_summary_title, android.graphics.Color.parseColor("#48BB78"))
                } else {
                    val pendingCount = (totalCount - completedCount).coerceAtLeast(0)
                    setTextViewText(R.id.notif_expanded_summary_title, "⚠️ CÒN $pendingCount MỤC CHƯA HOÀN THÀNH HÔM NAY:")
                    setTextViewText(R.id.notif_expanded_summary_body, pendingHabitsText)
                    setTextColor(R.id.notif_expanded_summary_title, android.graphics.Color.parseColor("#FFA726"))
                }

                // Gắn PendingIntent cho 3 nút bấm nhanh trên RemoteViews
                setOnClickPendingIntent(R.id.notif_btn_open_app, openAppPendingIntent)
                setOnClickPendingIntent(R.id.notif_btn_add_water, addWaterPendingIntent)
                setOnClickPendingIntent(R.id.notif_btn_add_steps, addStepsPendingIntent)
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_flame)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setContentTitle(fallbackTitle)
                .setContentText("💧 ${numberFormat.format(waterMl)}/2000ml • 👟 ${numberFormat.format(stepCount)}/10.000 bước")
                .setSubText("Ngày $dayNumber/$totalDays • 🔥 $currentStreak ngày")
                .setProgress(100, percent, false)
                .setCustomContentView(collapsedViews)
                .setCustomBigContentView(expandedViews)
                .addAction(R.drawable.ic_stat_flame, "💧 Nạp 250ml nước", addWaterPendingIntent)
                .addAction(R.drawable.ic_stat_flame, "👟 +500 bước", addStepsPendingIntent)
                .setContentIntent(openAppPendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setOnlyAlertOnce(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setLocalOnly(true)
                .build()

            notification.flags = notification.flags or
                    Notification.FLAG_ONGOING_EVENT or
                    Notification.FLAG_NO_CLEAR or
                    Notification.FLAG_FOREGROUND_SERVICE

            notification
        } catch (_: Exception) {
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_flame)
                .setContentTitle(fallbackTitle)
                .setContentText("👟 ${numberFormat.format(stepCount)}/10k bước • 💧 ${numberFormat.format(waterMl)}/2000ml")
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigTextSummary))
                .setProgress(100, percent, false)
                .addAction(R.drawable.ic_stat_flame, "💧 Nạp 250ml nước", addWaterPendingIntent)
                .addAction(R.drawable.ic_stat_flame, "👟 +500 bước", addStepsPendingIntent)
                .setContentIntent(openAppPendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build()
        }
    }

    fun postOrUpdateNotification(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as? Project90Application ?: return@launch
                val repo = app.repository
                val todayEpochDay = LocalDate.now().toEpochDay()

                val user = repo.userFlow.firstOrNull()
                val habits = repo.allHabitsFlow.firstOrNull() ?: emptyList()
                val logs = repo.getLogsForDate(todayEpochDay).firstOrNull() ?: emptyList()

                val dailyHabits = habits.filter { !it.isWeekly }
                val logsMap = logs.associateBy { it.habitId }

                val pendingHabits = dailyHabits.filter { habit ->
                    val log = logsMap[habit.id]
                    log?.isCompleted != true
                }

                val prefs = context.getSharedPreferences("project90_user_prefs", Context.MODE_PRIVATE)
                val officialStartEpoch = LocalDate.of(2026, 11, 7).toEpochDay()
                val startDay = if (prefs.contains("start_date_epoch_day")) {
                    prefs.getLong("start_date_epoch_day", officialStartEpoch)
                } else {
                    user?.startDateEpochDay ?: officialStartEpoch
                }
                val diffFromStart = (todayEpochDay - startDay).toInt()
                val dayNumber = if (diffFromStart < 0) 0 else diffFromStart
                val daysRemaining = (90L - dayNumber.toLong()).coerceAtLeast(0L)

                val completedCount = dailyHabits.size - pendingHabits.size
                val currentStreak = user?.currentStreak ?: 0
                val isCompletedAll = pendingHabits.isEmpty() && dailyHabits.isNotEmpty()

                val stepLog = logsMap["pedometer_10k"]
                val persistedSensorSteps = try {
                    context.getSharedPreferences(StepCounterManager.PREFS_PEDOMETER, Context.MODE_PRIVATE)
                        .getInt(StepCounterManager.KEY_TODAY_TOTAL_STEPS, 0)
                } catch (_: Exception) {
                    0
                }
                val stepCount = maxOf(stepLog?.progressValue ?: 0, persistedSensorSteps)

                val waterLog = logsMap["water_2l"]
                val waterMl = waterLog?.progressValue ?: 0

                val pushupDone = logsMap["pushups_50"]?.isCompleted == true
                val squatDone = logsMap["squats_100"]?.isCompleted == true
                val noSugarDone = logsMap["no_sugar"]?.isCompleted == true

                val pendingHabitsText = if (pendingHabits.isNotEmpty()) {
                    val numberFormat = NumberFormat.getInstance(Locale("vi", "VN"))
                    val items = pendingHabits.take(5).joinToString("\n") { habit ->
                        val log = logsMap[habit.id]
                        val progress = log?.progressValue ?: 0
                        if (habit.targetValue > 1) {
                            "• ${habit.title}: Đã ${numberFormat.format(progress)}/${numberFormat.format(habit.targetValue)} ${habit.unit}"
                        } else {
                            "• ${habit.title}"
                        }
                    }
                    val more = if (pendingHabits.size > 5) "\n(+${pendingHabits.size - 5} mục khác)" else ""
                    "$items$more\n👉 Bấm '💧 Nạp 250ml nước' hoặc '👟 +500 bước' bên dưới!"
                } else {
                    "Toàn bộ mục tiêu trong ngày đã hoàn tất xuất sắc! Kỷ luật thép được duy trì."
                }

                val notification = buildDashboardNotification(
                    context = context,
                    dayNumber = dayNumber,
                    totalDays = 90,
                    daysRemainingToFeb = daysRemaining,
                    currentStreak = currentStreak,
                    completedCount = completedCount,
                    totalCount = dailyHabits.size,
                    stepCount = stepCount,
                    waterMl = waterMl,
                    pushupDone = pushupDone,
                    squatDone = squatDone,
                    noSugarDone = noSugarDone,
                    pendingHabitsText = pendingHabitsText,
                    isCompletedAll = isCompletedAll
                )

                if (PersistentHabitNotificationService.isServiceRunning) {
                    PersistentHabitNotificationService.updateIfRunning(notification)
                } else {
                    val intent = Intent(context, PersistentHabitNotificationService::class.java)
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                    } catch (_: Exception) {}
                }

                val manager = NotificationManagerCompat.from(context)
                manager.notify(NOTIFICATION_ID, notification)
            } catch (_: Exception) {}
        }
    }
}
