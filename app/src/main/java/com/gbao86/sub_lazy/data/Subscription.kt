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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency

@Entity(
    tableName = "subscriptions",
    indices = [
        Index(value = ["category"]),
        Index(value = ["cycle"])
    ]
)
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val nextBillingDate: Long, // Using Long for timestamp
    val cycle: BillingCycle,
    val category: SubscriptionCategory,
    val colorHex: String = "#6366F1", // Default Indigo
    val iconName: String? = null,
    @ColumnInfo(defaultValue = "VND")
    val currency: SubscriptionCurrency = SubscriptionCurrency.VND,
    val remainingTimes: Int? = null,

    // Km-based tracking
    @ColumnInfo(defaultValue = "0")
    val isKmBased: Boolean = false,
    val lastOdometer: Double? = null,
    val targetIntervalKm: Double? = null,
    val dailyAverageKm: Double? = null,
    val lastOdometerUpdateDate: Long? = null,

    // Bank transfer details for VietQR
    val bankAccount: String? = null,
    val bankName: String? = null,
    val bankAccountHolder: String? = null,

    // Session-based tracking
    @ColumnInfo(defaultValue = "0")
    val isSessionBased: Boolean = false,
    val totalSessions: Int? = null,
    val remainingSessions: Int? = null,

    // Installment tracking
    @ColumnInfo(defaultValue = "0")
    val isInstallment: Boolean = false,
    val totalInstallmentPeriods: Int? = null,

    // Shared subscription splitting — members now stored in shared_members table (ForeignKey CASCADE)
    // sharedMembersJson kept for migration purposes only; cleared after MIGRATION_8_9
    @ColumnInfo(defaultValue = "0")
    val isShared: Boolean = false,
    @Deprecated("Use shared_members table via SubscriptionRepository.getSharedMembersForSubscription()")
    val sharedMembersJson: String? = null
)

