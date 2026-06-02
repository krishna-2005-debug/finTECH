package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.FinanceRepository
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // 1. Initialize local SQLite Room Database instance
    val database = AppDatabase.getDatabase(applicationContext)

    // 2. Initialize Finance Repository holding transactional schemas
    val repository = FinanceRepository(
      database.transactionDao(),
      database.budgetGoalDao(),
      database.financialAccountDao()
    )

    // 3. Instantiate state-tracking Finance ViewModel with dynamic factory
    val factory = FinanceViewModelFactory(application, repository)
    val viewModel = ViewModelProvider(this, factory)[FinanceViewModel::class.java]

    setContent {
      MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
        MainAppScreen(viewModel)
      }
    }
  }
}

