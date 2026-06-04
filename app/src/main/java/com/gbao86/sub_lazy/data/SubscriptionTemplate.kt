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

data class SubscriptionTemplate(
    val name: String,
    val amount: Double,
    val category: String,
    val colorHex: String,
    val cycle: String = "Monthly",
    val isKmBased: Boolean = false,
    val targetIntervalKm: Double? = null,
    val dailyAverageKm: Double? = null,
    val bankName: String? = null,
    val bankAccount: String? = null,
    val bankAccountHolder: String? = null
)

object SubscriptionTemplates {
    val digitalTemplates = listOf(
        SubscriptionTemplate("Netflix", 260000.0, "Entertainment", "#E50914"),
        SubscriptionTemplate("Spotify", 59000.0, "Music", "#1DB954"),
        SubscriptionTemplate("YouTube Premium", 79000.0, "Entertainment", "#FF0000"),
        SubscriptionTemplate("iCloud 50GB", 19000.0, "Cloud", "#007AFF"),
        SubscriptionTemplate("Net/Wifi", 250000.0, "Utilities", "#6366F1"),
        SubscriptionTemplate("ChatGPT Plus", 490000.0, "Work", "#10a37f")
    )

    val lifestyleTemplates = listOf(
        SubscriptionTemplate("Thay dầu xe máy", 120000.0, "Utilities", "#F59E0B", cycle = "Every 6 Months"),
        SubscriptionTemplate("Tẩy giun thú cưng", 50000.0, "Family", "#8B5CF6", cycle = "Every 3 Months"),
        SubscriptionTemplate("Thay lõi lọc nước", 300000.0, "Utilities", "#06B6D4", cycle = "Every 6 Months"),
        SubscriptionTemplate("Tiền nhà hàng tháng", 3500000.0, "Finance", "#10B981", cycle = "Monthly"),
        SubscriptionTemplate("Đóng phí chung cư", 500000.0, "Finance", "#6366F1", cycle = "Monthly"),
        SubscriptionTemplate("Khám răng định kỳ", 200000.0, "Family", "#F43F5E", cycle = "Every 6 Months")
    )
}
