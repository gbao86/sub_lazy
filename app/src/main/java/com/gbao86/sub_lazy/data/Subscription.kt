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

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val nextBillingDate: Long, // Using Long for timestamp
    val cycle: String, // e.g., "Monthly", "Yearly"
    val category: String,
    val colorHex: String = "#6366F1", // Default Indigo
    val iconName: String? = null,
    val currency: String = "VND",
    val remainingTimes: Int? = null,

    // Km-based tracking
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
    val isSessionBased: Boolean = false,
    val totalSessions: Int? = null,
    val remainingSessions: Int? = null,

    // Installment tracking
    val isInstallment: Boolean = false,
    val totalInstallmentPeriods: Int? = null,

    // Shared subscription splitting
    val isShared: Boolean = false,
    val sharedMembersJson: String? = null // Format: "Name:Amount:HasPaid:Phone;..."
)

