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

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.ui.BankruptcyRunwayResult
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.FinanceCalculator
import java.util.Calendar
import java.util.Locale

/**
 * Card that shows the LazyWallet cat mascot + financial health summary.
 * Includes the budget runway countdown and balance edit button.
 */
@Composable
fun LazyWalletHealthCard(
    subscriptions: List<Subscription>,
    userBalance: Double,
    budgetResetDay: Int,
    tickTrigger: Int,
    locale: Locale,
    onEditBudgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val runwayResult = remember(userBalance, subscriptions, budgetResetDay, tickTrigger) {
        FinanceCalculator.calculateBankruptcyRunway(userBalance, subscriptions, budgetResetDay)
    }
    val activeSubs = remember(subscriptions) {
        subscriptions.filter { it.cycle != BillingCycle.ONE_TIME && it.cycle != BillingCycle.YEARLY }
    }
    val nextNearestBillDate = remember(activeSubs) {
        activeSubs.minOfOrNull { it.nextBillingDate }
    }
    val totalMonthlyCostActive = remember(activeSubs) {
        activeSubs.sumOf { FinanceCalculator.calculateMonthlyEquivalentCostInVnd(it) }
    }
    val isPanicked = runwayResult is BankruptcyRunwayResult.AlreadyBankrupt ||
            totalMonthlyCostActive > userBalance ||
            (runwayResult is BankruptcyRunwayResult.DaysLeft && (
                runwayResult.diffMillis < 86400000L * 7 ||
                (nextNearestBillDate != null && runwayResult.targetTime <= nextNearestBillDate)
            ))

    val hasTrialExpiringSoon = remember(subscriptions) {
        subscriptions.any {
            it.category == SubscriptionCategory.TRIAL &&
                (it.nextBillingDate - System.currentTimeMillis() <= 86400000L * 3) &&
                (it.nextBillingDate >= System.currentTimeMillis())
        }
    }

    val catState = when {
        isPanicked || hasTrialExpiringSoon -> CatState.PANICKED
        totalMonthlyCostActive == 0.0 || (runwayResult is BankruptcyRunwayResult.DaysLeft && runwayResult.diffMillis >= 86400000L * 180) -> CatState.HAPPY
        else -> CatState.SLEEPING
    }

    Card(
        modifier = modifier,
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
                    .clickable { onEditBudgetClick() }
                    .heightIn(min = 48.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Rounded.AccountBalanceWallet,
                    contentDescription = "Edit balance icon",
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

            BudgetRunwayStatus(
                runwayResult = runwayResult,
                totalMonthlyCostActive = totalMonthlyCostActive,
                userBalance = userBalance,
                isPanicked = isPanicked
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Budget Runway Status — extracted from the when(runwayResult) block
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BudgetRunwayStatus(
    runwayResult: BankruptcyRunwayResult,
    totalMonthlyCostActive: Double,
    userBalance: Double,
    isPanicked: Boolean,
    modifier: Modifier = Modifier
) {
    when (runwayResult) {
        is BankruptcyRunwayResult.Infinite -> {
            Text(
                stringResource(R.string.budget_empty_services_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = modifier
            )
        }
        is BankruptcyRunwayResult.Safe -> {
            if (totalMonthlyCostActive > userBalance) {
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
                    modifier = modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Dangerous, contentDescription = "Dangerous status", tint = Color(0xFFC62828))
                        Column {
                            Text(
                                stringResource(R.string.budget_deficit_title),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFC62828)
                            )
                            Text(
                                stringResource(R.string.budget_deficit_desc),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Healthy status", tint = Color(0xFF2E7D32))
                        Column {
                            Text(
                                stringResource(R.string.budget_safe_title),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                stringResource(R.string.budget_safe_desc),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF2E7D32)
                            )
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
                modifier = modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Warning, contentDescription = "Warning status", tint = Color(0xFFC62828))
                    Column {
                        Text(
                            stringResource(R.string.budget_exhausted_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFC62828)
                        )
                        Text(
                            stringResource(R.string.budget_exhausted_desc),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFC62828)
                        )
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
                modifier = modifier.fillMaxWidth()
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
                            contentDescription = "Close dialog",
                            tint = if (isPanicked) Color(0xFFD32F2F) else Color(0xFFFBC02D)
                        )
                        Text(
                            text = if (isPanicked) stringResource(R.string.budget_running_out_title)
                                   else stringResource(R.string.budget_running_out_warning),
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
