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

data class SharedMember(
    val name: String,
    val amount: Double,
    val hasPaid: Boolean = false,
    val phone: String? = null
) {
    companion object {
        fun parseMembers(json: String?): List<SharedMember> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                json.split(";").filter { it.isNotBlank() }.map { memberStr ->
                    val parts = memberStr.split(":")
                    SharedMember(
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

        fun serializeMembers(members: List<SharedMember>): String {
            return members.joinToString(";") { member ->
                val encodedName = Uri.encode(member.name)
                val encodedPhone = Uri.encode(member.phone ?: "")
                "$encodedName:${member.amount}:${member.hasPaid}:$encodedPhone"
            }
        }
    }
}
