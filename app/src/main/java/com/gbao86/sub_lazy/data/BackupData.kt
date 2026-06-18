package com.gbao86.sub_lazy.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val version: Int = 1,
    val schemaVersion: Int = 1,
    val appVersion: String? = null,
    val exportSource: String = "MANUAL",
    val timestamp: Long,
    val createdAt: String? = null,
    val device: String? = null,
    val backupType: String? = null,
    val defaultCurrency: String? = null,
    val totalSubscriptions: Int? = null,
    val subscriptions: List<Subscription>,
    val paymentHistory: List<PaymentHistory>,
    val sharedMembers: List<SharedMember>
)
