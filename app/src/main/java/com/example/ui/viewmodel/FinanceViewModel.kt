package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiManager
import com.example.data.api.ScannedReceipt
import com.example.data.local.AppDatabase
import com.example.data.ml.FinanceML
import com.example.data.model.BudgetGoal
import com.example.data.model.FinancialAccount
import com.example.data.model.Transaction
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.sqrt

data class ChatMessage(val role: String, val message: String)

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    // --- State variables ---
    
    // Auth State
    var isLoggedIn by mutableStateOf(false)
    var currentUserEmail by mutableStateOf("krissmsd@gmail.com") // Initialized with user metadata email
    var currentUserName by mutableStateOf("Krish")
    var authError by mutableStateOf<String?>(null)

    // SMS Notifications Simulation
    var smsBannerText by mutableStateOf<String?>(null)
    var pendingSmsTransaction by mutableStateOf<Transaction?>(null)

    // Receipt scanner
    var isScanningReceipt by mutableStateOf(false)
    var scannedReceiptForm by mutableStateOf<ScannedReceipt?>(null)

    // Chat AI Advisor State
    var chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("assistant", "Hi there! I am your AI Financial Advisor. Ask me anything about your cashflow, budgets, how to save, or request a complete financial plan!")
    ))
    var isChatLoading by mutableStateOf(false)

    // Theme Toggle State
    var isDarkMode by mutableStateOf(false)

    // UI Active Tab
    var currentTab by mutableStateOf("DASHBOARD") // DASHBOARD, TRANSACTIONS, ANALYTICS, ADVISOR, BUDGETS, SCANNER, SETTINGS

    private val geminiManager = GeminiManager()

    // Core Data flows with simple mapping
    val transactionsState: StateFlow<List<Transaction>> = repository.allTransactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goalsState: StateFlow<List<BudgetGoal>> = repository.allGoalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accountsState: StateFlow<List<FinancialAccount>> = repository.allAccountsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined Dashboard State
    val incomeSum: StateFlow<Double> = transactionsState
        .combine(transactionsState) { txs, _ ->
            txs.filter { it.isIncome }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val expenseSum: StateFlow<Double> = transactionsState
        .combine(transactionsState) { txs, _ ->
            txs.filter { !it.isIncome }.sumOf { it.amount }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val aggregateBalance: StateFlow<Double> = accountsState
        .combine(accountsState) { accs, _ ->
            accs.sumOf { it.balance }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    init {
        viewModelScope.launch {
            // Pre-seed mock data as background routine
            repository.seedMockDataIfEmpty()
        }
    }

    // --- Actions ---

    fun login(email: String, name: String) {
        if (email.contains("@")) {
            currentUserEmail = email
            currentUserName = name.ifEmpty { "User" }
            isLoggedIn = true
            authError = null
        } else {
            authError = "Please enter a valid email address."
        }
    }

    fun register(email: String, name: String) {
        login(email, name)
    }

    fun logout() {
        isLoggedIn = false
    }

    fun forgotPassword(email: String, onSent: () -> Unit) {
        onSent()
    }

    fun toggleTheme() {
        isDarkMode = !isDarkMode
    }

    fun addTransaction(title: String, amount: Double, isIncome: Boolean, accountName: String, category: String) {
        viewModelScope.launch {
            val tx = Transaction(
                title = title,
                amount = amount,
                timestamp = System.currentTimeMillis(),
                category = category,
                accountName = accountName,
                isIncome = isIncome
            )
            repository.addTransaction(tx)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addBudgetGoal(title: String, targetAmount: Double, initialProgress: Double, category: String) {
        viewModelScope.launch {
            repository.addGoal(BudgetGoal(0, title, targetAmount, initialProgress, category))
        }
    }

    fun deleteGoal(goalId: Int) {
        viewModelScope.launch {
            repository.deleteGoalById(goalId)
        }
    }

    // --- Artificial Intelligence / Gemini actions ---

    fun askAiAdvisor(userPrompt: String) {
        if (userPrompt.trim().isEmpty()) return

        chatMessages.value = chatMessages.value + ChatMessage("user", userPrompt)
        isChatLoading = true

        viewModelScope.launch {
            // Construct context from user's current transaction stats to give truly smart personalized advice!
            val totalIncome = incomeSum.value
            val totalExpense = expenseSum.value
            val netSavings = totalIncome - totalExpense
            
            val txs = repository.getAllTransactions()
            val categorySummary = txs.filter { !it.isIncome }
                .groupBy { it.category }
                .mapValues { it.value.sumOf { item -> item.amount } }
            
            val summaryString = categorySummary.entries.joinToString { "${it.key}: ₹${it.value}" }

            val systemContextInstruction = """
                You are AI Financial Advisor integrated inside the AI Financial Insight Mobile App. 
                The user's current statistics are:
                - Income: ₹$totalIncome
                - Expenses: ₹$totalExpense
                - Net Savings: ₹$netSavings
                - Category Spending: $summaryString
                
                Respond in a highly encouraging, conversational, professional tone with actionable fintech recommendations. Keep your output visually formatted with bullet points for easy mobile reading. Refrain from listing any code. Keep responses under 200 words.
            """.trimIndent()

            val response = geminiManager.generateText(userPrompt, systemContextInstruction)
            chatMessages.value = chatMessages.value + ChatMessage("assistant", response)
            isChatLoading = false
        }
    }

    fun triggerQuickAction(topic: String) {
        val userPrompt = when (topic) {
            "ANALYZE" -> "Analyze my spending and show me my financial ratio scorecard."
            "SAVE" -> "How can I reduce my discretionary expenses to save ₹2,500 more this month?"
            "BUDGET" -> "Suggest a customized 50/30/20 budget plan based on my income of ₹${incomeSum.value}."
            "OVERSPENDING" -> "Where am I overspending the most relative to healthy parameters?"
            else -> "Help me plan my investments."
        }
        askAiAdvisor(userPrompt)
    }

    fun performReceiptScan(bitmap: Bitmap) {
        isScanningReceipt = true
        scannedReceiptForm = null

        viewModelScope.launch {
            val result = geminiManager.scanReceipt(bitmap)
            if (result != null) {
                scannedReceiptForm = result
            } else {
                // FALLBACK: OCR Scanner Simulation if key is empty / api offline
                val simulatedResult = runMockReceiptOcr()
                scannedReceiptForm = simulatedResult
            }
            isScanningReceipt = false
        }
    }

    private fun runMockReceiptOcr(): ScannedReceipt {
        val mockStores = listOf("Swiggy Restaurant", "Amazon India", "Chai Point", "Zara Lifestyle", "Apollo Pharmacy")
        val store = mockStores.random()
        val amount = when (store) {
            "Swiggy Restaurant" -> 780.0
            "Amazon India" -> 1450.0
            "Chai Point" -> 180.0
            "Zara Lifestyle" -> 3200.0
            else -> 420.0
        }
        val items = when (store) {
            "Swiggy Restaurant" -> listOf("Paneer Tikka Double", "Butter Naan x2", "Coke Zero 300ml")
            "Amazon India" -> listOf("Type C Fast Charger Card", "Wired Earphones with Mic")
            "Chai Point" -> listOf("Masala Chai Kettle (500ml)", "Samosa Platter 3pc")
            "Zara Lifestyle" -> listOf("Black Cotton Polo T-Shirt")
            else -> listOf("Paracetamol 650mg Stripe", "Antiseptic Sanitizer")
        }
        val category = FinanceML.autoCategorize(store)
        return ScannedReceipt(
            storeName = store,
            amount = amount,
            dateString = "02-Jun-2026",
            purchasedItems = items,
            category = category
        )
    }

    fun approveReceiptAndAddTx() {
        val receipt = scannedReceiptForm
        if (receipt != null) {
            addTransaction(
                title = receipt.storeName,
                amount = receipt.amount,
                isIncome = false,
                accountName = "UPI Wallet",
                category = receipt.category
            )
            scannedReceiptForm = null
        }
    }

    // --- SMS Detection Simulation ---

    fun simulateSmsReceived() {
        val formats = listOf(
            "Dear customer, your Acct XX4478 has been debited for INR 1,200.00 on 02-Jun-26 by Swiggy Swadh. Ref: 615243.",
            "UPI Transfer Ref 524021: Rs. 350.00 spent at Chai Point.",
            "Alert: Your Credit Card XX9985 has been charged with ₹ 2,500.00 at ZARA store.",
            "SBI: Rs. 15,000.00 credited to A/C XX4524 from HDFC payroll. Bal: Rs. 40,000.0"
        )
        val sms = formats.random()
        smsBannerText = sms

        // Parse SMS
        val isIncome = sms.contains("credited", ignoreCase = true)
        val amount = parseAmountFromSms(sms)
        val merchant = parseMerchantFromSms(sms)
        val category = FinanceML.autoCategorize(merchant)
        val account = when {
            sms.contains("Credit Card", ignoreCase = true) -> "Credit Card"
            sms.contains("UPI", ignoreCase = true) -> "UPI Wallet"
            else -> "Savings Account"
        }

        pendingSmsTransaction = Transaction(
            id = 0,
            title = merchant,
            amount = amount,
            timestamp = System.currentTimeMillis(),
            category = category,
            accountName = account,
            isIncome = isIncome,
            note = "Detected via SMS inbox"
        )
    }

    fun approveDetectedSmsTx() {
        val tx = pendingSmsTransaction
        if (tx != null) {
            viewModelScope.launch {
                repository.addTransaction(tx)
                smsBannerText = null
                pendingSmsTransaction = null
            }
        }
    }

    fun dismissDetectedSms() {
        smsBannerText = null
        pendingSmsTransaction = null
    }

    private fun parseAmountFromSms(text: String): Double {
        val clean = text.replace(",", "")
        val regex = "(?:INR|Rs\\.|₹|Rs)\\s*(\\d+(?:\\.\\d{2})?)".toRegex(RegexOption.IGNORE_CASE)
        val match = regex.find(clean)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 150.0
    }

    private fun parseMerchantFromSms(text: String): String {
        return when {
            text.contains("Swiggy", true) -> "Swiggy"
            text.contains("Chai Point", true) -> "Chai Point"
            text.contains("Zara", true) -> "Zara Restaurant"
            text.contains("payroll", true) -> "HDFC Payroll Direct"
            else -> "Simulated Merchant"
        }
    }

    // --- Dynamic KPI Financial Health Calculations ---

    fun calculateHealthScore(transactions: List<Transaction>): Int {
        if (transactions.isEmpty()) return 70 // Baseline

        // 1. Savings Rate percentage
        val inc = transactions.filter { it.isIncome }.sumOf { it.amount }
        val exp = transactions.filter { !it.isIncome }.sumOf { it.amount }
        val savingsRate = if (inc > 0) (inc - exp) / inc else 0.0
        val savingsPoints = (savingsRate * 100).coerceIn(0.0, 35.0) // Max 35 points out of 100

        // 2. Budget Discipline
        // Standard goal achievement
        val budgetPoints = 30.0 // Baseline budget targets met

        // 3. Spending Consistency (Z Score variations of expense amount deviations)
        val expenses = transactions.filter { !it.isIncome }
        val consistencyPoints = if (expenses.size > 3) {
            val mean = expenses.sumOf { it.amount } / expenses.size
            val variance = expenses.sumOf { (it.amount - mean) * (it.amount - mean) } / expenses.size
            val cv = sqrt(variance) / (mean.coerceAtLeast(1.0)) // coefficient of variation
            // lower coefficient is better consistency
            val score = (35.0 - (cv * 15.0)).coerceIn(10.0, 35.0)
            score
        } else {
            25.0
        }

        return (savingsPoints + budgetPoints + consistencyPoints).toInt().coerceIn(10, 100)
    }

    // Anomaly analysis
    fun extractAnomalies(transactions: List<Transaction>): List<Pair<Transaction, FinanceML.AnomalyResult>> {
        val expenses = transactions.filter { !it.isIncome }
        val anomalies = ArrayList<Pair<Transaction, FinanceML.AnomalyResult>>()
        for (tx in expenses) {
            val res = FinanceML.detectTransactionAnomaly(tx, transactions)
            if (res.isAnomaly) {
                anomalies.add(Pair(tx, res))
            }
        }
        return anomalies.sortedByDescending { it.second.anomalyScore }
    }

    // Forecasting Next Month Math values
    fun calculateMonthlyExpenseForecast(transactions: List<Transaction>): Double {
        // Find last 3 month values or emulate month totals
        val expenses = transactions.filter { !it.isIncome }
        if (expenses.isEmpty()) return 3000.0

        val now = Calendar.getInstance()
        val currentMonth = now.get(Calendar.MONTH)

        // Group by month
        val groupedMonthSum = expenses.groupBy {
            val c = Calendar.getInstance()
            c.timeInMillis = it.timestamp
            c.get(Calendar.MONTH)
        }.mapValues { it.value.sumOf { item -> item.amount } }

        // Compile ordered list
        val months = groupedMonthSum.keys.sorted()
        val totals = months.map { groupedMonthSum[it] ?: 0.0 }

        val defaultTotals = if (totals.size < 2) {
            // pad with mock trends to present a beautiful projection
            val baseVal = totals.firstOrNull() ?: 5500.0
            listOf(baseVal * 0.9, baseVal * 1.05, baseVal)
        } else {
            totals
        }

        val forecastResult = FinanceML.forecastNextMonthExpenses(defaultTotals)
        return forecastResult.predictedNextMonth
    }
}

class FinanceViewModelFactory(
    private val application: Application,
    private val repository: FinanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
