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

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.viewmodel.SubscriptionViewModel
import androidx.compose.ui.res.stringResource
import com.gbao86.sub_lazy.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
import java.util.Calendar
import java.time.Year

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionListScreen(
    viewModel: SubscriptionViewModel = viewModel(),
    onNavigateToDetail: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val subscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.list_title),
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (subscriptions.isNotEmpty()) {
                            Text(
                                "${subscriptions.size} dịch vụ đang theo dõi",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToDetail(-1L)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Thêm mới", fontWeight = FontWeight.SemiBold) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (subscriptions.isEmpty()) {
            // ── Premium Empty State ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(40.dp)
                ) {
                    rememberInfiniteTransition(label = "pulse")
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Subscriptions,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Text(
                        stringResource(R.string.list_empty),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Thêm dịch vụ đầu tiên của bạn\nbằng nút bên dưới 👇",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp, top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = subscriptions,
                    key = { _, sub -> sub.id }
                ) { index, subscription ->
                    // Staggered entrance animation
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay((index * 40L).coerceAtMost(300L))
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(300)) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                    ) {
                        SwipeToDeleteItem(
                            onDelete = {
                                val backup = subscription
                                viewModel.delete(subscription)
                                coroutineScope.launch {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Đã xóa ${backup.name}",
                                        actionLabel = "Hoàn tác",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.insert(backup)
                                    }
                                }
                            }
                        ) {
                            SubscriptionItem(
                                subscription = subscription,
                                onClick = { onNavigateToDetail(subscription.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteItem(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                }, label = "delete_bg"
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(color)
                    .padding(horizontal = 28.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Xóa",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        enableDismissFromStartToEnd = false,
        content = { content() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionItem(
    subscription: Subscription,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val daysLeft = DateUtils.getDaysLeft(subscription.nextBillingDate)
    val accentColor = remember(subscription.colorHex) {
        try { Color(subscription.colorHex.toColorInt()) }
        catch (_: Exception) { Color(0xFF6366F1) }
    }

    val cycleSuffix = when (subscription.cycle) {
        "Daily"          -> stringResource(R.string.list_daily_suffix)
        "Weekly"         -> stringResource(R.string.list_weekly_suffix)
        "Monthly"        -> stringResource(R.string.list_monthly_suffix)
        "Every 3 Months" -> stringResource(R.string.list_3_months_suffix)
        "Every 6 Months" -> stringResource(R.string.list_6_months_suffix)
        "Yearly"         -> stringResource(R.string.list_yearly_suffix)
        "One-time"       -> stringResource(R.string.list_one_time_suffix)
        else             -> ""
    }

    // Urgency colors
    val isUrgent = daysLeft <= 3
    val isWarning = daysLeft in 4..7
    val countdownColor = when {
        daysLeft <= 0 -> MaterialTheme.colorScheme.error
        isUrgent      -> MaterialTheme.colorScheme.error
        isWarning     -> Color(0xFFF59E0B) // amber
        else          -> MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isUrgent) Modifier.border(
                    1.5.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                    RoundedCornerShape(24.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUrgent) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Left: Color icon avatar ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subscription.name.take(1).uppercase(),
                    color = accentColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // ── Middle: Name + cost ──────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${CurrencyFormatter.format(subscription.amount, subscription.currency, locale)}$cycleSuffix",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subscription.remainingTimes != null && subscription.remainingTimes > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = stringResource(R.string.list_remaining_times, subscription.remainingTimes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Right: Countdown pill ─────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = countdownColor.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (daysLeft <= 0) "!" else "$daysLeft",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = countdownColor
                        )
                        Text(
                            text = if (daysLeft <= 0) stringResource(R.string.list_days_left_today)
                                   else stringResource(R.string.list_days_left_suffix),
                            style = MaterialTheme.typography.labelSmall,
                            color = countdownColor.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun SubscriptionListPreview() {
    Sub_lazyTheme {
        SubscriptionListScreen(
            onNavigateToDetail = {},
            onNavigateBack = {}
        )
    }
}
