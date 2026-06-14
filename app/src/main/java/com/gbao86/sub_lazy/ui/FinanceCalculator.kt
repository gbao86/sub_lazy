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
import java.util.Calendar
import java.util.Locale

sealed class BankruptcyRunwayResult {
    object Infinite : BankruptcyRunwayResult()
    object AlreadyBankrupt : BankruptcyRunwayResult()
    object Safe : BankruptcyRunwayResult()
    data class DaysLeft(val diffMillis: Long, val targetTime: Long) : BankruptcyRunwayResult()
}

data class MonthlyForecast(val monthName: String, val amount: Double)

object FinanceCalculator {

    /**
     * Calculates the monthly equivalent cost of a subscription, converted to VND.
     */
    fun calculateMonthlyEquivalentCostInVnd(sub: Subscription): Double {
        val costInVnd = CurrencyFormatter.convert(sub.amount, sub.currency.code, "VND")
        return costInVnd * sub.cycle.monthlyMultiplier
    }

    /**
     * Calculates the end of the current budget period based on the reset day.
     */
    fun getBudgetPeriodEnd(resetDay: Int): Long {
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_MONTH)
        
        val endCal = Calendar.getInstance()
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        endCal.set(Calendar.MILLISECOND, 999)

        val startMonthMax = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val actualStartDay = if (resetDay > startMonthMax) startMonthMax else resetDay

        if (today >= actualStartDay) {
            endCal.add(Calendar.MONTH, 1)
            val endMonthMax = endCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val actualEndDay = if (resetDay > endMonthMax) endMonthMax else resetDay
            endCal.set(Calendar.DAY_OF_MONTH, actualEndDay)
            endCal.add(Calendar.DAY_OF_MONTH, -1)
        } else {
            val actualEndDay = if (resetDay > startMonthMax) startMonthMax else resetDay
            endCal.set(Calendar.DAY_OF_MONTH, actualEndDay)
            endCal.add(Calendar.DAY_OF_MONTH, -1)
        }
        return endCal.timeInMillis
    }

    /**
     * Calculates when the user balance runs down based on subscriptions.
     * Restricted to the current monthly budget period to model budget exhaustion.
     * Yearly and One-time subscriptions are excluded from this calculation.
     */
    fun calculateBankruptcyRunway(
        balance: Double,
        subscriptions: List<Subscription>,
        resetDay: Int
    ): BankruptcyRunwayResult {
        if (balance <= 0) return BankruptcyRunwayResult.AlreadyBankrupt

        val now = System.currentTimeMillis()
        val activeSubs = subscriptions.filter {
            it.cycle != BillingCycle.ONE_TIME && it.cycle != BillingCycle.YEARLY
        }
        if (activeSubs.isEmpty()) return BankruptcyRunwayResult.Infinite

        data class RenewalInstance(val sub: Subscription, val time: Long)

        val list = mutableListOf<RenewalInstance>()
        
        // Limit simulation to the end of the current budget period
        val horizon = getBudgetPeriodEnd(resetDay)

        activeSubs.forEach { sub ->
            var nextDate = sub.nextBillingDate
            var remainingTimes = sub.remainingTimes

            if (nextDate <= horizon) {
                list.add(RenewalInstance(sub, nextDate))
            }

            if (remainingTimes != null) {
                remainingTimes -= 1
                if (remainingTimes <= 0) return@forEach
            }

            if (nextDate < now) {
                while (nextDate < now) {
                    val projected = DateUtils.getNextBillingDate(nextDate, sub.cycle)
                    if (projected <= nextDate) break
                    nextDate = projected
                    if (nextDate <= horizon) {
                        list.add(RenewalInstance(sub, nextDate))
                    }

                    if (remainingTimes != null) {
                        remainingTimes -= 1
                        if (remainingTimes <= 0) return@forEach
                    }
                }
            }

            while (nextDate <= horizon) {
                val projected = DateUtils.getNextBillingDate(nextDate, sub.cycle)
                if (projected <= nextDate) break
                nextDate = projected
                if (nextDate <= horizon) {
                    list.add(RenewalInstance(sub, nextDate))
                } else {
                    break
                }

                if (remainingTimes != null) {
                    remainingTimes -= 1
                    if (remainingTimes <= 0) break
                }
            }
        }

        list.sortBy { it.time }

        var currentBalance = balance
        var bankruptTime: Long? = null

        for (instance in list) {
            val costInVnd = CurrencyFormatter.convert(instance.sub.amount, instance.sub.currency.code, "VND")
            currentBalance -= costInVnd
            if (currentBalance < 0) {
                bankruptTime = instance.time
                break
            }
        }

        if (bankruptTime == null) return BankruptcyRunwayResult.Safe
        if (bankruptTime <= now) return BankruptcyRunwayResult.AlreadyBankrupt

        val diff = bankruptTime - now
        return BankruptcyRunwayResult.DaysLeft(diff, bankruptTime)
    }

    /**
     * Project spending over the next 6 months.
     * Yearly and One-time subscriptions are excluded from this calculation.
     */
    fun getForecastingData(
        subscriptions: List<Subscription>,
        locale: Locale
    ): List<MonthlyForecast> {
        val forecasts = mutableListOf<MonthlyForecast>()

        val months = (0 until 6).map { offset ->
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, offset) }
            val monthNum = cal.get(Calendar.MONTH) + 1
            val monthLabel = if (locale.language == "vi") "T$monthNum" else {
                java.time.format.DateTimeFormatter.ofPattern("MMM", locale)
                    .format(java.time.Instant.ofEpochMilli(cal.timeInMillis).atZone(java.time.ZoneId.systemDefault()))
            }
            val startCal = Calendar.getInstance().apply {
                timeInMillis = cal.timeInMillis
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                timeInMillis = startCal.timeInMillis
                add(Calendar.MONTH, 1)
                add(Calendar.MILLISECOND, -1)
            }
            Triple(monthLabel, startCal.timeInMillis, endCal.timeInMillis)
        }

        val monthlyAmounts = DoubleArray(6)
        val maxSimTime = months[5].third

        val filteredSubs = subscriptions.filter { it.cycle != BillingCycle.ONE_TIME && it.cycle != BillingCycle.YEARLY }

        filteredSubs.forEach { sub ->
            var currentBillingDate = sub.nextBillingDate
            var limit = sub.remainingTimes

            while (currentBillingDate <= maxSimTime) {
                for (i in 0 until 6) {
                    val (_, start, end) = months[i]
                    if (currentBillingDate in start..end) {
                        monthlyAmounts[i] += CurrencyFormatter.convert(sub.amount, sub.currency.code, "VND")
                    }
                }

                if (limit != null) {
                    limit -= 1
                    if (limit <= 0) break
                }

                val nextDate = DateUtils.getNextBillingDate(currentBillingDate, sub.cycle)
                if (nextDate <= currentBillingDate) break
                currentBillingDate = nextDate
            }
        }

        for (i in 0 until 6) {
            forecasts.add(MonthlyForecast(months[i].first, monthlyAmounts[i]))
        }
        return forecasts
    }
}
