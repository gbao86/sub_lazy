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
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.CategoryUtils
import com.gbao86.sub_lazy.ui.DateUtils
import com.gbao86.sub_lazy.ui.VietQRGenerator
import com.gbao86.sub_lazy.ui.FinanceCalculator
import com.gbao86.sub_lazy.ui.BankruptcyRunwayResult
import com.gbao86.sub_lazy.ui.MonthlyForecast
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.viewmodel.DashboardViewModel
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
    viewModel: DashboardViewModel = viewModel(),
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
                                    subscriptions.filter { it.cycle != BillingCycle.ONE_TIME && it.cycle != BillingCycle.YEARLY }
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
                                
                                val hasTrialExpiringSoon = remember(subscriptions) {
                                    subscriptions.any { it.category == SubscriptionCategory.TRIAL && (it.nextBillingDate - System.currentTimeMillis() <= 86400000L * 3) && (it.nextBillingDate >= System.currentTimeMillis()) }
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
                                            template.cycle.displayName,
                                            template.category.displayName,
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


@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    Sub_lazyTheme {
        DashboardContent(
            totalMonthlyCost = 1250000.0,
            spendingByCategory = listOf(
                CategorySpending(SubscriptionCategory.ENTERTAINMENT, 500000.0),
                CategorySpending(SubscriptionCategory.WORK, 300000.0),
                CategorySpending(SubscriptionCategory.UTILITIES, 450000.0)
            ),
            subscriptions = listOf(
                Subscription(id = 1, name = "Netflix", amount = 260000.0, cycle = BillingCycle.MONTHLY, category = SubscriptionCategory.ENTERTAINMENT, nextBillingDate = System.currentTimeMillis() + 86400000 * 2, colorHex = "#6366F1"),
                Subscription(id = 2, name = "Spotify", amount = 59000.0, cycle = BillingCycle.MONTHLY, category = SubscriptionCategory.MUSIC, nextBillingDate = System.currentTimeMillis() + 86400000 * 5, colorHex = "#06B6D4")
            ),
            paymentHistory = listOf(
                PaymentHistory(id = 1, subscriptionId = 1, subscriptionName = "Netflix", amount = 260000.0, currency = SubscriptionCurrency.VND, paymentDate = System.currentTimeMillis() - 86400000, cycle = BillingCycle.MONTHLY)
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
