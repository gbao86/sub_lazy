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

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.PaymentHistory
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.FinanceCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CashflowForecastingChart(subscriptions: List<Subscription>, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val forecasts = FinanceCalculator.getForecastingData(subscriptions, locale)
    val maxForecast = forecasts.maxByOrNull { it.amount }
    val maxVal = forecasts.maxOfOrNull { it.amount } ?: 0.0
    val displayMaxVal = if (maxVal == 0.0) 100000.0 else maxVal * 1.15

    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val highlightColor = MaterialTheme.colorScheme.error

    val pathProgress = remember { Animatable(0f) }
    val pointProgress = remember { Animatable(0f) }
    
    LaunchedEffect(forecasts) {
        launch {
            pathProgress.snapTo(0f)
            pathProgress.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))
        }
        launch {
            pointProgress.snapTo(0f)
            delay(500)
            pointProgress.animateTo(1f, tween(800, easing = EaseOutBack))
        }
    }

    Column(modifier = modifier.padding(24.dp)) {
        Text(
            text = stringResource(R.string.chart_forecasting_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (maxForecast != null && maxForecast.amount > 0.0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.chart_forecasting_peak_desc,
                    maxForecast.monthName,
                    CurrencyFormatter.format(maxForecast.amount, "VND", locale)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
        ) {
            val width = size.width
            val height = size.height
            val paddingLeft = 40f
            val paddingRight = 40f
            val paddingTop = 20f
            val paddingBottom = 40f
            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            for (i in 0..3) {
                val y = paddingTop + chartHeight * (i.toFloat() / 3)
                drawLine(
                    color = gridColor.copy(alpha = 0.3f),
                    start = Offset(paddingLeft, y),
                    end = Offset(paddingLeft + chartWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val points = forecasts.mapIndexed { index, forecast ->
                val x = paddingLeft + chartWidth * (index.toFloat() / (forecasts.size - 1))
                val y = if (displayMaxVal > 0.0) {
                    paddingTop + chartHeight * (1f - (forecast.amount / displayMaxVal).toFloat())
                } else {
                    paddingTop + chartHeight
                }
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                val fillPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, paddingTop + chartHeight)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, paddingTop + chartHeight)
                    close()
                }
                
                // Animate fill alpha
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.2f * pathProgress.value), Color.Transparent),
                        startY = points.minOf { it.y },
                        endY = paddingTop + chartHeight
                    )
                )
            }

            if (points.size > 1) {
                val strokePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cp1 = Offset(prev.x + (curr.x - prev.x) / 2f, prev.y)
                        val cp2 = Offset(prev.x + (curr.x - prev.x) / 2f, curr.y)
                        cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
                    }
                }
                
                // Use a measure to draw only partial path if needed, but for simplicity we'll just use pathProgress
                // For a more advanced path animation, one would use PathMeasure.
                // Here we'll just draw the stroke and animate alpha or use a simpler trick.
                // Actually, let's use pathProgress to clip the drawing area.
                
                clipRect(
                    right = paddingLeft + chartWidth * pathProgress.value
                ) {
                    drawPath(strokePath, color = primaryColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                }
            }

            val textPaint = Paint().apply {
                color = textColor.toArgb()
                textSize = 9.dp.toPx()
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            val amountPaint = Paint().apply {
                color = primaryColor.toArgb()
                textSize = 8.dp.toPx()
                textAlign = Paint.Align.CENTER
            }

            points.forEachIndexed { index, point ->
                val forecast = forecasts[index]
                val isMax = forecast == maxForecast && forecast.amount > 0.0
                
                // Animate point appearance
                val scale = if (index / (points.size - 1f) <= pathProgress.value) pointProgress.value else 0f

                if (scale > 0f) {
                    drawCircle(
                        color = if (isMax) highlightColor else primaryColor,
                        radius = (if (isMax) 5.dp.toPx() else 3.5f.dp.toPx()) * scale,
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = (if (isMax) 2.5f.dp.toPx() else 1.8f.dp.toPx()) * scale,
                        center = point
                    )
                    
                    // Month name
                    drawContext.canvas.nativeCanvas.drawText(
                        forecast.monthName, point.x, height - 6.dp.toPx(), textPaint.apply { alpha = (255 * scale).toInt() }
                    )
                    
                    if (forecast.amount > 0.0) {
                        val amountText = if (forecast.amount >= 1000000.0) {
                            String.format(locale, "%.1fM", forecast.amount / 1000000.0)
                        } else {
                            String.format(locale, "%.0fk", forecast.amount / 1000.0)
                        }
                        val paintToUse = if (isMax) {
                            Paint(amountPaint).apply {
                                color = highlightColor.toArgb()
                                typeface = Typeface.DEFAULT_BOLD
                            }
                        } else amountPaint
                        drawContext.canvas.nativeCanvas.drawText(amountText, point.x, point.y - 8.dp.toPx(), paintToUse.apply { alpha = (255 * scale).toInt() })
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentHistorySection(paymentHistory: List<PaymentHistory>, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val dateFormat = remember { java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", locale) }

    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.payment_history_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            if (paymentHistory.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        // Show actual count capped at 5
                        text = if (minOf(paymentHistory.size, 5) == 1) {
                            stringResource(R.string.dashboard_history_count_single)
                        } else {
                            stringResource(R.string.dashboard_history_count, minOf(paymentHistory.size, 5))
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (paymentHistory.isEmpty()) {
            Text(
                text = stringResource(R.string.payment_history_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                paymentHistory.take(5).forEach { record ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Payment history icon",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = record.subscriptionName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = dateFormat.format(java.time.Instant.ofEpochMilli(record.paymentDate).atZone(java.time.ZoneId.systemDefault())),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "-${CurrencyFormatter.format(record.amount, record.currency.code, locale)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }
    }
}
