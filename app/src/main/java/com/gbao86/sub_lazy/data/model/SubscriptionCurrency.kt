package com.gbao86.sub_lazy.data.model

enum class SubscriptionCurrency(val symbol: String, val code: String) {
    VND("₫", "VND"),
    USD("$", "USD");

    companion object {
        fun fromCode(code: String): SubscriptionCurrency =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: VND
    }
}
