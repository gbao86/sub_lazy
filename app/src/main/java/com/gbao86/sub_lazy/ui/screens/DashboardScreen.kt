/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational, 
and non-commercial purposes. 
Commercial exploitation, sale, or distribution of this software or any derivative works 
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy.ui.screens

import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Size
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import com.gbao86.sub_lazy.data.PaymentHistory
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.drawBehind
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.gbao86.sub_lazy.ui.toComposeColor
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.model.CategorySpending
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.CategoryUtils
import com.gbao86.sub_lazy.ui.DateUtils
import com.gbao86.sub_lazy.ui.FinanceCalculator
import com.gbao86.sub_lazy.ui.BankruptcyRunwayResult
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.viewmodel.DashboardViewModel
import java.util.*
import kotlin.math.sin
import kotlin.math.cos
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.gbao86.sub_lazy.data.api.BillOcrService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAdd: (String?, Double?, String?, String?, String?, String?, String?, String?) -> Unit,
    onNavigateToList: () -> Unit
) {
    val totalMonthlyCost by viewModel.totalMonthlyCost.collectAsStateWithLifecycle(initialValue = 0.0)
    val spendingByCategory by viewModel.spendingByCategory.collectAsStateWithLifecycle(initialValue = emptyList())
    val subscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val paymentHistory by viewModel.allPaymentHistory.collectAsStateWithLifecycle(initialValue = emptyList())
    val userBalance by viewModel.userBalance.collectAsStateWithLifecycle(initialValue = 2000000.0)
    val budgetResetDay by viewModel.budgetResetDay.collectAsStateWithLifecycle(initialValue = 1)
    val sharedMembersMap by viewModel.sharedMembersMap.collectAsStateWithLifecycle(initialValue = emptyMap())
    val context = LocalContext.current

    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            viewModel.exportData(uri) { result ->
                when (result) {
                    is com.gbao86.sub_lazy.data.BackupResult.Success -> android.widget.Toast.makeText(context, "Export thành công!", android.widget.Toast.LENGTH_SHORT).show()
                    is com.gbao86.sub_lazy.data.BackupResult.InvalidBackupFile -> android.widget.Toast.makeText(context, "Tệp xuất không hợp lệ. Vui lòng thử lại.", android.widget.Toast.LENGTH_SHORT).show()
                    is com.gbao86.sub_lazy.data.BackupResult.PermissionDenied -> android.widget.Toast.makeText(context, "Không có quyền lưu file. Vui lòng cấp quyền bộ nhớ.", android.widget.Toast.LENGTH_SHORT).show()
                    else -> android.widget.Toast.makeText(context, "Đã xảy ra lỗi không xác định khi xuất file.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.importData(uri) { result ->
                when (result) {
                    is com.gbao86.sub_lazy.data.BackupResult.Success -> android.widget.Toast.makeText(context, "Khôi phục dữ liệu thành công!", android.widget.Toast.LENGTH_SHORT).show()
                    is com.gbao86.sub_lazy.data.BackupResult.InvalidBackupFile -> android.widget.Toast.makeText(context, "Tệp sao lưu không hợp lệ hoặc đã bị hỏng.", android.widget.Toast.LENGTH_LONG).show()
                    is com.gbao86.sub_lazy.data.BackupResult.PermissionDenied -> android.widget.Toast.makeText(context, "Không thể đọc tệp. Vui lòng kiểm tra lại quyền truy cập.", android.widget.Toast.LENGTH_SHORT).show()
                    else -> android.widget.Toast.makeText(context, "Khôi phục thất bại. Vui lòng thử lại sau.", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val recoverDriveAuthLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.syncToDrive { success, errorMessage, _ ->
                if (success) {
                    android.widget.Toast.makeText(context, "Đồng bộ Drive thành công!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(context, "Đồng bộ thất bại: ${errorMessage ?: "Lỗi không xác định"}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            android.widget.Toast.makeText(context, "Bạn đã hủy cấp quyền Google Drive.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DashboardContent(
            totalMonthlyCost = totalMonthlyCost,
            spendingByCategory = spendingByCategory,
            subscriptions = subscriptions,
            paymentHistory = paymentHistory,
            userBalance = userBalance,
            budgetResetDay = budgetResetDay,
            sharedMembersMap = sharedMembersMap,
            onUpdateUserBalance = { viewModel.updateUserBalance(it) },
            onUpdateBudgetResetDay = { viewModel.updateBudgetResetDay(it) },
            onMarkAsPaid = { viewModel.markAsPaid(it) },
            onCheckInSession = { viewModel.checkInSession(it) },
            onToggleMemberPaidStatus = { sub, name -> viewModel.toggleMemberPaidStatus(sub, name) },
            onNavigateToAdd = onNavigateToAdd,
            onNavigateToList = onNavigateToList,
            onExport = { exportLauncher.launch("sub_lazy_backup.json") },
            onImport = { importLauncher.launch(arrayOf("application/json")) },
            onSyncToDrive = {
                viewModel.syncToDrive { success, errorMessage, intent ->
                    if (success) {
                        android.widget.Toast.makeText(context, "Đồng bộ Drive thành công!", android.widget.Toast.LENGTH_SHORT).show()
                    } else if (intent != null) {
                        recoverDriveAuthLauncher.launch(intent)
                    } else {
                        android.widget.Toast.makeText(context, "Đồng bộ thất bại: ${errorMessage ?: "Lỗi không xác định"}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
        
        if (isProcessing) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { /* Do nothing */ },
                properties = androidx.compose.ui.window.DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
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
    sharedMembersMap: Map<Long, List<com.gbao86.sub_lazy.data.SharedMember>> = emptyMap(),
    onUpdateUserBalance: (Double) -> Unit,
    onUpdateBudgetResetDay: (Int) -> Unit,
    onMarkAsPaid: (Subscription) -> Unit,
    onCheckInSession: (Subscription) -> Unit,
    onToggleMemberPaidStatus: (Subscription, String) -> Unit,
    onNavigateToAdd: (String?, Double?, String?, String?, String?, String?, String?, String?) -> Unit,
    onNavigateToList: () -> Unit,
    onExport: () -> Unit = {},
    onImport: () -> Unit = {},
    onSyncToDrive: () -> Unit = {}
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]

    var showBalanceEditDialog by remember { mutableStateOf(false) }
    var tickTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000)
            tickTrigger++
        }
    }

    if (showBalanceEditDialog) {
        BudgetEditorSheet(
            userBalance = userBalance,
            budgetResetDay = budgetResetDay,
            locale = locale,
            onDismiss = { showBalanceEditDialog = false },
            onSave = { newBalance, newResetDay ->
                onUpdateUserBalance(newBalance)
                onUpdateBudgetResetDay(newResetDay)
                showBalanceEditDialog = false
            }
        )
    }

    var selectedCategory by remember { mutableStateOf<CategorySpending?>(null) }
    var selectedUpcomingSub by remember { mutableStateOf<Subscription?>(null) }

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val billOcrService = remember { BillOcrService(context) }

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
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.readonly"), Scope("https://www.googleapis.com/auth/drive.file"))
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
            billOcrService.analyzeImage(uri, object : BillOcrService.BillOcrCallback {
                override fun onSuccess(result: BillOcrService.ParsedSubscription) {
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
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    var showLangMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showLangMenu = true }) {
                        Icon(Icons.Rounded.Language, contentDescription = "Language", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
                icon = { Icon(Icons.Rounded.Add, contentDescription = "Add Subscription") },
                text = { Text(stringResource(R.string.action_add), fontWeight = FontWeight.SemiBold) }
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
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        text = { Text(stringResource(R.string.dashboard_tab_overview), fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = "Dashboard") }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        text = { Text(stringResource(R.string.dashboard_tab_analysis), fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Rounded.Analytics, contentDescription = "Analytics") }
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
                                HeroSpendingCard(
                                    totalMonthlyCost = totalMonthlyCost,
                                    subscriptions = subscriptions,
                                    userBalance = userBalance,
                                    locale = locale,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // ── Lazy Wallet Pet & Financial Health Card ────────────────
                            item {
                                LazyWalletHealthCard(
                                    subscriptions = subscriptions,
                                    userBalance = userBalance,
                                    budgetResetDay = budgetResetDay,
                                    tickTrigger = tickTrigger,
                                    locale = locale,
                                    onEditBudgetClick = { showBalanceEditDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                )
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
                                        contentDescription = "Manage subscriptions icon",
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
                                        contentDescription = "Navigate to list",
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
                                            sharedMembersMap = sharedMembersMap,
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
                                            val primaryContainer = MaterialTheme.colorScheme.primaryContainer
                                            Box(
                                                modifier = Modifier
                                                    .size(88.dp)
                                                    .clip(RoundedCornerShape(28.dp))
                                                    .drawBehind {
                                                        drawRect(
                                                            brush = Brush.radialGradient(
                                                                listOf(
                                                                    primaryContainer.copy(alpha = pulsAlpha),
                                                                    primaryContainer.copy(alpha = 0.1f)
                                                                )
                                                            )
                                                        )
                                                    },
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
        AddActionBottomSheet(
            onDismiss = { showAddBottomSheet.value = false },
            onAddManually = { onNavigateToAdd(null, null, null, null, null, null, null, null) },
            onScanScreenshot = { imagePickerLauncher.launch("image/*") },
            onAddFromTemplate = { showTemplatesDialog.value = true }
        )
    }

    if (showTemplatesDialog.value) {
        TemplatesDialog(
            locale = locale,
            onDismiss = { showTemplatesDialog.value = false },
            onTemplateSelected = { name, amount, cycle, category, colorHex, bankName, bankAccount, bankHolder ->
                onNavigateToAdd(name, amount, cycle, category, colorHex, bankName, bankAccount, bankHolder)
            }
        )
    }

    if (showSettingsDialog.value) {
        SettingsDialog(
            context = context,
            linkedAccountEmail = linkedAccountEmail,
            googleSignInClient = googleSignInClient,
            onDismiss = { showSettingsDialog.value = false },
            onEmailChanged = { linkedAccountEmail = it },
            onGoogleSignIn = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
            onExport = onExport,
            onImport = onImport,
            onSyncToDrive = onSyncToDrive
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
