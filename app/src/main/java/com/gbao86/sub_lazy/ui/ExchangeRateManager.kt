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

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val PREFS_NAME = "exchange_rates_pref"
        private const val TAG = "ExchangeRateManager"
        
        private val DEFAULT_RATES = mapOf(
            "USD" to 1.0,
            "VND" to 25400.0,
            "CNY" to 7.24,
            "THB" to 36.5,
            "EUR" to 0.92,
            "JPY" to 156.0,
            "KRW" to 1370.0
        )
        
        @Volatile
        var instance: ExchangeRateManager? = null
            internal set
            
        fun getDefaultRate(currency: String): Double {
            return DEFAULT_RATES[currency.uppercase()] ?: 1.0
        }
        
        fun getTargetCurrencyForLocale(locale: Locale): String {
            return when (locale.language) {
                "vi" -> "VND"
                "zh" -> "CNY"
                "th" -> "THB"
                "es" -> "EUR"
                "ja" -> "JPY"
                "ko" -> "KRW"
                "fr" -> "EUR"
                else -> "USD"
            }
        }
    }
    
    fun getRate(currency: String): Double {
        val uppercaseCurrency = currency.uppercase()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultRate = DEFAULT_RATES[uppercaseCurrency] ?: 1.0
        val rateStr = prefs.getString(uppercaseCurrency, defaultRate.toString())
        return rateStr?.toDoubleOrNull() ?: defaultRate
    }
    
    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        val from = fromCurrency.uppercase()
        val to = toCurrency.uppercase()
        if (from == to) return amount

        val rateFrom = getRate(from)
        val rateTo = getRate(to)

        return (amount / rateFrom) * rateTo
    }
    
    suspend fun fetchRates() {
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://open.er-api.com/v6/latest/USD")
                    .build()
                    
                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val jsonObject = JSONObject(responseBody)
                        if (jsonObject.getString("result") == "success") {
                            val ratesJson = jsonObject.getJSONObject("rates")
                            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                            val editor = prefs.edit()
                            
                            DEFAULT_RATES.keys.forEach { currency ->
                                if (ratesJson.has(currency)) {
                                    val rate = ratesJson.getDouble(currency)
                                    editor.putString(currency, rate.toString())
                                }
                            }
                            editor.putLong("last_updated", System.currentTimeMillis())
                            editor.apply()
                            Log.d(TAG, "Exchange rates updated successfully from API.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch exchange rates: ${e.message}")
            }
        }
    }
}
