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
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.ISubscriptionRepository
import com.gbao86.sub_lazy.data.SubscriptionRepository
import com.gbao86.sub_lazy.data.PaymentHistory
import com.gbao86.sub_lazy.worker.NotificationScheduler
import com.gbao86.sub_lazy.ui.DateUtils
import com.gbao86.sub_lazy.data.model.BillingCycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SubscriptionListViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ISubscriptionRepository
    private val notificationScheduler = NotificationScheduler(application)

    val allSubscriptions: Flow<List<Subscription>>

    init {
        val dao = AppDatabase.getDatabase(application).subscriptionDao()
        repository = SubscriptionRepository(dao)
        allSubscriptions = repository.allSubscriptions
    }

    fun insert(subscription: Subscription) = viewModelScope.launch {
        repository.insert(subscription).onSuccess { id ->
            val newSub = subscription.copy(id = id)
            notificationScheduler.scheduleNotification(newSub)
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
}
