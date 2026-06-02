/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

Unauthorized copying of this file, via any medium, is strictly prohibited.
Proprietary and confidential.
This source code is provided for reference purposes only and may not be copied, 
modified, or distributed without the express written permission of the author.
*/

package com.gbao86.sub_lazy.ui

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private const val USD_TO_VND = 25400.0

    fun getFormatter(locale: Locale): NumberFormat {
        return if (locale.language == "vi") {
            NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        } else {
            NumberFormat.getCurrencyInstance(Locale.US)
        }
    }

    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) return amount
        return if (fromCurrency.equals("USD", ignoreCase = true) && toCurrency.equals("VND", ignoreCase = true)) {
            amount * USD_TO_VND
        } else if (fromCurrency.equals("VND", ignoreCase = true) && toCurrency.equals("USD", ignoreCase = true)) {
            amount / USD_TO_VND
        } else {
            amount
        }
    }

    fun format(amount: Double, sourceCurrency: String, locale: Locale): String {
        val targetCurrency = if (locale.language == "vi") "VND" else "USD"
        val convertedAmount = convert(amount, sourceCurrency, targetCurrency)
        return if (targetCurrency == "VND") {
            NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(convertedAmount)
        } else {
            NumberFormat.getCurrencyInstance(Locale.US).format(convertedAmount)
        }
    }
}
