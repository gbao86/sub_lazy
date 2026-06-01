package com.gbao86.sub_lazy.worker

import android.content.Context
import androidx.work.*
import com.gbao86.sub_lazy.data.Subscription
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    fun scheduleNotification(subscription: Subscription) {
        val workManager = WorkManager.getInstance(context)

        // Calculate delay: 2 days before renewal at 9:00 AM
        val delay = calculateDelay(subscription.nextBillingDate)
        
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

    private fun calculateDelay(nextBillingDate: Long): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            timeInMillis = nextBillingDate
            add(Calendar.DAY_OF_YEAR, -2) // 2 days before
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val delay = target.timeInMillis - now.timeInMillis
        return if (delay > 0) delay else 0
    }
}
