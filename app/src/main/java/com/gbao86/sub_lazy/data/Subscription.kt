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
    val iconName: String? = null
)
