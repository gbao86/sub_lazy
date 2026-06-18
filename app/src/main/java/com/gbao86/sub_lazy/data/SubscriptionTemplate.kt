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

import android.content.Context
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
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

@JsonClass(generateAdapter = true)
data class TemplatesResponse(
    val digital: List<SubscriptionTemplate>,
    val lifestyle: List<SubscriptionTemplate>
)

object SubscriptionTemplates {
    private var cachedDigital: List<SubscriptionTemplate>? = null
    private var cachedLifestyle: List<SubscriptionTemplate>? = null

    private fun loadTemplates(context: Context) {
        if (cachedDigital != null && cachedLifestyle != null) return
        try {
            val json = context.assets.open("templates.json").bufferedReader().use { it.readText() }
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(TemplatesResponse::class.java)
            val response = adapter.fromJson(json)
            cachedDigital = response?.digital ?: emptyList()
            cachedLifestyle = response?.lifestyle ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            cachedDigital = emptyList()
            cachedLifestyle = emptyList()
        }
    }

    fun getDigitalTemplates(context: Context): List<SubscriptionTemplate> {
        loadTemplates(context)
        return cachedDigital ?: emptyList()
    }

    fun getLifestyleTemplates(context: Context): List<SubscriptionTemplate> {
        loadTemplates(context)
        return cachedLifestyle ?: emptyList()
    }
}
