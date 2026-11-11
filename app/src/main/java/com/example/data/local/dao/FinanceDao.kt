package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.FinanceTransactionEntity
import com.example.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // 1. Transactions (Thu / Chi)
    @Query("SELECT * FROM finance_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<FinanceTransactionEntity>>

    @Query("SELECT * FROM finance_transactions WHERE dateEpochDay = :dateEpochDay ORDER BY timestamp DESC")
    fun getTransactionsForDate(dateEpochDay: Long): Flow<List<FinanceTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinanceTransactionEntity): Long

    @Query("DELETE FROM finance_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("SELECT SUM(amount) FROM finance_transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Long?>

    @Query("SELECT SUM(amount) FROM finance_transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Long?>

    @Query("SELECT SUM(savingsAllocated) FROM finance_transactions WHERE type = 'INCOME'")
    fun getTotalSavingsAllocated(): Flow<Long?>

    // 2. Debts (Cho vay / Đi mượn)
    @Query("SELECT * FROM finance_debts ORDER BY isSettled ASC, dueDateEpochDay ASC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Query("UPDATE finance_debts SET isSettled = :isSettled, paidAmount = :paidAmount WHERE id = :id")
    suspend fun updateDebtStatus(id: Long, isSettled: Boolean, paidAmount: Long)

    @Query("DELETE FROM finance_debts WHERE id = :id")
    suspend fun deleteDebt(id: Long)

    // 3. Savings Goals (Quỹ tiết kiệm mục tiêu)
    @Query("SELECT * FROM savings_goals ORDER BY createdAt ASC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT COUNT(*) FROM savings_goals")
    suspend fun getSavingsGoalCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long

    @Query("UPDATE savings_goals SET currentAmount = currentAmount + :amountDelta WHERE id = :goalId")
    suspend fun addAmountToSavingsGoal(goalId: Long, amountDelta: Long)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteSavingsGoal(id: Long)

    @Query("SELECT SUM(currentAmount) FROM savings_goals")
    fun getTotalGoalsSavedAmount(): Flow<Long?>
}
