/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational,
and non-commercial purposes.
Commercial exploitation, sale, or distribution of this software or any derivative works
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gbao86.sub_lazy.data.AppDatabase
import com.gbao86.sub_lazy.data.CategorySpending
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.SubscriptionRepository
import com.gbao86.sub_lazy.data.PaymentHistory
import com.gbao86.sub_lazy.worker.NotificationScheduler
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.time.Year
import android.content.Context
import android.content.SharedPreferences

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SubscriptionRepository
    private val notificationScheduler = NotificationScheduler(application)
    
    private val sharedPref = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val _userBalance = MutableStateFlow(sharedPref.getFloat("user_balance", 2000000f).toDouble())
    val userBalance: StateFlow<Double> = _userBalance.asStateFlow()

    private val _budgetResetDay = MutableStateFlow(sharedPref.getInt("budget_reset_day", 1))
    val budgetResetDay: StateFlow<Int> = _budgetResetDay.asStateFlow()

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "user_balance" -> {
                _userBalance.value = sharedPref.getFloat("user_balance", 2000000f).toDouble()
            }
            "budget_reset_day" -> {
                _budgetResetDay.value = sharedPref.getInt("budget_reset_day", 1)
            }
        }
    }

    val allSubscriptions: Flow<List<Subscription>>
    val totalMonthlyCost: Flow<Double?>
    val spendingByCategory: Flow<List<CategorySpending>>
    val allPaymentHistory: Flow<List<PaymentHistory>>

    fun updateUserBalance(balance: Double) {
        sharedPref.edit().putFloat("user_balance", balance.toFloat()).apply()
        _userBalance.value = balance
    }

    fun updateBudgetResetDay(day: Int) {
        sharedPref.edit().putInt("budget_reset_day", day).apply()
        _budgetResetDay.value = day
    }

    override fun onCleared() {
        super.onCleared()
        sharedPref.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    init {
        sharedPref.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        val dao = AppDatabase.getDatabase(application).subscriptionDao()
        repository = SubscriptionRepository(dao)
        allSubscriptions = repository.allSubscriptions
        allPaymentHistory = repository.allPaymentHistory

        // Rollover or delete past subscriptions on startup
        viewModelScope.launch {
            try {
                val list = allSubscriptions.first()
                checkAndRolloverSubscriptions(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Calculate total monthly cost in VND (Base Currency) reactively
        totalMonthlyCost = allSubscriptions.map { list ->
            if (list.isEmpty()) return@map 0.0
            list.sumOf { sub -> calculateMonthlyAmountInVnd(sub) }
        }

        // Calculate category spending in VND (Base Currency) reactively
        spendingByCategory = allSubscriptions.map { list ->
            list.groupBy { it.category }
                .map { (category, subs) ->
                    val totalAmountInVnd = subs.sumOf { sub -> calculateMonthlyAmountInVnd(sub) }
                    CategorySpending(category, totalAmountInVnd)
                }
        }
    }

    /**
     * Helper function to calculate monthly amount in VND, accounting for leap years
     */
    private fun calculateMonthlyAmountInVnd(sub: Subscription): Double {
        val billingYear = Calendar.getInstance()
            .apply { timeInMillis = sub.nextBillingDate }
            .get(Calendar.YEAR)
        val daysInYear = if (Year.isLeap(billingYear.toLong())) 366.0 else 365.0

        val monthlyAmount = when (sub.cycle) {
            "Daily" -> sub.amount * daysInYear / 12.0
            "Weekly" -> sub.amount * 52.0 / 12.0
            "Monthly" -> sub.amount
            "Every 3 Months" -> sub.amount / 3.0
            "Every 6 Months" -> sub.amount / 6.0
            "Yearly" -> sub.amount / 12.0
            else -> 0.0
        }
        return CurrencyFormatter.convert(monthlyAmount, sub.currency, "VND")
    }

    private fun checkAndRolloverSubscriptions(list: List<Subscription>) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        list.forEach { sub ->
            if (sub.nextBillingDate < now) {
                var currentSub = sub
                var shouldDelete = false
                while (currentSub.nextBillingDate < now) {
                    if (currentSub.cycle == "One-time") {
                        shouldDelete = true
                        break
                    }

                    val limit = currentSub.remainingTimes
                    if (limit != null && limit > 0) {
                        val newLimit = limit - 1
                        if (newLimit <= 0) {
                            shouldDelete = true
                            break
                        }
                        currentSub = currentSub.copy(remainingTimes = newLimit)
                    }

                    val nextDate = DateUtils.getNextBillingDate(currentSub.nextBillingDate, currentSub.cycle)
                    if (nextDate <= currentSub.nextBillingDate) {
                        // Prevent infinite loop if cycle is invalid or unrecognized
                        shouldDelete = true
                        break
                    }
                    currentSub = currentSub.copy(nextBillingDate = nextDate)
                }

                if (shouldDelete) {
                    repository.delete(sub)
                    notificationScheduler.cancelNotification(sub.id)
                } else {
                    repository.update(currentSub)
                    notificationScheduler.scheduleNotification(currentSub)
                }
            }
        }
    }



    fun insert(subscription: Subscription) = viewModelScope.launch {
        val id = repository.insert(subscription)
        val newSub = subscription.copy(id = id)
        notificationScheduler.scheduleNotification(newSub)
    }

    fun update(subscription: Subscription) = viewModelScope.launch {
        repository.update(subscription)
        notificationScheduler.scheduleNotification(subscription)
    }

    fun updateSubscriptionDetails(
        id: Long,
        name: String,
        amount: Double,
        nextBillingDate: Long,
        cycle: String,
        category: String,
        colorHex: String,
        currency: String,
        remainingTimes: Int?,
        bankAccount: String?,
        bankName: String?,
        bankAccountHolder: String?
    ) = viewModelScope.launch {
        val existing = repository.getSubscriptionById(id)
        if (existing != null) {
            val updated = existing.copy(
                name = name,
                amount = amount,
                nextBillingDate = nextBillingDate,
                cycle = cycle,
                category = category,
                colorHex = colorHex,
                currency = currency,
                remainingTimes = remainingTimes,
                bankAccount = bankAccount,
                bankName = bankName,
                bankAccountHolder = bankAccountHolder
            )
            repository.update(updated)
            notificationScheduler.scheduleNotification(updated)
        }
    }

    fun delete(subscription: Subscription) = viewModelScope.launch {
        repository.delete(subscription)
        notificationScheduler.cancelNotification(subscription.id)
    }

    suspend fun getSubscriptionById(id: Long): Subscription? {
        return repository.getSubscriptionById(id)
    }

    fun markAsPaid(subscription: Subscription) = viewModelScope.launch {
        val record = PaymentHistory(
            subscriptionId = subscription.id,
            subscriptionName = subscription.name,
            amount = subscription.amount,
            currency = subscription.currency,
            paymentDate = System.currentTimeMillis(),
            cycle = subscription.cycle
        )
        repository.insertPaymentHistory(record)

        if (subscription.cycle == "One-time") {
            repository.delete(subscription)
            notificationScheduler.cancelNotification(subscription.id)
        } else {
            var shouldDelete = false
            var currentSub = subscription

            val limit = currentSub.remainingTimes
            if (limit != null && limit > 0) {
                val newLimit = limit - 1
                if (newLimit <= 0) {
                    shouldDelete = true
                } else {
                    currentSub = currentSub.copy(remainingTimes = newLimit)
                }
            }

            if (shouldDelete) {
                repository.delete(subscription)
                notificationScheduler.cancelNotification(subscription.id)
            } else {
                val finalNextDate = DateUtils.getNextBillingDate(currentSub.nextBillingDate, currentSub.cycle)
                currentSub = currentSub.copy(nextBillingDate = finalNextDate)
                repository.update(currentSub)
                notificationScheduler.scheduleNotification(currentSub)
            }
        }
    }
}