package com.gbao86.sub_lazy.data

import kotlinx.coroutines.flow.Flow

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {
    val allSubscriptions: Flow<List<Subscription>> = subscriptionDao.getAllSubscriptions()
    val totalMonthlyCost: Flow<Double?> = subscriptionDao.getTotalMonthlyCost()

    suspend fun insert(subscription: Subscription): Long {
        return subscriptionDao.insertSubscription(subscription)
    }

    suspend fun update(subscription: Subscription) {
        subscriptionDao.updateSubscription(subscription)
    }

    suspend fun delete(subscription: Subscription) {
        subscriptionDao.deleteSubscription(subscription)
    }

    suspend fun getSubscriptionById(id: Long): Subscription? {
        return subscriptionDao.getSubscriptionById(id)
    }
}
