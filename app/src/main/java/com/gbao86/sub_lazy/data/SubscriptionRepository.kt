/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational, 
and non-commercial purposes. 
Commercial exploitation, sale, or distribution of this software or any derivative works 
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import com.gbao86.sub_lazy.data.model.CategorySpending

interface ISubscriptionRepository {
    val allSubscriptions: Flow<List<Subscription>>
    val totalMonthlyCost: Flow<Double?>
    val spendingByCategory: Flow<List<CategorySpending>>
    val allPaymentHistory: Flow<List<PaymentHistory>>

    suspend fun insert(subscription: Subscription): Result<Long>
    suspend fun update(subscription: Subscription): Result<Unit>
    suspend fun delete(subscription: Subscription): Result<Unit>
    suspend fun getSubscriptionById(id: Long): Result<Subscription?>
    suspend fun insertPaymentHistory(record: PaymentHistory): Result<Long>
    fun getPaymentHistoryForSubscription(subId: Long): Flow<List<PaymentHistory>>

    // SharedMember operations
    fun getSharedMembersForSubscription(subscriptionId: Long): Flow<List<SharedMember>>
    suspend fun getSharedMembersForSubscriptionOnce(subscriptionId: Long): Result<List<SharedMember>>
    suspend fun saveSharedMembers(subscriptionId: Long, members: List<SharedMember>): Result<Unit>
    suspend fun updateMemberPaidStatus(subscriptionId: Long, memberName: String, hasPaid: Boolean): Result<Unit>
}

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) : ISubscriptionRepository {
    private val TAG = "SubscriptionRepository"

    override val allSubscriptions: Flow<List<Subscription>> = subscriptionDao.getAllSubscriptions()
    override val totalMonthlyCost: Flow<Double?> = subscriptionDao.getTotalMonthlyCost()
    override val spendingByCategory: Flow<List<CategorySpending>> = subscriptionDao.getSpendingByCategory()
    override val allPaymentHistory: Flow<List<PaymentHistory>> = subscriptionDao.getAllPaymentHistory()

    override suspend fun insert(subscription: Subscription): Result<Long> = runCatching {
        subscriptionDao.insertSubscription(subscription)
    }.onFailure {
        Log.e(TAG, "Error inserting subscription", it)
    }

    override suspend fun update(subscription: Subscription): Result<Unit> = runCatching {
        subscriptionDao.updateSubscription(subscription)
    }.onFailure {
        Log.e(TAG, "Error updating subscription", it)
    }

    override suspend fun delete(subscription: Subscription): Result<Unit> = runCatching {
        subscriptionDao.deletePaymentHistoryBySubscriptionId(subscription.id)
        subscriptionDao.deleteSubscription(subscription)
    }.onFailure {
        Log.e(TAG, "Error deleting subscription", it)
    }

    override suspend fun getSubscriptionById(id: Long): Result<Subscription?> = runCatching {
        subscriptionDao.getSubscriptionById(id)
    }.onFailure {
        Log.e(TAG, "Error getting subscription by id: $id", it)
    }

    override suspend fun insertPaymentHistory(record: PaymentHistory): Result<Long> = runCatching {
        subscriptionDao.insertPaymentHistory(record)
    }.onFailure {
        Log.e(TAG, "Error inserting payment history", it)
    }

    override fun getPaymentHistoryForSubscription(subId: Long): Flow<List<PaymentHistory>> {
        return subscriptionDao.getPaymentHistoryForSubscription(subId)
    }

    override fun getSharedMembersForSubscription(subscriptionId: Long): Flow<List<SharedMember>> {
        return subscriptionDao.getSharedMembersForSubscription(subscriptionId)
    }

    override suspend fun getSharedMembersForSubscriptionOnce(subscriptionId: Long): Result<List<SharedMember>> = runCatching {
        subscriptionDao.getSharedMembersForSubscriptionOnce(subscriptionId)
    }.onFailure {
        Log.e(TAG, "Error getting shared members for subscription: $subscriptionId", it)
    }

    override suspend fun saveSharedMembers(subscriptionId: Long, members: List<SharedMember>): Result<Unit> = runCatching {
        subscriptionDao.deleteSharedMembersForSubscription(subscriptionId)
        val membersWithSubId = members.map { it.copy(subscriptionId = subscriptionId, id = 0) }
        subscriptionDao.insertSharedMembers(membersWithSubId)
    }.onFailure {
        Log.e(TAG, "Error saving shared members for subscription: $subscriptionId", it)
    }

    override suspend fun updateMemberPaidStatus(subscriptionId: Long, memberName: String, hasPaid: Boolean): Result<Unit> = runCatching {
        subscriptionDao.updateMemberPaidStatus(subscriptionId, memberName, hasPaid)
    }.onFailure {
        Log.e(TAG, "Error updating member paid status: $memberName for subscription: $subscriptionId", it)
    }
}
