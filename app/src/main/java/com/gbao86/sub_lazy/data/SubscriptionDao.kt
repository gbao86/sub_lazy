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

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.gbao86.sub_lazy.data.model.SubscriptionCategory

data class CategorySpending(
    val category: SubscriptionCategory,
    val totalAmount: Double
)

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY nextBillingDate ASC")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: Long): Subscription?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription): Long

    @Update
    suspend fun updateSubscription(subscription: Subscription)

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)

    @Query("SELECT SUM(CASE WHEN cycle = 'Daily' THEN amount * 365.25 / 12.0 WHEN cycle = 'Weekly' THEN amount * 52.0 / 12.0 WHEN cycle = 'Monthly' THEN amount WHEN cycle = 'Every 3 Months' THEN amount / 3.0 WHEN cycle = 'Every 6 Months' THEN amount / 6.0 WHEN cycle = 'Yearly' THEN amount / 12.0 ELSE 0.0 END) FROM subscriptions WHERE cycle != 'One-time'")
    fun getTotalMonthlyCost(): Flow<Double?>

    @Query("SELECT category, SUM(CASE WHEN cycle = 'Daily' THEN amount * 365.25 / 12.0 WHEN cycle = 'Weekly' THEN amount * 52.0 / 12.0 WHEN cycle = 'Monthly' THEN amount WHEN cycle = 'Every 3 Months' THEN amount / 3.0 WHEN cycle = 'Every 6 Months' THEN amount / 6.0 WHEN cycle = 'Yearly' THEN amount / 12.0 ELSE 0.0 END) as totalAmount FROM subscriptions WHERE cycle != 'One-time' GROUP BY category")
    fun getSpendingByCategory(): Flow<List<CategorySpending>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentHistory(record: PaymentHistory): Long

    @Query("DELETE FROM payment_history WHERE subscriptionId = :subscriptionId")
    suspend fun deletePaymentHistoryBySubscriptionId(subscriptionId: Long)

    @Query("SELECT * FROM payment_history ORDER BY paymentDate DESC")
    fun getAllPaymentHistory(): Flow<List<PaymentHistory>>

    @Query("SELECT * FROM payment_history WHERE subscriptionId = :subId ORDER BY paymentDate DESC")
    fun getPaymentHistoryForSubscription(subId: Long): Flow<List<PaymentHistory>>

    // --- SharedMember queries ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedMember(member: SharedMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedMembers(members: List<SharedMember>)

    @Update
    suspend fun updateSharedMember(member: SharedMember)

    @Delete
    suspend fun deleteSharedMember(member: SharedMember)

    @Query("SELECT * FROM shared_members WHERE subscriptionId = :subscriptionId ORDER BY name ASC")
    fun getSharedMembersForSubscription(subscriptionId: Long): Flow<List<SharedMember>>

    @Query("SELECT * FROM shared_members WHERE subscriptionId = :subscriptionId ORDER BY name ASC")
    suspend fun getSharedMembersForSubscriptionOnce(subscriptionId: Long): List<SharedMember>

    @Query("DELETE FROM shared_members WHERE subscriptionId = :subscriptionId")
    suspend fun deleteSharedMembersForSubscription(subscriptionId: Long)

    @Query("UPDATE shared_members SET hasPaid = :hasPaid WHERE subscriptionId = :subscriptionId AND name = :name")
    suspend fun updateMemberPaidStatus(subscriptionId: Long, name: String, hasPaid: Boolean)
}
