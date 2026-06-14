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

import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory

data class SubscriptionTemplate(
    val name: String,
    val amount: Double,
    val category: SubscriptionCategory,
    val colorHex: String,
    val cycle: BillingCycle = BillingCycle.MONTHLY,
    val isKmBased: Boolean = false,
    val targetIntervalKm: Double? = null,
    val dailyAverageKm: Double? = null,
    val bankName: String? = null,
    val bankAccount: String? = null,
    val bankAccountHolder: String? = null
)

object SubscriptionTemplates {
    val digitalTemplates = listOf(
        SubscriptionTemplate("Netflix", 260000.0, SubscriptionCategory.ENTERTAINMENT, "#E50914"),
        SubscriptionTemplate("Spotify", 59000.0, SubscriptionCategory.MUSIC, "#1DB954"),
        SubscriptionTemplate("YouTube Premium", 79000.0, SubscriptionCategory.ENTERTAINMENT, "#FF0000"),
        SubscriptionTemplate("iCloud 50GB", 19000.0, SubscriptionCategory.CLOUD, "#007AFF"),
        SubscriptionTemplate("Net/Wifi", 250000.0, SubscriptionCategory.UTILITIES, "#6366F1"),
        SubscriptionTemplate("ChatGPT Plus", 490000.0, SubscriptionCategory.WORK, "#10a37f"),
        SubscriptionTemplate("Galaxy Play VIP", 100000.0, SubscriptionCategory.ENTERTAINMENT, "#22D3EE"),
        SubscriptionTemplate("VieON VIP", 49000.0, SubscriptionCategory.ENTERTAINMENT, "#10B981"),
        SubscriptionTemplate("Clip TV", 50000.0, SubscriptionCategory.ENTERTAINMENT, "#F59E0B"),
        SubscriptionTemplate("Gói cước 4G V120", 120000.0, SubscriptionCategory.UTILITIES, "#E11D48"),
        SubscriptionTemplate("Canva Pro", 149000.0, SubscriptionCategory.WORK, "#00C4CC"),
        SubscriptionTemplate("Elsa Speak", 150000.0, SubscriptionCategory.WORK, "#6366F1", cycle = BillingCycle.YEARLY)
    )

    val lifestyleTemplates = listOf(
        SubscriptionTemplate("Thay dầu xe máy", 120000.0, SubscriptionCategory.UTILITIES, "#F59E0B", cycle = BillingCycle.EVERY_6_MONTHS),
        SubscriptionTemplate("Tẩy giun thú cưng", 50000.0, SubscriptionCategory.FAMILY, "#8B5CF6", cycle = BillingCycle.EVERY_3_MONTHS),
        SubscriptionTemplate("Thay lõi lọc nước", 300000.0, SubscriptionCategory.UTILITIES, "#06B6D4", cycle = BillingCycle.EVERY_6_MONTHS),
        SubscriptionTemplate("Tiền nhà hàng tháng", 3500000.0, SubscriptionCategory.FINANCE, "#10B981", cycle = BillingCycle.MONTHLY),
        SubscriptionTemplate("Đóng phí chung cư", 500000.0, SubscriptionCategory.FINANCE, "#6366F1", cycle = BillingCycle.MONTHLY),
        SubscriptionTemplate("Khám răng định kỳ", 200000.0, SubscriptionCategory.FAMILY, "#F43F5E", cycle = BillingCycle.EVERY_6_MONTHS),
        SubscriptionTemplate("Học phí tiếng Anh", 2500000.0, SubscriptionCategory.WORK, "#7C3AED", cycle = BillingCycle.MONTHLY),
        SubscriptionTemplate("Thẻ tập Gym", 600000.0, SubscriptionCategory.UTILITIES, "#F43F5E", cycle = BillingCycle.MONTHLY),
        SubscriptionTemplate("Cắt tóc định kỳ", 100000.0, SubscriptionCategory.OTHER, "#94A3B8", cycle = BillingCycle.MONTHLY)
    )
}
