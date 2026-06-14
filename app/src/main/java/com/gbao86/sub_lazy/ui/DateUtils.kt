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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object DateUtils {
    fun getDaysLeft(nextBillingDate: Long): Long {
        val nextDate = Instant.ofEpochMilli(nextBillingDate).atZone(ZoneId.systemDefault()).toLocalDate()
        val today = LocalDate.now(ZoneId.systemDefault())
        val diff = ChronoUnit.DAYS.between(today, nextDate)
        return if (diff < 0) 0L else diff
    }

    fun getNextBillingDate(currentDate: Long, cycle: BillingCycle): Long {
        val zonedDateTime = Instant.ofEpochMilli(currentDate).atZone(ZoneId.systemDefault())
        val nextZonedDateTime = when (cycle) {
            BillingCycle.DAILY          -> zonedDateTime.plusDays(1)
            BillingCycle.WEEKLY         -> zonedDateTime.plusWeeks(1)
            BillingCycle.MONTHLY        -> zonedDateTime.plusMonths(1)
            BillingCycle.EVERY_3_MONTHS -> zonedDateTime.plusMonths(3)
            BillingCycle.EVERY_6_MONTHS -> zonedDateTime.plusMonths(6)
            BillingCycle.YEARLY         -> zonedDateTime.plusYears(1)
            else                        -> zonedDateTime.plusMonths(1)
        }
        return nextZonedDateTime.toInstant().toEpochMilli()
    }
}
