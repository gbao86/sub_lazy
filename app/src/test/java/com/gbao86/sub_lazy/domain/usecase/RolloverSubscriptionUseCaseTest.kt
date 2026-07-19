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
import com.gbao86.sub_lazy.data.SharedMember
import com.gbao86.sub_lazy.data.Subscription
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import com.gbao86.sub_lazy.worker.NotificationScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class RolloverSubscriptionUseCaseTest {

    private lateinit var repository: ISubscriptionRepository
    private lateinit var scheduler: NotificationScheduler
    private lateinit var useCase: RolloverSubscriptionUseCase

    private fun createSubscription(
        id: Long = 1L,
        amount: Double = 100000.0,
        cycle: BillingCycle = BillingCycle.MONTHLY,
        nextBillingDate: Long = System.currentTimeMillis() - 86400000L, // 1 day ago
        remainingTimes: Int? = null,
        isShared: Boolean = false
    ): Subscription = Subscription(
        id = id,
        name = "Test Sub",
        amount = amount,
        nextBillingDate = nextBillingDate,
        cycle = cycle,
        category = SubscriptionCategory.ENTERTAINMENT,
        colorHex = "#6366F1",
        currency = SubscriptionCurrency.VND,
        remainingTimes = remainingTimes,
        isShared = isShared
    )

    @Before
    fun setUp() {
        repository = mock()
        scheduler = mock()
        useCase = RolloverSubscriptionUseCase(repository, scheduler)
    }

    @Test
    fun `one-time subscription is deleted`() = runTest {
        val sub = createSubscription(cycle = BillingCycle.ONE_TIME)
        whenever(repository.delete(sub)).thenReturn(Result.success(Unit))

        val result = useCase(sub)

        assertTrue(result is RolloverSubscriptionUseCase.RolloverResult.Deleted)
        verify(repository).delete(sub)
        verify(scheduler).cancelNotification(sub.id)
        verify(repository, never()).update(any())
    }

    @Test
    fun `monthly subscription rolls over to next month`() = runTest {
        val sub = createSubscription(cycle = BillingCycle.MONTHLY)
        whenever(repository.update(any())).thenReturn(Result.success(Unit))

        val result = useCase(sub)

        assertTrue(result is RolloverSubscriptionUseCase.RolloverResult.Updated)
        val updated = (result as RolloverSubscriptionUseCase.RolloverResult.Updated).subscription
        assertTrue(updated.nextBillingDate > sub.nextBillingDate)
        verify(repository).update(updated)
        verify(scheduler).scheduleNotification(updated)
    }

    @Test
    fun `subscription with remainingTimes 1 is deleted`() = runTest {
        val sub = createSubscription(remainingTimes = 1)
        whenever(repository.delete(sub)).thenReturn(Result.success(Unit))

        val result = useCase(sub)

        assertTrue(result is RolloverSubscriptionUseCase.RolloverResult.Deleted)
        verify(repository).delete(sub)
        verify(scheduler).cancelNotification(sub.id)
    }

    @Test
    fun `subscription with remainingTimes 3 decrements to 2`() = runTest {
        val sub = createSubscription(remainingTimes = 3)
        whenever(repository.update(any())).thenReturn(Result.success(Unit))

        val result = useCase(sub)

        assertTrue(result is RolloverSubscriptionUseCase.RolloverResult.Updated)
        val updated = (result as RolloverSubscriptionUseCase.RolloverResult.Updated).subscription
        assertEquals(2, updated.remainingTimes)
    }

    @Test
    fun `shared subscription resets member paid status`() = runTest {
        val sub = createSubscription(isShared = true)
        val members = listOf(
            SharedMember(id = 1, subscriptionId = 1, name = "A", amount = 50000.0, hasPaid = true),
            SharedMember(id = 2, subscriptionId = 1, name = "B", amount = 50000.0, hasPaid = true)
        )
        whenever(repository.update(any())).thenReturn(Result.success(Unit))
        whenever(repository.getSharedMembersForSubscriptionOnce(1L)).thenReturn(Result.success(members))
        whenever(repository.saveSharedMembers(eq(1L), any())).thenReturn(Result.success(Unit))

        val result = useCase(sub)

        assertTrue(result is RolloverSubscriptionUseCase.RolloverResult.Updated)
        verify(repository).saveSharedMembers(eq(1L), argThat { 
            all { !it.hasPaid }
        })
    }

    @Test
    fun `non-shared subscription does not touch shared members`() = runTest {
        val sub = createSubscription(isShared = false)
        whenever(repository.update(any())).thenReturn(Result.success(Unit))

        useCase(sub)

        verify(repository, never()).getSharedMembersForSubscriptionOnce(any())
        verify(repository, never()).saveSharedMembers(any(), any())
    }
}
