package com.example.data.local

import androidx.room.*
import com.example.data.model.BudgetGoal
import com.example.data.model.FinancialAccount
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactions(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

@Dao
interface BudgetGoalDao {
    @Query("SELECT * FROM budget_goals")
    fun getAllGoalsFlow(): Flow<List<BudgetGoal>>

    @Query("SELECT * FROM budget_goals")
    suspend fun getAllGoals(): List<BudgetGoal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: BudgetGoal)

    @Update
    suspend fun updateGoal(goal: BudgetGoal)

    @Delete
    suspend fun deleteGoal(goal: BudgetGoal)

    @Query("DELETE FROM budget_goals WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface FinancialAccountDao {
    @Query("SELECT * FROM financial_accounts")
    fun getAllAccountsFlow(): Flow<List<FinancialAccount>>

    @Query("SELECT * FROM financial_accounts")
    suspend fun getAllAccounts(): List<FinancialAccount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: FinancialAccount)

    @Update
    suspend fun updateAccount(account: FinancialAccount)

    @Query("UPDATE financial_accounts SET balance = balance + :delta WHERE name = :name")
    suspend fun updateBalance(name: String, delta: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<FinancialAccount>)
}
