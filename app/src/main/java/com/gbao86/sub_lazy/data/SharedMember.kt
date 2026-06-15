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

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shared_members",
    foreignKeys = [
        ForeignKey(
            entity = Subscription::class,
            parentColumns = ["id"],
            childColumns = ["subscriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subscriptionId"])]
)
data class SharedMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subscriptionId: Long = 0,
    val name: String,
    val amount: Double,
    val hasPaid: Boolean = false,
    val phone: String? = null
) {
    companion object {
        /**
         * Parses the legacy semicolon-delimited JSON string format.
         * Used only for migrating old data from sharedMembersJson column.
         */
        fun parseLegacyJson(json: String?, subscriptionId: Long): List<SharedMember> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                json.split(";").filter { it.isNotBlank() }.map { memberStr ->
                    val parts = memberStr.split(":")
                    SharedMember(
                        subscriptionId = subscriptionId,
                        name = Uri.decode(parts[0]),
                        amount = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
                        hasPaid = parts.getOrNull(2)?.toBoolean() ?: false,
                        phone = parts.getOrNull(3)?.let { Uri.decode(it) }?.takeIf { it.isNotBlank() }
                    )
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
