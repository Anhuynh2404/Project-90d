package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String, // Ví dụ: "Mua xe", "Đóng học", "Quỹ khẩn cấp"
    val targetAmount: Long, // Mục tiêu tích lũy (VND)
    val currentAmount: Long = 0L, // Số tiền đã trích vào quỹ (VND)
    val categoryIcon: String = "SAVINGS", // "CAR", "SCHOOL", "HOME", "SAVINGS"
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
