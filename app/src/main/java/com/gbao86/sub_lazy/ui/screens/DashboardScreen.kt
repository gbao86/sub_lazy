/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

Unauthorized copying of this file, via any medium, is strictly prohibited.
Proprietary and confidential.
This source code is provided for reference purposes only and may not be copied, 
modified, or distributed without the express written permission of the author.
*/

package com.gbao86.sub_lazy.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SubscriptionViewModel = viewModel(),
    onNavigateToAdd: (String?, Double?) -> Unit,
    onNavigateToList: () -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val totalMonthlyCost by viewModel.totalMonthlyCost.collectAsStateWithLifecycle(initialValue = 0.0)
    val spendingByCategory by viewModel.spendingByCategory.collectAsStateWithLifecycle(initialValue = emptyList())
    val subscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle(initialValue = emptyList())

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
                        onNavigateToAdd(result.name, result.amount)
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
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    var showLangMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showLangMenu = true }) {
                        Icon(Icons.Rounded.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
            LargeFloatingActionButton(
                onClick = { showAddBottomSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp, start = 24.dp, end = 24.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))).padding(24.dp)) {
                            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxHeight()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.dashboard_monthly_spending), style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
                                }
                                Text(
                                    text = CurrencyFormatter.format(totalMonthlyCost ?: 0.0, "VND", locale),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    softWrap = false,
                                    maxLines = 1,
                                    overflow = TextOverflow.Visible
                                )
                            }
                        }
                    }
                }

                item {
                    Button(onClick = onNavigateToList, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        Icon(Icons.AutoMirrored.Rounded.FormatListBulleted, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.dashboard_btn_manage), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                if (spendingByCategory.isNotEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.dashboard_distribution), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, modifier = Modifier.align(Alignment.Start))
                                Spacer(modifier = Modifier.height(32.dp))
                                InteractiveDonutChart(
                                    spending = spendingByCategory,
                                    totalSpending = spendingByCategory.sumOf { it.totalAmount },
                                    selectedCategory = selectedCategory,
                                    onCategorySelected = { selectedCategory = it },
                                    modifier = Modifier.size(220.dp)
                                )
                                Spacer(modifier = Modifier.height(32.dp))
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

                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp)) {
                            BillingCycleChart(subscriptions = subscriptions, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp)) {
                            UpcomingRenewalsTimeline(
                                subscriptions = subscriptions,
                                selectedSub = selectedUpcomingSub,
                                onSubSelected = { selectedUpcomingSub = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.TrendingUp, null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(text = stringResource(R.string.dashboard_no_data), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = TextAlign.Center)
                            }
                        }
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
                    onNavigateToAdd(null, null)
                })
                ListItem(headlineContent = { Text(stringResource(R.string.action_scan_screenshot)) }, leadingContent = { Icon(Icons.Rounded.PhotoCamera, null) }, modifier = Modifier.clickable {
                    showAddBottomSheet = false
                    imagePickerLauncher.launch("image/*")
                })
            }
        }
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

                drawArc(
                    color = colors[index % colors.size].copy(alpha = if (selectedCategory == null || isSelected) 1f else 0.3f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(shiftX, shiftY),
                    style = Stroke(width = if (isSelected) 38.dp.toPx() else 28.dp.toPx(), cap = StrokeCap.Round)
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
    val monthlyCost = subscriptions.filter { it.cycle == "Monthly" }.sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") }
    val yearlyCost = subscriptions.filter { it.cycle == "Yearly" }.sumOf { CurrencyFormatter.convert(it.amount, it.currency, "VND") } / 12.0
    val total = monthlyCost + yearlyCost

    Row(modifier = modifier.padding(24.dp).height(150.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceEvenly) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(CurrencyFormatter.format(monthlyCost, "VND", locale), style = MaterialTheme.typography.labelSmall, softWrap = false, maxLines = 1)
            Box(modifier = Modifier.width(40.dp).height((100 * (if (total > 0.0) monthlyCost / total else 0.0)).dp.coerceAtLeast(4.dp)).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(MaterialTheme.colorScheme.primary))
            Text(stringResource(R.string.cycle_monthly), style = MaterialTheme.typography.labelMedium)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(CurrencyFormatter.format(yearlyCost, "VND", locale), style = MaterialTheme.typography.labelSmall, softWrap = false, maxLines = 1)
            Box(modifier = Modifier.width(40.dp).height((100 * (if (total > 0.0) yearlyCost / total else 0.0)).dp.coerceAtLeast(4.dp)).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)).background(MaterialTheme.colorScheme.secondary))
            Text(stringResource(R.string.cycle_yearly), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun UpcomingRenewalsTimeline(subscriptions: List<Subscription>, selectedSub: Subscription?, onSubSelected: (Subscription?) -> Unit, modifier: Modifier = Modifier) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    val upcoming = subscriptions.sortedBy { it.nextBillingDate }.take(6)
    Column(modifier = modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.chart_upcoming_timeline), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(upcoming) { sub ->
                val days = DateUtils.getDaysLeft(sub.nextBillingDate)
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSubSelected(if (selectedSub?.id == sub.id) null else sub) }) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(sub.colorHex))).border(if (selectedSub?.id == sub.id) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape), contentAlignment = Alignment.Center) {
                        Text(sub.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text(sub.name, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
                    Text("${days}d", style = MaterialTheme.typography.labelSmall, color = if (days <= 3L) MaterialTheme.colorScheme.error else Color.Unspecified)
                }
            }
        }
        if (selectedSub != null) {
            val days = DateUtils.getDaysLeft(selectedSub.nextBillingDate)
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(selectedSub.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = CurrencyFormatter.format(selectedSub.amount, selectedSub.currency, locale),
                            style = MaterialTheme.typography.bodySmall,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "$days " + stringResource(R.string.list_days_left_suffix),
                        fontWeight = FontWeight.Bold,
                        color = if (days <= 3L) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    Sub_lazyTheme { DashboardScreen(onNavigateToAdd = { _, _ -> }, onNavigateToList = {}) }
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
