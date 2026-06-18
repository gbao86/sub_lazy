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
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency

import com.squareup.moshi.JsonClass

@Entity(
    tableName = "payment_history",
    foreignKeys = [
        ForeignKey(
            entity = Subscription::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["subscriptionId"])
    ]
)
@JsonClass(generateAdapter = true)
data class PaymentHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subscriptionId: Long,
    val subscriptionName: String,
    val amount: Double,
    val currency: SubscriptionCurrency,
    val paymentDate: Long,
    val cycle: BillingCycle
)
