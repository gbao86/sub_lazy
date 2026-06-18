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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import com.gbao86.sub_lazy.data.model.BillingCycle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.ui.BankruptcyRunwayResult
import com.gbao86.sub_lazy.ui.FinanceCalculator
import kotlin.math.cos
import kotlin.math.sin

enum class CatState {
    HAPPY,
    SLEEPING,
    PANICKED
}

@Composable
fun LazyWalletCatSection(
    catState: CatState,
    runwayResult: BankruptcyRunwayResult,
    subscriptions: List<Subscription>,
    userBalance: Double,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubble_float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val bubbleText = when (catState) {
            CatState.HAPPY -> {
                stringResource(R.string.cat_bubble_happy)
            }
            CatState.SLEEPING -> {
                stringResource(R.string.cat_bubble_sleeping)
            }
            CatState.PANICKED -> {
                when {
                    runwayResult is BankruptcyRunwayResult.AlreadyBankrupt -> {
                        stringResource(R.string.cat_bubble_bankrupt)
                    }
                    else -> {
                        val activeSubs = subscriptions.filter { it.cycle != BillingCycle.ONE_TIME && it.cycle != BillingCycle.YEARLY }
                        val totalMonthlyCost = activeSubs.sumOf { FinanceCalculator.calculateMonthlyEquivalentCostInVnd(it) }
                        if (totalMonthlyCost > userBalance) {
                            stringResource(R.string.cat_bubble_exceeded)
                        } else if (runwayResult is BankruptcyRunwayResult.DaysLeft && runwayResult.diffMillis < 86400000L * 7) {
                            val days = (runwayResult.diffMillis / 86400000L).coerceAtLeast(1)
                            stringResource(R.string.cat_bubble_depleted_in_days, days.toInt())
                        } else {
                            stringResource(R.string.cat_bubble_deficit_warning)
                        }
                    }
                }
            }
        }
        
        Column(
            modifier = Modifier
                .offset { androidx.compose.ui.unit.IntOffset(0, floatOffset.dp.roundToPx()) }
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (catState == CatState.PANICKED) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        1.dp, 
                        if (catState == CatState.PANICKED) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), 
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = bubbleText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (catState == CatState.PANICKED) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
            Box(
                modifier = Modifier
                    .size(16.dp, 8.dp)
                    .offset(y = (-1).dp)
                    .background(
                        color = if (catState == CatState.PANICKED) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        shape = TriangleEdgeShape()
                    )
            )
        }
        
        LazyWalletCat(
            catState = catState,
            modifier = Modifier.size(150.dp)
        )
    }
}

class TriangleEdgeShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width / 2f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}

