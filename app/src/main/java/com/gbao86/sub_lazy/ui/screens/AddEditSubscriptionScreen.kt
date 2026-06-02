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
 
    val categories = listOf("Entertainment", "Utilities", "Work", "Cloud", "Music", "Food", "Finance", "Anniversary", "Family", "Trial", "Notes", "Other")
    var expandedCategory by remember { mutableStateOf(false) }
 
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = nextBillingDate)
    var showDatePicker by remember { mutableStateOf(false) }
 
    val isEditMode = subscriptionId != null && subscriptionId != -1L
 
    LaunchedEffect(subscriptionId, prefillName, prefillAmount) {
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
            }
        } else {
            prefillName?.let { name = it }
            prefillAmount?.let { 
                amount = it.toString()
                selectedCurrency = if (it > 1000.0) "VND" else "USD"
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
        }
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
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Monthly", "Yearly", "One-time").forEach { item ->
                        val selected = cycle == item
                        FilterChip(
                            selected = selected,
                            onClick = { cycle = item },
                            label = { 
                                Text(
                                    stringResource(getCycleDisplayNameRes(item)), 
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                ) 
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (name.isNotBlank() && amount.isNotBlank()) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val sub = Subscription(
                            id = if (isEditMode) subscriptionId!! else 0,
                            name = name,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            nextBillingDate = nextBillingDate,
                            cycle = cycle,
                            category = category,
                            colorHex = colorHex,
                            currency = selectedCurrency
                        )
                        if (isEditMode) viewModel.update(sub) else viewModel.insert(sub)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (isEditMode) stringResource(R.string.add_edit_btn_update) else stringResource(R.string.add_edit_btn_track), 
                    fontSize = 18.sp, 
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
        "Monthly" -> R.string.cycle_monthly
        "Yearly" -> R.string.cycle_yearly
        "One-time" -> R.string.cycle_one_time
        else -> R.string.cycle_monthly
    }
}
