/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational, 
and non-commercial purposes. 
Commercial exploitation, sale, or distribution of this software or any derivative works 
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy.ui

import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class FinanceCalculatorTest {

    private fun createSubscription(
        amount: Double = 100000.0,
        cycle: BillingCycle = BillingCycle.MONTHLY,
        currency: SubscriptionCurrency = SubscriptionCurrency.VND,
        nextBillingDate: Long = System.currentTimeMillis() + 86400000L * 7, // 7 days from now
        remainingTimes: Int? = null,
        isShared: Boolean = false
    ): Subscription = Subscription(
        id = 1L,
        name = "Test Sub",
        amount = amount,
        nextBillingDate = nextBillingDate,
        cycle = cycle,
        category = SubscriptionCategory.ENTERTAINMENT,
        colorHex = "#6366F1",
        currency = currency,
        remainingTimes = remainingTimes,
        isShared = isShared
    )

    @Test
    fun `monthly subscription returns same amount as monthly equivalent`() {
        val sub = createSubscription(amount = 100000.0, cycle = BillingCycle.MONTHLY)
        val result = FinanceCalculator.calculateMonthlyEquivalentCostInVnd(sub)
        assertEquals(100000.0, result, 0.01)
    }

    @Test
    fun `yearly subscription divides by 12 for monthly equivalent`() {
        val sub = createSubscription(amount = 1200000.0, cycle = BillingCycle.YEARLY)
        val result = FinanceCalculator.calculateMonthlyEquivalentCostInVnd(sub)
        assertEquals(100000.0, result, 0.01)
    }

    @Test
    fun `weekly subscription multiplies by 52 div 12 for monthly equivalent`() {
        val sub = createSubscription(amount = 100000.0, cycle = BillingCycle.WEEKLY)
        val result = FinanceCalculator.calculateMonthlyEquivalentCostInVnd(sub)
        val expected = 100000.0 * 52.0 / 12.0
        assertEquals(expected, result, 0.01)
    }

    @Test
    fun `daily subscription multiplies by 365_25 div 12 for monthly equivalent`() {
        val sub = createSubscription(amount = 10000.0, cycle = BillingCycle.DAILY)
        val result = FinanceCalculator.calculateMonthlyEquivalentCostInVnd(sub)
        val expected = 10000.0 * 365.25 / 12.0
        assertEquals(expected, result, 0.01)
    }

    @Test
    fun `every 3 months subscription divides by 3 for monthly equivalent`() {
        val sub = createSubscription(amount = 300000.0, cycle = BillingCycle.EVERY_3_MONTHS)
        val result = FinanceCalculator.calculateMonthlyEquivalentCostInVnd(sub)
        assertEquals(100000.0, result, 0.01)
    }

    @Test
    fun `every 6 months subscription divides by 6 for monthly equivalent`() {
        val sub = createSubscription(amount = 600000.0, cycle = BillingCycle.EVERY_6_MONTHS)
        val result = FinanceCalculator.calculateMonthlyEquivalentCostInVnd(sub)
        assertEquals(100000.0, result, 0.01)
    }

    @Test
    fun `one-time subscription has zero monthly equivalent`() {
        val sub = createSubscription(amount = 500000.0, cycle = BillingCycle.ONE_TIME)
        val result = FinanceCalculator.calculateMonthlyEquivalentCostInVnd(sub)
        assertEquals(0.0, result, 0.01)
    }

    // --- Bankruptcy Runway Tests ---

    @Test
    fun `already bankrupt when balance is zero`() {
        val result = FinanceCalculator.calculateBankruptcyRunway(
            balance = 0.0,
            subscriptions = listOf(createSubscription()),
            resetDay = 1
        )
        assertTrue(result is BankruptcyRunwayResult.AlreadyBankrupt)
    }

    @Test
    fun `already bankrupt when balance is negative`() {
        val result = FinanceCalculator.calculateBankruptcyRunway(
            balance = -1000.0,
            subscriptions = listOf(createSubscription()),
            resetDay = 1
        )
        assertTrue(result is BankruptcyRunwayResult.AlreadyBankrupt)
    }

    @Test
    fun `infinite runway when no active subscriptions`() {
        val result = FinanceCalculator.calculateBankruptcyRunway(
            balance = 1000000.0,
            subscriptions = emptyList(),
            resetDay = 1
        )
        assertTrue(result is BankruptcyRunwayResult.Infinite)
    }

    @Test
    fun `infinite runway when only one-time subscriptions`() {
        val sub = createSubscription(cycle = BillingCycle.ONE_TIME)
        val result = FinanceCalculator.calculateBankruptcyRunway(
            balance = 1000000.0,
            subscriptions = listOf(sub),
            resetDay = 1
        )
        assertTrue(result is BankruptcyRunwayResult.Infinite)
    }

    @Test
    fun `infinite runway when only yearly subscriptions`() {
        val sub = createSubscription(cycle = BillingCycle.YEARLY)
        val result = FinanceCalculator.calculateBankruptcyRunway(
            balance = 1000000.0,
            subscriptions = listOf(sub),
            resetDay = 1
        )
        assertTrue(result is BankruptcyRunwayResult.Infinite)
    }

    // --- Budget Period End Tests ---

    @Test
    fun `budget period end is in the future`() {
        val resetDay = 1
        val periodEnd = FinanceCalculator.getBudgetPeriodEnd(resetDay)
        assertTrue(periodEnd > System.currentTimeMillis() - 86400000L) // At least near today
    }

    // --- Forecasting Tests ---

    @Test
    fun `forecasting returns 6 months of data`() {
        val sub = createSubscription()
        val result = FinanceCalculator.getForecastingData(
            subscriptions = listOf(sub),
            locale = Locale("vi")
        )
        assertEquals(6, result.size)
    }

    @Test
    fun `forecasting excludes one-time subscriptions`() {
        val sub = createSubscription(cycle = BillingCycle.ONE_TIME)
        val result = FinanceCalculator.getForecastingData(
            subscriptions = listOf(sub),
            locale = Locale.US
        )
        assertTrue(result.all { it.amount == 0.0 })
    }

    @Test
    fun `forecasting excludes yearly subscriptions`() {
        val sub = createSubscription(cycle = BillingCycle.YEARLY)
        val result = FinanceCalculator.getForecastingData(
            subscriptions = listOf(sub),
            locale = Locale.US
        )
        assertTrue(result.all { it.amount == 0.0 })
    }

    @Test
    fun `forecasting uses Vietnamese month labels for vi locale`() {
        val sub = createSubscription()
        val result = FinanceCalculator.getForecastingData(
            subscriptions = listOf(sub),
            locale = Locale("vi")
        )
        assertTrue(result[0].monthName.startsWith("T"))
    }

    @Test
    fun `forecasting respects remainingTimes limit`() {
        val sub = createSubscription(
            amount = 100000.0,
            cycle = BillingCycle.MONTHLY,
            remainingTimes = 1,
            nextBillingDate = System.currentTimeMillis() + 86400000L
        )
        val result = FinanceCalculator.getForecastingData(
            subscriptions = listOf(sub),
            locale = Locale.US
        )
        // Only 1 payment should appear across all 6 months
        val totalPayments = result.count { it.amount > 0 }
        assertEquals(1, totalPayments)
    }
}
