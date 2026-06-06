/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.
*/

package com.gbao86.sub_lazy.ui

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

object ExchangeRateManager {
    private const val PREFS_NAME = "exchange_rates_pref"
    private const val TAG = "ExchangeRateManager"

    // Default hardcoded exchange rates relative to USD (1 USD = X Currency)
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
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        if (applicationContext == null) {
            applicationContext = context.applicationContext
        }
    }

    fun getAppContext(): Context? = applicationContext

    fun getDefaultRate(currency: String): Double {
        return DEFAULT_RATES[currency.uppercase()] ?: 1.0
    }

    fun fetchRates(context: Context) {
        initialize(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val urlConnection = URL("https://open.er-api.com/v6/latest/USD").openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connectTimeout = 8000
                urlConnection.readTimeout = 8000
                
                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonObject = JSONObject(response.toString())
                    if (jsonObject.getString("result") == "success") {
                        val ratesJson = jsonObject.getJSONObject("rates")
                        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val editor = prefs.edit()
                        
                        // Save rates
                        DEFAULT_RATES.keys.forEach { currency ->
                            if (ratesJson.has(currency)) {
                                val rate = ratesJson.getDouble(currency)
                                editor.putFloat(currency, rate.toFloat())
                            }
                        }
                        editor.putLong("last_updated", System.currentTimeMillis())
                        editor.apply()
                        Log.d(TAG, "Exchange rates updated successfully from API.")
                    }
                }
                urlConnection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch exchange rates: ${e.message}")
            }
        }
    }

    fun getRate(context: Context, currency: String): Double {
        initialize(context)
        val uppercaseCurrency = currency.uppercase()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val defaultRate = DEFAULT_RATES[uppercaseCurrency] ?: 1.0
        return prefs.getFloat(uppercaseCurrency, defaultRate.toFloat()).toDouble()
    }

    fun convert(context: Context, amount: Double, fromCurrency: String, toCurrency: String): Double {
        val from = fromCurrency.uppercase()
        val to = toCurrency.uppercase()
        if (from == to) return amount

        val rateFrom = getRate(context, from)
        val rateTo = getRate(context, to)

        // Convert from -> USD -> to
        val amountInUsd = amount / rateFrom
        return amountInUsd * rateTo
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
