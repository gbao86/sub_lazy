/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

Unauthorized copying of this file, via any medium, is strictly prohibited.
Proprietary and confidential.
This source code is provided for reference purposes only and may not be copied, 
modified, or distributed without the express written permission of the author.
*/

package com.gbao86.sub_lazy.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class CategorySpending(
    val category: String,
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

    @Query("SELECT SUM(CASE WHEN cycle = 'Monthly' THEN amount WHEN cycle = 'Yearly' THEN amount / 12.0 ELSE 0.0 END) FROM subscriptions")
    fun getTotalMonthlyCost(): Flow<Double?>

    @Query("SELECT category, SUM(CASE WHEN cycle = 'Monthly' THEN amount WHEN cycle = 'Yearly' THEN amount / 12.0 ELSE 0.0 END) as totalAmount FROM subscriptions GROUP BY category")
    fun getSpendingByCategory(): Flow<List<CategorySpending>>
}
