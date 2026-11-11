package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "finance_debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String, // "LEND" (Cho vay) or "BORROW" (Đi mượn)
    val personName: String, // Người liên quan
    val amount: Long, // Tổng số tiền (VND)
    val paidAmount: Long = 0L, // Số tiền đã thanh toán (VND)
    val dueDateEpochDay: Long, // Hạn trả (Epoch Day)
    val createdDateEpochDay: Long, // Ngày ghi nhận
    val isSettled: Boolean = false, // Trạng thái tất toán
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
