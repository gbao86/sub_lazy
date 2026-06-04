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

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.gbao86.sub_lazy.data.PaymentHistory
import java.text.SimpleDateFormat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.viewmodel.SubscriptionViewModel
import java.util.*
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.gbao86.sub_lazy.data.api.GeminiService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
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

    DashboardContent(
        totalMonthlyCost = totalMonthlyCost,
        spendingByCategory = spendingByCategory,
        subscriptions = subscriptions,
        paymentHistory = paymentHistory,
        onMarkAsPaid = { viewModel.markAsPaid(it) },
        onInsertSubscription = { viewModel.insert(it) },
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
    onMarkAsPaid: (Subscription) -> Unit,
    onInsertSubscription: (Subscription) -> Unit,
    onNavigateToAdd: (String?, Double?, String?, String?, String?, String?, String?, String?) -> Unit,
    onNavigateToList: () -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]

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
            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("gmail_account", email)
                .apply()
            linkedAccountEmail = email
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: ApiException) {
            e.printStackTrace()
        }
    }

    var isAnalyzing by remember { mutableStateOf(false) }
    var showTemplatesDialog by remember { mutableStateOf(false) }
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
                        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAddBottomSheet by remember { mutableStateOf(false) }

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
                    IconButton(onClick = { showSettingsDialog = true }) {
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
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAddBottomSheet = true
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
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
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
                                .padding(horizontal = 28.dp, vertical = 28.dp)
                        ) {
                            // Decorative blur circles
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
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
                                    Spacer(modifier = Modifier.width(10.dp))
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
                                // Sub count badge
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${subscriptions.size} dịch vụ đang theo dõi",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Manage Subscriptions Button ──────────────────────────────
                item {
                    OutlinedButton(
                        onClick = onNavigateToList,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
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
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
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
                                            text = "${spendingByCategory.size} danh mục",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(28.dp))
                                InteractiveDonutChart(
                                    spending = spendingByCategory,
                                    totalSpending = spendingByCategory.sumOf { it.totalAmount },
                                    selectedCategory = selectedCategory,
                                    onCategorySelected = { selectedCategory = it },
                                    modifier = Modifier.size(220.dp)
                                )
                                Spacer(modifier = Modifier.height(28.dp))
                                InteractiveCategoryLegend(
                                    spending = spendingByCategory,
                                    totalSpending = spendingByCategory.sumOf { it.totalAmount },
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

                    // ── Upcoming Renewals Card ───────────────────────────────
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
                                modifier = Modifier.fillMaxWidth()
                            )
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
                                modifier = Modifier.fillMaxWidth().padding(40.dp),
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
                                Spacer(modifier = Modifier.height(20.dp))
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

            if (isAnalyzing) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (showAddBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showAddBottomSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ListItem(headlineContent = { Text(stringResource(R.string.action_add_manually)) }, leadingContent = { Icon(Icons.Rounded.Edit, null) }, modifier = Modifier.clickable {
                    showAddBottomSheet = false
                    onNavigateToAdd(null, null, null, null, null, null, null, null)
                })
                ListItem(headlineContent = { Text(stringResource(R.string.action_scan_screenshot)) }, leadingContent = { Icon(Icons.Rounded.PhotoCamera, null) }, modifier = Modifier.clickable {
                    showAddBottomSheet = false
                    imagePickerLauncher.launch("image/*")
                })
                ListItem(headlineContent = { Text("Thêm từ mẫu định kỳ") }, leadingContent = { Icon(Icons.Rounded.Bookmarks, null) }, modifier = Modifier.clickable {
                    showAddBottomSheet = false
                    showTemplatesDialog = true
                })
            }
        }
    }

    if (showTemplatesDialog) {
        var selectedTab by remember { mutableStateOf(0) } // 0: Digital, 1: Lifestyle
        AlertDialog(
            onDismissRequest = { showTemplatesDialog = false },
            title = { Text("Chọn mẫu định kỳ", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                            Text("Dịch vụ số", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                            Text("Đời sống", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    val templates = if (selectedTab == 0) {
                        SubscriptionTemplates.digitalTemplates
                    } else {
                        SubscriptionTemplates.lifestyleTemplates
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                    ) {
                        items(templates) { template ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    showTemplatesDialog = false
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
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(template.colorHex)).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (selectedTab == 0) Icons.Rounded.Devices else Icons.Rounded.DirectionsCar,
                                            contentDescription = null,
                                            tint = Color(android.graphics.Color.parseColor(template.colorHex)),
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
                TextButton(onClick = { showTemplatesDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val accountName = linkedAccountEmail
                    Text(text = if (accountName != null) stringResource(R.string.settings_gmail_linked, accountName) else stringResource(R.string.settings_gmail_not_linked))
                    Button(onClick = {
                        if (accountName != null) {
                            googleSignInClient.signOut().addOnCompleteListener {
                                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().remove("gmail_account").apply()
                                linkedAccountEmail = null
                            }
                        } else {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    }) {
                        Text(if (accountName != null) stringResource(R.string.settings_gmail_btn_unlink) else stringResource(R.string.settings_gmail_btn_link))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSettingsDialog = false }) { Text(stringResource(R.string.ok)) } }
        )
    }

}

@Composable
fun InteractiveDonutChart(
    spending: List<CategorySpending>,
    totalSpending: Double,
    selectedCategory: CategorySpending?,
    onCategorySelected: (CategorySpending?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(Color(0xFF6366F1), Color(0xFF06B6D4), Color(0xFFF43F5E), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF8B5CF6))
    val locale = LocalContext.current.resources.configuration.locales[0]
    val animateSweep = remember { Animatable(0f) }
    LaunchedEffect(spending) {
        animateSweep.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize().pointerInput(spending) {
            detectTapGestures { offset ->
                if (totalSpending <= 0.0) return@detectTapGestures
                val center = Offset(size.width / 2f, size.height / 2f)
                val dx = offset.x - center.x
                val dy = offset.y - center.y
                val dist = sqrt(dx * dx + dy * dy)
                val strokeWidthPx = 28.dp.toPx()
                if (dist >= (size.width/2 - strokeWidthPx*1.5) && dist <= size.width/2 + 10.dp.toPx()) {
                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                    if (angle < 0) angle += 360f
                    var currentAngle = 0f
                    spending.forEach { item ->
                        val sweep = ((item.totalAmount / totalSpending) * 360f).toFloat()
                        if (angle >= currentAngle && angle < currentAngle + sweep) {
                            onCategorySelected(if (selectedCategory?.category == item.category) null else item)
                            return@detectTapGestures
                        }
                        currentAngle += sweep
                    }
                } else onCategorySelected(null)
            }
        }) {
            var startAngle = -90f
            spending.forEachIndexed { index, item ->
                val sweepAngle = ((item.totalAmount / totalSpending) * 360f).toFloat() * animateSweep.value
                val isSelected = selectedCategory?.category == item.category
                val middleAngle = startAngle + sweepAngle / 2f
                val angleRad = Math.toRadians(middleAngle.toDouble())
                val shiftAmt = if (isSelected) 8.dp.toPx() else 0f
                val shiftX = (shiftAmt * kotlin.math.cos(angleRad)).toFloat()
                val shiftY = (shiftAmt * kotlin.math.sin(angleRad)).toFloat()

                // Deduct a tiny gap (2 degrees) between segments for visual separation
                val gap = if (spending.size > 1) 2f else 0f
                val finalSweepAngle = (sweepAngle - gap).coerceAtLeast(0f)
                val finalStartAngle = startAngle + gap / 2f

                drawArc(
                    color = colors[index % colors.size].copy(alpha = if (selectedCategory == null || isSelected) 1f else 0.3f),
                    startAngle = finalStartAngle,
                    sweepAngle = finalSweepAngle,
                    useCenter = false,
                    topLeft = Offset(shiftX, shiftY),
                    style = Stroke(width = if (isSelected) 38.dp.toPx() else 28.dp.toPx(), cap = StrokeCap.Butt)
                )
                startAngle += sweepAngle
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 40.dp)) {
            Text(
                text = if (selectedCategory != null) getCategoryDisplayName(selectedCategory!!.category) else stringResource(R.string.chart_all_categories),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = CurrencyFormatter.format(selectedCategory?.totalAmount ?: totalSpending, "VND", locale),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                softWrap = false,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InteractiveCategoryLegend(spending: List<CategorySpending>, totalSpending: Double, selectedCategory: CategorySpending?, onCategorySelected: (CategorySpending?) -> Unit, subscriptions: List<Subscription>) {
    val colors = listOf(Color(0xFF6366F1), Color(0xFF06B6D4), Color(0xFFF43F5E), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF8B5CF6))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        spending.forEachIndexed { index, item ->
            val isSelected = selectedCategory?.category == item.category
            InteractiveCategoryRow(item, if (totalSpending > 0.0) (item.totalAmount / totalSpending).toFloat() else 0f, colors[index % colors.size], isSelected, subscriptions.filter { it.category == item.category }) {
                onCategorySelected(if (isSelected) null else item)
            }
        }
    }
}

@Composable
fun InteractiveCategoryRow(item: CategorySpending, percentage: Float, color: Color, isSelected: Boolean, categorySubs: List<Subscription>, onClick: () -> Unit) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = CategoryUtils.getCategoryIcon(item.category), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
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
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 48.dp), verticalAlignment = Alignment.CenterVertically) {
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

@Composable
fun BillingCycleChart(subscriptions: List<Subscription>, modifier: Modifier = Modifier) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val weeklyCost = subscriptions.filter { it.cycle == "Weekly" }.sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } * 52.0 / 12.0 +
            subscriptions.filter { it.cycle == "Daily" }.sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } * 365.0 / 12.0
    val monthlyCost = subscriptions.filter { it.cycle == "Monthly" }.sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } +
            subscriptions.filter { it.cycle == "Every 3 Months" }.sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } / 3.0 +
            subscriptions.filter { it.cycle == "Every 6 Months" }.sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } / 6.0
    val yearlyCost = subscriptions.filter { it.cycle == "Yearly" }.sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } / 12.0
    val total = weeklyCost + monthlyCost + yearlyCost

    Column(modifier = modifier.padding(24.dp)) {
        Text(
            stringResource(R.string.chart_billing_cycle_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Weekly/Daily bar
            val weeklyAnim = remember { Animatable(0f) }
            val monthlyAnim = remember { Animatable(0f) }
            val yearlyAnim = remember { Animatable(0f) }
            LaunchedEffect(weeklyCost, monthlyCost, yearlyCost) {
                weeklyAnim.animateTo(if (total > 0.0) (weeklyCost / total).toFloat() else 0f, tween(900, easing = FastOutSlowInEasing))
                monthlyAnim.animateTo(if (total > 0.0) (monthlyCost / total).toFloat() else 0f, tween(900, 100, FastOutSlowInEasing))
                yearlyAnim.animateTo(if (total > 0.0) (yearlyCost / total).toFloat() else 0f, tween(900, 200, FastOutSlowInEasing))
            }
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
                                listOf(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
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
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
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
                                listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                            )
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.cycle_yearly), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun UpcomingRenewalsTimeline(
    subscriptions: List<Subscription>,
    selectedSub: Subscription?,
    onSubSelected: (Subscription?) -> Unit,
    onMarkAsPaid: (Subscription) -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val upcoming = subscriptions.sortedBy { it.nextBillingDate }.take(6)
    var showQrDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.chart_upcoming_timeline), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(upcoming) { sub ->
                val days = DateUtils.getDaysLeft(sub.nextBillingDate)
                val isSelected = selectedSub?.id == sub.id
                val subColor = remember(sub.colorHex) {
                    try { Color(android.graphics.Color.parseColor(sub.colorHex)) }
                    catch (e: Exception) { Color(0xFF6366F1) }
                }
                val urgencyColor = when {
                    days <= 0  -> Color(0xFFF43F5E)
                    days <= 3  -> Color(0xFFF43F5E)
                    days <= 7  -> Color(0xFFF59E0B)
                    else       -> subColor
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) subColor.copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                        .clickable { onSubSelected(if (isSelected) null else sub) }
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(subColor.copy(alpha = if (isSelected) 1f else 0.8f))
                            .then(
                                if (isSelected) Modifier.border(2.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            sub.name.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
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
            val remainingText = if (selectedSub.remainingTimes != null && selectedSub.remainingTimes > 0) {
                " • " + stringResource(R.string.list_remaining_times, selectedSub.remainingTimes)
            } else {
                ""
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    text = "${CurrencyFormatter.format(selectedSub.amount, selectedSub.currency, locale)} · $cycleText$remainingText",
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
                        Spacer(modifier = Modifier.height(14.dp))

                        if (selectedSub.bankAccount != null && selectedSub.bankName != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = { showQrDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Rounded.QrCode, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("VietQR", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                                }

                                Button(
                                    onClick = {
                                        onMarkAsPaid(selectedSub)
                                        onSubSelected(null)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
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
                                title = { Text("Quét mã VietQR", fontWeight = FontWeight.Bold) },
                                text = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
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
                                            "Dùng app Ngân hàng quét QR để thanh toán nhanh",
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
                                    ) { Text("Đóng") }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

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
            onMarkAsPaid = {},
            onInsertSubscription = {},
            onNavigateToAdd = { _, _, _, _, _, _, _, _ -> },
            onNavigateToList = {}
        )
    }
}

@Composable
private fun getCategoryDisplayName(category: String): String {
    val resId = when (category) {
        "Entertainment" -> R.string.category_entertainment
        "Utilities" -> R.string.category_utilities
        "Work" -> R.string.category_work
        "Cloud" -> R.string.category_cloud
        "Music" -> R.string.category_music
        "Food" -> R.string.category_food
        "Finance" -> R.string.category_finance
        "Anniversary" -> R.string.category_anniversary
        "Family" -> R.string.category_family
        "Trial" -> R.string.category_trial
        "Notes" -> R.string.category_notes
        else -> R.string.category_other
    }
    return stringResource(resId)
}

data class MonthlyForecast(
    val monthName: String,
    val amount: Double
)

@Composable
fun getForecastingData(subscriptions: List<Subscription>): List<MonthlyForecast> {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val forecasts = mutableListOf<MonthlyForecast>()
    
    val months = (0 until 6).map { offset ->
        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, offset)
        }
        val monthNum = cal.get(Calendar.MONTH) + 1
        val monthLabel = if (locale.language == "vi") "T$monthNum" else {
            val sdf = SimpleDateFormat("MMM", locale)
            sdf.format(cal.time)
        }
        
        val startCal = Calendar.getInstance().apply {
            timeInMillis = cal.timeInMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val endCal = Calendar.getInstance().apply {
            timeInMillis = startCal.timeInMillis
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        Triple(monthLabel, startCal.timeInMillis, endCal.timeInMillis)
    }
    
    val monthlyAmounts = DoubleArray(6) { 0.0 }
    
    subscriptions.forEach { sub ->
        var currentBillingDate = sub.nextBillingDate
        var limit = sub.remainingTimes
        var isFirst = true
        val maxSimTime = months[5].third
        
        while (currentBillingDate <= maxSimTime) {
            for (i in 0 until 6) {
                val (_, start, end) = months[i]
                if (currentBillingDate in start..end) {
                    val amountInVnd = CurrencyFormatter.convert(sub.amount, sub.currency, "VND")
                    monthlyAmounts[i] += amountInVnd
                }
            }
            
            if (limit != null) {
                if (isFirst) {
                    isFirst = false
                } else {
                    limit = limit - 1
                }
                if (limit <= 0) break
            }
            
            if (sub.cycle == "One-time") {
                break
            }
            
            val nextDate = getNextBillingDateProjection(currentBillingDate, sub.cycle)
            if (nextDate <= currentBillingDate) {
                break
            }
            currentBillingDate = nextDate
        }
    }
    
    for (i in 0 until 6) {
        forecasts.add(MonthlyForecast(months[i].first, monthlyAmounts[i]))
    }
    return forecasts
}

private fun getNextBillingDateProjection(currentDate: Long, cycle: String): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = currentDate }
    when (cycle) {
        "Daily" -> cal.add(Calendar.DAY_OF_YEAR, 1)
        "Weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
        "Monthly" -> cal.add(Calendar.MONTH, 1)
        "Every 3 Months" -> cal.add(Calendar.MONTH, 3)
        "Every 6 Months" -> cal.add(Calendar.MONTH, 6)
        "Yearly" -> cal.add(Calendar.YEAR, 1)
        else -> cal.add(Calendar.MONTH, 1)
    }
    return cal.timeInMillis
}

@Composable
fun CashflowForecastingChart(subscriptions: List<Subscription>, modifier: Modifier = Modifier) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val forecasts = getForecastingData(subscriptions)
    val maxForecast = forecasts.maxByOrNull { it.amount }
    val maxVal = forecasts.maxOfOrNull { it.amount } ?: 0.0
    val displayMaxVal = if (maxVal == 0.0) 100000.0 else maxVal * 1.15
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val highlightColor = MaterialTheme.colorScheme.error

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
        
        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val width = size.width
            val height = size.height
            val paddingLeft = 40f
            val paddingRight = 40f
            val paddingTop = 20f
            val paddingBottom = 40f
            
            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom
            
            val gridLines = 3
            for (i in 0..gridLines) {
                val y = paddingTop + chartHeight * (i.toFloat() / gridLines)
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
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent),
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
                drawPath(
                    path = strokePath,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            
            val textPaint = android.graphics.Paint().apply {
                color = textColor.toArgb()
                textSize = 9.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            
            val amountPaint = android.graphics.Paint().apply {
                color = primaryColor.toArgb()
                textSize = 8.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }
            
            points.forEachIndexed { index, point ->
                val forecast = forecasts[index]
                val isMax = forecast == maxForecast && forecast.amount > 0.0
                
                drawCircle(
                    color = if (isMax) highlightColor else primaryColor,
                    radius = if (isMax) 5.dp.toPx() else 3.5f.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = if (isMax) 2.5f.dp.toPx() else 1.8f.dp.toPx(),
                    center = point
                )
                
                drawContext.canvas.nativeCanvas.drawText(
                    forecast.monthName,
                    point.x,
                    height - 6.dp.toPx(),
                    textPaint
                )
                
                if (forecast.amount > 0.0) {
                    val amountText = if (forecast.amount >= 1000000.0) {
                        String.format(locale, "%.1fM", forecast.amount / 1000000.0)
                    } else {
                        String.format(locale, "%.0fk", forecast.amount / 1000.0)
                    }
                    
                    val paintToUse = if (isMax) {
                        android.graphics.Paint(amountPaint).apply {
                            color = highlightColor.toArgb()
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                    } else {
                        amountPaint
                    }
                    
                    drawContext.canvas.nativeCanvas.drawText(
                        amountText,
                        point.x,
                        point.y - 8.dp.toPx(),
                        paintToUse
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentHistorySection(paymentHistory: List<PaymentHistory>, modifier: Modifier = Modifier) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", locale) }
    
    Column(modifier = modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                        text = "${paymentHistory.take(5).size} giao dịch",
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
            val displayedHistory = paymentHistory.take(5)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                displayedHistory.forEach { record ->
                    val dateStr = dateFormat.format(Date(record.paymentDate))
                    val amountStr = CurrencyFormatter.format(record.amount, record.currency, locale)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                text = dateStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "-$amountStr",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981) // emerald green for paid
                        )
                    }
                }
            }
        }
    }
}
