package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HabitCategory(val displayName: String, val iconKey: String) {
    MOVEMENT("Vận Động", "fitness"),
    NUTRITION("Dinh Dưỡng", "nutrition"),
    DIGITAL_WELLBEING("Detox & Giấc Ngủ", "bedtime"),
    FINANCE("Quản Lý Tài Chính", "wallet"),
    SELF_GROWTH("Phát Triển Bản Thân", "book")
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // from HabitCategory name
    val isWeekly: Boolean = false,
    val targetValue: Int = 1,
    val unit: String = "",
    val description: String = "",
    val displayOrder: Int = 0
)
