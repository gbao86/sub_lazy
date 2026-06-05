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

import java.util.concurrent.TimeUnit

object DateUtils {
    fun getDaysLeft(nextBillingDate: Long): Long {
        val diff = nextBillingDate - System.currentTimeMillis()
        return if (diff < 0) 0L else TimeUnit.MILLISECONDS.toDays(diff)
    }

    fun getNextBillingDate(currentDate: Long, cycle: String): Long {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = currentDate }
        when (cycle) {
            "Daily"          -> cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            "Weekly"         -> cal.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            "Monthly"        -> cal.add(java.util.Calendar.MONTH, 1)
            "Every 3 Months" -> cal.add(java.util.Calendar.MONTH, 3)
            "Every 6 Months" -> cal.add(java.util.Calendar.MONTH, 6)
            "Yearly"         -> cal.add(java.util.Calendar.YEAR, 1)
            else             -> cal.add(java.util.Calendar.MONTH, 1)
        }
        return cal.timeInMillis
    }
}
