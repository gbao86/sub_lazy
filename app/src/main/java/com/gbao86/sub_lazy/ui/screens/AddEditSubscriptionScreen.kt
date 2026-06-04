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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.ui.CategoryUtils
import com.gbao86.sub_lazy.viewmodel.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.gbao86.sub_lazy.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionScreen(
    subscriptionId: Long? = null,
    prefillName: String? = null,
    prefillAmount: Double? = null,
    prefillCycle: String? = null,
    prefillCategory: String? = null,
    prefillColorHex: String? = null,
    prefillBankName: String? = null,
    prefillBankAccount: String? = null,
    prefillBankAccountHolder: String? = null,
    viewModel: SubscriptionViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val locale = context.resources.configuration.locales[0]
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var nextBillingDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var cycle by remember { mutableStateOf("Monthly") }
    var category by remember { mutableStateOf("Entertainment") }
    var colorHex by remember { mutableStateOf("#6366F1") }
    var selectedCurrency by remember { mutableStateOf(if (locale.language == "vi") "VND" else "USD") }
    var autoDeleteMode by remember { mutableStateOf("unlimited") } // "unlimited", "once", "custom"
    var customTimes by remember { mutableStateOf("") }

    // Bank transfer details for VietQR states
    var hasBankInfo by remember { mutableStateOf(false) }
    var bankAccount by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var bankAccountHolder by remember { mutableStateOf("") }
 
    val categories = listOf("Entertainment", "Utilities", "Work", "Cloud", "Music", "Food", "Finance", "Anniversary", "Family", "Trial", "Notes", "Other")
    var expandedCategory by remember { mutableStateOf(false) }
 
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = nextBillingDate)
    var showDatePicker by remember { mutableStateOf(false) }
 
    val isEditMode = subscriptionId != null && subscriptionId != -1L
 
    LaunchedEffect(subscriptionId, prefillName, prefillAmount, prefillCycle, prefillCategory, prefillColorHex, prefillBankName, prefillBankAccount, prefillBankAccountHolder) {
        if (isEditMode) {
            val sub = viewModel.getSubscriptionById(subscriptionId!!)
            sub?.let {
                name = it.name
                amount = it.amount.toString()
                nextBillingDate = it.nextBillingDate
                cycle = it.cycle
                category = it.category
                colorHex = it.colorHex
                selectedCurrency = it.currency
                
                val remTimes = it.remainingTimes
                if (remTimes == null || remTimes <= 0) {
                    autoDeleteMode = "unlimited"
                    customTimes = ""
                } else if (remTimes == 1) {
                    autoDeleteMode = "once"
                    customTimes = ""
                } else {
                    autoDeleteMode = "custom"
                    customTimes = remTimes.toString()
                }

                hasBankInfo = it.bankAccount != null
                bankAccount = it.bankAccount ?: ""
                bankName = it.bankName ?: ""
                bankAccountHolder = it.bankAccountHolder ?: ""
            }
        } else {
            prefillName?.let { name = it }
            prefillAmount?.let { 
                amount = it.toString()
                selectedCurrency = if (it > 1000.0) "VND" else "USD"
            }
            prefillCycle?.let { cycle = it }
            prefillCategory?.let { category = it }
            prefillColorHex?.let { colorHex = it }
            if (prefillBankAccount != null || prefillBankName != null) {
                hasBankInfo = true
                bankAccount = prefillBankAccount ?: ""
                bankName = prefillBankName ?: ""
                bankAccountHolder = prefillBankAccountHolder ?: ""
            }
        }
    }

    LaunchedEffect(name) {
        if (!isEditMode && (name.contains("thay dầu", ignoreCase = true) || name.contains("xe máy", ignoreCase = true) || name.contains("nhớt", ignoreCase = true))) {
            cycle = "Every 6 Months"
            category = "Utilities"
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        nextBillingDate = it
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) stringResource(R.string.edit_title) else stringResource(R.string.add_title),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Service Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.add_edit_service_name)) },
                placeholder = { Text(stringResource(R.string.add_edit_service_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            // Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.add_edit_price)) },
                    placeholder = { Text("0.0") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    prefix = { Text(if (selectedCurrency == "VND") "₫ " else "$ ", fontWeight = FontWeight.Bold) }
                )

                // Currency Selector Dropdown
                var expandedCurrency by remember { mutableStateOf(false) }
                Box(modifier = Modifier.padding(top = 8.dp)) {
                    OutlinedCard(
                        onClick = { expandedCurrency = true },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(56.dp).width(96.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(selectedCurrency, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    }
                    DropdownMenu(
                        expanded = expandedCurrency,
                        onDismissRequest = { expandedCurrency = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("VND (₫)") },
                            onClick = {
                                selectedCurrency = "VND"
                                expandedCurrency = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("USD ($)") },
                            onClick = {
                                selectedCurrency = "USD"
                                expandedCurrency = false
                            }
                        )
                    }
                }
            }

            // Billing Date
            val dateFormatter = remember { java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG) }
            OutlinedTextField(
                value = dateFormatter.format(Date(nextBillingDate)),
                onValueChange = { },
                label = { Text(stringResource(R.string.add_edit_renewal_date)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.add_edit_select_date), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            // Category Selection
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = getCategoryDisplayName(category),
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(R.string.add_edit_category)) },
                    leadingIcon = {
                        Icon(
                            imageVector = CategoryUtils.getCategoryIcon(category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = CategoryUtils.getCategoryIcon(selectionOption),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(getCategoryDisplayName(selectionOption)) },
                            onClick = {
                                category = selectionOption
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            // Cycle Selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.add_edit_billing_cycle), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Daily", "Weekly").forEach { item ->
                            val selected = cycle == item
                            FilterChip(
                                selected = selected,
                                onClick = { cycle = item },
                                label = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            stringResource(getCycleDisplayNameRes(item)), 
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Monthly", "Every 3 Months").forEach { item ->
                            val selected = cycle == item
                            FilterChip(
                                selected = selected,
                                onClick = { cycle = item },
                                label = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            stringResource(getCycleDisplayNameRes(item)), 
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Every 6 Months", "Yearly").forEach { item ->
                            val selected = cycle == item
                            FilterChip(
                                selected = selected,
                                onClick = { cycle = item },
                                label = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            stringResource(getCycleDisplayNameRes(item)), 
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("One-time").forEach { item ->
                            val selected = cycle == item
                            FilterChip(
                                selected = selected,
                                onClick = { cycle = item },
                                label = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            stringResource(getCycleDisplayNameRes(item)), 
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // Auto-delete Options Section
            if (cycle != "One-time") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.add_edit_auto_delete_title), 
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
                                onClick = { autoDeleteMode = mode },
                                label = { 
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            stringResource(labelRes), 
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
                                    customTimes = input
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


            // VietQR bank info switch and fields
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Thông tin chuyển khoản VietQR", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Cấu hình tài khoản ngân hàng để tạo mã QR thanh toán nhanh", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(checked = hasBankInfo, onCheckedChange = { hasBankInfo = it })
                    }

                    if (hasBankInfo) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = { Text("Tên ngân hàng (Ví dụ: VCB, TCB, MB)") },
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
                            onValueChange = { bankAccount = it },
                            label = { Text("Số tài khoản nhận tiền") },
                            placeholder = { Text("Nhập số tài khoản") },
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
                            onValueChange = { bankAccountHolder = it },
                            label = { Text("Tên chủ tài khoản") },
                            placeholder = { Text("Ví dụ: NGUYEN VAN A") },
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotBlank() && amount.isNotBlank()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        
                        val finalNextBillingDate = nextBillingDate

                        val remainingTimesVal = if (cycle == "One-time") {
                            null
                        } else {
                            when (autoDeleteMode) {
                                "once" -> 1
                                "custom" -> customTimes.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                else -> null
                            }
                        }

                        val sub = Subscription(
                            id = if (isEditMode) subscriptionId!! else 0,
                            name = name,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            nextBillingDate = finalNextBillingDate,
                            cycle = cycle,
                            category = category,
                            colorHex = colorHex,
                            currency = selectedCurrency,
                            remainingTimes = remainingTimesVal,
                            
                            isKmBased = false,
                            lastOdometer = null,
                            targetIntervalKm = null,
                            dailyAverageKm = null,
                            lastOdometerUpdateDate = null,
                            
                            bankAccount = if (hasBankInfo) bankAccount else null,
                            bankName = if (hasBankInfo) bankName else null,
                            bankAccountHolder = if (hasBankInfo) bankAccountHolder else null
                        )
                        if (isEditMode) viewModel.update(sub) else viewModel.insert(sub)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                enabled = name.isNotBlank() && amount.isNotBlank()
            ) {
                Text(
                    if (isEditMode) stringResource(R.string.add_edit_btn_update) else stringResource(R.string.add_edit_btn_track),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun AddEditPreview() {
    Sub_lazyTheme {
        AddEditSubscriptionScreen(onNavigateBack = {})
    }
}

private @Composable
fun getCategoryDisplayName(category: String): String {
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

private fun getCycleDisplayNameRes(cycle: String): Int {
    return when (cycle) {
        "Daily" -> R.string.cycle_daily
        "Weekly" -> R.string.cycle_weekly
        "Monthly" -> R.string.cycle_monthly
        "Every 3 Months" -> R.string.cycle_3_months
        "Every 6 Months" -> R.string.cycle_6_months
        "Yearly" -> R.string.cycle_yearly
        "One-time" -> R.string.cycle_one_time
        else -> R.string.cycle_monthly
    }
}
