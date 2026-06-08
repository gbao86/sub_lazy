/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational,
and non-commercial purposes.
Commercial exploitation, sale, or distribution of this software or any derivative works
is strictly prohibited without the express written permission of the author.
*/

@file:Suppress("DEPRECATION")

package com.gbao86.sub_lazy.ui.screens

import android.widget.Toast
import android.graphics.Paint
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import com.gbao86.sub_lazy.data.PaymentHistory
import java.text.SimpleDateFormat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.CategorySpending
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.CategoryUtils
import com.gbao86.sub_lazy.ui.DateUtils
import com.gbao86.sub_lazy.ui.VietQRGenerator
import com.gbao86.sub_lazy.ui.FinanceCalculator
import com.gbao86.sub_lazy.ui.BankruptcyRunwayResult
import com.gbao86.sub_lazy.ui.MonthlyForecast
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.viewmodel.SubscriptionViewModel
import java.util.*
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.gbao86.sub_lazy.data.api.GeminiService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context
import coil.compose.AsyncImage
import com.gbao86.sub_lazy.data.SubscriptionTemplates

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SubscriptionViewModel = viewModel(),
    onNavigateToAdd: (String?, Double?, String?, String?, String?, String?, String?, String?) -> Unit,
    onNavigateToList: () -> Unit
) {
    val totalMonthlyCost by viewModel.totalMonthlyCost.collectAsStateWithLifecycle(initialValue = 0.0)
    val spendingByCategory by viewModel.spendingByCategory.collectAsStateWithLifecycle(initialValue = emptyList())
    val subscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val paymentHistory by viewModel.allPaymentHistory.collectAsStateWithLifecycle(initialValue = emptyList())
    val userBalance by viewModel.userBalance.collectAsStateWithLifecycle(initialValue = 2000000.0)
    val budgetResetDay by viewModel.budgetResetDay.collectAsStateWithLifecycle(initialValue = 1)

    DashboardContent(
        totalMonthlyCost = totalMonthlyCost,
        spendingByCategory = spendingByCategory,
        subscriptions = subscriptions,
        paymentHistory = paymentHistory,
        userBalance = userBalance,
        budgetResetDay = budgetResetDay,
        onUpdateUserBalance = { viewModel.updateUserBalance(it) },
        onUpdateBudgetResetDay = { viewModel.updateBudgetResetDay(it) },
        onMarkAsPaid = { viewModel.markAsPaid(it) },
        onCheckInSession = { viewModel.checkInSession(it) },
        onToggleMemberPaidStatus = { sub, name -> viewModel.toggleMemberPaidStatus(sub, name) },
        onNavigateToAdd = onNavigateToAdd,
        onNavigateToList = onNavigateToList
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    totalMonthlyCost: Double?,
    spendingByCategory: List<CategorySpending>,
    subscriptions: List<Subscription>,
    paymentHistory: List<PaymentHistory>,
    userBalance: Double,
    budgetResetDay: Int,
    onUpdateUserBalance: (Double) -> Unit,
    onUpdateBudgetResetDay: (Int) -> Unit,
    onMarkAsPaid: (Subscription) -> Unit,
    onCheckInSession: (Subscription) -> Unit,
    onToggleMemberPaidStatus: (Subscription, String) -> Unit,
    onNavigateToAdd: (String?, Double?, String?, String?, String?, String?, String?, String?) -> Unit,
    onNavigateToList: () -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]

    var showBalanceEditDialog by remember { mutableStateOf(false) }
    var tickTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000)
            tickTrigger++
        }
    }

    if (showBalanceEditDialog) {
        var inputVal by remember {
            mutableStateOf(
                java.text.NumberFormat.getNumberInstance(locale).format(userBalance.toLong())
            )
        }
        var resetDayInput by remember { mutableStateOf(budgetResetDay.toString()) }
        ModalBottomSheet(
            onDismissRequest = { showBalanceEditDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.budget_update_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { text ->
                        val cleanDigits = text.replace("[^\\d]".toRegex(), "")
                        if (cleanDigits.isEmpty()) {
                            inputVal = ""
                        } else {
                            val parsed = cleanDigits.toLongOrNull()
                            if (parsed != null) {
                                inputVal = java.text.NumberFormat.getNumberInstance(locale).format(parsed)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.budget_monthly_label)) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = resetDayInput,
                    onValueChange = { text ->
                        val cleanDigits = text.replace("[^\\d]".toRegex(), "")
                        if (cleanDigits.isEmpty()) {
                            resetDayInput = ""
                        } else {
                            val parsed = cleanDigits.toIntOrNull()
                            if (parsed != null && parsed in 1..31) {
                                resetDayInput = parsed.toString()
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.budget_reset_day_label)) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = { showBalanceEditDialog = false },
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text(stringResource(R.string.btn_cancel), fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            val cleanDigits = inputVal.replace("[^\\d]".toRegex(), "")
                            val newBalance = cleanDigits.toDoubleOrNull() ?: 0.0
                            val newResetDay = resetDayInput.toIntOrNull() ?: 1
                            onUpdateUserBalance(newBalance)
                            onUpdateBudgetResetDay(newResetDay)
                            showBalanceEditDialog = false
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.btn_save), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    var selectedCategory by remember { mutableStateOf<CategorySpending?>(null) }
    var selectedUpcomingSub by remember { mutableStateOf<Subscription?>(null) }

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiService(context) }

    var linkedAccountEmail by remember {
        mutableStateOf(
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("gmail_account", null)
        )
    }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken("64362252049-i3htibobp5vetql6h451ov3a7nbo6lii.apps.googleusercontent.com")
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"))
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val email = account?.email ?: ""
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit {
                putString("gmail_account", email)
            }
            linkedAccountEmail = email
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: ApiException) {
            e.printStackTrace()
        }
    }

    var isAnalyzing by remember { mutableStateOf(false) }
    val showTemplatesDialog = remember { mutableStateOf(false) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isAnalyzing = true
            geminiService.analyzeImage(uri, object : GeminiService.GeminiCallback {
                override fun onSuccess(result: GeminiService.ParsedSubscription) {
                    isAnalyzing = false
                    coroutineScope.launch(Dispatchers.Main) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToAdd(result.name, result.amount, result.cycle, result.category, null, null, null, null)
                    }
                }
                override fun onError(message: String) {
                    isAnalyzing = false
                    coroutineScope.launch(Dispatchers.Main) {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    val showSettingsDialog = remember { mutableStateOf(false) }
    val showAddBottomSheet = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { showSettingsDialog.value = true }) {
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    var showLangMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showLangMenu = true }) {
                        Icon(Icons.Rounded.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = showLangMenu, onDismissRequest = { showLangMenu = false }) {
                        DropdownMenuItem(text = { Text("English") }, onClick = {
                            showLangMenu = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                        })
                        DropdownMenuItem(text = { Text("Tiếng Việt") }, onClick = {
                            showLangMenu = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("vi"))
                        })
                        DropdownMenuItem(text = { Text("简体中文") }, onClick = {
                            showLangMenu = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("zh"))
                        })
                        DropdownMenuItem(text = { Text("ไทย") }, onClick = {
                            showLangMenu = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("th"))
                        })
                        DropdownMenuItem(text = { Text("Español") }, onClick = {
                            showLangMenu = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
                        })
                        DropdownMenuItem(text = { Text("日本語") }, onClick = {
                            showLangMenu = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ja"))
                        })
                        DropdownMenuItem(text = { Text("한국어") }, onClick = {
                            showLangMenu = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ko"))
                        })
                        DropdownMenuItem(text = { Text("Français") }, onClick = {
                            showLangMenu = false
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("fr"))
                        })
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAddBottomSheet.value = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Thêm", fontWeight = FontWeight.SemiBold) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val pagerState = rememberPagerState(pageCount = { 2 })
        val coroutineScope = rememberCoroutineScope()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)) }
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        text = { Text(stringResource(R.string.dashboard_tab_overview), fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        text = { Text(stringResource(R.string.dashboard_tab_analysis), fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Rounded.Analytics, contentDescription = null) }
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    if (page == 0) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // ── Hero Spending Card ───────────────────────────────────────
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(32.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.primary.copy(red = 0.5f),
                                                        MaterialTheme.colorScheme.secondary
                                                    )
                                                )
                                            )
                                            .padding(horizontal = 24.dp, vertical = 24.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .align(Alignment.TopEnd)
                                                .offset(x = 20.dp, y = (-20).dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.06f))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .align(Alignment.BottomStart)
                                                .offset(x = (-16).dp, y = 16.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.04f))
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color.White.copy(alpha = 0.15f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.AutoMirrored.Rounded.TrendingUp,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    stringResource(R.string.dashboard_monthly_spending),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = Color.White.copy(alpha = 0.85f),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = CurrencyFormatter.format(totalMonthlyCost ?: 0.0, "VND", locale),
                                                style = MaterialTheme.typography.displaySmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                softWrap = false,
                                                maxLines = 1,
                                                overflow = TextOverflow.Visible
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color.White.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (subscriptions.size == 1) {
                                                        stringResource(R.string.dashboard_services_tracked_single)
                                                    } else {
                                                        stringResource(R.string.dashboard_services_tracked, subscriptions.size)
                                                    },
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }

                                            // Budget usage progress bar
                                            if (userBalance > 0.0) {
                                                val progress = ((totalMonthlyCost ?: 0.0) / userBalance).toFloat().coerceIn(0f, 1f)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "Ngốn ${(progress * 100).toInt()}% ngân sách tháng",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White.copy(alpha = 0.8f)
                                                        )
                                                        Text(
                                                            text = "${CurrencyFormatter.format(userBalance - (totalMonthlyCost ?: 0.0), "VND", locale)} còn lại",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White.copy(alpha = 0.8f)
                                                        )
                                                    }
                                                    LinearProgressIndicator(
                                                        progress = { progress },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(6.dp)
                                                            .clip(RoundedCornerShape(3.dp)),
                                                        color = Color.White,
                                                        trackColor = Color.White.copy(alpha = 0.25f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // ── Lazy Wallet Pet & Financial Health Card ────────────────
                            item {
                                val runwayResult = remember(userBalance, subscriptions, budgetResetDay, tickTrigger) {
                                    FinanceCalculator.calculateBankruptcyRunway(userBalance, subscriptions, budgetResetDay)
                                }
                                val activeSubs = remember(subscriptions) {
                                    subscriptions.filter { it.cycle != "One-time" && it.cycle != "Yearly" }
                                }
                                val nextNearestBillDate = remember(activeSubs) {
                                    activeSubs.minOfOrNull { it.nextBillingDate }
                                }
                                val totalMonthlyCost = remember(activeSubs) {
                                    activeSubs.sumOf { FinanceCalculator.calculateMonthlyEquivalentCostInVnd(it) }
                                }
                                val isPanicked = runwayResult is BankruptcyRunwayResult.AlreadyBankrupt ||
                                        totalMonthlyCost > userBalance ||
                                        (runwayResult is BankruptcyRunwayResult.DaysLeft && (
                                            runwayResult.diffMillis < 86400000L * 7 ||
                                            (nextNearestBillDate != null && runwayResult.targetTime <= nextNearestBillDate)
                                        ))
                                
                                val hasTrialExpiringSoon = subscriptions.any {
                                    it.category == "Trial" && (it.nextBillingDate - System.currentTimeMillis() <= 86400000L * 3) && (it.nextBillingDate >= System.currentTimeMillis())
                                }
                                
                                val catState = when {
                                    isPanicked || hasTrialExpiringSoon -> CatState.PANICKED
                                    totalMonthlyCost == 0.0 || (runwayResult is BankruptcyRunwayResult.DaysLeft && runwayResult.diffMillis >= 86400000L * 180) -> CatState.HAPPY
                                    else -> CatState.SLEEPING
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(28.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        LazyWalletCatSection(
                                            catState = catState,
                                            runwayResult = runwayResult,
                                            subscriptions = subscriptions,
                                            userBalance = userBalance,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                                .clickable { showBalanceEditDialog = true }
                                                .heightIn(min = 48.dp)
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.budget_monthly_spending_title),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = CurrencyFormatter.format(userBalance, "VND", locale),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                Icons.Rounded.Edit,
                                                contentDescription = "Edit Balance",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        when (runwayResult) {
                                            is BankruptcyRunwayResult.Infinite -> {
                                                Text(
                                                    stringResource(R.string.budget_empty_services_hint),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            is BankruptcyRunwayResult.Safe -> {
                                                if (totalMonthlyCost > userBalance) {
                                                    val pulseTransition = rememberInfiniteTransition(label = "pulse_deficit")
                                                    val pulseAlpha by pulseTransition.animateFloat(
                                                        initialValue = 0.6f,
                                                        targetValue = 1f,
                                                        animationSpec = infiniteRepeatable(
                                                            animation = tween(1000, easing = EaseInOutSine),
                                                            repeatMode = RepeatMode.Reverse
                                                        ),
                                                        label = "pulse"
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = Color(0xFFFFEBEE).copy(alpha = pulseAlpha),
                                                        border = BorderStroke(1.5.dp, Color(0xFFC62828)),
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.Dangerous, contentDescription = null, tint = Color(0xFFC62828))
                                                            Column {
                                                                Text(stringResource(R.string.budget_deficit_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC62828))
                                                                Text(stringResource(R.string.budget_deficit_desc), style = MaterialTheme.typography.labelMedium, color = Color(0xFFC62828))
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = Color(0xFFE8F5E9),
                                                        modifier = Modifier.fillMaxWidth()
                                                     ) {
                                                        Row(
                                                            modifier = Modifier.padding(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                                                            Column {
                                                                Text(stringResource(R.string.budget_safe_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32))
                                                                Text(stringResource(R.string.budget_safe_desc), style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            is BankruptcyRunwayResult.AlreadyBankrupt -> {
                                                val pulseTransition = rememberInfiniteTransition(label = "pulse_bankrupt")
                                                val pulseAlpha by pulseTransition.animateFloat(
                                                    initialValue = 0.4f,
                                                    targetValue = 1f,
                                                    animationSpec = infiniteRepeatable(
                                                        animation = tween(800, easing = EaseInOutSine),
                                                        repeatMode = RepeatMode.Reverse
                                                    ),
                                                    label = "pulse"
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = Color(0xFFFFEBEE).copy(alpha = pulseAlpha),
                                                    border = BorderStroke(1.5.dp, Color(0xFFC62828)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFC62828))
                                                        Column {
                                                            Text(stringResource(R.string.budget_exhausted_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFC62828))
                                                            Text(stringResource(R.string.budget_exhausted_desc), style = MaterialTheme.typography.labelMedium, color = Color(0xFFC62828))
                                                        }
                                                    }
                                                }
                                            }
                                            is BankruptcyRunwayResult.DaysLeft -> {
                                                val days = runwayResult.diffMillis / (24 * 3600 * 1000)
                                                val hours = (runwayResult.diffMillis % (24 * 3600 * 1000)) / (3600 * 1000)
                                                val minutes = (runwayResult.diffMillis % (3600 * 1000)) / (60 * 1000)
                                                val calendarTarget = Calendar.getInstance().apply { timeInMillis = runwayResult.targetTime }
                                                val formattedTargetDate = java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(calendarTarget.time)

                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = if (isPanicked) Color(0xFFFFEBEE) else Color(0xFFFFF8E1),
                                                    border = BorderStroke(
                                                        width = 1.5.dp,
                                                        color = if (isPanicked) Color(0xFFD32F2F) else Color(0xFFFBC02D)
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(16.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isPanicked) Icons.Rounded.Dangerous else Icons.Rounded.ReportProblem,
                                                                contentDescription = null,
                                                                tint = if (isPanicked) Color(0xFFD32F2F) else Color(0xFFFBC02D)
                                                            )
                                                            Text(
                                                                text = if (isPanicked) stringResource(R.string.budget_running_out_title) else stringResource(R.string.budget_running_out_warning),
                                                                fontWeight = FontWeight.Bold,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                color = if (isPanicked) Color(0xFFD32F2F) else Color(0xFFFBC02D)
                                                            )
                                                        }
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = Color.Black.copy(alpha = 0.9f),
                                                            modifier = Modifier.padding(vertical = 4.dp)
                                                        ) {
                                                            Text(
                                                                text = stringResource(R.string.budget_countdown_pattern, days, hours, minutes),
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isPanicked) Color(0xFFFF3333) else Color(0xFFFFD700),
                                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                                            )
                                                        }
                                                        Text(
                                                            text = stringResource(R.string.budget_depleted_on_date, formattedTargetDate),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // ── Manage Subscriptions Button ──────────────────────────────
                            item {
                                OutlinedButton(
                                    onClick = onNavigateToList,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    colors = ButtonDefaults.outlinedButtonColors()
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.FormatListBulleted,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        stringResource(R.string.dashboard_btn_manage),
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        Icons.Rounded.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // ── Upcoming Renewals Card ───────────────────────────────
                            if (subscriptions.isNotEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(28.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        UpcomingRenewalsTimeline(
                                            subscriptions = subscriptions,
                                            selectedSub = subscriptions.find { it.id == selectedUpcomingSub?.id },
                                            onSubSelected = { selectedUpcomingSub = it },
                                            onMarkAsPaid = onMarkAsPaid,
                                            onCheckInSession = onCheckInSession,
                                            onToggleMemberPaidStatus = onToggleMemberPaidStatus,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            // ── Payment History Card ─────────────────────────────────────
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(28.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    PaymentHistorySection(paymentHistory = paymentHistory, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp, top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            if (spendingByCategory.isNotEmpty()) {
                                // ── Donut Chart Card ─────────────────────────────────────
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(28.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    stringResource(R.string.dashboard_distribution),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                ) {
                                                    Text(
                                                        text = if (spendingByCategory.size == 1) {
                                                            stringResource(R.string.dashboard_categories_count_single)
                                                        } else {
                                                            stringResource(R.string.dashboard_categories_count, spendingByCategory.size)
                                                        },
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(24.dp))
                                            InteractiveDonutChart(
                                                spending = spendingByCategory,
                                                totalSpending = spendingByCategory.sumOf { it.totalAmount },
                                                selectedCategory = selectedCategory,
                                                onCategorySelected = { selectedCategory = it },
                                                modifier = Modifier.size(220.dp)
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))
                                            InteractiveCategoryLegend(
                                                spending = spendingByCategory,
                                                selectedCategory = selectedCategory,
                                                onCategorySelected = { selectedCategory = it },
                                                subscriptions = subscriptions
                                            )
                                        }
                                    }
                                }

                                // ── Billing Cycle Chart Card ─────────────────────────────
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(28.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        BillingCycleChart(subscriptions = subscriptions, modifier = Modifier.fillMaxWidth())
                                    }
                                }

                                // ── Cashflow Forecast Card ───────────────────────────────
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(28.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        CashflowForecastingChart(subscriptions = subscriptions, modifier = Modifier.fillMaxWidth())
                                    }
                                }
                            } else {
                                // ── Empty State ──────────────────────────────────────────
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(28.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(40.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val infiniteTransition = rememberInfiniteTransition(label = "dashboard_pulse")
                                            val pulsAlpha by infiniteTransition.animateFloat(
                                                initialValue = 0.3f, targetValue = 0.7f,
                                                animationSpec = infiniteRepeatable(
                                                    tween(1400, easing = FastOutSlowInEasing),
                                                    RepeatMode.Reverse
                                                ), label = "pulse_alpha"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(88.dp)
                                                    .clip(RoundedCornerShape(28.dp))
                                                    .background(
                                                        Brush.radialGradient(
                                                            listOf(
                                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = pulsAlpha),
                                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                                            )
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Rounded.TrendingUp,
                                                    null,
                                                    modifier = Modifier.size(40.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = stringResource(R.string.dashboard_no_data),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isAnalyzing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (showAddBottomSheet.value) {
        ModalBottomSheet(onDismissRequest = { showAddBottomSheet.value = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.action_add_manually)) },
                    leadingContent = { Icon(Icons.Rounded.Edit, null) },
                    modifier = Modifier.clickable {
                        showAddBottomSheet.value = false
                        onNavigateToAdd(null, null, null, null, null, null, null, null)
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.action_scan_screenshot)) },
                    leadingContent = { Icon(Icons.Rounded.PhotoCamera, null) },
                    modifier = Modifier.clickable {
                        showAddBottomSheet.value = false
                        imagePickerLauncher.launch("image/*")
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.action_add_from_template)) },
                    leadingContent = { Icon(Icons.Rounded.Bookmarks, null) },
                    modifier = Modifier.clickable {
                        showAddBottomSheet.value = false
                        showTemplatesDialog.value = true
                    }
                )
            }
        }
    }

    if (showTemplatesDialog.value) {
        var selectedTab by remember { mutableIntStateOf(0) }
        AlertDialog(
            onDismissRequest = { showTemplatesDialog.value = false },
            title = { Text(stringResource(R.string.template_dialog_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                            Text(stringResource(R.string.template_tab_digital), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                            Text(stringResource(R.string.template_tab_lifestyle), modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                        }
                    }

                    val templates = if (selectedTab == 0) {
                        SubscriptionTemplates.digitalTemplates
                    } else {
                        SubscriptionTemplates.lifestyleTemplates
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(templates, key = { it.name }) { template ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showTemplatesDialog.value = false
                                        onNavigateToAdd(
                                            template.name,
                                            template.amount,
                                            template.cycle,
                                            template.category,
                                            template.colorHex,
                                            template.bankName,
                                            template.bankAccount,
                                            template.bankAccountHolder
                                        )
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(template.colorHex.toColorInt()).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = CategoryUtils.getIconForName(template.name, template.category),
                                            contentDescription = null,
                                            tint = Color(template.colorHex.toColorInt()),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(template.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            text = CurrencyFormatter.format(template.amount, "VND", locale),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTemplatesDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showSettingsDialog.value) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog.value = false },
            title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val accountName = linkedAccountEmail
                    Text(
                        text = if (accountName != null)
                            stringResource(R.string.settings_gmail_linked, accountName)
                        else
                            stringResource(R.string.settings_gmail_not_linked)
                    )
                    Button(onClick = {
                        if (accountName != null) {
                            googleSignInClient.signOut().addOnCompleteListener {
                                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit {
                                    remove("gmail_account")
                                }
                                linkedAccountEmail = null
                            }
                        } else {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }) {
                        Text(
                            if (accountName != null)
                                stringResource(R.string.settings_gmail_btn_unlink)
                            else
                                stringResource(R.string.settings_gmail_btn_link)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog.value = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Donut Chart
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun InteractiveDonutChart(
    spending: List<CategorySpending>,
    totalSpending: Double,
    selectedCategory: CategorySpending?,
    onCategorySelected: (CategorySpending?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF6366F1), Color(0xFF06B6D4), Color(0xFFF43F5E),
        Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF8B5CF6)
    )
    val locale = LocalContext.current.resources.configuration.locales[0]
    val animateSweep = remember { Animatable(0f) }
    val animateScale = remember { Animatable(0.8f) }
    
    LaunchedEffect(spending) {
        launch {
            animateSweep.snapTo(0f)
            animateSweep.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
        }
        launch {
            animateScale.snapTo(0.8f)
            animateScale.animateTo(1f, tween(1000, easing = EaseOutBack))
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(animateScale.value)
                .pointerInput(spending) {
                    detectTapGestures { offset ->
                        if (totalSpending <= 0.0) return@detectTapGestures
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val dist = sqrt(dx * dx + dy * dy)
                        // FIX: Use exact stroke bounds (inner = radius - halfStroke, outer = radius + halfStroke)
                        val radius = size.width / 2f
                        val halfStroke = 19.dp.toPx() // half of 38dp (selected stroke)
                        val innerBound = radius - halfStroke - 4.dp.toPx()
                        val outerBound = radius + halfStroke + 4.dp.toPx()
                        if (dist in innerBound..outerBound) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                            if (angle < 0) angle += 360f
                            if (angle >= 360f) angle -= 360f
                            var currentAngle = 0f
                            spending.forEach { item ->
                                val sweep = ((item.totalAmount / totalSpending) * 360f).toFloat()
                                if (angle >= currentAngle && angle < currentAngle + sweep) {
                                    onCategorySelected(if (selectedCategory?.category == item.category) null else item)
                                    return@detectTapGestures
                                }
                                currentAngle += sweep
                            }
                        } else {
                            onCategorySelected(null)
                        }
                    }
                }
        ) {
            var startAngle = -90f
            spending.forEachIndexed { index, item ->
                val sweepAngle = ((item.totalAmount / totalSpending) * 360f).toFloat() * animateSweep.value
                val isSelected = selectedCategory?.category == item.category
                val middleAngle = startAngle + sweepAngle / 2f
                val angleRad = Math.toRadians(middleAngle.toDouble())
                val shiftAmt = if (isSelected) 8.dp.toPx() else 0f
                val shiftX = (shiftAmt * kotlin.math.cos(angleRad)).toFloat()
                val shiftY = (shiftAmt * kotlin.math.sin(angleRad)).toFloat()

                val gap = if (spending.size > 1) 2f else 0f
                val finalSweepAngle = (sweepAngle - gap).coerceAtLeast(0f)
                val finalStartAngle = startAngle + gap / 2f

                drawArc(
                    color = colors[index % colors.size].copy(
                        alpha = if (selectedCategory == null || isSelected) 1f else 0.3f
                    ),
                    startAngle = finalStartAngle,
                    sweepAngle = finalSweepAngle,
                    useCenter = false,
                    topLeft = Offset(shiftX, shiftY),
                    style = Stroke(
                        width = if (isSelected) 38.dp.toPx() else 28.dp.toPx(),
                        cap = StrokeCap.Butt
                    )
                )
                startAngle += sweepAngle
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Text(
                text = if (selectedCategory != null)
                    getCategoryDisplayName(selectedCategory.category)
                else
                    stringResource(R.string.chart_all_categories),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = CurrencyFormatter.format(
                    selectedCategory?.totalAmount ?: totalSpending, "VND", locale
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                softWrap = false,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category Legend
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun InteractiveCategoryLegend(
    spending: List<CategorySpending>,
    selectedCategory: CategorySpending?,
    onCategorySelected: (CategorySpending?) -> Unit,
    subscriptions: List<Subscription>
) {
    val colors = listOf(
        Color(0xFF6366F1), Color(0xFF06B6D4), Color(0xFFF43F5E),
        Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF8B5CF6)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        spending.forEachIndexed { index, item ->
            val isSelected = selectedCategory?.category == item.category
            InteractiveCategoryRow(
                item = item,
                color = colors[index % colors.size],
                isSelected = isSelected,
                categorySubs = subscriptions.filter { it.category == item.category }
            ) {
                onCategorySelected(if (isSelected) null else item)
            }
        }
    }
}

@Composable
fun InteractiveCategoryRow(
    item: CategorySpending,
    color: Color,
    isSelected: Boolean,
    categorySubs: List<Subscription>,
    onClick: () -> Unit
) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                color.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryUtils.getCategoryIcon(item.category),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = getCategoryDisplayName(item.category),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${categorySubs.size} / ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        softWrap = false,
                        maxLines = 1
                    )
                    Text(
                        text = CurrencyFormatter.format(item.totalAmount, "VND", locale),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        softWrap = false,
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }
            }
            if (isSelected) {
                categorySubs.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sub.name,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = CurrencyFormatter.format(sub.amount, sub.currency, locale),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            softWrap = false,
                            maxLines = 1,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Billing Cycle Chart
// FIX: animations now run in parallel via separate coroutine launches
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BillingCycleChart(subscriptions: List<Subscription>, modifier: Modifier = Modifier) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val daysInYear = if (java.time.Year.isLeap(currentYear.toLong())) 366.0 else 365.0
    val dailyMultiplier = daysInYear / 12.0

    val weeklyCost = subscriptions.filter { it.cycle == "Weekly" }
        .sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } * 52.0 / 12.0 +
            subscriptions.filter { it.cycle == "Daily" }
                .sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } * dailyMultiplier

    val monthlyCost = subscriptions.filter { it.cycle == "Monthly" }
        .sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } +
            subscriptions.filter { it.cycle == "Every 3 Months" }
                .sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } / 3.0 +
            subscriptions.filter { it.cycle == "Every 6 Months" }
                .sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } / 6.0

    val yearlyCost = subscriptions.filter { it.cycle == "Yearly" }
        .sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } / 12.0

    val total = weeklyCost + monthlyCost + yearlyCost

    // Declare animations outside Row to follow Compose best practices
    val weeklyAnim = remember { Animatable(0f) }
    val monthlyAnim = remember { Animatable(0f) }
    val yearlyAnim = remember { Animatable(0f) }

    // FIX: launch all three in parallel instead of sequential
    LaunchedEffect(weeklyCost, monthlyCost, yearlyCost) {
        val weeklyTarget = if (total > 0.0) (weeklyCost / total).toFloat() else 0f
        val monthlyTarget = if (total > 0.0) (monthlyCost / total).toFloat() else 0f
        val yearlyTarget = if (total > 0.0) (yearlyCost / total).toFloat() else 0f

        launch { weeklyAnim.animateTo(weeklyTarget, tween(900, easing = FastOutSlowInEasing)) }
        launch { monthlyAnim.animateTo(monthlyTarget, tween(900, easing = FastOutSlowInEasing)) }
        launch { yearlyAnim.animateTo(yearlyTarget, tween(900, easing = FastOutSlowInEasing)) }
    }

    Column(modifier = modifier.padding(24.dp)) {
        Text(
            stringResource(R.string.chart_billing_cycle_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    CurrencyFormatter.format(weeklyCost, "VND", locale),
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = false, maxLines = 1,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height((120 * weeklyAnim.value).dp.coerceAtLeast(6.dp))
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.cycle_weekly), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    CurrencyFormatter.format(monthlyCost, "VND", locale),
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = false, maxLines = 1,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height((120 * monthlyAnim.value).dp.coerceAtLeast(6.dp))
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.cycle_monthly), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    CurrencyFormatter.format(yearlyCost, "VND", locale),
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = false, maxLines = 1,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height((120 * yearlyAnim.value).dp.coerceAtLeast(6.dp))
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.cycle_yearly), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Upcoming Renewals Timeline
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun UpcomingRenewalsTimeline(
    subscriptions: List<Subscription>,
    selectedSub: Subscription?,
    onSubSelected: (Subscription?) -> Unit,
    onMarkAsPaid: (Subscription) -> Unit,
    onCheckInSession: (Subscription) -> Unit,
    onToggleMemberPaidStatus: (Subscription, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val upcoming = subscriptions.sortedBy { it.nextBillingDate }.take(6)
    var showQrDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.chart_upcoming_timeline),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(upcoming, key = { it.id ?: 0 }) { sub ->
                val days = DateUtils.getDaysLeft(sub.nextBillingDate)
                val isSelected = selectedSub?.id == sub.id
                val subColor = remember(sub.colorHex) {
                    try { Color(sub.colorHex.toColorInt()) }
                    catch (_: Exception) { Color(0xFF6366F1) }
                }
                val urgencyColor = when {
                    days <= 0 -> Color(0xFFF43F5E)
                    days <= 3 -> Color(0xFFF43F5E)
                    days <= 7 -> Color(0xFFF59E0B)
                    else -> subColor
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) subColor.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onSubSelected(if (isSelected) null else sub) }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(subColor.copy(alpha = if (isSelected) 1f else 0.8f))
                            .then(
                                if (isSelected)
                                    Modifier.border(2.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryUtils.getIconForName(sub.name, sub.category),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        sub.name,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = urgencyColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (days <= 0) "Hôm nay" else "${days}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = urgencyColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        if (selectedSub != null) {
            val days = DateUtils.getDaysLeft(selectedSub.nextBillingDate)
            val cycleText = when (selectedSub.cycle) {
                "Daily" -> stringResource(R.string.cycle_daily)
                "Weekly" -> stringResource(R.string.cycle_weekly)
                "Monthly" -> stringResource(R.string.cycle_monthly)
                "Every 3 Months" -> stringResource(R.string.cycle_3_months)
                "Every 6 Months" -> stringResource(R.string.cycle_6_months)
                "Yearly" -> stringResource(R.string.cycle_yearly)
                "One-time" -> stringResource(R.string.cycle_one_time)
                else -> selectedSub.cycle
            }
            
            val detailsLabel = when {
                selectedSub.isInstallment -> stringResource(R.string.dashboard_details_installment, selectedSub.remainingTimes ?: 0)
                selectedSub.isSessionBased -> stringResource(R.string.dashboard_details_session, selectedSub.remainingSessions ?: 0, selectedSub.totalSessions ?: 0)
                selectedSub.remainingTimes != null && selectedSub.remainingTimes > 0 -> 
                    stringResource(R.string.list_remaining_times, selectedSub.remainingTimes)
                else -> cycleText
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(200)) + expandVertically(tween(300, easing = FastOutSlowInEasing))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val accentColor = remember(selectedSub.colorHex) {
                                try { Color(selectedSub.colorHex.toColorInt()) }
                                catch (_: Exception) { Color(0xFF6366F1) }
                            }
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accentColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CategoryUtils.getIconForName(selectedSub.name, selectedSub.category),
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    selectedSub.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${CurrencyFormatter.format(selectedSub.amount, selectedSub.currency, locale)} · $detailsLabel",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    softWrap = false,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = (if (days <= 3L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (days >= 0L) "$days " + stringResource(R.string.list_days_left_suffix)
                                    else stringResource(R.string.list_days_left_today),
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (days <= 3L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Shared Members Splitting View
                        if (selectedSub.isShared && !selectedSub.sharedMembersJson.isNullOrBlank()) {
                            val members = com.gbao86.sub_lazy.data.SharedMember.parseMembers(selectedSub.sharedMembersJson)
                            if (members.isNotEmpty()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Text(stringResource(R.string.dashboard_group_split_cost), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                members.forEach { member ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = member.hasPaid,
                                                onCheckedChange = { onToggleMemberPaidStatus(selectedSub, member.name) }
                                            )
                                            Text(member.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = CurrencyFormatter.format(member.amount, selectedSub.currency, locale),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (member.hasPaid) Color.Gray else MaterialTheme.colorScheme.primary
                                            )
                                            if (!member.hasPaid) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                                val toastContext = LocalContext.current
                                                OutlinedButton(
                                                    onClick = {
                                                        val qrUrl = if (selectedSub.bankAccount != null && selectedSub.bankName != null) {
                                                            VietQRGenerator.generateQrUrl(
                                                                bankName = selectedSub.bankName,
                                                                accountNumber = selectedSub.bankAccount,
                                                                amount = member.amount,
                                                                description = "SubLazy ${selectedSub.name} ${member.name}",
                                                                accountHolder = selectedSub.bankAccountHolder
                                                            )
                                                        } else ""
                                                        val memberAmountFormatted = CurrencyFormatter.format(member.amount, selectedSub.currency, locale)
                                                        val message = toastContext.getString(
                                                            R.string.dashboard_split_reminder_pattern,
                                                            member.name,
                                                            selectedSub.name,
                                                            memberAmountFormatted,
                                                            qrUrl
                                                        )
                                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message))
                                                        Toast.makeText(toastContext, toastContext.getString(R.string.dashboard_reminder_copied), Toast.LENGTH_SHORT).show()
                                                        try {
                                                            val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                                putExtra(android.content.Intent.EXTRA_TEXT, message)
                                                                type = "text/plain"
                                                            }
                                                            val chooserTitle = toastContext.getString(R.string.dashboard_send_reminder_to, member.name)
                                                            toastContext.startActivity(android.content.Intent.createChooser(sendIntent, chooserTitle))
                                                        } catch (e: Exception) {
                                                            // fallback silently
                                                        }
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text(stringResource(R.string.dashboard_btn_remind), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Trial Sandbox Cancellation Guide View
                        if (selectedSub.category == "Trial") {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.trial_guide_title), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    }
                                    val cancelGuide = when {
                                        selectedSub.name.contains("Google", ignoreCase = true) || selectedSub.name.contains("Play", ignoreCase = true) -> {
                                            stringResource(R.string.trial_guide_play_store, selectedSub.name)
                                        }
                                        selectedSub.name.contains("Apple", ignoreCase = true) || selectedSub.name.contains("iCloud", ignoreCase = true) || selectedSub.name.contains("App Store", ignoreCase = true) -> {
                                            stringResource(R.string.trial_guide_apple_store, selectedSub.name)
                                        }
                                        selectedSub.name.contains("Netflix", ignoreCase = true) -> {
                                            stringResource(R.string.trial_guide_netflix)
                                        }
                                        selectedSub.name.contains("Spotify", ignoreCase = true) -> {
                                            stringResource(R.string.trial_guide_spotify)
                                        }
                                        selectedSub.name.contains("Youtube", ignoreCase = true) -> {
                                            stringResource(R.string.trial_guide_youtube)
                                        }
                                        else -> {
                                            stringResource(R.string.trial_guide_general)
                                        }
                                    }
                                    Text(cancelGuide, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Buttons Section
                        if (selectedSub.isSessionBased) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val remaining = selectedSub.remainingSessions ?: 0
                                OutlinedButton(
                                    onClick = { onCheckInSession(selectedSub) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                    enabled = remaining > 0
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.dashboard_check_in_session), fontWeight = FontWeight.SemiBold)
                                }
                                Button(
                                    onClick = {
                                        onMarkAsPaid(selectedSub)
                                        onSubSelected(null)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.dashboard_renew_plan), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else if (selectedSub.bankAccount != null && selectedSub.bankName != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { showQrDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Rounded.QrCode, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.dashboard_scan_vietqr), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                                }
                                Button(
                                    onClick = {
                                        onMarkAsPaid(selectedSub)
                                        onSubSelected(null)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.action_mark_paid), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    onMarkAsPaid(selectedSub)
                                    onSubSelected(null)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.action_mark_paid), fontWeight = FontWeight.SemiBold)
                            }
                        }

                        if (showQrDialog && selectedSub.bankAccount != null && selectedSub.bankName != null) {
                            val qrUrl = VietQRGenerator.generateQrUrl(
                                bankName = selectedSub.bankName,
                                accountNumber = selectedSub.bankAccount,
                                amount = selectedSub.amount,
                                description = "Thanh toan ${selectedSub.name}",
                                accountHolder = selectedSub.bankAccountHolder
                            )
                            AlertDialog(
                                onDismissRequest = { showQrDialog = false },
                                shape = RoundedCornerShape(28.dp),
                                title = { Text(stringResource(R.string.dashboard_scan_vietqr), fontWeight = FontWeight.Bold) },
                                text = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    "${selectedSub.bankName.uppercase()} · ${selectedSub.bankAccount}",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                selectedSub.bankAccountHolder?.let {
                                                    Text(
                                                        it.uppercase(),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        AsyncImage(
                                            model = qrUrl,
                                            contentDescription = "VietQR Code",
                                            modifier = Modifier
                                                .size(200.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White)
                                                .padding(8.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            stringResource(R.string.dashboard_scan_vietqr_desc),
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { showQrDialog = false },
                                        shape = RoundedCornerShape(14.dp)
                                    ) { Text(stringResource(R.string.dashboard_btn_close)) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Preview
// ─────────────────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    Sub_lazyTheme {
        DashboardContent(
            totalMonthlyCost = 1250000.0,
            spendingByCategory = listOf(
                CategorySpending("Entertainment", 500000.0),
                CategorySpending("Work", 300000.0),
                CategorySpending("Utilities", 450000.0)
            ),
            subscriptions = listOf(
                Subscription(id = 1, name = "Netflix", amount = 260000.0, cycle = "Monthly", category = "Entertainment", nextBillingDate = System.currentTimeMillis() + 86400000 * 2, colorHex = "#6366F1"),
                Subscription(id = 2, name = "Spotify", amount = 59000.0, cycle = "Monthly", category = "Music", nextBillingDate = System.currentTimeMillis() + 86400000 * 5, colorHex = "#06B6D4")
            ),
            paymentHistory = listOf(
                PaymentHistory(id = 1, subscriptionId = 1, subscriptionName = "Netflix", amount = 260000.0, currency = "VND", paymentDate = System.currentTimeMillis() - 86400000, cycle = "Monthly")
            ),
            userBalance = 2000000.0,
            budgetResetDay = 1,
            onUpdateUserBalance = {},
            onUpdateBudgetResetDay = {},
            onMarkAsPaid = {},
            onCheckInSession = {},
            onToggleMemberPaidStatus = { _, _ -> },
            onNavigateToAdd = { _, _, _, _, _, _, _, _ -> },
            onNavigateToList = {}
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bankruptcy Runway Logic (Moved to FinanceCalculator)
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// LazyWalletCat
// ─────────────────────────────────────────────────────────────────────────────

enum class CatState {
    HAPPY,
    SLEEPING,
    PANICKED
}

@Composable
fun LazyWalletCatSection(
    catState: CatState,
    runwayResult: BankruptcyRunwayResult,
    subscriptions: List<Subscription>,
    userBalance: Double,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val bubbleText = when (catState) {
            CatState.HAPPY -> {
                stringResource(R.string.cat_bubble_happy)
            }
            CatState.SLEEPING -> {
                stringResource(R.string.cat_bubble_sleeping)
            }
            CatState.PANICKED -> {
                when {
                    runwayResult is BankruptcyRunwayResult.AlreadyBankrupt -> {
                        stringResource(R.string.cat_bubble_bankrupt)
                    }
                    else -> {
                        val activeSubs = subscriptions.filter { it.cycle != "One-time" && it.cycle != "Yearly" }
                        val totalMonthlyCost = activeSubs.sumOf { FinanceCalculator.calculateMonthlyEquivalentCostInVnd(it) }
                        if (totalMonthlyCost > userBalance) {
                            stringResource(R.string.cat_bubble_exceeded)
                        } else if (runwayResult is BankruptcyRunwayResult.DaysLeft && runwayResult.diffMillis < 86400000L * 7) {
                            val days = (runwayResult.diffMillis / 86400000L).coerceAtLeast(1)
                            stringResource(R.string.cat_bubble_depleted_in_days, days.toInt())
                        } else {
                            stringResource(R.string.cat_bubble_deficit_warning)
                        }
                    }
                }
            }
        }
        
        Column(
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (catState == CatState.PANICKED) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp, 
                        if (catState == CatState.PANICKED) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), 
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = bubbleText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (catState == CatState.PANICKED) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
            Box(
                modifier = Modifier
                    .size(16.dp, 8.dp)
                    .offset(y = (-1).dp)
                    .background(
                        color = if (catState == CatState.PANICKED) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        shape = TriangleEdgeShape()
                    )
            )
        }
        
        LazyWalletCat(
            catState = catState,
            modifier = Modifier.size(150.dp)
        )
    }
}

class TriangleEdgeShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

@Composable
fun LazyWalletCat(
    catState: CatState,
    modifier: Modifier = Modifier
) {
    val sleepProgress = remember { Animatable(0f) }
    LaunchedEffect(catState) {
        if (catState == CatState.SLEEPING) {
            sleepProgress.snapTo(0f)
            sleepProgress.animateTo(1f, animationSpec = tween(3500, easing = EaseOutCubic))
        } else {
            sleepProgress.animateTo(0f, animationSpec = tween(300, easing = EaseInCubic))
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cat_anim")

    // Breathing anim (only active when sleeping)
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )
    val bodyScale = if (catState == CatState.SLEEPING) {
        1f + (breathingScale - 1f) * sleepProgress.value
    } else 1.0f

    // Shaking anim when panicked
    val shakeX by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(80, easing = LinearEasing), RepeatMode.Reverse),
        label = "shake_x"
    )
    val shakeY by infiniteTransition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(70, easing = LinearEasing), RepeatMode.Reverse),
        label = "shake_y"
    )

    // ZZZ letters animation
    val zzzAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 0, easing = LinearEasing), RepeatMode.Restart),
        label = "zzz1"
    )
    val zzzAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 600, easing = LinearEasing), RepeatMode.Restart),
        label = "zzz2"
    )
    val zzzAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 1200, easing = LinearEasing), RepeatMode.Restart),
        label = "zzz3"
    )

    val catColor = Color(0xFFFFB74D) // Beautiful Warm Orange
    val shadowColor = Color(0x1A000000)

    Box(
        modifier = modifier
            .size(150.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f + 10f
            val scaleFactor = width.coerceAtMost(height) / 150f

            // 1. Draw soft shadow under the cat
            drawOval(
                color = shadowColor,
                topLeft = Offset(centerX - 60f * scaleFactor, centerY + 30f * scaleFactor),
                size = androidx.compose.ui.geometry.Size(120f * scaleFactor, 18f * scaleFactor)
            )

            if (catState == CatState.PANICKED || catState == CatState.HAPPY) {
                // Thức dậy (Panicked hoặc Happy)
                val translationX = if (catState == CatState.PANICKED) shakeX else 0f
                val translationY = if (catState == CatState.PANICKED) shakeY else 0f
                
                translate(translationX, translationY) {
                    val headCenter = Offset(centerX, centerY - 30f * scaleFactor)
                    val headRadius = 38f * scaleFactor
                    
                    // Tail standing up (panicked) or curved wagging (happy)
                    if (catState == CatState.PANICKED) {
                        val tailPath = Path().apply {
                            moveTo(centerX - 28f * scaleFactor, centerY + 30f * scaleFactor)
                            quadraticTo(
                                centerX - 52f * scaleFactor + shakeX, centerY + 10f * scaleFactor + shakeY,
                                centerX - 46f * scaleFactor, centerY - 35f * scaleFactor
                            )
                        }
                        drawPath(
                            path = tailPath,
                            color = catColor,
                            style = Stroke(width = 8f * scaleFactor, cap = StrokeCap.Round)
                        )
                        val tailTipPath = Path().apply {
                            moveTo(centerX - 48f * scaleFactor, centerY - 25f * scaleFactor)
                            quadraticTo(
                                centerX - 50f * scaleFactor + shakeX, centerY - 30f * scaleFactor + shakeY,
                                centerX - 46f * scaleFactor, centerY - 35f * scaleFactor
                            )
                        }
                        drawPath(
                            path = tailTipPath,
                            color = Color(0xFFFFF9E6),
                            style = Stroke(width = 8f * scaleFactor, cap = StrokeCap.Round)
                        )
                    } else {
                        // Happy tail waving slowly
                        val happyTailWag = sin(System.currentTimeMillis() / 200.0) * 8.0
                        val tailPath = Path().apply {
                            moveTo(centerX - 28f * scaleFactor, centerY + 30f * scaleFactor)
                            quadraticTo(
                                centerX - 48f * scaleFactor, centerY + 15f * scaleFactor + happyTailWag.toFloat(),
                                centerX - 52f * scaleFactor, centerY - 2f * scaleFactor + happyTailWag.toFloat()
                            )
                        }
                        drawPath(
                            path = tailPath,
                            color = catColor,
                            style = Stroke(width = 8f * scaleFactor, cap = StrokeCap.Round)
                        )
                    }

                    // Left & Right Ears
                    val leftEarPath = Path().apply {
                        moveTo(centerX - 25f * scaleFactor, centerY - 55f * scaleFactor)
                        lineTo(centerX - 42f * scaleFactor, centerY - 88f * scaleFactor)
                        lineTo(centerX - 8f * scaleFactor, centerY - 65f * scaleFactor)
                        close()
                    }
                    drawPath(leftEarPath, brush = Brush.linearGradient(listOf(Color(0xFFFFCC80), catColor)))
                    val leftEarInner = Path().apply {
                        moveTo(centerX - 23f * scaleFactor, centerY - 58f * scaleFactor)
                        lineTo(centerX - 36f * scaleFactor, centerY - 80f * scaleFactor)
                        lineTo(centerX - 12f * scaleFactor, centerY - 64f * scaleFactor)
                        close()
                    }
                    drawPath(leftEarInner, color = Color(0xFFFFB7B2))

                    val rightEarPath = Path().apply {
                        moveTo(centerX + 25f * scaleFactor, centerY - 55f * scaleFactor)
                        lineTo(centerX + 42f * scaleFactor, centerY - 88f * scaleFactor)
                        lineTo(centerX + 8f * scaleFactor, centerY - 65f * scaleFactor)
                        close()
                    }
                    drawPath(rightEarPath, brush = Brush.linearGradient(listOf(Color(0xFFFFCC80), catColor)))
                    val rightEarInner = Path().apply {
                        moveTo(centerX + 23f * scaleFactor, centerY - 58f * scaleFactor)
                        lineTo(centerX + 36f * scaleFactor, centerY - 80f * scaleFactor)
                        lineTo(centerX + 12f * scaleFactor, centerY - 64f * scaleFactor)
                        close()
                    }
                    drawPath(rightEarInner, color = Color(0xFFFFB7B2))

                    // Body (chubby pear shape)
                    val bodyPath = Path().apply {
                        moveTo(centerX - 32f * scaleFactor, centerY + 38f * scaleFactor)
                        cubicTo(
                            centerX - 48f * scaleFactor, centerY + 38f * scaleFactor,
                            centerX - 36f * scaleFactor, centerY - 10f * scaleFactor,
                            centerX - 22f * scaleFactor, centerY - 10f * scaleFactor
                        )
                        lineTo(centerX + 22f * scaleFactor, centerY - 10f * scaleFactor)
                        cubicTo(
                            centerX + 36f * scaleFactor, centerY - 10f * scaleFactor,
                            centerX + 48f * scaleFactor, centerY + 38f * scaleFactor,
                            centerX + 32f * scaleFactor, centerY + 38f * scaleFactor
                        )
                        close()
                    }
                    drawPath(
                        path = bodyPath,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB74D)),
                            start = Offset(centerX, centerY - 10f * scaleFactor),
                            end = Offset(centerX, centerY + 38f * scaleFactor)
                        )
                    )

                    // White Belly Patch
                    drawOval(
                        color = Color(0xFFFFF8E7),
                        topLeft = Offset(centerX - 18f * scaleFactor, centerY + 5f * scaleFactor),
                        size = androidx.compose.ui.geometry.Size(36f * scaleFactor, 28f * scaleFactor)
                    )

                    // Head
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFFE082), Color(0xFFFFB74D)),
                            start = Offset(centerX, centerY - 60f * scaleFactor),
                            end = Offset(centerX, centerY + 8f * scaleFactor)
                        ),
                        radius = headRadius,
                        center = headCenter
                    )

                    // Cheek White Patches
                    drawCircle(
                        color = Color(0xFFFFF8E7),
                        radius = 11f * scaleFactor,
                        center = Offset(centerX - 18f * scaleFactor, centerY - 22f * scaleFactor)
                    )
                    drawCircle(
                        color = Color(0xFFFFF8E7),
                        radius = 11f * scaleFactor,
                        center = Offset(centerX + 18f * scaleFactor, centerY - 22f * scaleFactor)
                    )

                    // Eyes
                    if (catState == CatState.PANICKED) {
                        drawCircle(color = Color.White, radius = 9f * scaleFactor, center = Offset(centerX - 14f * scaleFactor, centerY - 30f * scaleFactor))
                        drawCircle(color = Color.White, radius = 9f * scaleFactor, center = Offset(centerX + 14f * scaleFactor, centerY - 30f * scaleFactor))
                        drawCircle(color = Color.Black, radius = 4f * scaleFactor, center = Offset(centerX - 14f * scaleFactor, centerY - 30f * scaleFactor))
                        drawCircle(color = Color.Black, radius = 4f * scaleFactor, center = Offset(centerX + 14f * scaleFactor, centerY - 30f * scaleFactor))
                        drawCircle(color = Color.White, radius = 1.5f * scaleFactor, center = Offset(centerX - 16f * scaleFactor, centerY - 32f * scaleFactor))
                        drawCircle(color = Color.White, radius = 1.5f * scaleFactor, center = Offset(centerX + 12f * scaleFactor, centerY - 32f * scaleFactor))

                        // Worried eyebrows
                        drawLine(
                            color = Color(0x99000000),
                            start = Offset(centerX - 22f * scaleFactor, centerY - 44f * scaleFactor),
                            end = Offset(centerX - 8f * scaleFactor, centerY - 40f * scaleFactor),
                            strokeWidth = 2f * scaleFactor,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = Color(0x99000000),
                            start = Offset(centerX + 22f * scaleFactor, centerY - 44f * scaleFactor),
                            end = Offset(centerX + 8f * scaleFactor, centerY - 40f * scaleFactor),
                            strokeWidth = 2f * scaleFactor,
                            cap = StrokeCap.Round
                        )
                    } else {
                        // HAPPY: Arched happy eyes ^^
                        val eyePathLeft = Path().apply {
                            moveTo(centerX - 22f * scaleFactor, centerY - 28f * scaleFactor)
                            quadraticTo(centerX - 15f * scaleFactor, centerY - 34f * scaleFactor, centerX - 8f * scaleFactor, centerY - 28f * scaleFactor)
                        }
                        drawPath(eyePathLeft, color = Color.Black, style = Stroke(width = 3.5f * scaleFactor, cap = StrokeCap.Round))
                        
                        val eyePathRight = Path().apply {
                            moveTo(centerX + 8f * scaleFactor, centerY - 28f * scaleFactor)
                            quadraticTo(centerX + 15f * scaleFactor, centerY - 34f * scaleFactor, centerX + 20f * scaleFactor, centerY - 28f * scaleFactor)
                        }
                        drawPath(eyePathRight, color = Color.Black, style = Stroke(width = 3.5f * scaleFactor, cap = StrokeCap.Round))
                    }

                    // Blush
                    drawCircle(color = Color(0xFFFF8E9E).copy(alpha = 0.5f), radius = 5f * scaleFactor, center = Offset(centerX - 22f * scaleFactor, centerY - 20f * scaleFactor))
                    drawCircle(color = Color(0xFFFF8E9E).copy(alpha = 0.5f), radius = 5f * scaleFactor, center = Offset(centerX + 22f * scaleFactor, centerY - 20f * scaleFactor))

                    // Nose
                    val nosePath = Path().apply {
                        moveTo(centerX - 2.5f * scaleFactor, centerY - 22f * scaleFactor)
                        lineTo(centerX + 2.5f * scaleFactor, centerY - 22f * scaleFactor)
                        lineTo(centerX, centerY - 19f * scaleFactor)
                        close()
                    }
                    drawPath(nosePath, color = Color(0xFFFF5252))

                    // Mouth
                    if (catState == CatState.PANICKED) {
                        drawCircle(color = Color(0x1F000000), radius = 5f * scaleFactor, center = Offset(centerX, centerY - 12f * scaleFactor))
                        drawCircle(color = Color(0xFF3E2723), radius = 3.5f * scaleFactor, center = Offset(centerX, centerY - 12f * scaleFactor))
                    } else {
                        // HAPPY mouth
                        val mouthPath = Path().apply {
                            moveTo(centerX - 4f * scaleFactor, centerY - 16f * scaleFactor)
                            quadraticTo(centerX - 2f * scaleFactor, centerY - 12f * scaleFactor, centerX, centerY - 15f * scaleFactor)
                            quadraticTo(centerX + 2f * scaleFactor, centerY - 12f * scaleFactor, centerX + 4f * scaleFactor, centerY - 15f * scaleFactor)
                        }
                        drawPath(mouthPath, color = Color(0xFF3E2723), style = Stroke(width = 2.5f * scaleFactor, cap = StrokeCap.Round))
                    }

                    // Whiskers
                    drawLine(Color.Black.copy(0.2f), Offset(centerX - 32f * scaleFactor, centerY - 20f * scaleFactor), Offset(centerX - 52f * scaleFactor, centerY - 24f * scaleFactor), strokeWidth = 2f)
                    drawLine(Color.Black.copy(0.2f), Offset(centerX - 32f * scaleFactor, centerY - 16f * scaleFactor), Offset(centerX - 55f * scaleFactor, centerY - 16f * scaleFactor), strokeWidth = 2f)
                    drawLine(Color.Black.copy(0.2f), Offset(centerX + 32f * scaleFactor, centerY - 20f * scaleFactor), Offset(centerX + 52f * scaleFactor, centerY - 24f * scaleFactor), strokeWidth = 2f)
                    drawLine(Color.Black.copy(0.2f), Offset(centerX + 32f * scaleFactor, centerY - 16f * scaleFactor), Offset(centerX + 55f * scaleFactor, centerY - 16f * scaleFactor), strokeWidth = 2f)

                    // Front Paws
                    drawCircle(color = Color(0xFFFFF8E7), radius = 6f * scaleFactor, center = Offset(centerX - 14f * scaleFactor, centerY + 36f * scaleFactor))
                    drawCircle(color = Color(0xFFFFF8E7), radius = 6f * scaleFactor, center = Offset(centerX + 14f * scaleFactor, centerY + 36f * scaleFactor))

                    // Sweat drops (only when panicked)
                    if (catState == CatState.PANICKED) {
                        drawCircle(Color(0xFF8AE9FF), radius = 3.5f * scaleFactor, center = Offset(centerX - 52f * scaleFactor - shakeX, centerY - 38f * scaleFactor))
                        drawCircle(Color(0xFF8AE9FF), radius = 3f * scaleFactor, center = Offset(centerX + 52f * scaleFactor + shakeX, centerY - 33f * scaleFactor))
                    }
                }
            } else {
                // Ngủ cuộn tròn mượt mà (dựa theo progress)
                val progress = sleepProgress.value
                
                if (progress < 0.5f) {
                    // Phase 1: Walking / running in from the left
                    val walkProgress = progress / 0.5f
                    val offsetX = -140f * scaleFactor * (1f - walkProgress)
                    
                    translate(offsetX, 0f) {
                        scale(scaleX = bodyScale, scaleY = bodyScale, pivot = Offset(centerX, centerY)) {
                            // Tail waving
                            val swing = sin(walkProgress * Math.PI.toFloat() * 6f)
                            val tailSwing = swing * 12f * scaleFactor
                            val tailPath = Path().apply {
                                moveTo(centerX - 40f * scaleFactor, centerY - 2f * scaleFactor)
                                quadraticTo(
                                    centerX - 60f * scaleFactor, centerY - 12f * scaleFactor + tailSwing,
                                    centerX - 66f * scaleFactor, centerY - 4f * scaleFactor + tailSwing
                                )
                            }
                            drawPath(
                                path = tailPath,
                                color = catColor,
                                style = Stroke(width = 7f * scaleFactor, cap = StrokeCap.Round)
                            )
                            val tailTipPath = Path().apply {
                                moveTo(centerX - 60f * scaleFactor, centerY - 8f * scaleFactor + tailSwing)
                                quadraticTo(
                                    centerX - 63f * scaleFactor, centerY - 6f * scaleFactor + tailSwing,
                                    centerX - 66f * scaleFactor, centerY - 4f * scaleFactor + tailSwing
                                )
                            }
                            drawPath(
                                path = tailTipPath,
                                color = Color(0xFFFFF8E7),
                                style = Stroke(width = 7f * scaleFactor, cap = StrokeCap.Round)
                            )

                            // 4 legs swinging
                            val swingLeg = sin(walkProgress * Math.PI.toFloat() * 6f) * 12f * scaleFactor
                            drawLine(catColor, Offset(centerX - 28f * scaleFactor, centerY + 18f * scaleFactor), Offset(centerX - 28f * scaleFactor + swingLeg, centerY + 34f * scaleFactor), strokeWidth = 7f * scaleFactor, cap = StrokeCap.Round)
                            drawLine(catColor, Offset(centerX - 16f * scaleFactor, centerY + 18f * scaleFactor), Offset(centerX - 16f * scaleFactor - swingLeg, centerY + 34f * scaleFactor), strokeWidth = 7f * scaleFactor, cap = StrokeCap.Round)
                            drawLine(catColor, Offset(centerX + 8f * scaleFactor, centerY + 18f * scaleFactor), Offset(centerX + 8f * scaleFactor + swingLeg, centerY + 34f * scaleFactor), strokeWidth = 7f * scaleFactor, cap = StrokeCap.Round)
                            drawLine(catColor, Offset(centerX + 20f * scaleFactor, centerY + 18f * scaleFactor), Offset(centerX + 20f * scaleFactor - swingLeg, centerY + 34f * scaleFactor), strokeWidth = 7f * scaleFactor, cap = StrokeCap.Round)

                            // Body (horizontal oval)
                            val bodyPath = Path().apply {
                                addOval(androidx.compose.ui.geometry.Rect(
                                    Offset(centerX - 42f * scaleFactor, centerY - 16f * scaleFactor),
                                    androidx.compose.ui.geometry.Size(68f * scaleFactor, 42f * scaleFactor)
                                ))
                            }
                            drawPath(bodyPath, brush = Brush.linearGradient(listOf(Color(0xFFFFD54F), catColor)))
                            drawOval(
                                color = Color(0xFFFFF8E7),
                                topLeft = Offset(centerX - 22f * scaleFactor, centerY + 6f * scaleFactor),
                                size = androidx.compose.ui.geometry.Size(35f * scaleFactor, 18f * scaleFactor)
                            )

                            // Ears
                            val leftEar = Path().apply {
                                moveTo(centerX + 18f * scaleFactor, centerY - 34f * scaleFactor)
                                lineTo(centerX + 8f * scaleFactor, centerY - 50f * scaleFactor)
                                lineTo(centerX + 26f * scaleFactor, centerY - 38f * scaleFactor)
                                close()
                            }
                            drawPath(leftEar, color = catColor)

                            val rightEar = Path().apply {
                                moveTo(centerX + 28f * scaleFactor, centerY - 34f * scaleFactor)
                                lineTo(centerX + 22f * scaleFactor, centerY - 50f * scaleFactor)
                                lineTo(centerX + 36f * scaleFactor, centerY - 38f * scaleFactor)
                                close()
                            }
                            drawPath(rightEar, color = catColor)

                            // Head
                            drawCircle(
                                brush = Brush.linearGradient(listOf(Color(0xFFFFE082), catColor)),
                                radius = 25f * scaleFactor,
                                center = Offset(centerX + 28f * scaleFactor, centerY - 16f * scaleFactor)
                            )

                            // Cheek patches
                            drawCircle(
                                color = Color(0xFFFFF8E7),
                                radius = 8f * scaleFactor,
                                center = Offset(centerX + 32f * scaleFactor, centerY - 12f * scaleFactor)
                            )

                            // Eyes looking forward
                            drawCircle(Color.Black, radius = 2.5f * scaleFactor, center = Offset(centerX + 34f * scaleFactor, centerY - 20f * scaleFactor))
                            drawCircle(Color.Black, radius = 2.5f * scaleFactor, center = Offset(centerX + 23f * scaleFactor, centerY - 20f * scaleFactor))

                            // Cheek blush
                            drawCircle(Color(0xFFFF8E9E).copy(alpha = 0.5f), radius = 4f * scaleFactor, center = Offset(centerX + 30f * scaleFactor, centerY - 10f * scaleFactor))

                            // Nose
                            drawCircle(Color(0xFFFF5252), radius = 1.8f * scaleFactor, center = Offset(centerX + 29f * scaleFactor, centerY - 14f * scaleFactor))
                        }
                    }
                } else {
                    // Phase 2: Sitting and curling up to sleep (progress >= 0.5f)
                    val sitProgress = (progress - 0.5f) / 0.5f
                    
                    scale(scaleX = bodyScale, scaleY = bodyScale, pivot = Offset(centerX, centerY)) {
                        // Tail sweeping and wrapping around body
                        val p0 = Offset(lerp(centerX - 40f * scaleFactor, centerX + 35f * scaleFactor, sitProgress), lerp(centerY - 2f * scaleFactor, centerY + 18f * scaleFactor, sitProgress))
                        val p1 = Offset(lerp(centerX - 60f * scaleFactor, centerX + 56f * scaleFactor, sitProgress), lerp(centerY - 12f * scaleFactor, centerY + 25f * scaleFactor, sitProgress))
                        val p2 = Offset(lerp(centerX - 66f * scaleFactor, centerX + 40f * scaleFactor, sitProgress), lerp(centerY - 4f * scaleFactor, centerY - 2f * scaleFactor, sitProgress))
                        
                        val tailPath = Path().apply {
                            moveTo(p0.x, p0.y)
                            quadraticTo(p1.x, p1.y, p2.x, p2.y)
                        }
                        drawPath(
                            path = tailPath,
                            color = catColor.copy(green = catColor.green * 0.92f),
                            style = Stroke(width = lerp(7f * scaleFactor, 9f * scaleFactor, sitProgress), cap = StrokeCap.Round)
                        )
                        val tailTipPath = Path().apply {
                            moveTo(lerp(p0.x, p2.x, 0.7f), lerp(p0.y, p2.y, 0.7f))
                            quadraticTo(p1.x, p1.y, p2.x, p2.y)
                        }
                        drawPath(
                            path = tailTipPath,
                            color = Color(0xFFFFF8E7),
                            style = Stroke(width = lerp(7f * scaleFactor, 9f * scaleFactor, sitProgress), cap = StrokeCap.Round)
                        )

                        // Legs shrinking
                        val legLength = lerp(16f * scaleFactor, 0f, sitProgress)
                        val legStroke = lerp(7f * scaleFactor, 0f, sitProgress)
                        if (legStroke > 0f) {
                            drawLine(catColor, Offset(centerX - 28f * scaleFactor, centerY + 18f * scaleFactor), Offset(centerX - 28f * scaleFactor, centerY + 18f * scaleFactor + legLength), strokeWidth = legStroke, cap = StrokeCap.Round)
                            drawLine(catColor, Offset(centerX - 16f * scaleFactor, centerY + 18f * scaleFactor), Offset(centerX - 16f * scaleFactor, centerY + 18f * scaleFactor + legLength), strokeWidth = legStroke, cap = StrokeCap.Round)
                            drawLine(catColor, Offset(centerX + 8f * scaleFactor, centerY + 18f * scaleFactor), Offset(centerX + 8f * scaleFactor, centerY + 18f * scaleFactor + legLength), strokeWidth = legStroke, cap = StrokeCap.Round)
                            drawLine(catColor, Offset(centerX + 20f * scaleFactor, centerY + 18f * scaleFactor), Offset(centerX + 20f * scaleFactor, centerY + 18f * scaleFactor + legLength), strokeWidth = legStroke, cap = StrokeCap.Round)
                        }

                        // Body
                        val bodyCenterX = lerp(centerX - 8f * scaleFactor, centerX, sitProgress)
                        val bodyCenterY = lerp(centerY + 5f * scaleFactor, centerY + 12f * scaleFactor, sitProgress)
                        val bodyW = lerp(68f * scaleFactor, 112f * scaleFactor, sitProgress)
                        val bodyH = lerp(42f * scaleFactor, 58f * scaleFactor, sitProgress)
                        
                        drawOval(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFFD54F), catColor),
                                start = Offset(bodyCenterX - bodyW/2, bodyCenterY - bodyH/2),
                                end = Offset(bodyCenterX + bodyW/2, bodyCenterY + bodyH/2)
                            ),
                            topLeft = Offset(bodyCenterX - bodyW/2, bodyCenterY - bodyH/2),
                            size = androidx.compose.ui.geometry.Size(bodyW, bodyH)
                        )

                        // Body White Belly
                        val bellyAlpha = sitProgress
                        drawOval(
                            color = Color(0xFFFFF8E7).copy(alpha = bellyAlpha),
                            topLeft = Offset(bodyCenterX - 24f * scaleFactor, bodyCenterY - 6f * scaleFactor),
                            size = androidx.compose.ui.geometry.Size(48f * scaleFactor, 24f * scaleFactor)
                        )

                        // Ears
                        val e1_l = Offset(lerp(centerX + 18f * scaleFactor, centerX - 18f * scaleFactor, sitProgress), lerp(centerY - 34f * scaleFactor, centerY - 28f * scaleFactor, sitProgress))
                        val e2_l = Offset(lerp(centerX + 8f * scaleFactor, centerX - 38f * scaleFactor, sitProgress), lerp(centerY - 50f * scaleFactor, centerY - 42f * scaleFactor, sitProgress))
                        val e3_l = Offset(lerp(centerX + 26f * scaleFactor, centerX - 6f * scaleFactor, sitProgress), lerp(centerY - 38f * scaleFactor, centerY - 36f * scaleFactor, sitProgress))
                        val leftEarPath = Path().apply {
                            moveTo(e1_l.x, e1_l.y)
                            lineTo(e2_l.x, e2_l.y)
                            lineTo(e3_l.x, e3_l.y)
                            close()
                        }
                        drawPath(leftEarPath, color = catColor)

                        val e1_r = Offset(lerp(centerX + 28f * scaleFactor, centerX + 18f * scaleFactor, sitProgress), lerp(centerY - 34f * scaleFactor, centerY - 28f * scaleFactor, sitProgress))
                        val e2_r = Offset(lerp(centerX + 22f * scaleFactor, centerX + 38f * scaleFactor, sitProgress), lerp(centerY - 50f * scaleFactor, centerY - 42f * scaleFactor, sitProgress))
                        val e3_r = Offset(lerp(centerX + 36f * scaleFactor, centerX + 6f * scaleFactor, sitProgress), lerp(centerY - 38f * scaleFactor, centerY - 36f * scaleFactor, sitProgress))
                        val rightEarPath = Path().apply {
                            moveTo(e1_r.x, e1_r.y)
                            lineTo(e2_r.x, e2_r.y)
                            lineTo(e3_r.x, e3_r.y)
                            close()
                        }
                        drawPath(rightEarPath, color = catColor)

                        // Pink Inner Ears
                        if (sitProgress > 0.3f) {
                            val innerEAlpha = (sitProgress - 0.3f) / 0.7f
                            val leftInner = Path().apply {
                                moveTo(lerp(e1_l.x, e3_l.x, 0.15f), lerp(e1_l.y, e3_l.y, 0.15f))
                                lineTo(lerp(e1_l.x, e2_l.x, 0.8f), lerp(e1_l.y, e2_l.y, 0.8f))
                                lineTo(lerp(e1_l.x, e3_l.x, 0.8f), lerp(e1_l.y, e3_l.y, 0.8f))
                                close()
                            }
                            drawPath(leftInner, color = Color(0xFFFFD1DC).copy(alpha = innerEAlpha))

                            val rightInner = Path().apply {
                                moveTo(lerp(e1_r.x, e3_r.x, 0.15f), lerp(e1_r.y, e3_r.y, 0.15f))
                                lineTo(lerp(e1_r.x, e2_r.x, 0.8f), lerp(e1_r.y, e2_r.y, 0.8f))
                                lineTo(lerp(e1_r.x, e3_r.x, 0.8f), lerp(e1_r.y, e3_r.y, 0.8f))
                                close()
                            }
                            drawPath(rightInner, color = Color(0xFFFFD1DC).copy(alpha = innerEAlpha))
                        }

                        // Head
                        val headCenterX = lerp(centerX + 28f * scaleFactor, centerX, sitProgress)
                        val headCenterY = lerp(centerY - 16f * scaleFactor, centerY - 8f * scaleFactor, sitProgress)
                        val headR = lerp(25f * scaleFactor, 34f * scaleFactor, sitProgress)
                        
                        drawCircle(
                            brush = Brush.linearGradient(listOf(Color(0xFFFFE082), catColor)),
                            radius = headR,
                            center = Offset(headCenterX, headCenterY)
                        )

                        // Head Cheek White Patches
                        drawCircle(
                            color = Color(0xFFFFF8E7),
                            radius = lerp(8f * scaleFactor, 10f * scaleFactor, sitProgress),
                            center = Offset(lerp(headCenterX + 4f * scaleFactor, headCenterX - 15f * scaleFactor, sitProgress), lerp(headCenterY + 4f * scaleFactor, headCenterY - 4f * scaleFactor, sitProgress))
                        )
                        if (sitProgress > 0.4f) {
                            drawCircle(
                                color = Color(0xFFFFF8E7).copy(alpha = (sitProgress - 0.4f) / 0.6f),
                                radius = 10f * scaleFactor,
                                center = Offset(headCenterX + 15f * scaleFactor, headCenterY - 4f * scaleFactor)
                            )
                        }

                        // Eyes
                        if (sitProgress < 0.6f) {
                            drawCircle(Color.Black, radius = 2.5f * scaleFactor, center = Offset(headCenterX + 6f * scaleFactor, headCenterY - 4f * scaleFactor))
                            drawCircle(Color.Black, radius = 2.5f * scaleFactor, center = Offset(headCenterX - 6f * scaleFactor, headCenterY - 4f * scaleFactor))
                        } else {
                            val eyeProgress = (sitProgress - 0.6f) / 0.4f
                            val eyePathLeft = Path().apply {
                                moveTo(headCenterX - 20f * scaleFactor, headCenterY - 8f * scaleFactor)
                                quadraticTo(headCenterX - 12f * scaleFactor, headCenterY - 3f * scaleFactor, headCenterX - 5f * scaleFactor, headCenterY - 8f * scaleFactor)
                            }
                            drawPath(
                                path = eyePathLeft,
                                color = Color.Black.copy(alpha = 0.4f * eyeProgress),
                                style = Stroke(width = 2.8f * scaleFactor, cap = StrokeCap.Round)
                            )

                            val eyePathRight = Path().apply {
                                moveTo(headCenterX + 5f * scaleFactor, headCenterY - 8f * scaleFactor)
                                quadraticTo(headCenterX + 12f * scaleFactor, headCenterY - 3f * scaleFactor, headCenterX + 20f * scaleFactor, headCenterY - 8f * scaleFactor)
                            }
                            drawPath(
                                path = eyePathRight,
                                color = Color.Black.copy(alpha = 0.4f * eyeProgress),
                                style = Stroke(width = 2.8f * scaleFactor, cap = StrokeCap.Round)
                            )
                        }

                        // Cheek blush
                        val blushAlpha = lerp(0.5f, 0.4f, sitProgress)
                        drawCircle(Color(0xFFFF8E9E).copy(alpha = blushAlpha), radius = lerp(4f * scaleFactor, 5f * scaleFactor, sitProgress), center = Offset(headCenterX + lerp(2f * scaleFactor, 16f * scaleFactor, sitProgress), headCenterY + lerp(6f * scaleFactor, 2f * scaleFactor, sitProgress)))
                        if (sitProgress > 0.5f) {
                            drawCircle(Color(0xFFFF8E9E).copy(alpha = blushAlpha * (sitProgress - 0.5f) / 0.5f), radius = 5f * scaleFactor, center = Offset(headCenterX - 16f * scaleFactor, headCenterY + 2f * scaleFactor))
                        }

                        // Nose
                        val noseC = Offset(headCenterX, headCenterY - 2f * scaleFactor)
                        val nosePath = Path().apply {
                            moveTo(noseC.x - 2f * scaleFactor, noseC.y - 1f * scaleFactor)
                            lineTo(noseC.x + 2f * scaleFactor, noseC.y - 1f * scaleFactor)
                            lineTo(noseC.x, noseC.y + 1f * scaleFactor)
                            close()
                        }
                        drawPath(nosePath, color = Color(0xFFFF85A1))

                        // Sleeping mouth
                        if (sitProgress > 0.5f) {
                            val mouthAlpha = (sitProgress - 0.5f) / 0.5f
                            val mouthPath = Path().apply {
                                moveTo(headCenterX - 3.5f * scaleFactor, headCenterY + 1f * scaleFactor)
                                quadraticTo(headCenterX - 1.5f * scaleFactor, headCenterY + 3.5f * scaleFactor, headCenterX, headCenterY + 1f * scaleFactor)
                                quadraticTo(headCenterX + 1.5f * scaleFactor, headCenterY + 3.5f * scaleFactor, headCenterX + 3.5f * scaleFactor, headCenterY + 1f * scaleFactor)
                            }
                            drawPath(
                                path = mouthPath,
                                color = Color.Black.copy(alpha = 0.3f * mouthAlpha),
                                style = Stroke(width = 1.8f * scaleFactor, cap = StrokeCap.Round)
                            )
                        }

                        // Whiskers
                        val whiskerAlpha = lerp(0.3f, 0.5f, sitProgress)
                        drawLine(Color.White.copy(alpha = whiskerAlpha), Offset(headCenterX - 26f * scaleFactor, headCenterY - 1f * scaleFactor), Offset(headCenterX - 44f * scaleFactor, headCenterY + 1f * scaleFactor), strokeWidth = 2f)
                        drawLine(Color.White.copy(alpha = whiskerAlpha), Offset(headCenterX - 26f * scaleFactor, headCenterY + 3f * scaleFactor), Offset(headCenterX - 42f * scaleFactor, headCenterY + 7f * scaleFactor), strokeWidth = 2f)
                        drawLine(Color.White.copy(alpha = whiskerAlpha), Offset(headCenterX + 26f * scaleFactor, headCenterY - 1f * scaleFactor), Offset(headCenterX + 44f * scaleFactor, headCenterY + 1f * scaleFactor), strokeWidth = 2f)
                        drawLine(Color.White.copy(alpha = whiskerAlpha), Offset(headCenterX + 26f * scaleFactor, headCenterY + 3f * scaleFactor), Offset(headCenterX + 42f * scaleFactor, headCenterY + 7f * scaleFactor), strokeWidth = 2f)
                    }

                    // ZZZ sleep bubble letters
                    if (progress > 0.8f) {
                        val fadeZ = (progress - 0.8f) / 0.2f
                        if (zzzAlpha1 > 0f) {
                            drawZ(centerX + 36f * scaleFactor + zzzAlpha1 * 20f * scaleFactor, centerY - 32f * scaleFactor + zzzAlpha1 * -40f * scaleFactor, size = 12f * scaleFactor, alpha = (1f - zzzAlpha1) * fadeZ)
                        }
                        if (zzzAlpha2 > 0f) {
                            drawZ(centerX + 36f * scaleFactor + zzzAlpha2 * 14f * scaleFactor, centerY - 32f * scaleFactor + zzzAlpha2 * -50f * scaleFactor, size = 9f * scaleFactor, alpha = (1f - zzzAlpha2) * fadeZ)
                        }
                        if (zzzAlpha3 > 0f) {
                            drawZ(centerX + 36f * scaleFactor + zzzAlpha3 * 18f * scaleFactor, centerY - 32f * scaleFactor + zzzAlpha3 * -45f * scaleFactor, size = 7f * scaleFactor, alpha = (1f - zzzAlpha3) * fadeZ)
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawZ(
    x: Float, y: Float, size: Float, alpha: Float
) {
    val path = Path().apply {
        moveTo(x, y)
        lineTo(x + size, y)
        lineTo(x, y + size)
        lineTo(x + size, y + size)
    }
    drawPath(
        path = path,
        color = Color(0xFF818CF8).copy(alpha = alpha),
        style = Stroke(
            width = size / 4f,
            cap = StrokeCap.Round
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun getCategoryDisplayName(category: String): String {
    val resId = when (category) {
        "Entertainment" -> R.string.category_entertainment
        "Utilities"     -> R.string.category_utilities
        "Work"          -> R.string.category_work
        "Cloud"         -> R.string.category_cloud
        "Music"         -> R.string.category_music
        "Food"          -> R.string.category_food
        "Finance"       -> R.string.category_finance
        "Anniversary"   -> R.string.category_anniversary
        "Family"        -> R.string.category_family
        "Trial"         -> R.string.category_trial
        "Notes"         -> R.string.category_notes
        else            -> R.string.category_other
    }
    return stringResource(resId)
}

@Composable
fun CashflowForecastingChart(subscriptions: List<Subscription>, modifier: Modifier = Modifier) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val forecasts = FinanceCalculator.getForecastingData(subscriptions, locale)
    val maxForecast = forecasts.maxByOrNull { it.amount }
    val maxVal = forecasts.maxOfOrNull { it.amount } ?: 0.0
    val displayMaxVal = if (maxVal == 0.0) 100000.0 else maxVal * 1.15

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val highlightColor = MaterialTheme.colorScheme.error

    val pathProgress = remember { Animatable(0f) }
    val pointProgress = remember { Animatable(0f) }
    
    LaunchedEffect(forecasts) {
        launch {
            pathProgress.snapTo(0f)
            pathProgress.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))
        }
        launch {
            pointProgress.snapTo(0f)
            delay(500)
            pointProgress.animateTo(1f, tween(800, easing = EaseOutBack))
        }
    }

    Column(modifier = modifier.padding(24.dp)) {
        Text(
            text = stringResource(R.string.chart_forecasting_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (maxForecast != null && maxForecast.amount > 0.0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.chart_forecasting_peak_desc,
                    maxForecast.monthName,
                    CurrencyFormatter.format(maxForecast.amount, "VND", locale)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
        ) {
            val width = size.width
            val height = size.height
            val paddingLeft = 40f
            val paddingRight = 40f
            val paddingTop = 20f
            val paddingBottom = 40f
            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            for (i in 0..3) {
                val y = paddingTop + chartHeight * (i.toFloat() / 3)
                drawLine(
                    color = gridColor.copy(alpha = 0.3f),
                    start = Offset(paddingLeft, y),
                    end = Offset(paddingLeft + chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val points = forecasts.mapIndexed { index, forecast ->
                val x = paddingLeft + chartWidth * (index.toFloat() / (forecasts.size - 1))
                val y = if (displayMaxVal > 0.0) {
                    paddingTop + chartHeight * (1f - (forecast.amount / displayMaxVal).toFloat())
                } else {
                    paddingTop + chartHeight
                }
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                val fillPath = Path().apply {
                    moveTo(points.first().x, paddingTop + chartHeight)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, paddingTop + chartHeight)
                    close()
                }
                
                // Animate fill alpha
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.2f * pathProgress.value), Color.Transparent),
                        startY = points.minOf { it.y },
                        endY = paddingTop + chartHeight
                    )
                )
            }

            if (points.size > 1) {
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cp1 = Offset(prev.x + (curr.x - prev.x) / 2f, prev.y)
                        val cp2 = Offset(prev.x + (curr.x - prev.x) / 2f, curr.y)
                        cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
                    }
                }
                
                // Use a measure to draw only partial path if needed, but for simplicity we'll just use pathProgress
                // For a more advanced path animation, one would use PathMeasure.
                // Here we'll just draw the stroke and animate alpha or use a simpler trick.
                // Actually, let's use pathProgress to clip the drawing area.
                
                clipRect(
                    right = paddingLeft + chartWidth * pathProgress.value
                ) {
                    drawPath(strokePath, color = primaryColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                }
            }

            val textPaint = Paint().apply {
                color = textColor.toArgb()
                textSize = 9.dp.toPx()
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            val amountPaint = Paint().apply {
                color = primaryColor.toArgb()
                textSize = 8.dp.toPx()
                textAlign = Paint.Align.CENTER
            }

            points.forEachIndexed { index, point ->
                val forecast = forecasts[index]
                val isMax = forecast == maxForecast && forecast.amount > 0.0
                
                // Animate point appearance
                val scale = if (index / (points.size - 1f) <= pathProgress.value) pointProgress.value else 0f

                if (scale > 0f) {
                    drawCircle(
                        color = if (isMax) highlightColor else primaryColor,
                        radius = (if (isMax) 5.dp.toPx() else 3.5f.dp.toPx()) * scale,
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = (if (isMax) 2.5f.dp.toPx() else 1.8f.dp.toPx()) * scale,
                        center = point
                    )
                    
                    // Month name
                    drawContext.canvas.nativeCanvas.drawText(
                        forecast.monthName, point.x, height - 6.dp.toPx(), textPaint.apply { alpha = (255 * scale).toInt() }
                    )
                    
                    if (forecast.amount > 0.0) {
                        val amountText = if (forecast.amount >= 1000000.0) {
                            String.format(locale, "%.1fM", forecast.amount / 1000000.0)
                        } else {
                            String.format(locale, "%.0fk", forecast.amount / 1000.0)
                        }
                        val paintToUse = if (isMax) {
                            Paint(amountPaint).apply {
                                color = highlightColor.toArgb()
                                typeface = Typeface.DEFAULT_BOLD
                            }
                        } else amountPaint
                        drawContext.canvas.nativeCanvas.drawText(amountText, point.x, point.y - 8.dp.toPx(), paintToUse.apply { alpha = (255 * scale).toInt() })
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Payment History Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PaymentHistorySection(paymentHistory: List<PaymentHistory>, modifier: Modifier = Modifier) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", locale) }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.payment_history_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            if (paymentHistory.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        // Show actual count capped at 5
                        text = if (minOf(paymentHistory.size, 5) == 1) {
                            stringResource(R.string.dashboard_history_count_single)
                        } else {
                            stringResource(R.string.dashboard_history_count, minOf(paymentHistory.size, 5))
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (paymentHistory.isEmpty()) {
            Text(
                text = stringResource(R.string.payment_history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                paymentHistory.take(5).forEach { record ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.subscriptionName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = dateFormat.format(Date(record.paymentDate)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "-${CurrencyFormatter.format(record.amount, record.currency, locale)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}