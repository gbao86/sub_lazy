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
import javax.inject.Inject

class CheckAndRolloverSubscriptionsUseCase @Inject constructor(
    private val repository: ISubscriptionRepository,
    private val rolloverUseCase: RolloverSubscriptionUseCase
) {
    suspend operator fun invoke(list: List<Subscription>): Result<Unit> {
        val now = System.currentTimeMillis()
        list.forEach { sub ->
            if (sub.nextBillingDate < now) {
                var currentSub = sub
                while (currentSub.nextBillingDate < now) {
                    when (val result = rolloverUseCase(currentSub)) {
                        is RolloverSubscriptionUseCase.RolloverResult.Deleted -> break
                        is RolloverSubscriptionUseCase.RolloverResult.Updated -> {
                            currentSub = result.subscription
                        }
                    }
                }
            }
        }
        return Result.success(Unit)
    }
}
