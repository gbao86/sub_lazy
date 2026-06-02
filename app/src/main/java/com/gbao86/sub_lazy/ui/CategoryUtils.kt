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
