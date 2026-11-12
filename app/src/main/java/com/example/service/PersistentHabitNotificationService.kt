package com.example.service

import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.Project90Application
import com.example.sensor.StepCounterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * PHẦN 1: Foreground Service hoạt động bền bỉ 24/7 cho Pedometer (Sensor.TYPE_STEP_COUNTER)
 * và bảng thông báo Dashboard kỷ luật.
 * - Hỗ trợ FOREGROUND_SERVICE_TYPE_HEALTH (API 34+) kết hợp FOREGROUND_SERVICE_TYPE_SPECIAL_USE
 * - Tự động lưu số bước chân vào Room Database mỗi khi cảm biến thay đổi
 * - Đầy đủ try-catch an toàn tuyệt đối trên máy thật (Android 8 -> Android 16)
 */
class PersistentHabitNotificationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var stepCounterManager: StepCounterManager? = null

    companion object {
        @Volatile
        var isServiceRunning = false
            private set

        @Volatile
        private var activeServiceInstance: PersistentHabitNotificationService? = null

        fun startOrUpdate(context: Context) {
            val appContext = context.applicationContext ?: context
            val intent = Intent(appContext, PersistentHabitNotificationService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(intent)
                } else {
                    appContext.startService(intent)
                }
            } catch (_: Exception) {
                try {
                    appContext.startService(intent)
                } catch (_: Exception) {}
            }

            HabitNotificationHelper.postOrUpdateNotification(appContext)
        }

        fun reconnectPedometerIfRunning(context: Context) {
            try {
                activeServiceInstance?.stepCounterManager?.refreshPermissionAndReconnect()
                startOrUpdate(context)
            } catch (_: Exception) {}
        }

        fun updateIfRunning(notification: Notification) {
            val service = activeServiceInstance
            if (service != null && isServiceRunning) {
                service.applyForegroundNotification(notification)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isServiceRunning = true
        activeServiceInstance = this

        try {
            HabitNotificationHelper.createNotificationChannel(this)
        } catch (_: Exception) {}

        // Khởi tạo và lắng nghe cảm biến phần cứng Sensor.TYPE_STEP_COUNTER
        val pedometer = StepCounterManager(this)
        stepCounterManager = pedometer
        pedometer.startListening()

        try {
            val initialNotif = HabitNotificationHelper.buildDashboardNotification(
                context = this,
                dayNumber = 1,
                totalDays = 90,
                daysRemainingToFeb = 80,
                currentStreak = 0,
                completedCount = 0,
                totalCount = 12,
                stepCount = pedometer.stepCount.value,
                waterMl = 0,
                pushupDone = false,
                squatDone = false,
                noSugarDone = false,
                pendingHabitsText = "Đang đồng bộ cảm biến bước chân & bảng kỷ luật 24/7...",
                isCompletedAll = false
            )
            applyForegroundNotification(initialNotif)
            HabitNotificationHelper.postOrUpdateNotification(this)
        } catch (_: Exception) {}

        // Lắng nghe số bước chân thay đổi và lưu ngay vào Room Database để không mất dữ liệu khi app bị kill
        serviceScope.launch {
            try {
                val app = applicationContext as? Project90Application ?: return@launch
                val repo = app.repository
                pedometer.stepCount.collect { steps ->
                    if (steps >= 0) {
                        try {
                            val todayEpochDay = LocalDate.now().toEpochDay()
                            if (steps > 0) {
                                repo.updateSteps(todayEpochDay, steps)
                            }
                            HabitNotificationHelper.postOrUpdateNotification(this@PersistentHabitNotificationService)
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
        }

        // Theo dõi dữ liệu Room Database để cập nhật thông báo ghim 24/7
        serviceScope.launch {
            try {
                val app = applicationContext as? Project90Application ?: return@launch
                val repo = app.repository
                val todayEpochDay = LocalDate.now().toEpochDay()

                combine(
                    repo.userFlow,
                    repo.allHabitsFlow,
                    repo.getLogsForDate(todayEpochDay)
                ) { _, _, _ ->
                    HabitNotificationHelper.postOrUpdateNotification(this@PersistentHabitNotificationService)
                }.collect {}
            } catch (_: Exception) {}
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceRunning = true
        activeServiceInstance = this
        try {
            if (stepCounterManager == null) {
                stepCounterManager = StepCounterManager(this)
            }
            stepCounterManager?.refreshPermissionAndReconnect()
            HabitNotificationHelper.postOrUpdateNotification(this)
        } catch (_: Exception) {}
        return START_STICKY
    }

    fun applyForegroundNotification(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val hasActivityRec = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED

                val primaryType = if (hasActivityRec) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH or ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                }

                try {
                    ServiceCompat.startForeground(
                        this,
                        HabitNotificationHelper.NOTIFICATION_ID,
                        notification,
                        primaryType
                    )
                } catch (_: Exception) {
                    // Fallback an toàn về SPECIAL_USE nếu thiết bị OEM chặn HEALTH FGS khi chưa đủ điều kiện
                    ServiceCompat.startForeground(
                        this,
                        HabitNotificationHelper.NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    HabitNotificationHelper.NOTIFICATION_ID,
                    notification,
                    0
                )
            } else {
                startForeground(HabitNotificationHelper.NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {
            try {
                startForeground(HabitNotificationHelper.NOTIFICATION_ID, notification)
            } catch (_: Exception) {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            stepCounterManager?.stopListening()
        } catch (_: Exception) {}
        isServiceRunning = false
        activeServiceInstance = null
        serviceScope.cancel()

        val restartIntent = Intent(applicationContext, PersistentHabitNotificationService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(restartIntent)
            } else {
                applicationContext.startService(restartIntent)
            }
        } catch (_: Exception) {
            try {
                applicationContext.startService(restartIntent)
            } catch (_: Exception) {}
        }
        try {
            HabitNotificationHelper.postOrUpdateNotification(applicationContext)
        } catch (_: Exception) {}
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val restartIntent = Intent(applicationContext, PersistentHabitNotificationService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(restartIntent)
            } else {
                applicationContext.startService(restartIntent)
            }
        } catch (_: Exception) {
            try {
                applicationContext.startService(restartIntent)
            } catch (_: Exception) {}
        }
        try {
            HabitNotificationHelper.postOrUpdateNotification(applicationContext)
        } catch (_: Exception) {}
    }
}
