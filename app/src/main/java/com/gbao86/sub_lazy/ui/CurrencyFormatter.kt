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
