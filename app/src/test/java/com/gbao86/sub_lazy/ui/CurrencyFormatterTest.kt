package com.gbao86.sub_lazy.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class CurrencyFormatterTest {

    @Test
    fun convert_sameCurrency_returnsOriginalAmount() {
        val amount = 100.0
        val result = CurrencyFormatter.convert(amount, "USD", "USD")
        assertEquals(100.0, result, 0.0)
    }

    @Test
    fun format_vietnamLocale_formatsToVND() {
        // ExchangeRateManager.getAppContext() will be null in unit test, 
        // it will fallback to ExchangeRateManager.getDefaultRate()
        val formatted = CurrencyFormatter.format(100.0, "VND", Locale("vi", "VN"))
        
        // Assert it contains typical VND symbols
        assertTrue("Formatted string should contain ₫ or VND, but was $formatted", 
            formatted.contains("₫") || formatted.contains("VND"))
    }

    @Test
    fun format_usLocale_formatsToUSD() {
        val formatted = CurrencyFormatter.format(100.0, "USD", Locale.US)
        
        // Assert it contains $
        assertTrue("Formatted string should contain $, but was $formatted", 
            formatted.contains("$"))
    }

    @Test
    fun format_japanLocale_formatsToJPY() {
        val formatted = CurrencyFormatter.format(100.0, "JPY", Locale("ja", "JP"))
        
        // Assert it contains JP¥
        assertTrue("Formatted string should contain JP¥, but was $formatted", 
            formatted.contains("JP¥") || formatted.contains("￥"))
    }
}
