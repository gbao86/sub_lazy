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
import com.gbao86.sub_lazy.worker.NotificationScheduler
import javax.inject.Inject

class InsertSubscriptionUseCase @Inject constructor(
    private val repository: ISubscriptionRepository,
    private val notificationScheduler: NotificationScheduler
) {
    suspend operator fun invoke(subscription: Subscription): Result<Long> {
        return repository.insert(subscription).onSuccess { id ->
            val newSub = subscription.copy(id = id)
            notificationScheduler.scheduleNotification(newSub)
        }
    }
}
