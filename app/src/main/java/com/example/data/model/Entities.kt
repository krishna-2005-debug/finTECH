package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val timestamp: Long,
    val category: String, // Food, Shopping, Travel, Entertainment, Healthcare, Bills, Education, Others
    val accountName: String, // Cash, Savings Account, Current Account, Credit Card, UPI Wallet
    val isIncome: Boolean,
    val note: String = ""
) : Serializable

@Entity(tableName = "budget_goals")
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val category: String = "General"
) : Serializable

@Entity(tableName = "financial_accounts")
data class FinancialAccount(
    @PrimaryKey val name: String, // "Cash", "Savings Account", "Current Account", "Credit Card", "UPI Wallet"
    val balance: Double,
    val accountType: String // e.g. "CASH", "BANK", "CREDIT", "WALLET"
) : Serializable
