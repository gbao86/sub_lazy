/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational, 
and non-commercial purposes. 
Commercial exploitation, sale, or distribution of this software or any derivative works 
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy.worker

import android.content.Context
import androidx.work.*
import com.gbao86.sub_lazy.data.Subscription
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    fun scheduleNotification(subscription: Subscription) {
        val workManager = WorkManager.getInstance(context)

        // Calculate delay based on due date and category
        val delay = calculateDelay(subscription.nextBillingDate, subscription.category)
        
        if (delay <= 0) return // Already past or too close

        val inputData = workDataOf("subscriptionId" to subscription.id)

        val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag("sub_${subscription.id}")
            .build()

        workManager.enqueueUniqueWork(
            "sub_${subscription.id}",
            ExistingWorkPolicy.REPLACE,
            notificationRequest
        )
    }

    fun cancelNotification(subscriptionId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("sub_$subscriptionId")
    }

    private fun calculateDelay(nextBillingDate: Long, category: String): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            timeInMillis = nextBillingDate
            if (category in listOf("Anniversary", "Family", "Trial", "Notes")) {
                // Alert exactly on the day of the event/deadline at 9:00 AM
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            } else {
                // Alert 2 days before for financial/subscription renewals at 9:00 AM
                add(Calendar.DAY_OF_YEAR, -2)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
        }

        val delay = target.timeInMillis - now.timeInMillis
        return if (delay > 0) delay else 0
    }
}
