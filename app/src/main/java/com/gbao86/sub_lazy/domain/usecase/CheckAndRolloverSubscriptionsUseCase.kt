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

class CheckAndRolloverSubscriptionsUseCase @Inject constructor(
    private val repository: ISubscriptionRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(list: List<Subscription>): Result<Unit> {
        val now = System.currentTimeMillis()
        list.forEach { sub ->
            if (sub.nextBillingDate < now) {
                var currentSub = sub
                var shouldDelete = false
                while (currentSub.nextBillingDate < now) {
                    if (currentSub.cycle == BillingCycle.ONE_TIME) {
                        shouldDelete = true
                        break
                    }

                    val limit = currentSub.remainingTimes
                    if (limit != null && limit > 0) {
                        val newLimit = limit - 1
                        if (newLimit <= 0) {
                            shouldDelete = true
                            break
                        }
                        currentSub = currentSub.copy(remainingTimes = newLimit)
                    }

                    val nextDate = DateUtils.getNextBillingDate(currentSub.nextBillingDate, currentSub.cycle)
                    if (nextDate <= currentSub.nextBillingDate) {
                        shouldDelete = true
                        break
                    }
                    currentSub = currentSub.copy(nextBillingDate = nextDate)
                }

                if (shouldDelete) {
                    repository.delete(sub).onSuccess {
                        notificationScheduler.cancelNotification(sub.id)
                    }
                } else {
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
        return Result.success(Unit)
    }
}
