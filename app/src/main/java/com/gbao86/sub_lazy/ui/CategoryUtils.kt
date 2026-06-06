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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryUtils {
    fun getCategoryIcon(category: String): ImageVector {
        return when (category) {
            "Entertainment" -> Icons.Rounded.Movie
            "Utilities" -> Icons.Rounded.Bolt
            "Work" -> Icons.Rounded.Laptop
            "Cloud" -> Icons.Rounded.Cloud
            "Music" -> Icons.Rounded.MusicNote
            "Food" -> Icons.Rounded.Restaurant
            "Finance" -> Icons.Rounded.AccountBalanceWallet
            "Anniversary" -> Icons.Rounded.Favorite
            "Family" -> Icons.Rounded.People
            "Trial" -> Icons.Rounded.Timer
            "Notes" -> Icons.Rounded.Description
            else -> Icons.Rounded.Category
        }
    }

    fun getIconForName(name: String, category: String = ""): ImageVector {
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
