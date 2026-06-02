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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SubscriptionRepository
    private val notificationScheduler = NotificationScheduler(application)
    
    val allSubscriptions: Flow<List<Subscription>>
    val totalMonthlyCost: Flow<Double?>
    val spendingByCategory: Flow<List<CategorySpending>>
    val allPaymentHistory: Flow<List<PaymentHistory>>

    init {
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
            list.sumOf { sub ->
                val monthlyAmount = when (sub.cycle) {
                    "Weekly" -> sub.amount * 52.0 / 12.0
                    "Monthly" -> sub.amount
                    "Yearly" -> sub.amount / 12.0
                    else -> 0.0
                }
                CurrencyFormatter.convert(monthlyAmount, sub.currency, "VND")
            }
        }
        
        // Calculate category spending in VND (Base Currency) reactively
        spendingByCategory = allSubscriptions.map { list ->
            list.groupBy { it.category }
                .map { (category, subs) ->
                    val totalAmountInVnd = subs.sumOf { sub ->
                        val monthlyAmount = when (sub.cycle) {
                            "Weekly" -> sub.amount * 52.0 / 12.0
                            "Monthly" -> sub.amount
                            "Yearly" -> sub.amount / 12.0
                            else -> 0.0
                        }
                        CurrencyFormatter.convert(monthlyAmount, sub.currency, "VND")
                    }
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
                    
                    val nextDate = getNextBillingDate(currentSub.nextBillingDate, currentSub.cycle)
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

    private fun getNextBillingDate(currentDate: Long, cycle: String): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = currentDate }
        when (cycle) {
            "Weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            "Monthly" -> cal.add(Calendar.MONTH, 1)
            "Yearly" -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
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
                val nextDate = getNextBillingDate(currentSub.nextBillingDate, currentSub.cycle)
                val finalNextDate = if (nextDate <= currentSub.nextBillingDate) {
                    val cal = Calendar.getInstance().apply { timeInMillis = currentSub.nextBillingDate }
                    cal.add(Calendar.MONTH, 1)
                    cal.timeInMillis
                } else {
                    nextDate
                }
                currentSub = currentSub.copy(nextBillingDate = finalNextDate)
                repository.update(currentSub)
                notificationScheduler.scheduleNotification(currentSub)
            }
        }
    }
}
