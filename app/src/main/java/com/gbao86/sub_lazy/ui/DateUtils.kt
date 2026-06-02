package com.gbao86.sub_lazy.ui

import java.util.concurrent.TimeUnit

object DateUtils {
    fun getDaysLeft(nextBillingDate: Long): Long {
        val diff = nextBillingDate - System.currentTimeMillis()
        return if (diff < 0) 0L else TimeUnit.MILLISECONDS.toDays(diff)
    }
}
