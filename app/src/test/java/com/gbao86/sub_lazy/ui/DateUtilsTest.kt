package com.gbao86.sub_lazy.ui

import com.gbao86.sub_lazy.data.model.BillingCycle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DateUtilsTest {

    @Test
    fun getDaysLeft_futureDate_returnsCorrectDays() {
        // Set future date exactly 5 days from today
        val today = LocalDate.now(ZoneId.systemDefault())
        val futureDate = today.plusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val daysLeft = DateUtils.getDaysLeft(futureDate)
        assertEquals(5L, daysLeft)
    }

    @Test
    fun getDaysLeft_pastDate_returnsZero() {
        val today = LocalDate.now(ZoneId.systemDefault())
        val pastDate = today.minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val daysLeft = DateUtils.getDaysLeft(pastDate)
        assertEquals(0L, daysLeft)
    }

    @Test
    fun getDaysLeft_today_returnsZero() {
        val today = LocalDate.now(ZoneId.systemDefault())
        val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val daysLeft = DateUtils.getDaysLeft(todayMillis)
        assertEquals(0L, daysLeft)
    }

    @Test
    fun getNextBillingDate_monthly_addsOneMonth() {
        val currentDate = Instant.parse("2026-06-15T12:00:00Z").toEpochMilli()
        val nextDate = DateUtils.getNextBillingDate(currentDate, BillingCycle.MONTHLY)
        
        // Convert back to Instant to verify
        val nextInstant = Instant.ofEpochMilli(nextDate).atZone(ZoneId.systemDefault())
        val currentInstant = Instant.ofEpochMilli(currentDate).atZone(ZoneId.systemDefault())
        
        assertEquals(currentInstant.plusMonths(1).toInstant().toEpochMilli(), nextDate)
    }

    @Test
    fun getNextBillingDate_yearly_addsOneYear() {
        val currentDate = Instant.parse("2026-06-15T12:00:00Z").toEpochMilli()
        val nextDate = DateUtils.getNextBillingDate(currentDate, BillingCycle.YEARLY)
        
        val currentInstant = Instant.ofEpochMilli(currentDate).atZone(ZoneId.systemDefault())
        assertEquals(currentInstant.plusYears(1).toInstant().toEpochMilli(), nextDate)
    }
    
    @Test
    fun getNextBillingDate_weekly_addsOneWeek() {
        val currentDate = Instant.parse("2026-06-15T12:00:00Z").toEpochMilli()
        val nextDate = DateUtils.getNextBillingDate(currentDate, BillingCycle.WEEKLY)
        
        val currentInstant = Instant.ofEpochMilli(currentDate).atZone(ZoneId.systemDefault())
        assertEquals(currentInstant.plusWeeks(1).toInstant().toEpochMilli(), nextDate)
    }
}
