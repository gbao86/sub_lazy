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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.SharedMember
import com.gbao86.sub_lazy.ui.CategoryUtils
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.viewmodel.AddEditViewModel
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
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
    viewModel: AddEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val locale = context.resources.configuration.locales[0]
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var nextBillingDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var cycle by remember { mutableStateOf(BillingCycle.MONTHLY) }
    var category by remember { mutableStateOf(SubscriptionCategory.ENTERTAINMENT) }
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
    var isSharedExpanded by remember { mutableStateOf(false) }
    var isBankExpanded by remember { mutableStateOf(false) }
 
    val categories = SubscriptionCategory.entries
    var showCategorySheet by remember { mutableStateOf(false) }
 
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = nextBillingDate)
    var showDatePicker by remember { mutableStateOf(false) }
 
    val isEditMode = subscriptionId != null && subscriptionId != -1L
 
    LaunchedEffect(subscriptionId, prefillName, prefillAmount, prefillCycle, prefillCategory, prefillColorHex, prefillBankName, prefillBankAccount, prefillBankAccountHolder) {
        if (isEditMode) {
            val sub = viewModel.getSubscriptionById(subscriptionId)
            sub?.let {
                name = it.name
                selectedCurrency = it.currency.code
                amount = formatDoubleToInput(it.amount, it.currency.code, locale)
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
                isBankExpanded = it.bankAccount != null
                bankAccount = it.bankAccount ?: ""
                bankName = it.bankName ?: ""
                bankAccountHolder = it.bankAccountHolder ?: ""

                // Map new fields
                totalSessions = it.totalSessions?.toString() ?: ""
                remainingSessions = it.remainingSessions?.toString() ?: ""
                totalInstallmentPeriods = it.totalInstallmentPeriods?.toString() ?: ""
                isShared = it.isShared
                isSharedExpanded = it.isShared
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
            prefillCycle?.let { cycle = BillingCycle.fromDisplayName(it) }
            prefillCategory?.let { category = SubscriptionCategory.fromDisplayName(it) }
            prefillColorHex?.let { colorHex = it }
            if (prefillBankAccount != null || prefillBankName != null) {
                hasBankInfo = true
                isBankExpanded = true
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
                    cycle = BillingCycle.EVERY_6_MONTHS
                    category = SubscriptionCategory.UTILITIES
                    serviceType = "standard"
                }
                lowercaseName.contains("máy lạnh") || lowercaseName.contains("điều hòa") || lowercaseName.contains("vệ sinh máy") -> {
                    cycle = BillingCycle.EVERY_6_MONTHS
                    category = SubscriptionCategory.UTILITIES
                    serviceType = "standard"
                }
                lowercaseName.contains("gym") || lowercaseName.contains("yoga") || lowercaseName.contains("phòng tập") || lowercaseName.contains("bể bơi") -> {
                    serviceType = "session"
                    totalSessions = "10"
                    remainingSessions = "10"
                    category = SubscriptionCategory.UTILITIES
                }
                lowercaseName.contains("spaylater") || lowercaseName.contains("fundiin") || lowercaseName.contains("trả góp") || lowercaseName.contains("kỳ hạn") -> {
                    serviceType = "installment"
                    totalInstallmentPeriods = "12"
                    cycle = BillingCycle.MONTHLY
                    category = SubscriptionCategory.FINANCE
                }
                lowercaseName.contains("bảo hiểm") -> {
                    cycle = BillingCycle.YEARLY
                    category = SubscriptionCategory.FINANCE
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

            CommitmentTypeSelector(
                serviceType = serviceType,
                onServiceTypeChange = { serviceType = it }
            )

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
                    value = CategoryUtils.getCategoryDisplayName(category),
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
                                            text = CategoryUtils.getCategoryDisplayName(cat),
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
            
            if (serviceType == "standard") {
                BillingCycleSelector(
                    cycle = cycle,
                    onCycleChange = { cycle = it }
                )
                if (cycle != BillingCycle.ONE_TIME) {
                    AutoDeleteSection(
                        autoDeleteMode = autoDeleteMode,
                        onAutoDeleteModeChange = { autoDeleteMode = it },
                        customTimes = customTimes,
                        onCustomTimesChange = { customTimes = it }
                    )
                }
            } else {
                InstallmentSessionFields(
                    serviceType = serviceType,
                    totalInstallmentPeriods = totalInstallmentPeriods,
                    onTotalInstallmentPeriodsChange = { totalInstallmentPeriods = it },
                    totalSessions = totalSessions,
                    onTotalSessionsChange = { totalSessions = it },
                    remainingSessions = remainingSessions,
                    onRemainingSessionsChange = { remainingSessions = it }
                )
            }
            
            // Shared Subscription Card
            SharedMemberSection(
                isSharedExpanded = isSharedExpanded,
                onIsSharedExpandedChange = { isSharedExpanded = it },
                isShared = isShared,
                onIsSharedChange = { isShared = it },
                sharedMembersList = sharedMembersList,
                onSharedMembersListChange = { sharedMembersList = it },
                selectedCurrency = selectedCurrency,
                locale = locale
            )


            VietQRBankSection(
                isBankExpanded = isBankExpanded,
                onIsBankExpandedChange = { isBankExpanded = it },
                hasBankInfo = hasBankInfo,
                onHasBankInfoChange = { hasBankInfo = it },
                bankName = bankName,
                onBankNameChange = { bankName = it },
                bankAccount = bankAccount,
                onBankAccountChange = { bankAccount = it },
                bankAccountHolder = bankAccountHolder,
                onBankAccountHolderChange = { bankAccountHolder = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && amount.isNotBlank()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        
                        val finalNextBillingDate = nextBillingDate
                        val finalCycle = when (serviceType) {
                            "installment" -> BillingCycle.MONTHLY
                            "session" -> BillingCycle.ONE_TIME
                            else -> cycle
                        }

                        val remainingTimesVal = when (serviceType) {
                            "installment" -> totalInstallmentPeriods.toIntOrNull()?.coerceAtLeast(1) ?: 12
                            "session" -> null
                            else -> {
                                if (finalCycle == BillingCycle.ONE_TIME) {
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
                                currency = SubscriptionCurrency.fromCode(selectedCurrency),
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
                                currency = SubscriptionCurrency.fromCode(selectedCurrency),
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





