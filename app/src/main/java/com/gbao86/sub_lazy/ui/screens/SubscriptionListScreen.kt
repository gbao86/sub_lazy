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
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.gbao86.sub_lazy.ui.toComposeColor
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.ui.CategoryUtils
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.viewmodel.SubscriptionListViewModel
import androidx.compose.ui.res.stringResource
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
import java.text.Normalizer
import java.util.Calendar
import java.util.regex.Pattern
import java.time.Year

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionListScreen(
    viewModel: SubscriptionListViewModel = hiltViewModel(),
    onNavigateToDetail: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
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
                                stringResource(R.string.list_services_tracked, subscriptions.size),
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
                icon = { Icon(Icons.Rounded.Add, contentDescription = "Add subscription") },
                text = { Text(stringResource(R.string.list_btn_add_new), fontWeight = FontWeight.SemiBold) }
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
                            contentDescription = "Empty list icon",
                            modifier = Modifier.size(52.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        stringResource(R.string.list_empty),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.list_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            var searchQuery by remember { mutableStateOf("") }
            var selectedCategoryFilter by remember { mutableStateOf<SubscriptionCategory?>(null) }

            // Category Filter Chips — scrollable row with auto-scroll to selected chip
            val uniqueCategories = remember(subscriptions) {
                subscriptions.map { it.category }.distinct()
            }

            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { uniqueCategories.size + 1 }
            )

            // When page changes in the pager (via swipe), update the filter selection
            LaunchedEffect(pagerState.currentPage) {
                selectedCategoryFilter = if (pagerState.currentPage == 0) {
                    null
                } else {
                    uniqueCategories.getOrNull(pagerState.currentPage - 1)
                }
            }

            // Auto-scroll pager to page 0 ("Tất cả") when user starts typing a search query
            LaunchedEffect(searchQuery) {
                if (searchQuery.isNotEmpty() && pagerState.currentPage != 0) {
                    pagerState.animateScrollToPage(0)
                }
            }

            val chipListState = rememberLazyListState()

            // Auto-scroll selected chip into center view when selection changes
            LaunchedEffect(selectedCategoryFilter) {
                val targetIndex = if (selectedCategoryFilter == null) {
                    0 // "All" chip is always at index 0
                } else {
                    val categoryIndex = uniqueCategories.indexOf(selectedCategoryFilter)
                    if (categoryIndex >= 0) categoryIndex + 1 else return@LaunchedEffect
                }
                chipListState.animateScrollToItem(
                    index = targetIndex,
                    scrollOffset = -40 // slight offset so chip isn't flush at edge
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.list_search_hint)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Clear search text")
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                LazyRow(
                    state = chipListState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    flingBehavior = ScrollableDefaults.flingBehavior(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(0)
                                }
                            },
                            label = { Text(stringResource(R.string.list_filter_all)) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategoryFilter == null,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                borderWidth = 1.dp
                            )
                        )
                    }

                    items(uniqueCategories, key = { it.name }) { category ->
                        val isSelected = selectedCategoryFilter == category
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                coroutineScope.launch {
                                    val targetPage = uniqueCategories.indexOf(category) + 1
                                    pagerState.animateScrollToPage(targetPage)
                                }
                            },
                            label = { Text(CategoryUtils.getCategoryDisplayName(category)) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                borderWidth = 1.dp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) { pageIndex ->
                    val pageCategory = if (pageIndex == 0) null else uniqueCategories.getOrNull(pageIndex - 1)
                    val pageSubs = remember(subscriptions, searchQuery, pageCategory) {
                        subscriptions.filter { sub ->
                            val normalizedName = sub.name.removeDiacritics().lowercase()
                            val normalizedQuery = searchQuery.removeDiacritics().lowercase()
                            val matchesSearch = normalizedName.contains(normalizedQuery)
                            val matchesCategory = pageCategory == null || sub.category == pageCategory
                            matchesSearch && matchesCategory
                        }
                    }

                    if (pageSubs.isEmpty()) {
                        // Search Empty State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(40.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = "Subscription item icon",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    stringResource(R.string.list_search_no_results),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.list_search_no_results_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(
                                items = pageSubs,
                                key = { _, sub -> sub.id }
                            ) { index, subscription ->
                                // Staggered entrance animation
                                var visible by remember { mutableStateOf(false) }
                                LaunchedEffect(subscription.id) {
                                    visible = true
                                }
                                AnimatedVisibility(
                                    visible = visible,
                                    enter = fadeIn(tween(200)) + slideInVertically(
                                        initialOffsetY = { it / 6 },
                                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                                    )
                                ) {
                                    SwipeToDeleteItem(
                                        onDelete = {
                                            val backup = subscription
                                            viewModel.delete(subscription)
                                            coroutineScope.launch {
                                                snackbarHostState.currentSnackbarData?.dismiss()
                                                val result = snackbarHostState.showSnackbar(
                                                    message = context.getString(R.string.list_deleted_name, backup.name),
                                                    actionLabel = context.getString(R.string.list_undo),
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
                    .padding(horizontal = 24.dp),
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
                            stringResource(R.string.list_swipe_delete),
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
    val locale = LocalConfiguration.current.locales[0]
    val daysLeft = DateUtils.getDaysLeft(subscription.nextBillingDate)
    val accentColor = remember(subscription.colorHex) {
        subscription.colorHex.toComposeColor(Color(0xFF6366F1))
    }

    val cycleSuffix = when (subscription.cycle) {
        BillingCycle.DAILY          -> stringResource(R.string.list_daily_suffix)
        BillingCycle.WEEKLY         -> stringResource(R.string.list_weekly_suffix)
        BillingCycle.MONTHLY        -> stringResource(R.string.list_monthly_suffix)
        BillingCycle.EVERY_3_MONTHS -> stringResource(R.string.list_3_months_suffix)
        BillingCycle.EVERY_6_MONTHS -> stringResource(R.string.list_6_months_suffix)
        BillingCycle.YEARLY         -> stringResource(R.string.list_yearly_suffix)
        BillingCycle.ONE_TIME       -> stringResource(R.string.list_one_time_suffix)
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
                Icon(
                    imageVector = CategoryUtils.getIconForName(subscription.name, subscription.category),
                    contentDescription = "Billing cycle info icon",
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${CurrencyFormatter.format(subscription.amount, subscription.currency.code, locale)}$cycleSuffix",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val tagText = when {
                    subscription.isInstallment -> stringResource(R.string.list_tag_installment, subscription.remainingTimes ?: 0)
                    subscription.isSessionBased -> stringResource(R.string.list_tag_session, subscription.remainingSessions ?: 0, subscription.totalSessions ?: 0)
                    subscription.isShared -> stringResource(R.string.list_tag_shared)
                    subscription.category == SubscriptionCategory.TRIAL -> stringResource(R.string.list_tag_trial)
                    subscription.remainingTimes != null && subscription.remainingTimes > 0 -> 
                        stringResource(R.string.list_remaining_times, subscription.remainingTimes)
                    else -> null
                }
                
                if (tagText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            subscription.isInstallment -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                            subscription.isSessionBased -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            subscription.isShared -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            subscription.category == SubscriptionCategory.TRIAL -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        }
                    ) {
                        Text(
                            text = tagText,
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                subscription.isInstallment -> MaterialTheme.colorScheme.onTertiaryContainer
                                subscription.isSessionBased -> MaterialTheme.colorScheme.onSecondaryContainer
                                subscription.isShared -> MaterialTheme.colorScheme.onPrimaryContainer
                                subscription.category == SubscriptionCategory.TRIAL -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // ── Right: Countdown pill ─────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = countdownColor.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
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

private fun String.removeDiacritics(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(temp).replaceAll("").replace("đ", "d").replace("Đ", "D")
}

