package com.gbao86.sub_lazy.ui

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun getFormatter(locale: Locale): NumberFormat {
        return if (locale.language == "vi") {
            NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        } else {
            NumberFormat.getCurrencyInstance(Locale.US)
        }
    }
}