@Composable
fun LazyWalletCat(
    catState: CatState,
    modifier: Modifier = Modifier
) {
    val sleepProgress = remember { Animatable(0f) }
    LaunchedEffect(catState) {
        if (catState == CatState.SLEEPING) {
            sleepProgress.snapTo(0f)
            sleepProgress.animateTo(1f, animationSpec = tween(3500, easing = EaseOutCubic))
        } else {
            sleepProgress.animateTo(0f, animationSpec = tween(300, easing = EaseInCubic))
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cat_anim")

    // Breathing anim (only active when sleeping)
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )
    val bodyScale = if (catState == CatState.SLEEPING) {
        1f + (breathingScale - 1f) * sleepProgress.value
    } else 1.0f

    // Shaking anim when panicked
    val shakeX by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(80, easing = LinearEasing), RepeatMode.Reverse),
        label = "shake_x"
    )
    val shakeY by infiniteTransition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(70, easing = LinearEasing), RepeatMode.Reverse),
        label = "shake_y"
    )

    // ZZZ letters animation
    val zzzAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 0, easing = LinearEasing), RepeatMode.Restart),
        label = "zzz1"
    )
    val zzzAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 600, easing = LinearEasing), RepeatMode.Restart),
        label = "zzz2"
    )
    val zzzAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, delayMillis = 1200, easing = LinearEasing), RepeatMode.Restart),
        label = "zzz3"
    )

    val catColor = Color(0xFFFFB74D) // Beautiful Warm Orange
    val shadowColor = Color(0x1A000000)

    Box(
        modifier = modifier
            .size(150.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f + 10f
            val scaleFactor = width.coerceAtMost(height) / 150f

            // 1. Draw soft shadow under the cat
            drawOval(
                color = shadowColor,
                topLeft = Offset(centerX - 60f * scaleFactor, centerY + 30f * scaleFactor),
                size = Size(120f * scaleFactor, 18f * scaleFactor)
            )

            if (catState == CatState.PANICKED || catState == CatState.HAPPY) {
                // Thức dậy (Panicked hoặc Happy)
                val translationX = if (catState == CatState.PANICKED) shakeX else 0f
                val translationY = if (catState == CatState.PANICKED) shakeY else 0f
                
                translate(translationX, translationY) {
                    val headCenter = Offset(centerX, centerY - 30f * scaleFactor)
                    val headRadius = 38f * scaleFactor
                    
                    // Tail standing up (panicked) or curved wagging (happy)
                    if (catState == CatState.PANICKED) {
                        val tailPath = Path().apply {
                            moveTo(centerX - 28f * scaleFactor, centerY + 30f * scaleFactor)
                            quadraticTo(
                                centerX - 52f * scaleFactor + shakeX, centerY + 10f * scaleFactor + shakeY,
                                centerX - 46f * scaleFactor, centerY - 35f * scaleFactor
                            )
                        }
                        drawPath(
                            path = tailPath,
                            color = catColor,
                            style = Stroke(width = 8f * scaleFactor, cap = StrokeCap.Round)
                        )
                        val tailTipPath = Path().apply {
                            moveTo(centerX - 48f * scaleFactor, centerY - 25f * scaleFactor)
                            quadraticTo(
                                centerX - 50f * scaleFactor + shakeX, centerY - 30f * scaleFactor + shakeY,
                                centerX - 46f * scaleFactor, centerY - 35f * scaleFactor
                            )
                        }
                        drawPath(
                            path = tailTipPath,
                            color = Color(0xFFFFF9E6),
                            style = Stroke(width = 8f * scaleFactor, cap = StrokeCap.Round)
                        )
                    } else {
                        // Happy tail waving slowly
                        val happyTailWag = sin(System.currentTimeMillis() / 200.0) * 8.0
                        val tailPath = Path().apply {
                            moveTo(centerX - 28f * scaleFactor, centerY + 30f * scaleFactor)
                            quadraticTo(
                                centerX - 48f * scaleFactor, centerY + 15f * scaleFactor + happyTailWag.toFloat(),
                                centerX - 52f * scaleFactor, centerY - 2f * scaleFactor + happyTailWag.toFloat()
                            )
                        }
                        drawPath(
                            path = tailPath,
                            color = catColor,
                            style = Stroke(width = 8f * scaleFactor, cap = StrokeCap.Round)
                        )
                    }

                    // Left & Right Ears
                    val leftEarPath = Path().apply {
                        moveTo(centerX - 25f * scaleFactor, centerY - 55f * scaleFactor)
                        lineTo(centerX - 42f * scaleFactor, centerY - 88f * scaleFactor)
                        lineTo(centerX - 8f * scaleFactor, centerY - 65f * scaleFactor)
                        close()
                    }
                    drawPath(leftEarPath, brush = Brush.linearGradient(listOf(Color(0xFFFFCC80), catColor)))
                    val leftEarInner = Path().apply {
                        moveTo(centerX - 23f * scaleFactor, centerY - 58f * scaleFactor)
                        lineTo(centerX - 36f * scaleFactor, centerY - 80f * scaleFactor)
                        lineTo(centerX - 12f * scaleFactor, centerY - 64f * scaleFactor)
                        close()
                    }
                    drawPath(leftEarInner, color = Color(0xFFFFB7B2))

                    val rightEarPath = Path().apply {
                        moveTo(centerX + 25f * scaleFactor, centerY - 55f * scaleFactor)
                        lineTo(centerX + 42f * scaleFactor, centerY - 88f * scaleFactor)
                        lineTo(centerX + 8f * scaleFactor, centerY - 65f * scaleFactor)
                        close()
                    }
                    drawPath(rightEarPath, brush = Brush.linearGradient(listOf(Color(0xFFFFCC80), catColor)))
                    val rightEarInner = Path().apply {
                        moveTo(centerX + 23f * scaleFactor, centerY - 58f * scaleFactor)
                        lineTo(centerX + 36f * scaleFactor, centerY - 80f * scaleFactor)
                        lineTo(centerX + 12f * scaleFactor, centerY - 64f * scaleFactor)
                        close()
                    }
                    drawPath(rightEarInner, color = Color(0xFFFFB7B2))

                    // Cat Body
                    drawOval(
                        color = catColor,
                        topLeft = Offset(centerX - 35f * scaleFactor, centerY - 10f * scaleFactor),
                        size = Size(70f * scaleFactor, 45f * scaleFactor)
                    )
                    // White chest
                    drawOval(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = Offset(centerX - 18f * scaleFactor, centerY - 5f * scaleFactor),
                        size = Size(36f * scaleFactor, 26f * scaleFactor)
                    )

                    // Head
                    drawCircle(catColor, radius = headRadius, center = headCenter)

                    // Eyes
                    val eyeY = centerY - 32f * scaleFactor
                    if (catState == CatState.PANICKED) {
                        // Wide panic eyes
                        drawCircle(Color.White, radius = 9f * scaleFactor, center = Offset(centerX - 14f * scaleFactor, eyeY))
                        drawCircle(Color.White, radius = 9f * scaleFactor, center = Offset(centerX + 14f * scaleFactor, eyeY))
                        drawCircle(Color.Black, radius = 4f * scaleFactor, center = Offset(centerX - 14f * scaleFactor + shakeX * 0.3f, eyeY + shakeY * 0.3f))
                        drawCircle(Color.Black, radius = 4f * scaleFactor, center = Offset(centerX + 14f * scaleFactor + shakeX * 0.3f, eyeY + shakeY * 0.3f))
                    } else {
                        // Happy closed curved eyes (^^)
                        val eyePathLeft = Path().apply {
                            moveTo(centerX - 20f * scaleFactor, eyeY + 2f * scaleFactor)
                            quadraticTo(centerX - 14f * scaleFactor, eyeY - 4f * scaleFactor, centerX - 8f * scaleFactor, eyeY + 2f * scaleFactor)
                        }
                        drawPath(eyePathLeft, Color.Black.copy(alpha = 0.6f), style = Stroke(width = 3f * scaleFactor, cap = StrokeCap.Round))

                        val eyePathRight = Path().apply {
                            moveTo(centerX + 8f * scaleFactor, eyeY + 2f * scaleFactor)
                            quadraticTo(centerX + 14f * scaleFactor, eyeY - 4f * scaleFactor, centerX + 20f * scaleFactor, eyeY + 2f * scaleFactor)
                        }
                        drawPath(eyePathRight, Color.Black.copy(alpha = 0.6f), style = Stroke(width = 3f * scaleFactor, cap = StrokeCap.Round))
                    }

                    // Cheek blush
                    drawCircle(Color(0xFFFF8E9E).copy(alpha = 0.6f), radius = 5f * scaleFactor, center = Offset(centerX - 20f * scaleFactor, eyeY + 8f * scaleFactor))
                    drawCircle(Color(0xFFFF8E9E).copy(alpha = 0.6f), radius = 5f * scaleFactor, center = Offset(centerX + 20f * scaleFactor, eyeY + 8f * scaleFactor))

                    // Nose
                    val noseCenter = Offset(centerX, centerY - 26f * scaleFactor)
                    val nosePath = Path().apply {
                        moveTo(noseCenter.x - 3f * scaleFactor, noseCenter.y - 1.5f * scaleFactor)
                        lineTo(noseCenter.x + 3f * scaleFactor, noseCenter.y - 1.5f * scaleFactor)
                        lineTo(noseCenter.x, noseCenter.y + 1.5f * scaleFactor)
                        close()
                    }
                    drawPath(nosePath, color = Color(0xFFFF85A1))

                    // Mouth
                    if (catState == CatState.PANICKED) {
                        // Small open mouth (O shape)
                        drawOval(
                            color = Color(0xFFE57373),
                            topLeft = Offset(centerX - 4f * scaleFactor, centerY - 21f * scaleFactor),
                            size = Size(8f * scaleFactor, 10f * scaleFactor)
                        )
                    } else {
                        // Happy cute cat mouth (w shape)
                        val mouthPath = Path().apply {
                            moveTo(centerX - 5f * scaleFactor, centerY - 22f * scaleFactor)
                            quadraticTo(centerX - 2.5f * scaleFactor, centerY - 19f * scaleFactor, centerX, centerY - 21f * scaleFactor)
                            quadraticTo(centerX + 2.5f * scaleFactor, centerY - 19f * scaleFactor, centerX + 5f * scaleFactor, centerY - 22f * scaleFactor)
                        }
                        drawPath(mouthPath, Color.Black.copy(alpha = 0.6f), style = Stroke(width = 2f * scaleFactor, cap = StrokeCap.Round))
                    }

                    // Whiskers
                    drawLine(Color.White.copy(alpha = 0.6f), Offset(centerX - 30f * scaleFactor, centerY - 23f * scaleFactor), Offset(centerX - 48f * scaleFactor, centerY - 21f * scaleFactor), strokeWidth = 2f)
                    drawLine(Color.White.copy(alpha = 0.6f), Offset(centerX - 30f * scaleFactor, centerY - 19f * scaleFactor), Offset(centerX - 46f * scaleFactor, centerY - 15f * scaleFactor), strokeWidth = 2f)
                    drawLine(Color.White.copy(alpha = 0.6f), Offset(centerX + 30f * scaleFactor, centerY - 23f * scaleFactor), Offset(centerX + 48f * scaleFactor, centerY - 21f * scaleFactor), strokeWidth = 2f)
                    drawLine(Color.White.copy(alpha = 0.6f), Offset(centerX + 30f * scaleFactor, centerY - 19f * scaleFactor), Offset(centerX + 46f * scaleFactor, centerY - 15f * scaleFactor), strokeWidth = 2f)

                    // Front Paws
                    drawCircle(catColor, radius = 7f * scaleFactor, center = Offset(centerX - 15f * scaleFactor, centerY + 30f * scaleFactor))
                    drawCircle(Color(0xFFFFF9E6), radius = 4f * scaleFactor, center = Offset(centerX - 15f * scaleFactor, centerY + 32f * scaleFactor))
                    drawCircle(catColor, radius = 7f * scaleFactor, center = Offset(centerX + 15f * scaleFactor, centerY + 30f * scaleFactor))
                    drawCircle(Color(0xFFFFF9E6), radius = 4f * scaleFactor, center = Offset(centerX + 15f * scaleFactor, centerY + 32f * scaleFactor))
                }
            } else {
                // Sleeping Cat
                // Apply subtle breathing scale to body components
                val progress = sleepProgress.value
                val sitProgress = 1f - progress // 1 when standing/sitting, 0 when curled sleeping

                val scaleX = bodyScale
                val scaleY = 1f / bodyScale

                scale(scaleX, scaleY, pivot = Offset(centerX, centerY + 28f * scaleFactor)) {
                    val headCenterX = lerp(centerX, centerX + 18f * scaleFactor, progress)
                    val headCenterY = lerp(centerY - 30f * scaleFactor, centerY - 4f * scaleFactor, progress)
                    val headRadius = lerp(38f * scaleFactor, 32f * scaleFactor, progress)

                    // Sleeping curled body
                    val bodyW = lerp(70f * scaleFactor, 102f * scaleFactor, progress)
                    val bodyH = lerp(45f * scaleFactor, 68f * scaleFactor, progress)
                    drawOval(
                        color = catColor,
                        topLeft = Offset(centerX - lerp(35f * scaleFactor, 56f * scaleFactor, progress), centerY - lerp(10f * scaleFactor, 34f * scaleFactor, progress)),
                        size = Size(bodyW, bodyH)
                    )

                    // Tail wrapping body
                    if (progress > 0.5f) {
                        val tailAlpha = (progress - 0.5f) / 0.5f
                        val tailPath = Path().apply {
                            moveTo(centerX - 46f * scaleFactor, centerY + 22f * scaleFactor)
                            quadraticTo(
                                centerX - 2f * scaleFactor, centerY + 40f * scaleFactor,
                                centerX + 46f * scaleFactor, centerY + 16f * scaleFactor
                            )
                        }
                        drawPath(
                            path = tailPath,
                            color = catColor.copy(alpha = tailAlpha),
                            style = Stroke(width = 10f * scaleFactor, cap = StrokeCap.Round)
                        )
                    }

                    // Ears (rotating and flattening down as sleep approaches)
                    val leftEarPath = Path().apply {
                        val earBaseX = headCenterX - 18f * scaleFactor
                        val earBaseY = headCenterY - 22f * scaleFactor
                        moveTo(earBaseX, earBaseY)
                        lineTo(
                            lerp(headCenterX - 42f * scaleFactor, headCenterX - 45f * scaleFactor, progress),
                            lerp(centerY - 88f * scaleFactor, headCenterY - 38f * scaleFactor, progress)
                        )
                        lineTo(headCenterX - 5f * scaleFactor, headCenterY - 26f * scaleFactor)
                        close()
                    }
                    drawPath(leftEarPath, brush = Brush.linearGradient(listOf(Color(0xFFFFCC80), catColor)))

                    val rightEarPath = Path().apply {
                        val earBaseX = headCenterX + 18f * scaleFactor
                        val earBaseY = headCenterY - 22f * scaleFactor
                        moveTo(earBaseX, earBaseY)
                        lineTo(
                            lerp(headCenterX + 42f * scaleFactor, headCenterX + 44f * scaleFactor, progress),
                            lerp(centerY - 88f * scaleFactor, headCenterY - 36f * scaleFactor, progress)
                        )
                        lineTo(headCenterX + 5f * scaleFactor, headCenterY - 26f * scaleFactor)
                        close()
                    }
                    drawPath(rightEarPath, brush = Brush.linearGradient(listOf(Color(0xFFFFCC80), catColor)))

                    // Head
                    drawCircle(catColor, radius = headRadius, center = Offset(headCenterX, headCenterY))

                    // Eyes
                    val eyeY = headCenterY - lerp(2f * scaleFactor, 0f, progress)
                    if (sitProgress > 0.5f) {
                        val standAlpha = (sitProgress - 0.5f) / 0.5f
                        val eyePathLeft = Path().apply {
                            moveTo(headCenterX - 20f * scaleFactor, eyeY + 2f * scaleFactor)
                            quadraticTo(headCenterX - 14f * scaleFactor, eyeY - 4f * scaleFactor, headCenterX - 8f * scaleFactor, eyeY + 2f * scaleFactor)
                        }
                        drawPath(eyePathLeft, Color.Black.copy(alpha = 0.6f * standAlpha), style = Stroke(width = 3f * scaleFactor, cap = StrokeCap.Round))

                        val eyePathRight = Path().apply {
                            moveTo(headCenterX + 8f * scaleFactor, eyeY + 2f * scaleFactor)
                            quadraticTo(headCenterX + 14f * scaleFactor, eyeY - 4f * scaleFactor, headCenterX + 20f * scaleFactor, eyeY + 2f * scaleFactor)
                        }
                        drawPath(eyePathRight, Color.Black.copy(alpha = 0.6f * standAlpha), style = Stroke(width = 3f * scaleFactor, cap = StrokeCap.Round))
                    } else {
                        // Curled closed eyes (sleeping loops)
                        val sleepAlpha = (0.5f - sitProgress) / 0.5f
                        val eyePathLeft = Path().apply {
                            moveTo(headCenterX - 15f * scaleFactor, headCenterY - 2f * scaleFactor)
                            quadraticTo(headCenterX - 10f * scaleFactor, headCenterY + 2f * scaleFactor, headCenterX - 5f * scaleFactor, headCenterY - 2f * scaleFactor)
                        }
                        drawPath(
                            path = eyePathLeft,
                            color = Color.Black.copy(alpha = 0.35f * sleepAlpha),
                            style = Stroke(width = 2.8f * scaleFactor, cap = StrokeCap.Round)
                        )
                        val eyePathRight = Path().apply {
                            moveTo(headCenterX + 5f * scaleFactor, headCenterY - 2f * scaleFactor)
                            quadraticTo(headCenterX + 10f * scaleFactor, headCenterY + 2f * scaleFactor, headCenterX + 15f * scaleFactor, headCenterY - 2f * scaleFactor)
                        }
                        drawPath(
                            path = eyePathRight,
                            color = Color.Black.copy(alpha = 0.35f * sleepAlpha),
                            style = Stroke(width = 2.8f * scaleFactor, cap = StrokeCap.Round)
                        )
                    }

                    // Cheek blush
                    val blushAlpha = lerp(0.5f, 0.4f, sitProgress)
                    drawCircle(Color(0xFFFF8E9E).copy(alpha = blushAlpha), radius = lerp(4f * scaleFactor, 5f * scaleFactor, sitProgress), center = Offset(headCenterX + lerp(2f * scaleFactor, 16f * scaleFactor, sitProgress), headCenterY + lerp(6f * scaleFactor, 2f * scaleFactor, sitProgress)))
                    if (sitProgress > 0.5f) {
                        drawCircle(Color(0xFFFF8E9E).copy(alpha = blushAlpha * (sitProgress - 0.5f) / 0.5f), radius = 5f * scaleFactor, center = Offset(headCenterX - 16f * scaleFactor, headCenterY + 2f * scaleFactor))
                    }

                    // Nose
                    val noseC = Offset(headCenterX, headCenterY - 2f * scaleFactor)
                    val nosePath = Path().apply {
                        moveTo(noseC.x - 2f * scaleFactor, noseC.y - 1f * scaleFactor)
                        lineTo(noseC.x + 2f * scaleFactor, noseC.y - 1f * scaleFactor)
                        lineTo(noseC.x, noseC.y + 1f * scaleFactor)
                        close()
                    }
                    drawPath(nosePath, color = Color(0xFFFF85A1))

                    // Sleeping mouth
                    if (sitProgress > 0.5f) {
                        val mouthAlpha = (sitProgress - 0.5f) / 0.5f
                        val mouthPath = Path().apply {
                            moveTo(headCenterX - 3.5f * scaleFactor, headCenterY + 1f * scaleFactor)
                            quadraticTo(headCenterX - 1.5f * scaleFactor, headCenterY + 3.5f * scaleFactor, headCenterX, headCenterY + 1f * scaleFactor)
                            quadraticTo(headCenterX + 1.5f * scaleFactor, headCenterY + 3.5f * scaleFactor, headCenterX + 3.5f * scaleFactor, headCenterY + 1f * scaleFactor)
                        }
                        drawPath(
                            path = mouthPath,
                            color = Color.Black.copy(alpha = 0.3f * mouthAlpha),
                            style = Stroke(width = 1.8f * scaleFactor, cap = StrokeCap.Round)
                        )
                    }

                    // Whiskers
                    val whiskerAlpha = lerp(0.3f, 0.5f, sitProgress)
                    drawLine(Color.White.copy(alpha = whiskerAlpha), Offset(headCenterX - 26f * scaleFactor, headCenterY - 1f * scaleFactor), Offset(headCenterX - 44f * scaleFactor, headCenterY + 1f * scaleFactor), strokeWidth = 2f)
                    drawLine(Color.White.copy(alpha = whiskerAlpha), Offset(headCenterX - 26f * scaleFactor, headCenterY + 3f * scaleFactor), Offset(headCenterX - 42f * scaleFactor, headCenterY + 7f * scaleFactor), strokeWidth = 2f)
                    drawLine(Color.White.copy(alpha = whiskerAlpha), Offset(headCenterX + 26f * scaleFactor, headCenterY - 1f * scaleFactor), Offset(headCenterX + 44f * scaleFactor, headCenterY + 1f * scaleFactor), strokeWidth = 2f)
                    drawLine(Color.White.copy(alpha = whiskerAlpha), Offset(headCenterX + 26f * scaleFactor, headCenterY + 3f * scaleFactor), Offset(headCenterX + 42f * scaleFactor, headCenterY + 7f * scaleFactor), strokeWidth = 2f)
                }

                // ZZZ sleep bubble letters
                if (progress > 0.8f) {
                    val fadeZ = (progress - 0.8f) / 0.2f
                    if (zzzAlpha1 > 0f) {
                        drawZ(centerX + 36f * scaleFactor + zzzAlpha1 * 20f * scaleFactor, centerY - 32f * scaleFactor + zzzAlpha1 * -40f * scaleFactor, size = 12f * scaleFactor, alpha = (1f - zzzAlpha1) * fadeZ)
                    }
                    if (zzzAlpha2 > 0f) {
                        drawZ(centerX + 36f * scaleFactor + zzzAlpha2 * 14f * scaleFactor, centerY - 32f * scaleFactor + zzzAlpha2 * -50f * scaleFactor, size = 9f * scaleFactor, alpha = (1f - zzzAlpha2) * fadeZ)
                    }
                    if (zzzAlpha3 > 0f) {
                        drawZ(centerX + 36f * scaleFactor + zzzAlpha3 * 18f * scaleFactor, centerY - 32f * scaleFactor + zzzAlpha3 * -45f * scaleFactor, size = 7f * scaleFactor, alpha = (1f - zzzAlpha3) * fadeZ)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawZ(
    x: Float, y: Float, size: Float, alpha: Float
) {
    val path = Path().apply {
        moveTo(x, y)
        lineTo(x + size, y)
        lineTo(x, y + size)
        lineTo(x + size, y + size)
    }
    drawPath(
        path = path,
        color = Color(0xFF818CF8).copy(alpha = alpha),
        style = Stroke(
            width = size / 4f,
            cap = StrokeCap.Round
        )
    )
}
