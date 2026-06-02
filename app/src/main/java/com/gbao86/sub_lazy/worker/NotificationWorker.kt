/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

Unauthorized copying of this file, via any medium, is strictly prohibited.
Proprietary and confidential.
This source code is provided for reference purposes only and may not be copied, 
modified, or distributed without the express written permission of the author.
*/

package com.gbao86.sub_lazy.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.AppDatabase
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import java.util.*

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val subscriptionId = inputData.getLong("subscriptionId", -1L)
        if (subscriptionId == -1L) return Result.failure()

        val db = AppDatabase.getDatabase(applicationContext)
        val subscription = db.subscriptionDao().getSubscriptionById(subscriptionId) ?: return Result.failure()

        val locale = applicationContext.resources.configuration.locales[0]
        val amountFormatted = CurrencyFormatter.format(subscription.amount, subscription.currency, locale)

        val (title, message) = when (subscription.category) {
            "Anniversary" -> {
                val t = applicationContext.getString(R.string.notification_anniversary_title)
                val m = applicationContext.getString(R.string.notification_anniversary_message, subscription.name)
                t to m
            }
            "Family" -> {
                val t = applicationContext.getString(R.string.notification_family_title)
                val m = applicationContext.getString(R.string.notification_family_message, subscription.name)
                t to m
            }
            "Trial" -> {
                val t = applicationContext.getString(R.string.notification_trial_title)
                val m = applicationContext.getString(R.string.notification_trial_message, subscription.name, amountFormatted)
                t to m
            }
            "Notes" -> {
                val t = applicationContext.getString(R.string.notification_notes_title)
                val m = applicationContext.getString(R.string.notification_notes_message, subscription.name)
                t to m
            }
            else -> {
                val t = subscription.name
                val m = applicationContext.getString(
                    R.string.notification_message,
                    subscription.name,
                    subscription.name,
                    amountFormatted
                )
                t to m
            }
        }

        sendNotification(title, message)

        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "billing_alerts"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = applicationContext.getString(R.string.notification_channel_name)
            val channelDesc = applicationContext.getString(R.string.notification_channel_desc)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDesc
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
