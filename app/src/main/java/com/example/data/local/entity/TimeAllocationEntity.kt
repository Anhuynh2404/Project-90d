package com.example.data.local.entity

import androidx.room.Entity

enum class TimeActivityCategory(val code: String, val labelVi: String, val recommendedHours: Float) {
    SLEEP("SLEEP", "Ngủ", 7.0f),
    WORK("WORK", "Làm việc", 8.0f),
    STUDY("STUDY", "Học tập", 3.0f),
    REST("REST", "Nghỉ ngơi", 3.5f),
    PLAY("PLAY", "Chơi & Giải trí", 2.5f)
}

@Entity(
    tableName = "time_allocations",
    primaryKeys = ["dateEpochDay", "activityType"]
)
data class TimeAllocationEntity(
    val dateEpochDay: Long,
    val activityType: String, // "SLEEP", "REST", "PLAY", "STUDY", "WORK"
    val hours: Float, // Thời lượng phân bổ (0f..24f)
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
