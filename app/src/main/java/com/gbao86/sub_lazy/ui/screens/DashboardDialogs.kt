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

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.SubscriptionTemplates
import com.gbao86.sub_lazy.ui.CategoryUtils
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.toComposeColor
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Budget Editor Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetEditorSheet(
    userBalance: Double,
    budgetResetDay: Int,
    locale: Locale,
    onDismiss: () -> Unit,
    onSave: (Double, Int) -> Unit
) {
    var inputVal by remember {
        mutableStateOf(
            java.text.NumberFormat.getNumberInstance(locale).format(userBalance.toLong())
        )
    }
    var resetDayInput by remember { mutableStateOf(budgetResetDay.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.budget_update_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = inputVal,
                onValueChange = { text ->
                    val cleanDigits = text.replace("[^\\d]".toRegex(), "")
                    if (cleanDigits.isEmpty()) {
                        inputVal = ""
                    } else {
                        val parsed = cleanDigits.toLongOrNull()
                        if (parsed != null) {
                            inputVal = java.text.NumberFormat.getNumberInstance(locale).format(parsed)
                        }
                    }
                },
                label = { Text(stringResource(R.string.budget_monthly_label)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = resetDayInput,
                onValueChange = { text ->
                    val cleanDigits = text.replace("[^\\d]".toRegex(), "")
                    if (cleanDigits.isEmpty()) {
                        resetDayInput = ""
                    } else {
                        val parsed = cleanDigits.toIntOrNull()
                        if (parsed != null && parsed in 1..31) {
                            resetDayInput = parsed.toString()
                        }
                    }
                },
                label = { Text(stringResource(R.string.budget_reset_day_label)) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(stringResource(R.string.btn_cancel), fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        val cleanDigits = inputVal.replace("[^\\d]".toRegex(), "")
                        val newBalance = cleanDigits.toDoubleOrNull() ?: 0.0
                        val newResetDay = resetDayInput.toIntOrNull() ?: 1
                        onSave(newBalance, newResetDay)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.btn_save), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Bottom Sheet (how to add: manually / scan / template)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddActionBottomSheet(
    onDismiss: () -> Unit,
    onAddManually: () -> Unit,
    onScanScreenshot: () -> Unit,
    onAddFromTemplate: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.action_add_manually)) },
                leadingContent = { Icon(Icons.Rounded.Edit, null) },
                modifier = Modifier.clickable {
                    onDismiss()
                    onAddManually()
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.action_scan_screenshot)) },
                leadingContent = { Icon(Icons.Rounded.PhotoCamera, null) },
                modifier = Modifier.clickable {
                    onDismiss()
                    onScanScreenshot()
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.action_add_from_template)) },
                leadingContent = { Icon(Icons.Rounded.Bookmarks, null) },
                modifier = Modifier.clickable {
                    onDismiss()
                    onAddFromTemplate()
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Templates Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TemplatesDialog(
    locale: Locale,
    onDismiss: () -> Unit,
    onTemplateSelected: (String?, Double?, String?, String?, String?, String?, String?, String?) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.template_dialog_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text(
                            stringResource(R.string.template_tab_digital),
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text(
                            stringResource(R.string.template_tab_lifestyle),
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val context = androidx.compose.ui.platform.LocalContext.current
                val templates = if (selectedTab == 0) {
                    SubscriptionTemplates.getDigitalTemplates(context)
                } else {
                    SubscriptionTemplates.getLifestyleTemplates(context)
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    items(templates, key = { it.name }) { template ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDismiss()
                                    onTemplateSelected(
                                        template.name,
                                        template.amount,
                                        template.cycle.displayName,
                                        template.category.displayName,
                                        template.colorHex,
                                        template.bankName,
                                        template.bankAccount,
                                        template.bankAccountHolder
                                    )
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(template.colorHex.toComposeColor().copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CategoryUtils.getIconForName(template.name, template.category),
                                        contentDescription = "Subscription Template Icon",
                                        tint = template.colorHex.toComposeColor(),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        template.name,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = CurrencyFormatter.format(template.amount, "VND", locale),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Settings Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsDialog(
    context: Context,
    linkedAccountEmail: String?,
    googleSignInClient: GoogleSignInClient,
    onDismiss: () -> Unit,
    onEmailChanged: (String?) -> Unit,
    onGoogleSignIn: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onSyncToDrive: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val accountName = linkedAccountEmail
                Text(
                    text = if (accountName != null)
                        stringResource(R.string.settings_gmail_linked, accountName)
                    else
                        stringResource(R.string.settings_gmail_not_linked)
                )
                Button(onClick = {
                    if (accountName != null) {
                        googleSignInClient.signOut().addOnCompleteListener {
                            context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit {
                                remove("gmail_account")
                            }
                            onEmailChanged(null)
                        }
                    } else {
                        onGoogleSignIn()
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (accountName != null)
                            stringResource(R.string.settings_gmail_btn_unlink)
                        else
                            stringResource(R.string.settings_gmail_btn_link)
                    )
                }
                
                HorizontalDivider()
                Text("Backup & Khôi phục", fontWeight = FontWeight.Bold)
                
                Button(onClick = onSyncToDrive, modifier = Modifier.fillMaxWidth(), enabled = accountName != null) {
                    Text("Đồng bộ lên Google Drive")
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onExport, modifier = Modifier.weight(1f)) {
                        Text("Export JSON")
                    }
                    OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) {
                        Text("Import JSON")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
