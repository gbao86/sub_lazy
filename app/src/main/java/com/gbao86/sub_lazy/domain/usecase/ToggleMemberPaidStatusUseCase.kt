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

class ToggleMemberPaidStatusUseCase @Inject constructor(
    private val repository: ISubscriptionRepository
) {
    suspend operator fun invoke(subscription: Subscription, memberName: String): Result<Unit> {
        // Get current paid status from the shared_members table
        val membersResult = repository.getSharedMembersForSubscriptionOnce(subscription.id)
        val members = membersResult.getOrElse { return Result.failure(it) }
        val member = members.find { it.name == memberName }
            ?: return Result.failure(Exception("Member '$memberName' not found"))
        return repository.updateMemberPaidStatus(subscription.id, memberName, !member.hasPaid)
    }
}
