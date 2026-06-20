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

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.gbao86.sub_lazy.ui.toComposeColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.SharedMember
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import com.gbao86.sub_lazy.ui.CategoryUtils
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.DateUtils
import com.gbao86.sub_lazy.ui.VietQRGenerator
import kotlinx.coroutines.launch
import java.util.Calendar

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
    modifier: Modifier = Modifier,
    sharedMembersMap: Map<Long, List<SharedMember>> = emptyMap()
) {
    val locale = LocalConfiguration.current.locales[0]
    val upcoming = subscriptions.sortedBy { it.nextBillingDate }.take(6)
    var showQrDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            stringResource(R.string.chart_upcoming_timeline),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (upcoming.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cat_saving),
                    contentDescription = "No upcoming",
                    modifier = Modifier.size(120.dp)
                )
                Text(
                    text = "Thảnh thơi! Chưa có hoá đơn nào sắp tới.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
            items(upcoming, key = { it.id }) { sub ->
                val days = DateUtils.getDaysLeft(sub.nextBillingDate)
                val isSelected = selectedSub?.id == sub.id
                val subColor = remember(sub.colorHex) {
                    sub.colorHex.toComposeColor(Color(0xFF6366F1))
                }
                val errorColor = MaterialTheme.colorScheme.error
                val tertiaryColor = MaterialTheme.colorScheme.tertiary
                val urgencyColor = when {
                    days <= 0 -> errorColor
                    days <= 3 -> errorColor
                    days <= 7 -> tertiaryColor
                    else -> subColor
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) subColor.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { 
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSubSelected(if (isSelected) null else sub) 
                        }
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
                            contentDescription = "No upcoming subscriptions icon",
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
                            text = if (days <= 0) stringResource(R.string.list_days_left_today) else "${days}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = urgencyColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }

    if (selectedSub != null) {
            val days = DateUtils.getDaysLeft(selectedSub.nextBillingDate)
            val cycleText = when (selectedSub.cycle) {
                BillingCycle.DAILY -> stringResource(R.string.cycle_daily)
                BillingCycle.WEEKLY -> stringResource(R.string.cycle_weekly)
                BillingCycle.MONTHLY -> stringResource(R.string.cycle_monthly)
                BillingCycle.EVERY_3_MONTHS -> stringResource(R.string.cycle_3_months)
                BillingCycle.EVERY_6_MONTHS -> stringResource(R.string.cycle_6_months)
                BillingCycle.YEARLY -> stringResource(R.string.cycle_yearly)
                BillingCycle.ONE_TIME -> stringResource(R.string.cycle_one_time)
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
                                 selectedSub.colorHex.toComposeColor(Color(0xFF6366F1))
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
                                    contentDescription = "Subscription icon",
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
                                    text = "${CurrencyFormatter.format(selectedSub.amount, selectedSub.currency.code, locale)} · $detailsLabel",
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
                        if (selectedSub.isShared) {
                            val members = sharedMembersMap[selectedSub.id] ?: emptyList()
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
                                                text = CurrencyFormatter.format(member.amount, selectedSub.currency.code, locale),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (member.hasPaid) Color.Gray else MaterialTheme.colorScheme.primary
                                            )
                                            if (!member.hasPaid) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                val clipboardManager = LocalClipboardManager.current
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
                                                        val memberAmountFormatted = CurrencyFormatter.format(member.amount, selectedSub.currency.code, locale)
                                                        val message = toastContext.getString(
                                                            R.string.dashboard_split_reminder_pattern,
                                                            member.name,
                                                            selectedSub.name,
                                                            memberAmountFormatted,
                                                            qrUrl
                                                        )
                                                        clipboardManager.setText(AnnotatedString(message))
                                                        Toast.makeText(toastContext, toastContext.getString(R.string.dashboard_reminder_copied), Toast.LENGTH_SHORT).show()
                                                        try {
                                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                                putExtra(Intent.EXTRA_TEXT, message)
                                                                type = "text/plain"
                                                            }
                                                            val chooserTitle = toastContext.getString(R.string.dashboard_send_reminder_to, member.name)
                                                            toastContext.startActivity(Intent.createChooser(sendIntent, chooserTitle))
                                                        } catch (_: Exception) {
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
                        if (selectedSub.category == SubscriptionCategory.TRIAL) {
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
                                            contentDescription = "Shared member avatar",
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
                                    onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onCheckInSession(selectedSub) 
                                    },
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
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                    onClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        showQrDialog = true 
                                    },
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
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
