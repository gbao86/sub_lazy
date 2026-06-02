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
}
