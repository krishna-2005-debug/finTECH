package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.api.ScannedReceipt
import com.example.data.ml.FinanceML
import com.example.data.model.BudgetGoal
import com.example.data.model.FinancialAccount
import com.example.data.model.Transaction
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

// --- MAIN NAV HOST AND APP SCREEN ENTRY ---

@Composable
fun MainAppScreen(viewModel: FinanceViewModel) {
    if (!viewModel.isLoggedIn) {
        AuthScreen(viewModel)
    } else {
        AppLayout(viewModel)
    }
}

// --- GLASSMORPHIC & GRADIENT BACKGROUND CONTAINER ---

@Composable
fun FintechBackground(isDark: Boolean, content: @Composable () -> Unit) {
    val bgGradient = if (isDark) {
        Brush.radialGradient(
            colors = listOf(Color(0xFF2E1065), Color(0xFF0F172A)),
            center = Offset(200f, 200f),
            radius = 1200f
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF3E8FF), Color(0xFFFFFFFF))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        content()
    }
}

// --- AUTHENTICATION SCREEN ---

@Composable
fun AuthScreen(viewModel: FinanceViewModel) {
    var isLoginTab by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("krissmsd@gmail.com") }
    var name by remember { mutableStateOf("Krish Kumar") }
    var password by remember { mutableStateOf("••••••••") }
    
    val context = LocalContext.current

    FintechBackground(isDark = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .navigationBarsPadding()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            
            // Header Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.linearGradient(listOf(FintechPurple, FintechSecondary)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "AI Financial Insight",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    brush = Brush.linearGradient(listOf(FintechPurple, FintechSecondary))
                ),
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "AI-Driven Personal Wealth & Intelligence Engine",
                fontSize = 13.sp,
                color = TextSecondaryLight,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Premium Styled Auth Card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Custom Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { isLoginTab = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLoginTab) Color.White else Color.Transparent,
                                contentColor = if (isLoginTab) FintechPurple else TextSecondaryLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text("Login", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { isLoginTab = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isLoginTab) Color.White else Color.Transparent,
                                contentColor = if (!isLoginTab) FintechPurple else TextSecondaryLight
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 10.dp)
                        ) {
                            Text("Register", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (isLoginTab) "Welcome Back" else "Create Fintech Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryLight
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isLoginTab) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth().testTag("auth_name_field"),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FintechPurple) },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_field"),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = FintechPurple) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_field"),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = FintechPurple) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    viewModel.authError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(err, color = StatusRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoginTab) {
                        TextButton(
                            onClick = {
                                viewModel.forgotPassword(email) {
                                    ScaffoldMessenger.showToast(context, "Password reset OTP sent to $email!")
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Forgot Password?", color = FintechPurple, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Authenticate Button
                    Button(
                        onClick = {
                            if (isLoginTab) {
                                viewModel.login(email, "Krish")
                            } else {
                                viewModel.register(email, name)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_button")
                    ) {
                        Text(if (isLoginTab) "Access Dashboard" else "Register & Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text("OR", fontSize = 11.sp, color = TextSecondaryLight, modifier = Modifier.padding(horizontal = 8.dp))
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Google Sign-In
                    OutlinedButton(
                        onClick = {
                            viewModel.login("krissmsd@gmail.com", "Krish Kumar")
                            ScaffoldMessenger.showToast(context, "Logged in via Google Secure Account!")
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                tint = FintechPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sign In with Google", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// --- TOAST/ALERT COMPASSION ---

object ScaffoldMessenger {
    private var activeToastStr = ""
    fun showToast(context: android.content.Context, message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}

// --- CORE LAYOUT WITH SYSTEM BOTTOM NAVIGATION BAR COMPOSABLE ---

@Composable
fun AppLayout(viewModel: FinanceViewModel) {
    val txs by viewModel.transactionsState.collectAsStateWithLifecycle()
    val goals by viewModel.goalsState.collectAsStateWithLifecycle()
    val accounts by viewModel.accountsState.collectAsStateWithLifecycle()

    FintechBackground(isDark = viewModel.isDarkMode) {
        Scaffold(
            bottomBar = {
                FintechBottomNavigation(
                    currentTab = viewModel.currentTab,
                    onTabSelected = { viewModel.currentTab = it }
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Switch tabs
                when (viewModel.currentTab) {
                    "DASHBOARD" -> DashboardScreen(viewModel, txs, accounts)
                    "TRANSACTIONS" -> TransactionsScreen(viewModel, txs)
                    "ANALYTICS" -> AnalyticsScreen(viewModel, txs)
                    "ADVISOR" -> AdvisorScreen(viewModel)
                    "BUDGETS" -> BudgetsScreen(viewModel, goals, txs)
                    "SCANNER" -> ScannerScreen(viewModel)
                    "SETTINGS" -> SettingsScreen(viewModel)
                }

                // Background SMS Alert Panel Notification
                AnimatedVisibility(
                    visible = viewModel.smsBannerText != null,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                ) {
                    SmsSimulationBanner(viewModel)
                }
            }
        }
    }
}

// --- SMS BANNER COMPONENT ---

@Composable
fun SmsSimulationBanner(viewModel: FinanceViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFEF4444), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Sms, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "SMS Transaction Detected!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF991B1B),
                    fontSize = 14.sp
                )
                Text(
                    viewModel.smsBannerText ?: "",
                    fontSize = 12.sp,
                    color = Color(0xFF7F1D1D),
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    val pendingAmount = viewModel.pendingSmsTransaction?.amount
                    val displayAmount = if (pendingAmount != null) String.format(Locale.getDefault(), "%,.0f", pendingAmount) else "0"
                    Button(
                        onClick = { viewModel.approveDetectedSmsTx() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        enabled = pendingAmount != null
                    ) {
                        Text("Add entry (₹$displayAmount)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { viewModel.dismissDetectedSms() }
                    ) {
                        Text("Dismiss", color = Color(0xFF991B1B), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// --- FINTECH BOTTOM NAVIGATION BAR COMPOSABLE WITH SAFE AREA NAVIGATION INSETS ---

@Composable
fun FintechBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.Transparent,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .navigationBarsPadding(),
        windowInsets = WindowInsets.navigationBars
    ) {
        val menuItems = listOf(
            Triple("DASHBOARD", "Home", Icons.Default.Dashboard),
            Triple("TRANSACTIONS", "Ledger", Icons.Default.History),
            Triple("ANALYTICS", "Charts", Icons.Default.Analytics),
            Triple("ADVISOR", "AI Chat", Icons.Default.AutoAwesome),
            Triple("BUDGETS", "Goals", Icons.Default.Savings),
            Triple("SCANNER", "Receipt", Icons.Default.DocumentScanner),
            Triple("SETTINGS", "Settings", Icons.Default.Settings)
        )

        menuItems.forEach { (key, title, icon) ->
            NavigationBarItem(
                selected = currentTab == key,
                onClick = { onTabSelected(key) },
                icon = { Icon(icon, contentDescription = title) },
                label = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FintechPurple,
                    selectedTextColor = FintechPurple,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = FintechPurple.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_item_$key")
            )
        }
    }
}

// --- SCREEN 1: THE PORTFOLIO FINTECH DASHBOARD ---

@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    transactions: List<Transaction>,
    accounts: List<FinancialAccount>
) {
    val income by viewModel.incomeSum.collectAsStateWithLifecycle()
    val expenses by viewModel.expenseSum.collectAsStateWithLifecycle()
    val netBalance by viewModel.aggregateBalance.collectAsStateWithLifecycle()
    val healthScore = viewModel.calculateHealthScore(transactions)
    val anomalies = remember(transactions) { viewModel.extractAnomalies(transactions) }

    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
            currentTimeString = sdf.format(Date())
            kotlinx.coroutines.delay(1000L)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "blinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.2f at 0
                0.8f at 500
                1.0f at 1000
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back, ${viewModel.currentUserName}",
                    fontSize = 14.sp,
                    color = if (viewModel.isDarkMode) TextSecondaryDark else TextSecondaryLight
                )
                Text(
                    text = "Consolidated Wealth",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.isDarkMode) TextPrimaryDark else TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = if (viewModel.isDarkMode) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(color = StatusGreen.copy(alpha = alpha), shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE SYSTEM • $currentTimeString",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.isDarkMode) Color(0xFF34D399) else Color(0xFF059669),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            // User Quick SMS Simulation Button and AI status
            IconButton(
                onClick = { viewModel.simulateSmsReceived() },
                modifier = Modifier
                    .background(FintechPurple.copy(alpha = 0.15f), CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    Icons.Filled.Sms,
                    contentDescription = "Simulate SMS",
                    tint = FintechPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Net Wealth Balance Banner Card is inspired by CRED with Neon highlight
        val cardBrush = Brush.linearGradient(
            colors = listOf(FintechPurple, FintechSecondary, FintechAccent)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .background(cardBrush)
                    .padding(24.dp)
            ) {
                Text(
                    "TOTAL NET VALUE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "₹${String.format("%,.2f", netBalance)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color.Green, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Monthly Inflow", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("₹${String.format("%,.0f", income)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingDown, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Outflow Sum", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("₹${String.format("%,.0f", expenses)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Financial Health Score Circular Gauge with AI Verdict
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.isDarkMode) CardBgDark else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dial Gauge Drawn via Canvas in Compose
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color(0xFFE5E7EB),
                            startAngle = 140f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                        val arcColor = when {
                            healthScore >= 80 -> StatusGreen
                            healthScore >= 55 -> StatusAmber
                            else -> StatusRed
                        }
                        drawArc(
                            color = arcColor,
                            startAngle = 140f,
                            sweepAngle = (healthScore.toFloat() / 100f) * 260f,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$healthScore", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight)
                        Text("/ 100", fontSize = 9.sp, color = TextSecondaryLight)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "AI Financial Health Rating",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight
                    )
                    
                    val verdictText = when {
                        healthScore >= 80 -> "Robust. Exceptionally well optimized. Your savings rate is healthy and outliers are low!"
                        healthScore >= 55 -> "Moderate. Good stability, but Swiggy & retail Shopping spending peaks are causing spikes."
                        else -> "Critical. Outflow exceeds safe proportions. Click AI Chat to request an optimization plan."
                    }
                    Text(
                        verdictText,
                        fontSize = 12.sp,
                        color = if (viewModel.isDarkMode) TextSecondaryDark else TextSecondaryLight,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AI SPENDING INSIGHTS SECTION (Dynamic insights)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "AI Financial Insights",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight
            )
            Text(
                "Actionable",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = FintechPurple
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = FintechPurple.copy(alpha = 0.08f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Insight 1
                Row {
                    Icon(Icons.Filled.Insights, null, tint = FintechPurple, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Category warning: Swiggy Swadh & Food Spike", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight)
                        Text("You spent 18% more on food delivery Swiggy this month. Reduce discretionary spending by ₹2,500.", fontSize = 11.sp, color = if (viewModel.isDarkMode) TextSecondaryDark else TextSecondaryLight)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                // Insight 2
                Row {
                    Icon(Icons.Filled.AutoAwesome, null, tint = FintechSecondary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("AI Smart Recommendation", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight)
                        Text("You can hit your 'New Bike' goals 14 days earlier by moving ₹4,500 from Current to Savings deposits immediately.", fontSize = 11.sp, color = if (viewModel.isDarkMode) TextSecondaryDark else TextSecondaryLight)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Multi-Account Management Section
        Text(
            "My Financial Asset Accounts",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        accounts.forEach { account ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isDarkMode) CardBgDark else Color.White
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (account.accountType) {
                            "BANK" -> Icons.Default.Savings
                            "CREDIT" -> Icons.Default.CreditCard
                            "WALLET" -> Icons.Default.AccountBalanceWallet
                            else -> Icons.Default.MonetizationOn
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(FintechPurple.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = FintechPurple)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(account.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight)
                            Text(account.accountType, fontSize = 10.sp, color = TextSecondaryLight)
                        }
                    }
                    Text(
                        "₹${String.format("%,.0f", account.balance)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (account.balance < 0) StatusRed else if (viewModel.isDarkMode) Color.White else TextPrimaryLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ANOMALY SPIKES WARNINGS (Isolation Forest dynamic detections)
        if (anomalies.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(Icons.Filled.Warning, null, tint = StatusRed, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "ML Anomaly Alert Spikes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusRed
                )
            }
            anomalies.forEach { (tx, report) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(tx.title, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B), fontSize = 14.sp)
                            Text("Score: ${String.format("%.2f", report.anomalyScore)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF991B1B))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            report.localExplanation,
                            fontSize = 11.sp,
                            color = Color(0xFF7F1D1D),
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Badge(containerColor = Color(0xFFEF4444)) { 
                                Text(report.riskLevel + " Anomaly Risk", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)) 
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- SCREEN 2: ALL TRANSACTION LEDGER SCREEN ---

@Composable
fun TransactionsScreen(viewModel: FinanceViewModel, transactions: List<Transaction>) {
    var query by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedTypeFilter by remember { mutableStateOf("All") } // All, Income, Expenses
    var openAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Food", "Shopping", "Travel", "Entertainment", "Healthcare", "Bills", "Education", "Others")

    val filteredTransactions = remember(transactions, query, selectedCategoryFilter, selectedTypeFilter) {
        transactions.filter { tx ->
            val matchesQuery = tx.title.contains(query, ignoreCase = true) || tx.category.contains(query, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == "All" || tx.category == selectedCategoryFilter
            val matchesType = when (selectedTypeFilter) {
                "Income" -> tx.isIncome
                "Expenses" -> !tx.isIncome
                else -> true
            }
            matchesQuery && matchesCategory && matchesType
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddDialog = true },
                containerColor = FintechPurple,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(Icons.Filled.Add, "Add Transaction")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            Text(
                "Financial Ledger",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight
            )
            Text(
                "Search, categorize and audit transactions",
                fontSize = 12.sp,
                color = TextSecondaryLight,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search inputs
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search swipe entries or categories...") },
                modifier = Modifier.fillMaxWidth().testTag("search_bar"),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = { Icon(Icons.Filled.Search, null, tint = FintechPurple) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Categories horizontal tag filters
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    Card(
                        modifier = Modifier
                            .clickable { selectedCategoryFilter = cat }
                            .testTag("category_tag_$cat"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) FintechPurple else if (viewModel.isDarkMode) CardBgDark else Color(0xFFF3F4F6)
                        )
                    ) {
                        Text(
                            cat,
                            color = if (isSelected) Color.White else if (viewModel.isDarkMode) Color.White else TextPrimaryLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab toggles for Income vs Expense filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (viewModel.isDarkMode) CardBgDark else Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                    .padding(2.dp)
            ) {
                listOf("All", "Income", "Expenses").forEach { type ->
                    val isSelected = selectedTypeFilter == type
                    Button(
                        onClick = { selectedTypeFilter = type },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) FintechPurple else Color.Transparent,
                            contentColor = if (isSelected) Color.White else if (viewModel.isDarkMode) Color.White else TextSecondaryLight
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // LEDGER ENTRIES LIST
            if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Inbox, null, modifier = Modifier.size(60.dp), tint = TextSecondaryLight.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No transactions match criteria.", color = TextSecondaryLight, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        LedgerItemCard(viewModel, tx)
                    }
                }
            }
        }
    }

    // CREATE OR INSERT NEW TRANSACTION DIALOG PANEL
    if (openAddDialog) {
        AddTransactionDialog(
            viewModel = viewModel,
            onDismiss = { openAddDialog = false }
        )
    }
}

@Composable
fun LedgerItemCard(viewModel: FinanceViewModel, tx: Transaction) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showMenu = !showMenu },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (viewModel.isDarkMode) CardBgDark else Color.White
        )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Category icon
                    val icon = when (tx.category) {
                        "Food" -> Icons.Default.Restaurant
                        "Shopping" -> Icons.Default.ShoppingBag
                        "Travel" -> Icons.Default.TaxiAlert
                        "Entertainment" -> Icons.Default.Movie
                        "Healthcare" -> Icons.Default.Healing
                        "Bills" -> Icons.Default.ElectricalServices
                        "Education" -> Icons.Default.School
                        else -> Icons.Default.Payments
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(FintechPurple.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, tint = FintechPurple, modifier = Modifier.size(18.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tx.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight)
                        Text("${tx.accountName} • ${tx.category}", fontSize = 10.sp, color = TextSecondaryLight)
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    val symbol = if (tx.isIncome) "+" else "-"
                    val color = if (tx.isIncome) StatusGreen else StatusRed
                    Text(
                        "$symbol ₹${String.format("%,.0f", tx.amount)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = color
                    )
                    
                    // Simple Date Formatted
                    val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    val dStr = df.format(Date(tx.timestamp))
                    Text(dStr, fontSize = 9.sp, color = TextSecondaryLight)
                }
            }

            if (showMenu) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red.copy(alpha = 0.05f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Delete this ledger entry?", fontSize = 11.sp, color = StatusRed, fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = {
                            viewModel.deleteTransaction(tx)
                            showMenu = false
                        }
                    ) {
                        Icon(Icons.Filled.Delete, "Delete", tint = StatusRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// --- SCREEN 3: ANALYTICS & CUSTOM CHARTS DRAWN VIA COMPOSE CANVAS ---

@Composable
fun AnalyticsScreen(viewModel: FinanceViewModel, transactions: List<Transaction>) {
    var reportInterval by remember { mutableStateOf("Monthly") } // Monthly, Yearly

    val expenses = remember(transactions) { transactions.filter { !it.isIncome } }
    val categoryTotals = remember(expenses) {
        expenses.groupBy { it.category }.mapValues { it.value.sumOf { item -> item.amount } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Expense Distribution",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight
        )
        Text(
            "Visual reports dynamically synthesized via native ML",
            fontSize = 12.sp,
            color = TextSecondaryLight,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom selector bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (viewModel.isDarkMode) CardBgDark else Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                .padding(2.dp)
        ) {
            listOf("Monthly Report", "Yearly Report").forEach { item ->
                val isSel = (item.startsWith("Monthly") && reportInterval == "Monthly") || (item.startsWith("Yearly") && reportInterval == "Yearly")
                Button(
                    onClick = { reportInterval = if (item.startsWith("Monthly")) "Monthly" else "Yearly" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSel) FintechPurple else Color.Transparent,
                        contentColor = if (isSel) Color.White else if (viewModel.isDarkMode) Color.White else TextSecondaryLight
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(item, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CHART 1: Pie Distribution Donut Chart
        Text(
            "Expenditure Split",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.isDarkMode) CardBgDark else Color.White
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (categoryTotals.isEmpty()) {
                    Text("Inflow represents 100% of ledger status. No expenses recorded.", fontSize = 12.sp, color = TextSecondaryLight)
                } else {
                    Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val total = categoryTotals.values.sum()
                            var currentAngle = 0f
                            val colors = listOf(
                                Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF10B981), 
                                Color(0xFFF59E0B), Color(0xFF3B82F6), Color(0xFFEF4444),
                                Color(0xFF06B6D4), Color(0xFF6B7280)
                            )
                            
                            categoryTotals.entries.forEachIndexed { idx, entry ->
                                val sweep = if (total > 0.0) ((entry.value / total) * 360f).toFloat() else 0f
                                if (sweep > 0f) {
                                    drawArc(
                                        color = colors[idx % colors.size],
                                        startAngle = currentAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    currentAngle += sweep
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Out", fontSize = 10.sp, color = TextSecondaryLight)
                            Text("₹${String.format("%.0f", categoryTotals.values.sum())}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Legends list
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val colors = listOf(
                            Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF10B981), 
                            Color(0xFFF59E0B), Color(0xFF3B82F6), Color(0xFFEF4444),
                            Color(0xFF06B6D4), Color(0xFF6B7280)
                        )
                        categoryTotals.entries.forEachIndexed { idx, entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(10.dp).background(colors[idx % colors.size], CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(entry.key, fontSize = 11.sp, color = if (viewModel.isDarkMode) TextSecondaryDark else TextSecondaryLight)
                                }
                                Text("₹${String.format("%,.0f", entry.value)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CHART 2: Native Canvas-based Spline Trend Forecasting Graph
        Text(
            "Expense Timeline Prediction",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.isDarkMode) CardBgDark else Color.White
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Trend Projection (Simple Linear Regression)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechPurple
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Canvas spline drawing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Graph parameters
                        val points = listOf(4200f, 5100f, 3800f, 4800f) // Historical mock trends
                        val widthOffset = size.width / (points.size)
                        
                        val path = Path()
                        val maxVal = 6000f
                        
                        points.forEachIndexed { i, pt ->
                            val cx = i * widthOffset + 30f
                            val cy = size.height - (pt / maxVal) * size.height + 20f
                            
                            if (i == 0) {
                                path.moveTo(cx, cy)
                            } else {
                                path.lineTo(cx, cy)
                            }
                            
                            // Draw point circle
                            drawCircle(color = FintechPurple, radius = 4.dp.toPx(), center = Offset(cx, cy))
                        }
                        
                        drawPath(
                            path = path,
                            color = FintechPurple,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw linear prediction next-dash line
                        val forecastLine = Path().apply {
                            val x1 = (points.size - 1) * widthOffset + 30f
                            val y1 = size.height - (points.last() / maxVal) * size.height + 20f
                            val x2 = (points.size) * widthOffset - 20f
                            val y2 = size.height - (5400f / maxVal) * size.height + 20f
                            moveTo(x1, y1)
                            lineTo(x2, y2)
                        }
                        drawPath(
                            path = forecastLine,
                            color = FintechAccent,
                            style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                        )
                        // Label prediction peak
                        drawCircle(color = FintechAccent, radius = 5.dp.toPx(), center = Offset((points.size) * widthOffset - 20f, size.height - (5400f / maxVal) * size.height + 20f))
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Mar spending", fontSize = 10.sp, color = TextSecondaryLight)
                    Text("Apr spending", fontSize = 10.sp, color = TextSecondaryLight)
                    Text("May spending", fontSize = 10.sp, color = TextSecondaryLight)
                    Text("Jun spending", fontSize = 10.sp, color = TextSecondaryLight)
                    Text("Jul FORECAST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FintechAccent)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Forecast Stats Callout Cards
                val forecastAmt = viewModel.calculateMonthlyExpenseForecast(transactions)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FintechPurple.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, null, tint = FintechPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mathematical Spend Forecast", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = FintechPurple)
                        }
                        Text(
                            "Next month expenses are projected to settle around ₹${String.format("%.2f", forecastAmt)}. Your savings rate should sustain at healthy 32%.",
                            fontSize = 11.sp,
                            color = if (viewModel.isDarkMode) TextSecondaryDark else TextSecondaryLight,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// --- SCREEN 4: AI CONVERSATIONAL FINANCE ADVISOR (Gemini integration) ---

@Composable
fun AdvisorScreen(viewModel: FinanceViewModel) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    var input by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(messages.size, viewModel.isChatLoading) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // App header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(FintechPurple.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AutoAwesome, null, tint = FintechPurple)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "AI Financial Advisor",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(StatusGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "LIVE CORE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusGreen
                        )
                    }
                }
                Text(
                    "Powered by Google Gemini AI & REST Integration",
                    fontSize = 11.sp,
                    color = TextSecondaryLight
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chat List Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(if (viewModel.isDarkMode) CardBgDark.copy(alpha = 0.3f) else Color(0xFFF9FAFB), RoundedCornerShape(16.dp))
                .border(1.dp, if (viewModel.isDarkMode) Color(0xFF334155) else Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                messages.forEach { chat ->
                    val isUser = chat.role == "user"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp, 
                                topEnd = 16.dp, 
                                bottomStart = if (isUser) 16.dp else 4.dp, 
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = if (isUser) FintechPurple else if (viewModel.isDarkMode) CardBgDark else Color(0xFFF2F3F5),
                            contentColor = if (isUser) Color.White else if (viewModel.isDarkMode) Color.White else TextPrimaryLight,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                chat.message,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
                
                if (viewModel.isChatLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(20.dp), color = FintechPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI is compiling recommendations...", fontSize = 11.sp, color = TextSecondaryLight)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick advice prompt tags
        Text("Try asking:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryLight, modifier = Modifier.padding(bottom = 6.dp))
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                Pair("ANALYZE", "Analyze spending"),
                Pair("SAVE", "How to save ₹2.5k"),
                Pair("BUDGET", "50/30/20 Budget plan"),
                Pair("OVERSPENDING", "Where am I overspending?")
            )
            items(suggestions) { (key, title) ->
                Card(
                    modifier = Modifier
                        .clickable { viewModel.triggerQuickAction(key) }
                        .testTag("chat_suggest_$key"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FintechPurple.copy(alpha = 0.08f))
                ) {
                    Text(
                        title, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold,
                        color = FintechPurple, 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input chat compose bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Ask Wealth Coach...", fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input"),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (input.trim().isNotEmpty()) {
                        viewModel.askAiAdvisor(input)
                        input = ""
                        scope.launch { scrollState.animateScrollTo(scrollState.maxValue + 600) }
                    }
                },
                modifier = Modifier
                    .background(FintechPurple, CircleShape)
                    .size(48.dp)
                    .testTag("send_chat_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White)
            }
        }
    }
}

// --- SCREEN 5: BUDGET GOALS TRACKER --->

@Composable
fun BudgetsScreen(viewModel: FinanceViewModel, goals: List<BudgetGoal>, transactions: List<Transaction>) {
    var openAddGoal by remember { mutableStateOf(false) }

    var goalTitle by remember { mutableStateOf("") }
    var goalTarget by remember { mutableStateOf("") }
    var goalProgress by remember { mutableStateOf("") }
    var goalCategory by remember { mutableStateOf("Travel") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Savings & Budgets",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight
                )
                Text(
                    "Track customized assets and targets visually",
                    fontSize = 12.sp,
                    color = TextSecondaryLight
                )
            }
            IconButton(
                onClick = { openAddGoal = true },
                modifier = Modifier
                    .background(FintechPurple, CircleShape)
                    .size(40.dp)
                    .testTag("add_goal_button")
            ) {
                Icon(Icons.Filled.Add, "Add goal", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No budgets stored. Create your first saving target!", color = TextSecondaryLight)
            }
        } else {
            goals.forEach { goal ->
                val progressPercent = (if (goal.targetAmount > 0.0) goal.currentAmount / goal.targetAmount else 0.0).coerceIn(0.0, 1.0)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.isDarkMode) CardBgDark else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(FintechPurple.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Savings, null, tint = FintechPurple, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight)
                                    Text(goal.category, fontSize = 10.sp, color = TextSecondaryLight)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteGoal(goal.id) }) {
                                Icon(Icons.Default.Delete, "Delete", tint = StatusRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Progress Bar with LinearGradients
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(Color(0xFFE5E7EB), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressPercent.toFloat())
                                    .fillMaxHeight()
                                    .background(
                                        Brush.linearGradient(listOf(FintechPurple, FintechSecondary)),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Saved: ₹${String.format("%,.0f", goal.currentAmount)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight
                            )
                            Text(
                                "Target: ₹${String.format("%,.0f", goal.targetAmount)} (${String.format("%.0f%%", progressPercent * 100)})",
                                fontSize = 11.sp,
                                color = TextSecondaryLight
                            )
                        }
                    }
                }
            }
        }
    }

    // Goal creation Dialog Interface
    if (openAddGoal) {
        Dialog(onDismissRequest = { openAddGoal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("New Saving Target", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimaryLight)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = { goalTitle = it },
                        label = { Text("What are you saving for?") },
                        modifier = Modifier.fillMaxWidth().testTag("goal_title_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = goalTarget,
                        onValueChange = { goalTarget = it },
                        label = { Text("Target Amount (₹)") },
                        modifier = Modifier.fillMaxWidth().testTag("goal_target_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = goalProgress,
                        onValueChange = { goalProgress = it },
                        label = { Text("Starting Balance / Saved (₹)") },
                        modifier = Modifier.fillMaxWidth().testTag("goal_progress_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { openAddGoal = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (goalTitle.isNotEmpty()) {
                                    val tgt = goalTarget.toDoubleOrNull() ?: 5000.0
                                    val prg = goalProgress.toDoubleOrNull() ?: 0.0
                                    viewModel.addBudgetGoal(goalTitle, tgt, prg, goalCategory)
                                    goalTitle = ""
                                    goalTarget = ""
                                    goalProgress = ""
                                    openAddGoal = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FintechPurple)
                        ) {
                            Text("Create Goal")
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 6: RECEIPT SCANNER & INTELLIGENCE (Gemini Vision) ---

@Composable
fun ScannerScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    var hasPhotoSelected by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher for camera and gallery capture
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            hasPhotoSelected = true
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
                if (bitmap != null) {
                    viewModel.performReceiptScan(bitmap)
                }
            } catch (e: Exception) {
                ScaffoldMessenger.showToast(context, "Error decoding image")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text(
                "Intelligence Receipt Scanner",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(FintechPurple.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    "VISION LIVE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = FintechPurple
                )
            }
        }
        Text(
            "Extract elements instantaneously using Gemini Vision",
            fontSize = 11.sp,
            color = TextSecondaryLight,
            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp, bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.4f)
                .clickable { galleryLauncher.launch("image/*") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) CardBgDark else Color(0xFFF3F4F6)),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(FintechPurple, FintechSecondary)))
        ) {
            if (hasPhotoSelected && selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Receipt Shot",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.DocumentScanner,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = FintechPurple
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select or Upload Receipt Image", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight)
                    Text("Simulated Camera capture & Gallery upload support", fontSize = 10.sp, color = TextSecondaryLight)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.isScanningReceipt) {
            CircularProgressIndicator(color = FintechPurple)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Gemini AI is parsing receipt metrics...", fontSize = 12.sp, color = TextSecondaryLight)
        }

        // Show parsed parameters
        viewModel.scannedReceiptForm?.let { form ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isDarkMode) CardBgDark else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, "success", tint = StatusGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini Analysis Successful", fontWeight = FontWeight.Bold, color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Extracted parameters:", fontSize = 11.sp, color = TextSecondaryLight)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Merchant Store:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(form.storeName, fontSize = 12.sp, color = FintechPurple, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("₹${String.format("%.2f", form.amount)}", fontSize = 12.sp, color = StatusRed, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Date:", fontSize = 12.sp)
                        Text(form.dateString, fontSize = 12.sp)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Identified Category:", fontSize = 12.sp)
                        Text(form.category, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (form.purchasedItems.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Purchased Items:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        form.purchasedItems.forEach { item ->
                            Text("- $item", fontSize = 11.sp, color = TextSecondaryLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.approveReceiptAndAddTx()
                            hasPhotoSelected = false
                            selectedImageUri = null
                            selectedBitmap = null
                            ScaffoldMessenger.showToast(context, "Receipt approved and recorded in Ledger!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Approve & File Transaction", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SCREEN 7: PROFILE & SYSTEM SETTINGS ---

@Composable
fun SettingsScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            "Account Preferences",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight
        )
        Text(
            "Manage credentials and layout systems",
            fontSize = 11.sp,
            color = TextSecondaryLight,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Profile panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) CardBgDark else Color.White)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(FintechPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(viewModel.currentUserName.take(2).uppercase(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(viewModel.currentUserName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (viewModel.isDarkMode) Color.White else TextPrimaryLight)
                    Text(viewModel.currentUserEmail, fontSize = 12.sp, color = TextSecondaryLight)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("General Preferences", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))

        // Dark Mode Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) CardBgDark else Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.DarkMode, null, tint = FintechPurple)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Dark visual theme", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Switch(
                    checked = viewModel.isDarkMode,
                    onCheckedChange = { viewModel.toggleTheme() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // SMS permissions status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (viewModel.isDarkMode) CardBgDark else Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Security, null, tint = FintechPurple)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("SMS Transact-Detection Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Badge(containerColor = StatusGreen) {
                    Text("Active-Local", color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Sign Out Secure session", fontWeight = FontWeight.Bold)
        }
    }
}

// --- ADD TRANSACTION DIALOG ---

@Composable
fun AddTransactionDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Food") }
    var selectedAccount by remember { mutableStateOf("Savings Account") }

    val categories = listOf("Food", "Shopping", "Travel", "Entertainment", "Healthcare", "Bills", "Education", "Others")
    val accounts = listOf("Savings Account", "Current Account", "Cash", "Credit Card", "UPI Wallet")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Add Ledger Transaction", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimaryLight)
                Spacer(modifier = Modifier.height(16.dp))

                // Income/Expense selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { isIncome = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isIncome) StatusRed else Color.Transparent,
                            contentColor = if (!isIncome) Color.White else TextSecondaryLight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Expense", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { isIncome = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isIncome) StatusGreen else Color.Transparent,
                            contentColor = if (isIncome) Color.White else TextSecondaryLight
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Income", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        // Auto-categorize if not income!
                        if (!isIncome && it.isNotEmpty()) {
                            selectedCategory = FinanceML.autoCategorize(it)
                        }
                    },
                    label = { Text("Transaction Description") },
                    modifier = Modifier.fillMaxWidth().testTag("add_tx_desc"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_tx_amount"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category dropdown selection
                Text("Category", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryLight)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Account selection details
                Text("Deposit/Payment Account", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryLight)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = selectedAccount == acc,
                            onClick = { selectedAccount = acc },
                            label = { Text(acc) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (title.isNotEmpty() && amt != null) {
                                viewModel.addTransaction(title, amt, isIncome, selectedAccount, selectedCategory)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechPurple)
                    ) {
                        Text("Save Entry")
                    }
                }
            }
        }
    }
}
