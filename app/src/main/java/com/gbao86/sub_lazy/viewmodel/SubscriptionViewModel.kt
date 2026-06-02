/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

Unauthorized copying of this file, via any medium, is strictly prohibited.
Proprietary and confidential.
This source code is provided for reference purposes only and may not be copied, 
modified, or distributed without the express written permission of the author.
*/

package com.gbao86.sub_lazy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gbao86.sub_lazy.data.AppDatabase
import com.gbao86.sub_lazy.data.CategorySpending
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.SubscriptionRepository
import com.gbao86.sub_lazy.worker.NotificationScheduler
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SubscriptionRepository
    private val notificationScheduler = NotificationScheduler(application)
    
    val allSubscriptions: Flow<List<Subscription>>
    val totalMonthlyCost: Flow<Double?>
    val spendingByCategory: Flow<List<CategorySpending>>

    init {
        val dao = AppDatabase.getDatabase(application).subscriptionDao()
        repository = SubscriptionRepository(dao)
        allSubscriptions = repository.allSubscriptions
        
        // Calculate total monthly cost in VND (Base Currency) reactively
        totalMonthlyCost = allSubscriptions.map { list ->
            if (list.isEmpty()) return@map 0.0
            list.sumOf { sub ->
                val monthlyAmount = if (sub.cycle == "Monthly") sub.amount else sub.amount / 12.0
                CurrencyFormatter.convert(monthlyAmount, sub.currency, "VND")
            }
        }
        
        // Calculate category spending in VND (Base Currency) reactively
        spendingByCategory = allSubscriptions.map { list ->
            list.groupBy { it.category }
                .map { (category, subs) ->
                    val totalAmountInVnd = subs.sumOf { sub ->
                        val monthlyAmount = if (sub.cycle == "Monthly") sub.amount else sub.amount / 12.0
                        CurrencyFormatter.convert(monthlyAmount, sub.currency, "VND")
                    }
                    CategorySpending(category, totalAmountInVnd)
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

    fun delete(subscription: Subscription) = viewModelScope.launch {
        repository.delete(subscription)
        notificationScheduler.cancelNotification(subscription.id)
    }

    suspend fun getSubscriptionById(id: Long): Subscription? {
        return repository.getSubscriptionById(id)
    }
}
