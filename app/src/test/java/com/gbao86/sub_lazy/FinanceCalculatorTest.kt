/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational, 
and non-commercial purposes. 
Commercial exploitation, sale, or distribution of this software or any derivative works 
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy

import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import com.gbao86.sub_lazy.ui.BankruptcyRunwayResult
import com.gbao86.sub_lazy.ui.FinanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class FinanceCalculatorTest {

    @Test
    fun testCalculateMonthlyEquivalentCostInVnd() {
        // VND calculations (no exchange conversion needed)
        val vndMonthlySub = Subscription(
            id = 1,
            name = "VND Monthly",
            amount = 120000.0,
            nextBillingDate = System.currentTimeMillis(),
            cycle = BillingCycle.MONTHLY,
            category = SubscriptionCategory.ENTERTAINMENT,
            currency = SubscriptionCurrency.VND
        )
        assertEquals(120000.0, FinanceCalculator.calculateMonthlyEquivalentCostInVnd(vndMonthlySub), 0.01)

        val vndYearlySub = vndMonthlySub.copy(cycle = BillingCycle.YEARLY, amount = 1200000.0)
        assertEquals(100000.0, FinanceCalculator.calculateMonthlyEquivalentCostInVnd(vndYearlySub), 0.01)

        val vndWeeklySub = vndMonthlySub.copy(cycle = BillingCycle.WEEKLY, amount = 30000.0)
        val expectedWeekly = 30000.0 * (52.0 / 12.0)
        assertEquals(expectedWeekly, FinanceCalculator.calculateMonthlyEquivalentCostInVnd(vndWeeklySub), 0.01)

        val vndDailySub = vndMonthlySub.copy(cycle = BillingCycle.DAILY, amount = 5000.0)
        val expectedDaily = 5000.0 * (365.25 / 12.0)
        assertEquals(expectedDaily, FinanceCalculator.calculateMonthlyEquivalentCostInVnd(vndDailySub), 0.01)

        val vndOneTimeSub = vndMonthlySub.copy(cycle = BillingCycle.ONE_TIME)
        assertEquals(0.0, FinanceCalculator.calculateMonthlyEquivalentCostInVnd(vndOneTimeSub), 0.01)

        // USD calculations (requires default exchange rates: 1 USD = 25400 VND)
        val usdMonthlySub = Subscription(
            id = 2,
            name = "USD Monthly",
            amount = 10.0,
            nextBillingDate = System.currentTimeMillis(),
            cycle = BillingCycle.MONTHLY,
            category = SubscriptionCategory.WORK,
            currency = SubscriptionCurrency.USD
        )
        // 10 USD * 25400 VND/USD = 254000 VND
        assertEquals(254000.0, FinanceCalculator.calculateMonthlyEquivalentCostInVnd(usdMonthlySub), 0.01)
    }

    @Test
    fun testGetBudgetPeriodEnd() {
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_MONTH)
        val resetDay = 15

        val endMillis = FinanceCalculator.getBudgetPeriodEnd(resetDay)
        val endCal = Calendar.getInstance().apply { timeInMillis = endMillis }

        // Period end calendar is set to 23:59:59.999 of the day before the budget reset
        assertEquals(23, endCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, endCal.get(Calendar.MINUTE))

        val expectedEndDay = if (today >= resetDay) {
            // budget reset has already happened or is today -> next reset is next month day 15.
            // so period end is next month day 14.
            14
        } else {
            // budget reset hasn't happened yet -> next reset is this month day 15.
            // so period end is this month day 14.
            14
        }
        assertEquals(expectedEndDay, endCal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testCalculateBankruptcyRunway() {
        // 1. Balance <= 0 -> Already Bankrupt
        val resultAlreadyBankrupt = FinanceCalculator.calculateBankruptcyRunway(0.0, emptyList(), 1)
        assertTrue(resultAlreadyBankrupt is BankruptcyRunwayResult.AlreadyBankrupt)

        // 2. Empty subscription list -> Infinite runway
        val resultEmpty = FinanceCalculator.calculateBankruptcyRunway(100000.0, emptyList(), 15)
        assertTrue(resultEmpty is BankruptcyRunwayResult.Infinite)

        // 3. Subscriptions cost less than balance -> Safe
        val sub1 = Subscription(
            id = 1,
            name = "Sub 1",
            amount = 10000.0,
            nextBillingDate = System.currentTimeMillis() + 100000000L,
            cycle = BillingCycle.MONTHLY,
            category = SubscriptionCategory.ENTERTAINMENT,
            currency = SubscriptionCurrency.VND
        )
        val resultSafe = FinanceCalculator.calculateBankruptcyRunway(200000.0, listOf(sub1), 15)
        assertTrue(resultSafe is BankruptcyRunwayResult.Safe)

        // 4. Subscriptions cost more than balance -> DaysLeft
        // Budget ends in about 20-30 days depending on current date.
        // Let's set next billing date to tomorrow (now + 24h) and make amount exceed balance.
        val tomorrow = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
        val expensiveSub = Subscription(
            id = 2,
            name = "Expensive Sub",
            amount = 150000.0,
            nextBillingDate = tomorrow,
            cycle = BillingCycle.MONTHLY,
            category = SubscriptionCategory.ENTERTAINMENT,
            currency = SubscriptionCurrency.VND
        )
        val resultRunway = FinanceCalculator.calculateBankruptcyRunway(100000.0, listOf(expensiveSub), 28)
        assertTrue(resultRunway is BankruptcyRunwayResult.DaysLeft)
        val daysLeft = resultRunway as BankruptcyRunwayResult.DaysLeft
        assertEquals(tomorrow, daysLeft.targetTime)
    }

    @Test
    fun testGetForecastingData() {
        val now = System.currentTimeMillis()
        val sub = Subscription(
            id = 1,
            name = "Monthly Netflix",
            amount = 10000.0,
            nextBillingDate = now,
            cycle = BillingCycle.MONTHLY,
            category = SubscriptionCategory.ENTERTAINMENT,
            currency = SubscriptionCurrency.VND
        )
        val forecast = FinanceCalculator.getForecastingData(listOf(sub), Locale.US)
        assertEquals(6, forecast.size)
        // First month should contain the charge since nextBillingDate = now
        assertTrue(forecast[0].amount >= 10000.0)
    }
}
