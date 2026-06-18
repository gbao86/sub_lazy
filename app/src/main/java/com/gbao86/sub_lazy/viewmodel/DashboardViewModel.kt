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

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gbao86.sub_lazy.data.CategorySpending
import com.gbao86.sub_lazy.data.SharedMember
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.ISubscriptionRepository
import com.gbao86.sub_lazy.data.PaymentHistory
import com.gbao86.sub_lazy.domain.usecase.*
import com.gbao86.sub_lazy.ui.FinanceCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ISubscriptionRepository,
    private val checkAndRolloverSubscriptionsUseCase: CheckAndRolloverSubscriptionsUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase,
    private val markPaymentAsPaidUseCase: MarkPaymentAsPaidUseCase,
    private val checkInSessionUseCase: CheckInSessionUseCase,
    private val toggleMemberPaidStatusUseCase: ToggleMemberPaidStatusUseCase,
    private val backupRestoreManager: com.gbao86.sub_lazy.data.BackupRestoreManager,
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private fun getStoredBalance(): Double {
        val strVal = sharedPref.getString("user_balance_str", null)
        if (strVal != null) {
            val parsed = strVal.toDoubleOrNull()
            if (parsed != null) return parsed
        }
        // Migrate legacy Float value (stored as string for precision)
        val legacyStr = sharedPref.getString("user_balance_long", null)
        return legacyStr?.toDoubleOrNull() ?: 2000000.0
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

    val allSubscriptions: Flow<List<Subscription>> = repository.allSubscriptions
    val totalMonthlyCost: Flow<Double?>
    val spendingByCategory: Flow<List<CategorySpending>>
    val allPaymentHistory: Flow<List<PaymentHistory>> = repository.allPaymentHistory

    /**
     * Reactive map of subscriptionId -> List<SharedMember> for all shared subscriptions.
     * Updates whenever subscriptions list or any member row changes.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sharedMembersMap: Flow<Map<Long, List<SharedMember>>> = repository.allSubscriptions
        .flatMapLatest { subs ->
            val sharedSubs = subs.filter { it.isShared }
            if (sharedSubs.isEmpty()) {
                flowOf(emptyMap())
            } else {
                val memberFlows = sharedSubs.map { sub ->
                    repository.getSharedMembersForSubscription(sub.id).map { members -> sub.id to members }
                }
                combine(memberFlows) { pairs -> pairs.toMap() }
            }
        }

    fun updateUserBalance(balance: Double) {
        sharedPref.edit()
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

        // Rollover or delete past subscriptions on startup
        viewModelScope.launch {
            runCatching {
                val list = allSubscriptions.first()
                checkAndRolloverSubscriptionsUseCase(list)
            }.onFailure { it.printStackTrace() }
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

    fun delete(subscription: Subscription) = viewModelScope.launch {
        deleteSubscriptionUseCase(subscription)
    }

    fun markAsPaid(subscription: Subscription) = viewModelScope.launch {
        markPaymentAsPaidUseCase(subscription)
    }

    fun checkInSession(subscription: Subscription) = viewModelScope.launch {
        checkInSessionUseCase(subscription)
    }

    fun toggleMemberPaidStatus(subscription: Subscription, memberName: String) = viewModelScope.launch {
        toggleMemberPaidStatusUseCase(subscription, memberName)
    }

    private val _isProcessing = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isProcessing: kotlinx.coroutines.flow.StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun exportData(uri: android.net.Uri, onComplete: (com.gbao86.sub_lazy.data.BackupResult) -> Unit) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = backupRestoreManager.exportData(context, uri)
                onComplete(result)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun importData(uri: android.net.Uri, onComplete: (com.gbao86.sub_lazy.data.BackupResult) -> Unit) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val result = backupRestoreManager.importData(context, uri)
                onComplete(result)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun syncToDrive(onComplete: (Boolean, String?) -> Unit) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val json = backupRestoreManager.getBackupJsonString()
                val service = com.gbao86.sub_lazy.data.api.GoogleDriveService(context)
                val result = service.uploadBackup(json)
                if (result.isSuccess) {
                    onComplete(true, null)
                } else {
                    val e = result.exceptionOrNull()
                    e?.printStackTrace()
                    onComplete(false, e?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false, e.message)
            } finally {
                _isProcessing.value = false
            }
        }
    }
}
