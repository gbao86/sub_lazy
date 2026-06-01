package com.gbao86.sub_lazy.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gbao86.sub_lazy.data.AppDatabase
import com.gbao86.sub_lazy.data.CategorySpending
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.SubscriptionRepository
import com.gbao86.sub_lazy.worker.NotificationScheduler
import kotlinx.coroutines.flow.Flow
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
        totalMonthlyCost = repository.totalMonthlyCost
        spendingByCategory = dao.getSpendingByCategory()
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
