package com.example.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.example.Project90Application
import com.example.sensor.StepCounterManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

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
            val intent = Intent(context, PersistentHabitNotificationService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}

            HabitNotificationHelper.postOrUpdateNotification(context)
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
        HabitNotificationHelper.createNotificationChannel(this)

        // Start Hardware Pedometer Sensor (Sensor.TYPE_STEP_COUNTER) inside Foreground Service
        val pedometer = StepCounterManager(this)
        stepCounterManager = pedometer
        pedometer.startListening()

        val initialNotif = HabitNotificationHelper.buildDashboardNotification(
            context = this,
            dayNumber = 1,
            totalDays = 90,
            daysRemainingToFeb = 80,
            currentStreak = 0,
            completedCount = 0,
            totalCount = 12,
            stepCount = 0,
            waterMl = 0,
            pushupDone = false,
            squatDone = false,
            noSugarDone = false,
            pendingHabitsText = "Đang đồng bộ cảm biến bước chân & bảng kỷ luật 24/7...",
            isCompletedAll = false
        )
        applyForegroundNotification(initialNotif)
        HabitNotificationHelper.postOrUpdateNotification(this)

        // Observe hardware step sensor in background and persist to Room Database
        serviceScope.launch {
            val app = applicationContext as? Project90Application ?: return@launch
            val repo = app.repository
            pedometer.stepCount.collect { steps ->
                if (steps > 0) {
                    val todayEpochDay = LocalDate.now().toEpochDay()
                    repo.updateSteps(todayEpochDay, steps)
                }
            }
        }

        // Real-time observer on repository: whenever data changes, update pinned notification
        serviceScope.launch {
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
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isServiceRunning = true
        activeServiceInstance = this
        stepCounterManager?.startListening()
        HabitNotificationHelper.postOrUpdateNotification(this)
        return START_STICKY
    }

    fun applyForegroundNotification(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            ServiceCompat.startForeground(this, HabitNotificationHelper.NOTIFICATION_ID, notification, fgsType)
        } else {
            startForeground(HabitNotificationHelper.NOTIFICATION_ID, notification)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stepCounterManager?.stopListening()
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
        } catch (_: Exception) {}
        HabitNotificationHelper.postOrUpdateNotification(applicationContext)
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
        } catch (_: Exception) {}
        HabitNotificationHelper.postOrUpdateNotification(applicationContext)
    }
}
