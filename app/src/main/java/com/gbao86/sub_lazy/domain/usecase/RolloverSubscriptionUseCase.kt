/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational, 
and non-commercial purposes. 
Commercial exploitation, sale, or distribution of this software or any derivative works 
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy.domain.usecase

import com.gbao86.sub_lazy.data.ISubscriptionRepository
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.ui.DateUtils
import com.gbao86.sub_lazy.worker.NotificationScheduler
import javax.inject.Inject

class RolloverSubscriptionUseCase @Inject constructor(
    private val repository: ISubscriptionRepository,
    private val notificationScheduler: NotificationScheduler
) {
    sealed class RolloverResult {
        data class Updated(val subscription: Subscription) : RolloverResult()
        object Deleted : RolloverResult()
    }

    suspend operator fun invoke(subscription: Subscription): RolloverResult {
        if (subscription.cycle == BillingCycle.ONE_TIME) {
            repository.delete(subscription)
            notificationScheduler.cancelNotification(subscription.id)
            return RolloverResult.Deleted
        }

        var currentSub = subscription
        var shouldDelete = false

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
            return RolloverResult.Deleted
        }

        val finalNextDate = DateUtils.getNextBillingDate(currentSub.nextBillingDate, currentSub.cycle)
        currentSub = currentSub.copy(nextBillingDate = finalNextDate)

        if (currentSub.isShared) {
            repository.getSharedMembersForSubscriptionOnce(currentSub.id).onSuccess { members ->
                val resetMembers = members.map { it.copy(hasPaid = false) }
                repository.saveSharedMembers(currentSub.id, resetMembers)
            }
        }

        repository.update(currentSub)
        notificationScheduler.scheduleNotification(currentSub)
        return RolloverResult.Updated(currentSub)
    }
}
