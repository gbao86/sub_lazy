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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.ISubscriptionRepository
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import com.gbao86.sub_lazy.domain.usecase.InsertSubscriptionUseCase
import com.gbao86.sub_lazy.domain.usecase.UpdateSubscriptionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val repository: ISubscriptionRepository,
    private val insertSubscriptionUseCase: InsertSubscriptionUseCase,
    private val updateSubscriptionUseCase: UpdateSubscriptionUseCase
) : ViewModel() {

    fun insert(subscription: Subscription) = viewModelScope.launch {
        insertSubscriptionUseCase(subscription)
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
                updateSubscriptionUseCase(updated)
            }
        }
    }

    suspend fun getSubscriptionById(id: Long): Subscription? {
        return repository.getSubscriptionById(id).getOrNull()
    }
}
