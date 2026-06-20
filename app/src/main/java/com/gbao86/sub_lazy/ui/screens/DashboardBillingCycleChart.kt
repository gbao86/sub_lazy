package com.gbao86.sub_lazy.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun BillingCycleChart(subscriptions: List<Subscription>, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val daysInYear = if (java.time.Year.isLeap(currentYear.toLong())) 366.0 else 365.0
    val dailyMultiplier = daysInYear / 12.0

    val weeklyCost = subscriptions.filter { it.cycle == BillingCycle.WEEKLY }
        .sumOf { CurrencyFormatter.convert(it.amount, it.currency.code, "VND") } * 52.0 / 12.0 +
            subscriptions.filter { it.cycle == BillingCycle.DAILY }
                .sumOf { CurrencyFormatter.convert(it.amount, it.currency.code, "VND") } * dailyMultiplier

    val monthlyCost = subscriptions.filter { it.cycle == BillingCycle.MONTHLY }
        .sumOf { CurrencyFormatter.convert(it.amount, it.currency.code, "VND") } +
            subscriptions.filter { it.cycle == BillingCycle.EVERY_3_MONTHS }
                .sumOf { CurrencyFormatter.convert(it.amount, it.currency.code, "VND") } / 3.0 +
            subscriptions.filter { it.cycle == BillingCycle.EVERY_6_MONTHS }
                .sumOf { CurrencyFormatter.convert(it.amount, it.currency.code, "VND") } / 6.0

    val yearlyCost = subscriptions.filter { it.cycle == BillingCycle.YEARLY }
        .sumOf { CurrencyFormatter.convert(it.amount, it.currency.code, "VND") } / 12.0

    val total = weeklyCost + monthlyCost + yearlyCost

    val weeklyAnim = remember { Animatable(0f) }
    val monthlyAnim = remember { Animatable(0f) }
    val yearlyAnim = remember { Animatable(0f) }

    LaunchedEffect(weeklyCost, monthlyCost, yearlyCost) {
        val weeklyTarget = if (total > 0.0) (weeklyCost / total).toFloat() else 0f
        val monthlyTarget = if (total > 0.0) (monthlyCost / total).toFloat() else 0f
        val yearlyTarget = if (total > 0.0) (yearlyCost / total).toFloat() else 0f

        launch { weeklyAnim.animateTo(weeklyTarget, tween(900, easing = FastOutSlowInEasing)) }
        launch { monthlyAnim.animateTo(monthlyTarget, tween(900, easing = FastOutSlowInEasing)) }
        launch { yearlyAnim.animateTo(yearlyTarget, tween(900, easing = FastOutSlowInEasing)) }
    }

    Column(modifier = modifier.padding(24.dp)) {
        Text(
            stringResource(R.string.chart_billing_cycle_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    CurrencyFormatter.format(weeklyCost, "VND", locale),
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = false, maxLines = 1,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height((120 * weeklyAnim.value).dp.coerceAtLeast(6.dp))
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.cycle_weekly), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    CurrencyFormatter.format(monthlyCost, "VND", locale),
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = false, maxLines = 1,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height((120 * monthlyAnim.value).dp.coerceAtLeast(6.dp))
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.cycle_monthly), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    CurrencyFormatter.format(yearlyCost, "VND", locale),
                    style = MaterialTheme.typography.labelSmall,
                    softWrap = false, maxLines = 1,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height((120 * yearlyAnim.value).dp.coerceAtLeast(6.dp))
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(stringResource(R.string.cycle_yearly), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}
