package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DailyLogDao
import com.example.data.local.dao.FinanceDao
import com.example.data.local.dao.HabitDao
import com.example.data.local.dao.JournalDao
import com.example.data.local.dao.TimeAllocationDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.AccountWalletEntity
import com.example.data.local.entity.DailyJournalEntity
import com.example.data.local.entity.DailyLogEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.FinanceTransactionEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TimeAllocationEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        HabitEntity::class,
        DailyLogEntity::class,
        FinanceTransactionEntity::class,
        DebtEntity::class,
        SavingsGoalEntity::class,
        TimeAllocationEntity::class,
        DailyJournalEntity::class,
        AccountWalletEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun habitDao(): HabitDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun financeDao(): FinanceDao
    abstract fun journalDao(): JournalDao
    abstract fun timeAllocationDao(): TimeAllocationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "project90_database.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
