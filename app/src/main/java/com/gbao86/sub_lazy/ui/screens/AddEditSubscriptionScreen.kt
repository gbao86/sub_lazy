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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.SharedMember
import com.gbao86.sub_lazy.ui.CategoryUtils
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.viewmodel.SubscriptionViewModel
import java.util.Date

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
    var selectedCurrency by remember { mutableStateOf(com.gbao86.sub_lazy.ui.ExchangeRateManager.getTargetCurrencyForLocale(locale)) }
    var autoDeleteMode by remember { mutableStateOf("unlimited") } // "unlimited", "once", "custom"
    var customTimes by remember { mutableStateOf("") }

    // Service Type States
    var serviceType by remember { mutableStateOf("standard") } // "standard", "installment", "session"
    var totalSessions by remember { mutableStateOf("") }
    var remainingSessions by remember { mutableStateOf("") }
    var totalInstallmentPeriods by remember { mutableStateOf("") }

    // Shared Subscription States
    var isShared by remember { mutableStateOf(false) }
    var sharedMembersList by remember { mutableStateOf(emptyList<SharedMember>()) }
    var newMemberName by remember { mutableStateOf("") }
    var newMemberAmount by remember { mutableStateOf("") }
    var newMemberPhone by remember { mutableStateOf("") }

    // Bank transfer details for VietQR states
    var hasBankInfo by remember { mutableStateOf(false) }
    var bankAccount by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var bankAccountHolder by remember { mutableStateOf("") }
 
    val categories = listOf("Entertainment", "Utilities", "Work", "Cloud", "Music", "Food", "Finance", "Anniversary", "Family", "Trial", "Notes", "Other")
    var showCategorySheet by remember { mutableStateOf(false) }
 
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = nextBillingDate)
    var showDatePicker by remember { mutableStateOf(false) }
 
    val isEditMode = subscriptionId != null && subscriptionId != -1L
 
    LaunchedEffect(subscriptionId, prefillName, prefillAmount, prefillCycle, prefillCategory, prefillColorHex, prefillBankName, prefillBankAccount, prefillBankAccountHolder) {
        if (isEditMode) {
            val sub = viewModel.getSubscriptionById(subscriptionId)
            sub?.let {
                name = it.name
                selectedCurrency = it.currency
                amount = formatDoubleToInput(it.amount, it.currency, locale)
                nextBillingDate = it.nextBillingDate
                cycle = it.cycle
                category = it.category
                colorHex = it.colorHex
                
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

                // Map new fields
                totalSessions = it.totalSessions?.toString() ?: ""
                remainingSessions = it.remainingSessions?.toString() ?: ""
                totalInstallmentPeriods = it.totalInstallmentPeriods?.toString() ?: ""
                isShared = it.isShared
                sharedMembersList = SharedMember.parseMembers(it.sharedMembersJson)
                serviceType = when {
                    it.isInstallment -> "installment"
                    it.isSessionBased -> "session"
                    else -> "standard"
                }
            }
        } else {
            prefillName?.let { name = it }
            prefillAmount?.let { 
                selectedCurrency = if (it > 1000.0) "VND" else "USD"
                amount = formatDoubleToInput(it, selectedCurrency, locale)
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
        if (!isEditMode) {
            val lowercaseName = name.lowercase()
            when {
                lowercaseName.contains("thay dầu") || lowercaseName.contains("xe máy") || lowercaseName.contains("nhớt") || lowercaseName.contains("bảo trì xe") -> {
                    cycle = "Every 6 Months"
                    category = "Utilities"
                    serviceType = "standard"
                }
                lowercaseName.contains("máy lạnh") || lowercaseName.contains("điều hòa") || lowercaseName.contains("vệ sinh máy") -> {
                    cycle = "Every 6 Months"
                    category = "Utilities"
                    serviceType = "standard"
                }
                lowercaseName.contains("gym") || lowercaseName.contains("yoga") || lowercaseName.contains("phòng tập") || lowercaseName.contains("bể bơi") -> {
                    serviceType = "session"
                    totalSessions = "10"
                    remainingSessions = "10"
                    category = "Utilities"
                }
                lowercaseName.contains("spaylater") || lowercaseName.contains("fundiin") || lowercaseName.contains("trả góp") || lowercaseName.contains("kỳ hạn") -> {
                    serviceType = "installment"
                    totalInstallmentPeriods = "12"
                    cycle = "Monthly"
                    category = "Finance"
                }
                lowercaseName.contains("bảo hiểm") -> {
                    cycle = "Yearly"
                    category = "Finance"
                    serviceType = "standard"
                }
            }
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
                trailingIcon = {
                    if (name.isNotBlank()) {
                        Icon(
                            imageVector = CategoryUtils.getIconForName(name, category),
                            contentDescription = "Auto-detected Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            // Service Type Selector (Thường, Trả góp, Số buổi)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.add_edit_commitment_type), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                            onClick = { serviceType = typeVal },
                            label = {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(
                                        typeLabel,
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

            // Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input ->
                        amount = formatInputString(input, selectedCurrency, locale)
                    },
                    label = { Text(stringResource(R.string.add_edit_price)) },
                    placeholder = { Text("0.0") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
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

                // Currency Segmented Control
                val targetCurrency = com.gbao86.sub_lazy.ui.ExchangeRateManager.getTargetCurrencyForLocale(locale)
                val options = if (targetCurrency == "USD") listOf("USD", "VND") else listOf(targetCurrency, "USD")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .height(64.dp)
                        .width(160.dp)
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        options.forEach { option ->
                            val isSelected = option == selectedCurrency
                            val backgroundAlpha by animateFloatAsState(
                                targetValue = if (isSelected) 1f else 0f,
                                label = "bg_alpha"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                label = "text_color"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = backgroundAlpha))
                                    .clickable {
                                        val prev = selectedCurrency
                                        if (prev != option) {
                                            selectedCurrency = option
                                            val doubleVal = parseFormattedAmount(amount, locale)
                                            amount = formatDoubleToInput(doubleVal, option, locale)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val optionLabel = when (option) {
                                    "VND" -> "₫ VND"
                                    "EUR" -> "€ EUR"
                                    "CNY" -> "¥ CNY"
                                    "JPY" -> "¥ JPY"
                                    "THB" -> "฿ THB"
                                    "KRW" -> "₩ KRW"
                                    else -> "$ USD"
                                }
                                Text(
                                    text = optionLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }

            // Billing Date
            val dateFormatter = remember { java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = dateFormatter.format(Date(nextBillingDate)),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.add_edit_renewal_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                    ),
                    trailingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.add_edit_select_date))
                    }
                )
            }

            // Category Selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategorySheet = true }
            ) {
                OutlinedTextField(
                    value = getCategoryDisplayName(category),
                    onValueChange = { },
                    readOnly = true,
                    enabled = false,
                    label = { Text(stringResource(R.string.add_edit_category)) },
                    leadingIcon = {
                        Icon(
                            imageVector = CategoryUtils.getCategoryIcon(category),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (showCategorySheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCategorySheet = false },
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .navigationBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.add_edit_select_category),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.heightIn(max = 340.dp)
                        ) {
                            items(categories.size, key = { categories[it] }) { index ->
                                val cat = categories[index]
                                val isSelected = cat == category
                                val catColor = getCategoryAccentColor(cat)
                                
                                OutlinedCard(
                                    onClick = {
                                        category = cat
                                        showCategorySheet = false
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    ),
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = if (isSelected) catColor.copy(alpha = 0.08f) else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(88.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = CategoryUtils.getCategoryIcon(cat),
                                            contentDescription = null,
                                            tint = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = getCategoryDisplayName(cat),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            
            // Conditional Sections based on Service Type
            if (serviceType == "standard") {
                // Cycle Selection
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.add_edit_billing_cycle), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    val cycleRows = listOf(
                        listOf("Daily", "Weekly", "Monthly"),
                        listOf("Every 3 Months", "Every 6 Months", "Yearly")
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
                                        onClick = { cycle = item },
                                        label = { 
                                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                                Text(
                                                    stringResource(getCycleDisplayNameRes(item)), 
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
            } else if (serviceType == "installment") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.add_edit_installment_info), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = totalInstallmentPeriods,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                totalInstallmentPeriods = input
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
                        stringResource(R.string.add_edit_installment_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (serviceType == "session") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.add_edit_session_info), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = totalSessions,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    totalSessions = input
                                    if (remainingSessions.isEmpty()) {
                                        remainingSessions = input
                                    }
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
                                    remainingSessions = input
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
                        stringResource(R.string.add_edit_session_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Shared Subscription Card
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
                            Text(stringResource(R.string.add_edit_shared_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(stringResource(R.string.add_edit_shared_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(checked = isShared, onCheckedChange = { isShared = it })
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
                                            formatDoubleToInput(member.amount, selectedCurrency, locale) + " " + selectedCurrency,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                sharedMembersList = sharedMembersList.filterIndexed { i, _ -> i != idx }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.delete_dialog_confirm),
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
                                    sharedMembersList = sharedMembersList + SharedMember(
                                        name = newMemberName,
                                        amount = amt,
                                        hasPaid = false,
                                        phone = newMemberPhone.takeIf { it.isNotBlank() }
                                    )
                                    newMemberName = ""
                                    newMemberAmount = ""
                                    newMemberPhone = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.add_edit_shared_btn_add), style = MaterialTheme.typography.labelLarge)
                        }
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
                            Text(stringResource(R.string.add_edit_bank_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(stringResource(R.string.add_edit_bank_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(checked = hasBankInfo, onCheckedChange = { hasBankInfo = it })
                    }

                    if (hasBankInfo) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
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
                            onValueChange = { bankAccount = it },
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
                            onValueChange = { bankAccountHolder = it },
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && amount.isNotBlank()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        
                        val finalNextBillingDate = nextBillingDate
                        val finalCycle = when (serviceType) {
                            "installment" -> "Monthly"
                            "session" -> "One-time"
                            else -> cycle
                        }

                        val remainingTimesVal = when (serviceType) {
                            "installment" -> totalInstallmentPeriods.toIntOrNull()?.coerceAtLeast(1) ?: 12
                            "session" -> null
                            else -> {
                                if (finalCycle == "One-time") {
                                    null
                                } else {
                                    when (autoDeleteMode) {
                                        "once" -> 1
                                        "custom" -> customTimes.toIntOrNull()?.coerceAtLeast(1) ?: 1
                                        else -> null
                                    }
                                }
                            }
                        }

                        val finalIsSessionBased = serviceType == "session"
                        val finalTotalSessions = if (finalIsSessionBased) totalSessions.toIntOrNull() else null
                        val finalRemainingSessions = if (finalIsSessionBased) remainingSessions.toIntOrNull() else null
                        val finalIsInstallment = serviceType == "installment"
                        val finalTotalInstallmentPeriods = if (finalIsInstallment) totalInstallmentPeriods.toIntOrNull() else null
                        
                        val finalIsShared = isShared
                        val finalSharedMembersJson = if (finalIsShared) SharedMember.serializeMembers(sharedMembersList) else null

                        if (isEditMode) {
                            viewModel.updateSubscriptionDetails(
                                id = subscriptionId,
                                name = name,
                                amount = parseFormattedAmount(amount, locale),
                                nextBillingDate = finalNextBillingDate,
                                cycle = finalCycle,
                                category = category,
                                colorHex = colorHex,
                                currency = selectedCurrency,
                                remainingTimes = remainingTimesVal,
                                bankAccount = if (hasBankInfo) bankAccount else null,
                                bankName = if (hasBankInfo) bankName else null,
                                bankAccountHolder = if (hasBankInfo) bankAccountHolder else null,
                                isSessionBased = finalIsSessionBased,
                                totalSessions = finalTotalSessions,
                                remainingSessions = finalRemainingSessions,
                                isInstallment = finalIsInstallment,
                                isShared = finalIsShared,
                                sharedMembersJson = finalSharedMembersJson
                            )
                        } else {
                            val sub = Subscription(
                                name = name,
                                amount = parseFormattedAmount(amount, locale),
                                nextBillingDate = finalNextBillingDate,
                                cycle = finalCycle,
                                category = category,
                                colorHex = colorHex,
                                currency = selectedCurrency,
                                remainingTimes = remainingTimesVal,
                                bankAccount = if (hasBankInfo) bankAccount else null,
                                bankName = if (hasBankInfo) bankName else null,
                                bankAccountHolder = if (hasBankInfo) bankAccountHolder else null,
                                isSessionBased = finalIsSessionBased,
                                totalSessions = finalTotalSessions,
                                remainingSessions = finalRemainingSessions,
                                isInstallment = finalIsInstallment,
                                totalInstallmentPeriods = finalTotalInstallmentPeriods,
                                isShared = finalIsShared,
                                sharedMembersJson = finalSharedMembersJson
                            )
                            viewModel.insert(sub)
                        }
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

private fun getCategoryAccentColor(category: String): Color {
    return when (category) {
        "Entertainment" -> Color(0xFF7C3AED)
        "Utilities" -> Color(0xFFF59E0B)
        "Work" -> Color(0xFF3B82F6)
        "Cloud" -> Color(0xFF06B6D4)
        "Music" -> Color(0xFFEC4899)
        "Food" -> Color(0xFFF97316)
        "Finance" -> Color(0xFF10B981)
        "Anniversary" -> Color(0xFFEF4444)
        "Family" -> Color(0xFF14B8A6)
        "Trial" -> Color(0xFF8B5CF6)
        "Notes" -> Color(0xFF64748B)
        else -> Color(0xFF94A3B8)
    }
}

private fun formatInputString(input: String, currency: String, locale: java.util.Locale): String {
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

private fun formatDoubleToInput(value: Double, currency: String, locale: java.util.Locale): String {
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

private fun parseFormattedAmount(amountStr: String, locale: java.util.Locale): Double {
    if (amountStr.isBlank()) return 0.0
    val symbols = java.text.DecimalFormatSymbols.getInstance(locale)
    val decimalChar = symbols.decimalSeparator.toString()
    val thousandChar = symbols.groupingSeparator.toString()
    
    val cleanStr = amountStr.replace(thousandChar, "").replace(decimalChar, ".")
    return cleanStr.toDoubleOrNull() ?: 0.0
}

