package com.example.data.repository

import com.example.data.local.BudgetGoalDao
import com.example.data.local.FinancialAccountDao
import com.example.data.local.TransactionDao
import com.example.data.model.BudgetGoal
import com.example.data.model.FinancialAccount
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetGoalDao: BudgetGoalDao,
    private val financialAccountDao: FinancialAccountDao
) {
    val allTransactionsFlow: Flow<List<Transaction>> = transactionDao.getAllTransactionsFlow()
    val allGoalsFlow: Flow<List<BudgetGoal>> = budgetGoalDao.getAllGoalsFlow()
    val allAccountsFlow: Flow<List<FinancialAccount>> = financialAccountDao.getAllAccountsFlow()

    suspend fun getAllTransactions(): List<Transaction> = transactionDao.getAllTransactions()
    suspend fun getAllGoals(): List<BudgetGoal> = budgetGoalDao.getAllGoals()
    suspend fun getAllAccounts(): List<FinancialAccount> = financialAccountDao.getAllAccounts()

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
        // Update account balance
        val delta = if (transaction.isIncome) transaction.amount else -transaction.amount
        financialAccountDao.updateBalance(transaction.accountName, delta)
    }

    suspend fun updateTransaction(transaction: Transaction, oldTransaction: Transaction) {
        // Rollback old transaction on account
        val rollbackDelta = if (oldTransaction.isIncome) -oldTransaction.amount else oldTransaction.amount
        financialAccountDao.updateBalance(oldTransaction.accountName, rollbackDelta)

        // Apply new transaction on account
        transactionDao.insertTransaction(transaction)
        val newDelta = if (transaction.isIncome) transaction.amount else -transaction.amount
        financialAccountDao.updateBalance(transaction.accountName, newDelta)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
        // Rollback current amount from account
        val rollbackDelta = if (transaction.isIncome) -transaction.amount else transaction.amount
        financialAccountDao.updateBalance(transaction.accountName, rollbackDelta)
    }

    suspend fun addGoal(goal: BudgetGoal) = budgetGoalDao.insertGoal(goal)
    suspend fun updateGoal(goal: BudgetGoal) = budgetGoalDao.insertGoal(goal)
    suspend fun deleteGoal(goal: BudgetGoal) = budgetGoalDao.deleteGoal(goal)
    suspend fun deleteGoalById(id: Int) = budgetGoalDao.deleteById(id)

    suspend fun addAccount(account: FinancialAccount) = financialAccountDao.insertAccount(account)

    suspend fun seedMockDataIfEmpty() {
        val existingAccounts = financialAccountDao.getAllAccounts()
        if (existingAccounts.isEmpty()) {
            // Seed 5 accounts
            val defaultAccounts = listOf(
                FinancialAccount("Savings Account", 25000.0, "BANK"),
                FinancialAccount("Current Account", 12000.0, "BANK"),
                FinancialAccount("Cash", 3000.0, "CASH"),
                FinancialAccount("Credit Card", 15000.0, "CREDIT"), // e.g. credit left
                FinancialAccount("UPI Wallet", 4500.0, "WALLET")
            )
            financialAccountDao.insertAll(defaultAccounts)

            // Seed budget goals
            val defaultGoals = listOf(
                BudgetGoal(0, "New Bike", 80000.0, 35000.0, "Others"),
                BudgetGoal(0, "Laptop for Work", 65000.0, 48000.0, "Education"),
                BudgetGoal(0, "Bali Vacation", 50000.0, 15000.0, "Travel"),
                BudgetGoal(0, "Emergency Fund", 100000.0, 45000.0, "Others")
            )
            for (g in defaultGoals) {
                budgetGoalDao.insertGoal(g)
            }

            // Seed historical transactions of the last 30 days
            val calendar = Calendar.getInstance()
            val nowTime = calendar.timeInMillis

            // We generate 12 detailed mock transactions
            val mockTransactions = listOf(
                // Month ago salary
                Transaction(0, "Monthly Salary", 45000.0, nowTime - 25 * 24 * 3600 * 1000L, "Others", "Savings Account", true, "Primary income payroll"),
                Transaction(0, "Freelance Design", 12000.0, nowTime - 18 * 24 * 3600 * 1000L, "Others", "Savings Account", true, "Website redesign UI payout"),
                
                // Expenses
                Transaction(0, "Zomato dinner delivery", 650.0, nowTime - 2 * 24 * 3600 * 1000L, "Food", "UPI Wallet", false, "Burgers & Fries"),
                Transaction(0, "Grocery supermarket", 1850.0, nowTime - 3 * 24 * 3600 * 1000L, "Food", "Savings Account", false, "Weekly milk and vegetables"),
                Transaction(0, "Netflix Premium", 649.0, nowTime - 5 * 24 * 3600 * 1000L, "Entertainment", "Credit Card", false, "Auto debit subscription"),
                Transaction(0, "Uber office commute", 350.0, nowTime - 4 * 24 * 3600 * 1000L, "Travel", "UPI Wallet", false, "AC Cab ride to HSR layout"),
                Transaction(0, "Zara retail jeans", 2500.0, nowTime - 8 * 24 * 3600 * 1000L, "Shopping", "Credit Card", false, "Bought slim fit denims"),
                Transaction(0, "Medical pharmacy", 550.0, nowTime - 12 * 24 * 3600 * 1000L, "Healthcare", "Cash", false, "Multi-vitamins and cough syrup"),
                Transaction(0, "Udemy Kotlin Course", 450.0, nowTime - 15 * 24 * 3600 * 1000L, "Education", "Credit Card", false, "Complete Jetpack Compose bootcamp"),
                Transaction(0, "Electricity bill auto", 2100.0, nowTime - 20 * 24 * 3600 * 1000L, "Bills", "Savings Account", false, "May BESCOM power bill"),
                Transaction(0, "Starbucks Brewed Coffee", 280.0, nowTime - 1 * 24 * 3600 * 1000L, "Food", "UPI Wallet", false, "Frappuccino with espresso shot"),
                Transaction(0, "Fuel tank refill", 1500.0, nowTime - 6 * 24 * 3600 * 1000L, "Travel", "Cash", false, "Petrol for the scooter")
            )

            // Insert without modifying account balances directly because the starting balance above is already net-real values!
            for (tx in mockTransactions) {
                transactionDao.insertTransaction(tx)
            }
        }
    }
}
