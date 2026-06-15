/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational, 
and non-commercial purposes. 
Commercial exploitation, sale, or distribution of this software or any derivative works 
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy.ui

import com.gbao86.sub_lazy.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import com.gbao86.sub_lazy.data.model.SubscriptionCategory

object CategoryUtils {
    fun getCategoryIcon(category: SubscriptionCategory): ImageVector {
        return when (category) {
            SubscriptionCategory.ENTERTAINMENT -> Icons.Rounded.Movie
            SubscriptionCategory.UTILITIES -> Icons.Rounded.Bolt
            SubscriptionCategory.WORK -> Icons.Rounded.Laptop
            SubscriptionCategory.CLOUD -> Icons.Rounded.Cloud
            SubscriptionCategory.MUSIC -> Icons.Rounded.MusicNote
            SubscriptionCategory.FOOD -> Icons.Rounded.Restaurant
            SubscriptionCategory.FINANCE -> Icons.Rounded.AccountBalanceWallet
            SubscriptionCategory.ANNIVERSARY -> Icons.Rounded.Favorite
            SubscriptionCategory.FAMILY -> Icons.Rounded.People
            SubscriptionCategory.TRIAL -> Icons.Rounded.Timer
            SubscriptionCategory.NOTES -> Icons.Rounded.Description
            else -> Icons.Rounded.Category
        }
    }

    @Composable
    fun getCategoryDisplayName(category: SubscriptionCategory): String {
        val resId = when (category) {
            SubscriptionCategory.ENTERTAINMENT -> R.string.category_entertainment
            SubscriptionCategory.UTILITIES     -> R.string.category_utilities
            SubscriptionCategory.WORK          -> R.string.category_work
            SubscriptionCategory.CLOUD         -> R.string.category_cloud
            SubscriptionCategory.MUSIC         -> R.string.category_music
            SubscriptionCategory.FOOD          -> R.string.category_food
            SubscriptionCategory.FINANCE       -> R.string.category_finance
            SubscriptionCategory.ANNIVERSARY   -> R.string.category_anniversary
            SubscriptionCategory.FAMILY        -> R.string.category_family
            SubscriptionCategory.TRIAL         -> R.string.category_trial
            SubscriptionCategory.NOTES         -> R.string.category_notes
            else            -> R.string.category_other
        }
        return stringResource(resId)
    }

    fun getIconForName(name: String, category: SubscriptionCategory = SubscriptionCategory.OTHER): ImageVector {
        val lower = name.lowercase()
        return when {
            // Digital Services
            lower.contains("netflix") -> Icons.Rounded.Tv
            lower.contains("spotify") || lower.contains("nhạc") || lower.contains("music") -> Icons.Rounded.Headphones
            lower.contains("youtube") || lower.contains("ytb") -> Icons.Rounded.Subscriptions
            lower.contains("icloud") || lower.contains("drive") || lower.contains("cloud") || lower.contains("dropbox") -> Icons.Rounded.Cloud
            lower.contains("wifi") || lower.contains("internet") || lower.contains("mạng") -> Icons.Rounded.Wifi
            lower.contains("chatgpt") || lower.contains("gpt") || lower.contains("openai") || lower.contains("claude") || lower.contains("gemini") -> Icons.Rounded.SmartToy
            
            // Real Life & Maintenance
            lower.contains("dầu") || lower.contains("nhớt") || lower.contains("xe máy") || lower.contains("motor") || lower.contains("vespa") || lower.contains("honda") -> Icons.Rounded.TwoWheeler
            lower.contains("thú cưng") || lower.contains("chó") || lower.contains("mèo") || lower.contains("pet") || lower.contains("giun") -> Icons.Rounded.Pets
            lower.contains("lọc nước") || lower.contains("nước uống") || lower.contains("aquafina") -> Icons.Rounded.WaterDrop
            lower.contains("tiền nhà") || lower.contains("thuê nhà") || lower.contains("phòng trọ") -> Icons.Rounded.Home
            lower.contains("chung cư") || lower.contains("phí quản lý") || lower.contains("apart") -> Icons.Rounded.Apartment
            lower.contains("răng") || lower.contains("nha khoa") || lower.contains("dentist") || lower.contains("y tế") || lower.contains("bác sĩ") -> Icons.Rounded.MedicalServices
            lower.contains("gym") || lower.contains("fitness") || lower.contains("thể hình") || lower.contains("yoga") || lower.contains("tập") -> Icons.Rounded.FitnessCenter
            lower.contains("bơi") || lower.contains("pool") -> Icons.Rounded.Pool
            lower.contains("bảo hiểm") || lower.contains("insurance") -> Icons.Rounded.Shield
            lower.contains("điện") || lower.contains("tiền điện") || lower.contains("power") -> Icons.Rounded.Bolt
            lower.contains("nước sinh hoạt") || lower.contains("tiền nước") -> Icons.Rounded.WaterDrop
            lower.contains("học") || lower.contains("khóa học") || lower.contains("school") || lower.contains("coursera") || lower.contains("udemy") -> Icons.Rounded.School
            lower.contains("ô tô") || lower.contains("car") || lower.contains("đăng kiểm") || lower.contains("lốp") -> Icons.Rounded.DirectionsCar
            
            // Fallback to Category Icon
            else -> getCategoryIcon(category)
        }
    }
}

fun String.toComposeColor(fallback: Color = Color.Gray): Color {
    return try {
        Color(this.toColorInt())
    } catch (e: Exception) {
        fallback
    }
}
