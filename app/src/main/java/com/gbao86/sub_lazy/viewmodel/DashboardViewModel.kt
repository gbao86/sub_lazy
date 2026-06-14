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
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gbao86.sub_lazy.data.AppDatabase
import com.gbao86.sub_lazy.data.CategorySpending
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.ISubscriptionRepository
import com.gbao86.sub_lazy.data.SubscriptionRepository
import com.gbao86.sub_lazy.data.PaymentHistory
import com.gbao86.sub_lazy.worker.NotificationScheduler
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import com.gbao86.sub_lazy.ui.FinanceCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ISubscriptionRepository
    private val notificationScheduler = NotificationScheduler(application)
    
    private val sharedPref = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private fun getStoredBalance(): Double {
        val strVal = sharedPref.getString("user_balance_str", null)
        return strVal?.toDoubleOrNull() ?: sharedPref.getFloat("user_balance", 2000000f).toDouble()
    }

    private val _userBalance = MutableStateFlow(getStoredBalance())
    val userBalance: StateFlow<Double> = _userBalance.asStateFlow()

    private val _budgetResetDay = MutableStateFlow(sharedPref.getInt("budget_reset_day", 1))
    val budgetResetDay: StateFlow<Int> = _budgetResetDay.asStateFlow()

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            "user_balance", "user_balance_str" -> {
                _userBalance.value = getStoredBalance()
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
        sharedPref.edit()
            .putFloat("user_balance", balance.toFloat())
            .putString("user_balance_str", balance.toString())
            .apply()
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

        // Calculate total monthly cost reactively
        totalMonthlyCost = allSubscriptions.map { list ->
            if (list.isEmpty()) return@map 0.0
            list.sumOf { sub -> FinanceCalculator.calculateMonthlyEquivalentCostInVnd(sub) }
        }

        // Calculate category spending reactively
        spendingByCategory = allSubscriptions.map { list ->
            list.groupBy { it.category }
                .map { (category, subs) ->
                    val totalAmountInVnd = subs.sumOf { sub -> FinanceCalculator.calculateMonthlyEquivalentCostInVnd(sub) }
                    CategorySpending(category, totalAmountInVnd)
                }
        }
    }

    private fun checkAndRolloverSubscriptions(list: List<Subscription>) = viewModelScope.launch {
        val now = System.currentTimeMillis()
        list.forEach { sub ->
            if (sub.nextBillingDate < now) {
                var currentSub = sub
                var shouldDelete = false
                while (currentSub.nextBillingDate < now) {
                    if (currentSub.cycle == BillingCycle.ONE_TIME) {
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
                        shouldDelete = true
                        break
                    }
                    currentSub = currentSub.copy(nextBillingDate = nextDate)
                }

                if (shouldDelete) {
                    repository.delete(sub).onSuccess {
                        notificationScheduler.cancelNotification(sub.id)
                    }
                } else {
                    if (currentSub.isShared && !currentSub.sharedMembersJson.isNullOrBlank()) {
                        val members = com.gbao86.sub_lazy.data.SharedMember.parseMembers(currentSub.sharedMembersJson)
                        val resetMembers = members.map { it.copy(hasPaid = false) }
                        currentSub = currentSub.copy(sharedMembersJson = com.gbao86.sub_lazy.data.SharedMember.serializeMembers(resetMembers))
                    }
                    repository.update(currentSub).onSuccess {
                        notificationScheduler.scheduleNotification(currentSub)
                    }
                }
            }
        }
    }

    fun delete(subscription: Subscription) = viewModelScope.launch {
        repository.delete(subscription).onSuccess {
            notificationScheduler.cancelNotification(subscription.id)
        }
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
        repository.insertPaymentHistory(record).onSuccess {
            if (subscription.cycle == BillingCycle.ONE_TIME) {
                repository.delete(subscription).onSuccess {
                    notificationScheduler.cancelNotification(subscription.id)
                }
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
                    repository.delete(subscription).onSuccess {
                        notificationScheduler.cancelNotification(subscription.id)
                    }
                } else {
                    val finalNextDate = DateUtils.getNextBillingDate(currentSub.nextBillingDate, currentSub.cycle)
                    currentSub = currentSub.copy(nextBillingDate = finalNextDate)
                    
                    if (currentSub.isShared && !currentSub.sharedMembersJson.isNullOrBlank()) {
                        val members = com.gbao86.sub_lazy.data.SharedMember.parseMembers(currentSub.sharedMembersJson)
                        val resetMembers = members.map { it.copy(hasPaid = false) }
                        currentSub = currentSub.copy(sharedMembersJson = com.gbao86.sub_lazy.data.SharedMember.serializeMembers(resetMembers))
                    }
                    
                    repository.update(currentSub).onSuccess {
                        notificationScheduler.scheduleNotification(currentSub)
                    }
                }
            }
        }
    }

    fun checkInSession(subscription: Subscription) = viewModelScope.launch {
        val rem = subscription.remainingSessions ?: return@launch
        if (rem > 0) {
            val updated = subscription.copy(remainingSessions = rem - 1)
            repository.update(updated)
        }
    }

    fun toggleMemberPaidStatus(subscription: Subscription, memberName: String) = viewModelScope.launch {
        val json = subscription.sharedMembersJson ?: return@launch
        val members = com.gbao86.sub_lazy.data.SharedMember.parseMembers(json)
        val updatedMembers = members.map {
            if (it.name == memberName) {
                it.copy(hasPaid = !it.hasPaid)
            } else {
                it
            }
        }
        val updated = subscription.copy(sharedMembersJson = com.gbao86.sub_lazy.data.SharedMember.serializeMembers(updatedMembers))
        repository.update(updated)
    }
}
