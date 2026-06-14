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
import com.gbao86.sub_lazy.worker.NotificationScheduler
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import kotlinx.coroutines.launch

class AddEditViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ISubscriptionRepository
    private val notificationScheduler = NotificationScheduler(application)

    init {
        val dao = AppDatabase.getDatabase(application).subscriptionDao()
        repository = SubscriptionRepository(dao)
    }

    fun insert(subscription: Subscription) = viewModelScope.launch {
        repository.insert(subscription).onSuccess { id ->
            val newSub = subscription.copy(id = id)
            notificationScheduler.scheduleNotification(newSub)
        }
    }

    fun updateSubscriptionDetails(
        id: Long,
        name: String,
        amount: Double,
        nextBillingDate: Long,
        cycle: BillingCycle,
        category: SubscriptionCategory,
        colorHex: String,
        currency: SubscriptionCurrency,
        remainingTimes: Int?,
        bankAccount: String?,
        bankName: String?,
        bankAccountHolder: String?,
        isSessionBased: Boolean,
        totalSessions: Int?,
        remainingSessions: Int?,
        isInstallment: Boolean,
        isShared: Boolean,
        sharedMembersJson: String?
    ) = viewModelScope.launch {
        repository.getSubscriptionById(id).onSuccess { existing ->
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
                    bankAccountHolder = bankAccountHolder,
                    isSessionBased = isSessionBased,
                    totalSessions = totalSessions,
                    remainingSessions = remainingSessions,
                    isInstallment = isInstallment,
                    isShared = isShared,
                    sharedMembersJson = sharedMembersJson
                )
                repository.update(updated).onSuccess {
                    notificationScheduler.scheduleNotification(updated)
                }
            }
        }
    }

    suspend fun getSubscriptionById(id: Long): Subscription? {
        return repository.getSubscriptionById(id).getOrNull()
    }
}
