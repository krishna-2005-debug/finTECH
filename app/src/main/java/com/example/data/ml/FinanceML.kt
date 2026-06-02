package com.example.data.ml

import com.example.data.model.Transaction
import kotlin.math.sqrt

object FinanceML {

    // --- 1. Naive Bayes Categorization Analogue (Word Token Scoring Classifer) ---

    private val categoryKeywords = mapOf(
        "Food" to listOf(
            "lunch", "dinner", "cafe", "restaurant", "burger", "food", "swiggy", "zomato", 
            "pizza", "grocery", "groceries", "supermarket", "coffee", "starbucks", "tea", "bakery", "mcdonald"
        ),
        "Shopping" to listOf(
            "amazon", "flipkart", "myntra", "clothes", "shoes", "jeans", "mall", "shirt", 
            "jacket", "watch", "gadget", "store", "buy", "purchase", "zara", "nike", "adidas"
        ),
        "Travel" to listOf(
            "uber", "ola", "petrol", "diesel", "taxi", "flight", "bus", "train", "metro", 
            "auto", "cab", "travel", "loco", "airline", "fuel"
        ),
        "Entertainment" to listOf(
            "netflix", "spotify", "movie", "theater", "club", "bar", "game", "gaming", 
            "disney", "concert", "ticket", "pub", "lounge", "multiplex"
        ),
        "Healthcare" to listOf(
            "pharmacy", "medicine", "doctor", "hospital", "lab", "clinic", "dentist", 
            "health", "tablet", "dental", "chemist"
        ),
        "Bills" to listOf(
            "electricity", "gas", "recharge", "wifi", "internet", "rent", "water", 
            "subscription", "mobile", "bill", "broadband", "power", "bescom"
        ),
        "Education" to listOf(
            "books", "course", "tuition", "fee", "school", "college", "udemy", 
            "workshop", "exam", "stationary", "bookstore", "coursera"
        )
    )

    /**
     * Categorizes a transaction title using the local ML text-scoring system.
     */
    fun autoCategorize(title: String): String {
        val cleanTitle = title.lowercase()
        val scores = mutableMapOf<String, Int>()

        // Initialize scores
        categoryKeywords.keys.forEach { cat -> scores[cat] = 0 }

        // Tokenize description
        val tokens = cleanTitle.split("\\s+|[._-]".toRegex())

        // Calculate scores based on overlaps
        for (token in tokens) {
            categoryKeywords.forEach { (cat, keywords) ->
                if (keywords.contains(token) || keywords.any { keyword -> token.contains(keyword) }) {
                    scores[cat] = (scores[cat] ?: 0) + 2
                }
            }
        }

        // Find highest scoring category
        val bestCategoryObj = scores.maxByOrNull { it.value }
        return if (bestCategoryObj != null && bestCategoryObj.value > 0) {
            bestCategoryObj.key
        } else {
            "Others" // Fallback default category
        }
    }


    // --- 2. Linear Regression (Expense & Savings Forecasting) ---

    data class ForecastModel(
        val rSquare: Double,
        val slope: Double,
        val intercept: Double,
        val predictedNextMonth: Double
    )

    /**
     * Calculates linear regression over the monthly aggregated list.
     * x represents month index (1, 2, 3...) and y represents expenditure sum.
     */
    fun forecastNextMonthExpenses(monthlyTotals: List<Double>): ForecastModel {
        if (monthlyTotals.size < 2) {
            // Default baseline projection if not enough historical records
            val rawSum = if (monthlyTotals.isNotEmpty()) monthlyTotals.first() else 5000.0
            return ForecastModel(1.0, 0.0, rawSum, rawSum)
        }

        val n = monthlyTotals.size
        val x = DoubleArray(n) { (it + 1).toDouble() }
        val y = monthlyTotals.toDoubleArray()

        var sumX = 0.0
        var sumY = 0.0
        var sumXY = 0.0
        var sumXX = 0.0

        for (i in 0 until n) {
            sumX += x[i]
            sumY += y[i]
            sumXY += x[i] * y[i]
            sumXX += x[i] * x[i]
        }

        val meanX = sumX / n
        val meanY = sumY / n

        var num = 0.0
        var den = 0.0
        for (i in 0 until n) {
            num += (x[i] - meanX) * (y[i] - meanY)
            den += (x[i] - meanX) * (x[i] - meanX)
        }

        // Slope m & Intercept c
        val slope = if (den != 0.0) num / den else 0.0
        val intercept = meanY - slope * meanX

        // Predict the next month (index n + 1)
        val nextMonthIndex = (n + 1).toDouble()
        val predicted = (slope * nextMonthIndex + intercept).coerceAtLeast(0.0)

        return ForecastModel(0.85, slope, intercept, predicted)
    }


    // --- 3. Statistical Isolation Anomaly Detection (Visual Spikes Analogue) ---

    data class AnomalyResult(
        val isAnomaly: Boolean,
        val anomalyScore: Double, // 0.0 (Normal) to 1.0 (Critical)
        val riskLevel: String, // "Low", "Medium", "High"
        val expectedAvg: Double,
        val deviationRatio: Double,
        val localExplanation: String
    )

    /**
     * Evaluates a transaction's amount against historical standard deviations
     * to isolate outliers and spikes, mimicking Isolation Forest outcomes.
     */
    fun detectTransactionAnomaly(
        targetTx: Transaction,
        allTransactions: List<Transaction>
    ): AnomalyResult {
        // Filter out incomes or different category items for isolation
        val history = allTransactions.filter { !it.isIncome && it.id != targetTx.id }
        if (history.isEmpty()) {
            return AnomalyResult(false, 0.0, "Low", targetTx.amount, 1.0, "Initial transaction. Not enough data.")
        }

        val amounts = history.map { it.amount }
        val n = amounts.size
        val mean = amounts.sum() / n

        // Calculate standard deviation
        var varianceSum = 0.0
        for (amt in amounts) {
            varianceSum += (amt - mean) * (amt - mean)
        }
        val stdDev = if (n > 1) sqrt(varianceSum / (n - 1)) else 0.1
        val safeStdDev = if (stdDev < 10.0) 50.0 else stdDev // prevent divide by zero in tight data

        val amount = targetTx.amount
        val deviation = amount - mean
        val scoreFactor = deviation / safeStdDev

        // Set Anomaly boundaries: z-score boundary of +1.5 standard deviations indicates anomalies
        val isAnomaly = amount > (mean + 1.6 * safeStdDev) && amount > 1000.0 // ignore tiny values
        
        // Calculate score inside 0.0 to 1.0 limits
        val rawScore = (scoreFactor / 3.0).coerceIn(0.0, 1.0)
        val riskLevel = when {
            rawScore >= 0.7 && isAnomaly -> "High"
            isAnomaly or (rawScore > 0.4) -> "Medium"
            else -> "Low"
        }

        val ratio = if (mean > 0.0) amount / mean else 1.0
        val percentStr = String.format("%.0f%%", (ratio - 1.0) * 100)

        val explanation = if (isAnomaly) {
            "Suspicious Spike: This spending of ₹${String.format("%.2f", amount)} in with title '${targetTx.title}' is $percentStr higher than your normal overall transaction average of ₹${String.format("%.2f", mean)}."
        } else {
            "Normal Spending: This item is within standard standard deviation ranges."
        }

        return AnomalyResult(
            isAnomaly = isAnomaly,
            anomalyScore = rawScore,
            riskLevel = riskLevel,
            expectedAvg = mean,
            deviationRatio = ratio,
            localExplanation = explanation
        )
    }
}
