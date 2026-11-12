package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PHẦN 2: Bảng quản lý Ví điện tử & Tài khoản Ngân hàng (AccountWalletEntity)
 * Hỗ trợ tự động cộng/trừ số dư khi NotificationListenerService đọc được biến động số dư
 * từ MoMo, Cake, Vietcombank, MB Bank, TNEX, TPBank, Techcombank, ZaloPay...
 */
@Entity(tableName = "account_wallets")
data class AccountWalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String, // VD: "MoMo", "Cake", "Vietcombank", "MB Bank", "TNEX", "TPBank", "Tiền mặt"
    val balance: Long, // Số dư hiện tại (VND)
    val iconType: String = "BANK", // "MOMO", "CAKE", "VCB", "MB", "TNEX", "TPB", "TCB", "ZALOPAY", "CASH", "BANK"
    val packageKeywords: String = "", // Từ khóa nhận diện thông báo tự động
    val updatedAt: Long = System.currentTimeMillis()
)
