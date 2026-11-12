package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * PHẦN 3: Cấu trúc lại Database Quản lý Khoản Vay / Nợ (LoanDebtEntity / DebtEntity)
 * Hỗ trợ:
 * - Loại khoản vay: Vay ngân hàng ("BANK") / Công ty tài chính ("FINANCE_COMPANY") / Bạn bè ("FRIEND")
 * - Nơi vay / Người cho vay (personName)
 * - Tổng số tiền vay (amount)
 * - Số tiền trả hàng tháng - Gốc + Lãi (monthlyInstallment)
 * - Tổng số tháng vay (totalMonths)
 * - Số tháng đã đóng (paidMonths) -> Tự động tính số tháng còn lại = totalMonths - paidMonths
 * - Ngày đến hạn trả hàng tháng (dueDayOfMonth: 1..31, ví dụ ngày 15 hàng tháng)
 */
@Entity(tableName = "finance_debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val type: String, // "BANK" (Vay ngân hàng), "FINANCE_COMPANY" (Công ty tài chính), "FRIEND" (Bạn bè), "BORROW", "LEND"
    val personName: String, // Nơi vay / Người cho vay (VD: Vietcombank, FE Credit, Home Credit, Bạn bè...)
    val amount: Long, // Tổng số tiền vay gốc (VND)
    val monthlyInstallment: Long = 0L, // Số tiền trả hàng tháng (Gốc + Lãi)
    val totalMonths: Int = 1, // Tổng số tháng vay
    val paidMonths: Int = 0, // Số tháng đã đóng
    val dueDayOfMonth: Int = 15, // Ngày đến hạn trả lãi hàng tháng (VD: ngày 15 hàng tháng)
    val paidAmount: Long = 0L, // Số tiền đã thanh toán tích lũy (VND)
    val dueDateEpochDay: Long = LocalDate.now().plusDays(15).toEpochDay(), // Hạn trả tiếp theo (Epoch Day)
    val createdDateEpochDay: Long = LocalDate.now().toEpochDay(), // Ngày ghi nhận
    val isSettled: Boolean = false, // Trạng thái tất toán
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    /** Số tháng còn lại = Tổng số tháng vay - Số tháng đã đóng */
    val remainingMonths: Int
        get() = if (isSettled) 0 else (totalMonths - paidMonths).coerceAtLeast(0)

    /** Tiến độ trả nợ (0.0f -> 1.0f) tính theo % số tháng đã đóng */
    val repaymentProgress: Float
        get() = when {
            isSettled -> 1f
            totalMonths > 0 && (monthlyInstallment > 0L || paidMonths > 0) ->
                (paidMonths.toFloat() / totalMonths.toFloat()).coerceIn(0f, 1f)
            amount > 0L ->
                (paidAmount.toFloat() / amount.toFloat()).coerceIn(0f, 1f)
            else -> 0f
        }

    /** Tổng số tiền phải trả trong suốt kỳ hạn (nếu có trả góp hàng tháng thì = monthlyInstallment * totalMonths, ngược lại = amount) */
    val totalPayableAmount: Long
        get() = if (monthlyInstallment > 0L && totalMonths > 0) {
            maxOf(amount, monthlyInstallment * totalMonths.toLong())
        } else {
            amount
        }

    /** Tổng số tiền đã đóng tính tới hiện tại */
    val calculatedPaidAmount: Long
        get() = when {
            isSettled -> totalPayableAmount
            monthlyInstallment > 0L && paidMonths > 0 ->
                (monthlyInstallment * paidMonths.toLong()).coerceAtMost(totalPayableAmount)
            else -> paidAmount.coerceAtMost(totalPayableAmount)
        }

    /** Số tiền còn nợ hiện tại */
    val remainingDebtAmount: Long
        get() = if (isSettled) 0L else (totalPayableAmount - calculatedPaidAmount).coerceAtLeast(0L)

    /** Tên hiển thị loại khoản vay bằng tiếng Việt */
    val loanTypeLabelVi: String
        get() = when (type.uppercase()) {
            "BANK", "BANK_LOAN" -> "Vay Ngân Hàng"
            "FINANCE_COMPANY", "FINANCE" -> "Công Ty Tài Chính"
            "FRIEND", "FRIEND_LOAN", "BORROW" -> "Vay Bạn Bè / Cá Nhân"
            "LEND" -> "Cho Vay"
            else -> "Khoản Vay Trả Góp"
        }

    /** Tính số ngày đếm ngược tới ngày đóng tiền hàng tháng (dueDayOfMonth) */
    fun daysUntilNextDueDate(today: LocalDate = LocalDate.now()): Long {
        if (isSettled) return 0L
        return try {
            val safeDay = dueDayOfMonth.coerceIn(1, 28)
            val thisMonthDue = today.withDayOfMonth(safeDay.coerceAtMost(today.lengthOfMonth()))
            val nextDue = if (!thisMonthDue.isBefore(today)) {
                thisMonthDue
            } else {
                val nextMonth = today.plusMonths(1)
                nextMonth.withDayOfMonth(safeDay.coerceAtMost(nextMonth.lengthOfMonth()))
            }
            ChronoUnit.DAYS.between(today, nextDue).coerceAtLeast(0L)
        } catch (_: Exception) {
            (dueDateEpochDay - today.toEpochDay()).coerceAtLeast(0L)
        }
    }
}

typealias LoanDebtEntity = DebtEntity
