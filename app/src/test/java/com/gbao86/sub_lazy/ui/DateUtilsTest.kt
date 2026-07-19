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

import com.gbao86.sub_lazy.data.model.BillingCycle
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class DateUtilsTest {

    @Test
    fun `getDaysLeft returns 0 for past dates`() {
        val pastDate = System.currentTimeMillis() - 86400000L * 5 // 5 days ago
        val result = DateUtils.getDaysLeft(pastDate)
        assertEquals(0L, result)
    }

    @Test
    fun `getDaysLeft returns positive for future dates`() {
        val futureDate = System.currentTimeMillis() + 86400000L * 10 // 10 days from now
        val result = DateUtils.getDaysLeft(futureDate)
        assertTrue(result > 0)
    }

    @Test
    fun `getNextBillingDate daily adds 1 day`() {
        val now = System.currentTimeMillis()
        val next = DateUtils.getNextBillingDate(now, BillingCycle.DAILY)
        
        val nowDate = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        val nextDate = Instant.ofEpochMilli(next).atZone(ZoneId.systemDefault()).toLocalDate()
        
        assertEquals(nowDate.plusDays(1), nextDate)
    }

    @Test
    fun `getNextBillingDate weekly adds 7 days`() {
        val now = System.currentTimeMillis()
        val next = DateUtils.getNextBillingDate(now, BillingCycle.WEEKLY)
        
        val nowDate = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        val nextDate = Instant.ofEpochMilli(next).atZone(ZoneId.systemDefault()).toLocalDate()
        
        assertEquals(nowDate.plusWeeks(1), nextDate)
    }

    @Test
    fun `getNextBillingDate monthly adds 1 month`() {
        val now = System.currentTimeMillis()
        val next = DateUtils.getNextBillingDate(now, BillingCycle.MONTHLY)
        
        val nowDate = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        val nextDate = Instant.ofEpochMilli(next).atZone(ZoneId.systemDefault()).toLocalDate()
        
        assertEquals(nowDate.plusMonths(1), nextDate)
    }

    @Test
    fun `getNextBillingDate every 3 months adds 3 months`() {
        val now = System.currentTimeMillis()
        val next = DateUtils.getNextBillingDate(now, BillingCycle.EVERY_3_MONTHS)
        
        val nowDate = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        val nextDate = Instant.ofEpochMilli(next).atZone(ZoneId.systemDefault()).toLocalDate()
        
        assertEquals(nowDate.plusMonths(3), nextDate)
    }

    @Test
    fun `getNextBillingDate every 6 months adds 6 months`() {
        val now = System.currentTimeMillis()
        val next = DateUtils.getNextBillingDate(now, BillingCycle.EVERY_6_MONTHS)
        
        val nowDate = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        val nextDate = Instant.ofEpochMilli(next).atZone(ZoneId.systemDefault()).toLocalDate()
        
        assertEquals(nowDate.plusMonths(6), nextDate)
    }

    @Test
    fun `getNextBillingDate yearly adds 1 year`() {
        val now = System.currentTimeMillis()
        val next = DateUtils.getNextBillingDate(now, BillingCycle.YEARLY)
        
        val nowDate = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
        val nextDate = Instant.ofEpochMilli(next).atZone(ZoneId.systemDefault()).toLocalDate()
        
        assertEquals(nowDate.plusYears(1), nextDate)
    }

    @Test
    fun `getNextBillingDate always returns future date`() {
        val now = System.currentTimeMillis()
        BillingCycle.entries.forEach { cycle ->
            val next = DateUtils.getNextBillingDate(now, cycle)
            assertTrue("Cycle $cycle should return future date", next > now)
        }
    }
}
