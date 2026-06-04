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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.ui.theme.Sub_lazyTheme
import com.gbao86.sub_lazy.viewmodel.SubscriptionViewModel
import java.util.*
import androidx.compose.ui.platform.LocalContext
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.data.SubscriptionTemplate
import com.gbao86.sub_lazy.data.SubscriptionTemplates
import androidx.compose.ui.res.stringResource
import com.gbao86.sub_lazy.R
import kotlinx.coroutines.delay
import androidx.core.graphics.toColorInt

val popularTemplates = SubscriptionTemplates.digitalTemplates.take(5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: SubscriptionViewModel = viewModel(),
    onFinishOnboarding: () -> Unit
) {
    var selectedTemplates by remember { mutableStateOf(setOf<SubscriptionTemplate>()) }

    Scaffold(
        bottomBar = {
            // ── Premium CTA Button ──────────────────────────────────────────
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                val hasSelected = selectedTemplates.isNotEmpty()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = {
                            selectedTemplates.forEach { template ->
                                val cal = Calendar.getInstance()
                                when (template.cycle) {
                                    "Daily" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                                    "Weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                                    "Monthly" -> cal.add(Calendar.MONTH, 1)
                                    "Every 3 Months" -> cal.add(Calendar.MONTH, 3)
                                    "Every 6 Months" -> cal.add(Calendar.MONTH, 6)
                                    "Yearly" -> cal.add(Calendar.YEAR, 1)
                                    else -> cal.add(Calendar.MONTH, 1)
                                }
                                viewModel.insert(
                                    Subscription(
                                        name = template.name,
                                        amount = template.amount,
                                        nextBillingDate = cal.timeInMillis,
                                        cycle = template.cycle,
                                        category = template.category,
                                        colorHex = template.colorHex
                                    )
                                )
                            }
                            onFinishOnboarding()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (hasSelected) 8.dp else 0.dp
                        )
                    ) {
                        AnimatedContent(
                            targetState = hasSelected,
                            label = "btn_text"
                        ) { selected ->
                            if (selected) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${stringResource(R.string.onboarding_btn_start)} (${selectedTemplates.size})",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.onboarding_btn_empty),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // ── Hero Header with Gradient ────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    Column {
                        // Skip button top right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onFinishOnboarding) {
                                Text(
                                    text = stringResource(R.string.onboarding_skip),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // App icon + name badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = buildAnnotatedString {
                                append(stringResource(R.string.onboarding_welcome) + "\n")
                                withStyle(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                ) {
                                    append("Lazy Mode 😴")
                                }
                            },
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.onboarding_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OnboardingStatChip(
                                emoji = "⚡",
                                label = "Tự động hóa",
                                modifier = Modifier.weight(1f)
                            )
                            OnboardingStatChip(
                                emoji = "🔔",
                                label = "Nhắc nhở",
                                modifier = Modifier.weight(1f)
                            )
                            OnboardingStatChip(
                                emoji = "🛡️",
                                label = "Bảo mật",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Chọn dịch vụ bạn đang dùng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            itemsIndexed(popularTemplates) { index, template ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(index * 60L)
                    visible = true
                }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(300)) + slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    TemplateItem(
                        template = template,
                        isSelected = selectedTemplates.contains(template),
                        onToggle = {
                            selectedTemplates = if (selectedTemplates.contains(template)) {
                                selectedTemplates - template
                            } else {
                                selectedTemplates + template
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingStatChip(
    emoji: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TemplateItem(
    template: SubscriptionTemplate,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]

    val accentColor = remember(template.colorHex) {
        try { Color(template.colorHex.toColorInt()) }
        catch (_: Exception) { Color(0xFF6366F1) }
    }

    val cardBackground = animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.08f)
                      else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "card_bg"
    )

    val borderColor = animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.6f)
                      else Color.Transparent,
        animationSpec = tween(200),
        label = "border"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBackground.value)
            .border(1.5.dp, borderColor.value, RoundedCornerShape(20.dp))
            .clickable { onToggle() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = template.name.take(1).uppercase(),
                color = accentColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = CurrencyFormatter.format(template.amount, "VND", locale),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Animated checkbox
        AnimatedContent(
            targetState = isSelected,
            transitionSpec = { scaleIn(tween(200)) togetherWith scaleOut(tween(200)) },
            label = "check"
        ) { checked ->
            if (checked) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    color = Color.Transparent,
                    modifier = Modifier.size(28.dp)
                ) {}
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun OnboardingPreview() {
    Sub_lazyTheme {
        OnboardingScreen(onFinishOnboarding = {})
    }
}
