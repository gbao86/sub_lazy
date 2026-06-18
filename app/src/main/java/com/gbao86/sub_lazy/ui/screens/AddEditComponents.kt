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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.SharedMember
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory


@Composable
fun CommitmentTypeSelector(
    serviceType: String,
    onServiceTypeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.add_edit_commitment_type),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val types = listOf(
                "standard" to stringResource(R.string.add_edit_type_standard),
                "installment" to stringResource(R.string.add_edit_type_installment),
                "session" to stringResource(R.string.add_edit_type_session)
            )
            types.forEach { (typeVal, typeLabel) ->
                val selected = serviceType == typeVal
                FilterChip(
                    selected = selected,
                    onClick = { onServiceTypeChange(typeVal) },
                    label = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = typeLabel,
                                modifier = Modifier.padding(vertical = 4.dp),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
fun BillingCycleSelector(
    cycle: BillingCycle,
    onCycleChange: (BillingCycle) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.add_edit_billing_cycle),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        val cycleRows = listOf(
            listOf(BillingCycle.DAILY, BillingCycle.WEEKLY, BillingCycle.MONTHLY),
            listOf(BillingCycle.EVERY_3_MONTHS, BillingCycle.EVERY_6_MONTHS, BillingCycle.YEARLY)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            cycleRows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        val selected = cycle == item
                        FilterChip(
                            selected = selected,
                            onClick = { onCycleChange(item) },
                            label = { 
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = stringResource(getCycleDisplayNameRes(item)), 
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    ) 
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                    if (rowItems.size < 3) {
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AutoDeleteSection(
    autoDeleteMode: String,
    onAutoDeleteModeChange: (String) -> Unit,
    customTimes: String,
    onCustomTimesChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.add_edit_auto_delete_title), 
            style = MaterialTheme.typography.titleSmall, 
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("unlimited", "once", "custom").forEach { mode ->
                val selected = autoDeleteMode == mode
                val labelRes = when (mode) {
                    "unlimited" -> R.string.auto_delete_unlimited
                    "once" -> R.string.auto_delete_once
                    else -> R.string.auto_delete_custom
                }
                FilterChip(
                    selected = selected,
                    onClick = { onAutoDeleteModeChange(mode) },
                    label = { 
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(labelRes), 
                                modifier = Modifier.padding(vertical = 8.dp),
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            ) 
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
        
        if (autoDeleteMode == "custom") {
            OutlinedTextField(
                value = customTimes,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        onCustomTimesChange(input)
                    }
                },
                label = { Text(stringResource(R.string.auto_delete_custom_hint)) },
                placeholder = { Text("e.g. 5") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}

@Composable
fun InstallmentSessionFields(
    serviceType: String,
    totalInstallmentPeriods: String,
    onTotalInstallmentPeriodsChange: (String) -> Unit,
    totalSessions: String,
    onTotalSessionsChange: (String) -> Unit,
    remainingSessions: String,
    onRemainingSessionsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (serviceType == "installment") {
            Text(
                text = stringResource(R.string.add_edit_installment_info),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = totalInstallmentPeriods,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        onTotalInstallmentPeriodsChange(input)
                    }
                },
                label = { Text(stringResource(R.string.add_edit_installment_periods)) },
                placeholder = { Text(stringResource(R.string.add_edit_installment_periods_hint)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Text(
                text = stringResource(R.string.add_edit_installment_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (serviceType == "session") {
            Text(
                text = stringResource(R.string.add_edit_session_info),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = totalSessions,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            onTotalSessionsChange(input)
                        }
                    },
                    label = { Text(stringResource(R.string.add_edit_session_total)) },
                    placeholder = { Text(stringResource(R.string.add_edit_session_total_hint)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = remainingSessions,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            onRemainingSessionsChange(input)
                        }
                    },
                    label = { Text(stringResource(R.string.add_edit_session_remaining)) },
                    placeholder = { Text(stringResource(R.string.add_edit_session_total_hint)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            Text(
                text = stringResource(R.string.add_edit_session_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SharedMemberSection(
    isSharedExpanded: Boolean,
    onIsSharedExpandedChange: (Boolean) -> Unit,
    isShared: Boolean,
    onIsSharedChange: (Boolean) -> Unit,
    sharedMembersList: List<SharedMember>,
    onSharedMembersListChange: (List<SharedMember>) -> Unit,
    selectedCurrency: String,
    locale: java.util.Locale,
    modifier: Modifier = Modifier
) {
    var newMemberName by remember { mutableStateOf("") }
    var newMemberAmount by remember { mutableStateOf("") }
    var newMemberPhone by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isSharedExpanded) 0.5f else 0.25f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSharedExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIsSharedExpandedChange(!isSharedExpanded) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.People,
                    contentDescription = "Search category",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.add_edit_shared_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.add_edit_shared_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isSharedExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = "Clear search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isSharedExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.add_edit_shared_title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Switch(checked = isShared, onCheckedChange = onIsSharedChange)
                    }

                    if (isShared) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        
                        // List added members
                        if (sharedMembersList.isNotEmpty()) {
                            Text(stringResource(R.string.add_edit_shared_members), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            sharedMembersList.forEachIndexed { idx, member ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(member.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        if (!member.phone.isNullOrBlank()) {
                                            Text(member.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = formatDoubleToInput(member.amount, selectedCurrency, locale) + " " + selectedCurrency,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                onSharedMembersListChange(sharedMembersList.filterIndexed { i, _ -> i != idx })
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Confirm deletion",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        }
                        
                        // Add new member form
                        Text(stringResource(R.string.add_edit_shared_add_member), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = newMemberName,
                            onValueChange = { newMemberName = it },
                            label = { Text(stringResource(R.string.add_edit_shared_member_name)) },
                            placeholder = { Text(stringResource(R.string.add_edit_shared_member_name_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newMemberAmount,
                                onValueChange = { newMemberAmount = formatInputString(it, selectedCurrency, locale) },
                                label = { Text(stringResource(R.string.add_edit_shared_member_amount)) },
                                placeholder = { Text("0.0") },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                prefix = {
                                    val prefixText = when (selectedCurrency) {
                                        "VND" -> "₫ "
                                        "EUR" -> "€ "
                                        "CNY" -> "CN¥ "
                                        "JPY" -> "JP¥ "
                                        "THB" -> "฿ "
                                        "KRW" -> "₩ "
                                        else -> "$ "
                                    }
                                    Text(prefixText, fontWeight = FontWeight.Bold)
                                }
                            )
                            OutlinedTextField(
                                value = newMemberPhone,
                                onValueChange = { newMemberPhone = it },
                                label = { Text(stringResource(R.string.add_edit_shared_member_phone)) },
                                placeholder = { Text(stringResource(R.string.add_edit_shared_member_phone_hint)) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true
                            )
                        }
                        
                        Button(
                            onClick = {
                                if (newMemberName.isNotBlank() && newMemberAmount.isNotBlank()) {
                                    val amt = parseFormattedAmount(newMemberAmount, locale)
                                    onSharedMembersListChange(sharedMembersList + SharedMember(
                                        name = newMemberName,
                                        amount = amt,
                                        hasPaid = false,
                                        phone = newMemberPhone.takeIf { it.isNotBlank() }
                                    ))
                                    newMemberName = ""
                                    newMemberAmount = ""
                                    newMemberPhone = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add shared member", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.add_edit_shared_btn_add), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VietQRBankSection(
    isBankExpanded: Boolean,
    onIsBankExpandedChange: (Boolean) -> Unit,
    hasBankInfo: Boolean,
    onHasBankInfoChange: (Boolean) -> Unit,
    bankName: String,
    onBankNameChange: (String) -> Unit,
    bankAccount: String,
    onBankAccountChange: (String) -> Unit,
    bankAccountHolder: String,
    onBankAccountHolderChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isBankExpanded) 0.5f else 0.25f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isBankExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIsBankExpandedChange(!isBankExpanded) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalance,
                    contentDescription = "Copy Bank Account",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.add_edit_bank_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.add_edit_bank_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isBankExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = "Scan VietQR",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isBankExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.add_edit_bank_title), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Switch(checked = hasBankInfo, onCheckedChange = onHasBankInfoChange)
                    }

                    if (hasBankInfo) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        OutlinedTextField(
                            value = bankName,
                            onValueChange = onBankNameChange,
                            label = { Text(stringResource(R.string.add_edit_bank_name)) },
                            placeholder = { Text("e.g. MB") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            )
                        )

                        OutlinedTextField(
                            value = bankAccount,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    onBankAccountChange(input)
                                }
                            },
                            label = { Text(stringResource(R.string.add_edit_bank_account)) },
                            placeholder = { Text("e.g. 123456...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            )
                        )

                        OutlinedTextField(
                            value = bankAccountHolder,
                            onValueChange = onBankAccountHolderChange,
                            label = { Text(stringResource(R.string.add_edit_bank_account_holder)) },
                            placeholder = { Text("e.g. NGUYEN VAN A") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            )
                        )
                    }
                }
            }
        }
    }
}

fun getCycleDisplayNameRes(cycle: BillingCycle): Int {
    return when (cycle) {
        BillingCycle.DAILY -> R.string.cycle_daily
        BillingCycle.WEEKLY -> R.string.cycle_weekly
        BillingCycle.MONTHLY -> R.string.cycle_monthly
        BillingCycle.EVERY_3_MONTHS -> R.string.cycle_3_months
        BillingCycle.EVERY_6_MONTHS -> R.string.cycle_6_months
        BillingCycle.YEARLY -> R.string.cycle_yearly
        BillingCycle.ONE_TIME -> R.string.cycle_one_time
    }
}

fun formatInputString(input: String, currency: String, locale: java.util.Locale): String {
    val symbols = java.text.DecimalFormatSymbols.getInstance(locale)
    val decimalChar = symbols.decimalSeparator
    val thousandChar = symbols.groupingSeparator
    val hasNoDecimals = currency == "VND" || currency == "JPY" || currency == "KRW"
    
    val cleanBuilder = StringBuilder()
    var decimalSeen = false
    for (char in input) {
        if (char.isDigit()) {
            cleanBuilder.append(char)
        } else if (char == decimalChar && !decimalSeen && !hasNoDecimals) {
            cleanBuilder.append(char)
            decimalSeen = true
        }
    }
    val clean = cleanBuilder.toString()
    if (clean.isEmpty()) return ""
    
    val parts = clean.split(decimalChar)
    val integerPart = parts[0]
    val decimalPart = if (parts.size > 1) parts[1] else null
    
    val formattedInteger = if (integerPart.isNotEmpty()) {
        val sb = StringBuilder()
        var count = 0
        for (i in integerPart.length - 1 downTo 0) {
            sb.append(integerPart[i])
            count++
            if (count % 3 == 0 && i > 0) {
                sb.append(thousandChar)
            }
        }
        sb.reverse().toString()
    } else {
        ""
    }
    
    return if (decimalPart != null) {
        val truncatedDecimal = if (decimalPart.length > 2) decimalPart.substring(0, 2) else decimalPart
        "$formattedInteger$decimalChar$truncatedDecimal"
    } else if (input.endsWith(decimalChar) && !hasNoDecimals) {
        "$formattedInteger$decimalChar"
    } else {
        formattedInteger
    }
}

fun formatDoubleToInput(value: Double, currency: String, locale: java.util.Locale): String {
    val symbols = java.text.DecimalFormatSymbols.getInstance(locale)
    val decimalChar = symbols.decimalSeparator
    val thousandChar = symbols.groupingSeparator
    val hasNoDecimals = currency == "VND" || currency == "JPY" || currency == "KRW"
    if (hasNoDecimals || value % 1.0 == 0.0) {
        val longVal = value.toLong()
        val str = longVal.toString()
        val sb = StringBuilder()
        var count = 0
        for (i in str.length - 1 downTo 0) {
            sb.append(str[i])
            count++
            if (count % 3 == 0 && i > 0) {
                sb.append(thousandChar)
            }
        }
        return sb.reverse().toString()
    } else {
        val formatted = String.format(java.util.Locale.US, "%.2f", value)
        val parts = formatted.split('.')
        val integerPart = parts[0]
        val decimalPart = parts[1]
        
        val sb = StringBuilder()
        var count = 0
        for (i in integerPart.length - 1 downTo 0) {
            sb.append(integerPart[i])
            count++
            if (count % 3 == 0 && i > 0) {
                sb.append(thousandChar)
            }
        }
        val formattedInteger = sb.reverse().toString()
        return "$formattedInteger$decimalChar$decimalPart"
    }
}

fun parseFormattedAmount(amountStr: String, locale: java.util.Locale): Double {
    if (amountStr.isBlank()) return 0.0
    val symbols = java.text.DecimalFormatSymbols.getInstance(locale)
    val decimalChar = symbols.decimalSeparator.toString()
    val thousandChar = symbols.groupingSeparator.toString()
    
    val cleanStr = amountStr.replace(thousandChar, "").replace(decimalChar, ".")
    return cleanStr.toDoubleOrNull() ?: 0.0
}

fun getCategoryAccentColor(category: SubscriptionCategory): Color {
    return when (category) {
        SubscriptionCategory.ENTERTAINMENT -> Color(0xFF7C3AED)
        SubscriptionCategory.UTILITIES -> Color(0xFFF59E0B)
        SubscriptionCategory.WORK -> Color(0xFF3B82F6)
        SubscriptionCategory.CLOUD -> Color(0xFF06B6D4)
        SubscriptionCategory.MUSIC -> Color(0xFFEC4899)
        SubscriptionCategory.FOOD -> Color(0xFFF97316)
        SubscriptionCategory.FINANCE -> Color(0xFF10B981)
        SubscriptionCategory.ANNIVERSARY -> Color(0xFFEF4444)
        SubscriptionCategory.FAMILY -> Color(0xFF14B8A6)
        SubscriptionCategory.TRIAL -> Color(0xFF8B5CF6)
        SubscriptionCategory.NOTES -> Color(0xFF64748B)
        else -> Color(0xFF94A3B8)
    }
}

