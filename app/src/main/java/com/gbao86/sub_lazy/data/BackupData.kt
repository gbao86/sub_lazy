package com.gbao86.sub_lazy.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val version: Int = 1,
    val timestamp: Long,
    val subscriptions: List<Subscription>,
    val paymentHistory: List<PaymentHistory>,
    val sharedMembers: List<SharedMember>
)
