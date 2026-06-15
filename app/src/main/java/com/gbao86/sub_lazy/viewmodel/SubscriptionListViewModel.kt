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
import com.gbao86.sub_lazy.domain.usecase.DeleteSubscriptionUseCase
import com.gbao86.sub_lazy.domain.usecase.InsertSubscriptionUseCase
import com.gbao86.sub_lazy.domain.usecase.MarkPaymentAsPaidUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionListViewModel @Inject constructor(
    private val repository: ISubscriptionRepository,
    private val insertSubscriptionUseCase: InsertSubscriptionUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase,
    private val markPaymentAsPaidUseCase: MarkPaymentAsPaidUseCase
) : ViewModel() {

    val allSubscriptions: Flow<List<Subscription>> = repository.allSubscriptions

    fun insert(subscription: Subscription) = viewModelScope.launch {
        insertSubscriptionUseCase(subscription)
    }

    fun delete(subscription: Subscription) = viewModelScope.launch {
        deleteSubscriptionUseCase(subscription)
    }

    fun markAsPaid(subscription: Subscription) = viewModelScope.launch {
        markPaymentAsPaidUseCase(subscription)
    }
}
