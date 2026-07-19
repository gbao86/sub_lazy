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

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) return amount
        val mgr = ExchangeRateManager.instance
        return if (mgr != null) {
            mgr.convert(amount, fromCurrency, toCurrency)
        } else {
            val rateFrom = ExchangeRateManager.getDefaultRate(fromCurrency)
            val rateTo = ExchangeRateManager.getDefaultRate(toCurrency)
            (amount / rateFrom) * rateTo
        }
    }

    fun format(amount: Double, sourceCurrency: String, locale: Locale): String {
        val targetCurrency = ExchangeRateManager.getTargetCurrencyForLocale(locale)
        val convertedAmount = convert(amount, sourceCurrency, targetCurrency)
        
        val formatLocale = when (targetCurrency) {
            "VND" -> Locale("vi", "VN")
            "CNY" -> Locale("zh", "CN")
            "THB" -> Locale("th", "TH")
            "EUR" -> if (locale.language == "es") Locale("es", "ES") else Locale("fr", "FR")
            "JPY" -> Locale("ja", "JP")
            "KRW" -> Locale("ko", "KR")
            else -> Locale.US
        }
        
        return try {
            val formatter = NumberFormat.getCurrencyInstance(formatLocale)
            var formatted = formatter.format(convertedAmount)
            if (targetCurrency == "CNY") {
                formatted = formatted.replace("¥", "CN¥").replace("￥", "CN¥")
            } else if (targetCurrency == "JPY") {
                formatted = formatted.replace("¥", "JP¥").replace("￥", "JP¥")
            }
            formatted
        } catch (e: Exception) {
            "$targetCurrency ${String.format(Locale.US, "%.2f", convertedAmount)}"
        }
    }
}
