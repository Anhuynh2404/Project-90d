package com.example

import android.app.Application
import com.example.data.local.database.AppDatabase
import com.example.data.repository.HabitRepository
import com.example.service.PersistentHabitNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class Project90Application : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        HabitRepository(
            userDao = database.userDao(),
            habitDao = database.habitDao(),
            dailyLogDao = database.dailyLogDao(),
            financeDao = database.financeDao(),
            journalDao = database.journalDao(),
            timeAllocationDao = database.timeAllocationDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            repository.initializeDefaultsIfNeeded()
            // Start persistent sticky habit & pedometer notification service
            try {
                PersistentHabitNotificationService.startOrUpdate(this@Project90Application)
            } catch (_: Exception) {}
        }
    }
}
