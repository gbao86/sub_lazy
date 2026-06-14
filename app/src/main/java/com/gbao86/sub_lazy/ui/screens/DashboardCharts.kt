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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.CategorySpending
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.CategoryUtils
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.sqrt

// ─────────────────────────────────────────────────────────────────────────────
// Donut Chart
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun InteractiveDonutChart(
    spending: List<CategorySpending>,
    totalSpending: Double,
    selectedCategory: CategorySpending?,
    onCategorySelected: (CategorySpending?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF6366F1), Color(0xFF06B6D4), Color(0xFFF43F5E),
        Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF8B5CF6)
    )
    val locale = LocalContext.current.resources.configuration.locales[0]
    val animateSweep = remember { Animatable(0f) }
    val animateScale = remember { Animatable(0.8f) }
    
    LaunchedEffect(spending) {
        launch {
            animateSweep.snapTo(0f)
            animateSweep.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
        }
        launch {
            animateScale.snapTo(0.8f)
            animateScale.animateTo(1f, tween(1000, easing = EaseOutBack))
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(animateScale.value)
                .pointerInput(spending) {
                    detectTapGestures { offset ->
                        if (totalSpending <= 0.0) return@detectTapGestures
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val dist = sqrt(dx * dx + dy * dy)
                        // FIX: Use exact stroke bounds (inner = radius - halfStroke, outer = radius + halfStroke)
                        val radius = size.width / 2f
                        val halfStroke = 19.dp.toPx() // half of 38dp (selected stroke)
                        val innerBound = radius - halfStroke - 4.dp.toPx()
                        val outerBound = radius + halfStroke + 4.dp.toPx()
                        if (dist in innerBound..outerBound) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                            if (angle < 0) angle += 360f
                            if (angle >= 360f) angle -= 360f
                            var currentAngle = 0f
                            spending.forEach { item ->
                                val sweep = ((item.totalAmount / totalSpending) * 360f).toFloat()
                                if (angle >= currentAngle && angle < currentAngle + sweep) {
                                    onCategorySelected(if (selectedCategory?.category == item.category) null else item)
                                    return@detectTapGestures
                                }
                                currentAngle += sweep
                            }
                        } else {
                            onCategorySelected(null)
                        }
                    }
                }
        ) {
            var startAngle = -90f
            spending.forEachIndexed { index, item ->
                val sweepAngle = ((item.totalAmount / totalSpending) * 360f).toFloat() * animateSweep.value
                val isSelected = selectedCategory?.category == item.category
                val middleAngle = startAngle + sweepAngle / 2f
                val angleRad = Math.toRadians(middleAngle.toDouble())
                val shiftAmt = if (isSelected) 8.dp.toPx() else 0f
                val shiftX = (shiftAmt * kotlin.math.cos(angleRad)).toFloat()
                val shiftY = (shiftAmt * kotlin.math.sin(angleRad)).toFloat()

                val gap = if (spending.size > 1) 2f else 0f
                val finalSweepAngle = (sweepAngle - gap).coerceAtLeast(0f)
                val finalStartAngle = startAngle + gap / 2f

                drawArc(
                    color = colors[index % colors.size].copy(
                        alpha = if (selectedCategory == null || isSelected) 1f else 0.3f
                    ),
                    startAngle = finalStartAngle,
                    sweepAngle = finalSweepAngle,
                    useCenter = false,
                    topLeft = Offset(shiftX, shiftY),
                    style = Stroke(
                        width = if (isSelected) 38.dp.toPx() else 28.dp.toPx(),
                        cap = StrokeCap.Butt
                    )
                )
                startAngle += sweepAngle
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Text(
                text = if (selectedCategory != null)
                    CategoryUtils.getCategoryDisplayName(selectedCategory.category)
                else
                    stringResource(R.string.chart_all_categories),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = CurrencyFormatter.format(
                    selectedCategory?.totalAmount ?: totalSpending, "VND", locale
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                softWrap = false,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category Legend
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun InteractiveCategoryLegend(
    spending: List<CategorySpending>,
    selectedCategory: CategorySpending?,
    onCategorySelected: (CategorySpending?) -> Unit,
    subscriptions: List<Subscription>
) {
    val colors = listOf(
        Color(0xFF6366F1), Color(0xFF06B6D4), Color(0xFFF43F5E),
        Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF8B5CF6)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        spending.forEachIndexed { index, item ->
            val isSelected = selectedCategory?.category == item.category
            InteractiveCategoryRow(
                item = item,
                color = colors[index % colors.size],
                isSelected = isSelected,
                categorySubs = subscriptions.filter { it.category == item.category }
            ) {
                onCategorySelected(if (isSelected) null else item)
            }
        }
    }
}

@Composable
fun InteractiveCategoryRow(
    item: CategorySpending,
    color: Color,
    isSelected: Boolean,
    categorySubs: List<Subscription>,
    onClick: () -> Unit
) {
    val locale = LocalContext.current.resources.configuration.locales[0]
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                color.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryUtils.getCategoryIcon(item.category),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = CategoryUtils.getCategoryDisplayName(item.category),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${categorySubs.size} / ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        softWrap = false,
                        maxLines = 1
                    )
                    Text(
                        text = CurrencyFormatter.format(item.totalAmount, "VND", locale),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        softWrap = false,
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }
            }
            if (isSelected) {
                categorySubs.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sub.name,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = CurrencyFormatter.format(sub.amount, sub.currency.code, locale),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            softWrap = false,
                            maxLines = 1,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
