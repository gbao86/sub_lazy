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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gbao86.sub_lazy.MainActivity
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.AppDatabase
import com.gbao86.sub_lazy.ui.CurrencyFormatter

import com.gbao86.sub_lazy.data.model.SubscriptionCategory
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "NotificationWorker"

    override suspend fun doWork(): Result {
        return try {
            val subscriptionId = inputData.getLong("subscriptionId", -1L)
            if (subscriptionId == -1L) return Result.failure()

            val db = AppDatabase.getDatabase(applicationContext)
            val subscription = db.subscriptionDao().getSubscriptionById(subscriptionId) ?: return Result.failure()

            val locale = applicationContext.resources.configuration.locales[0]
            val amountFormatted = CurrencyFormatter.format(subscription.amount, subscription.currency.code, locale)

            val (title, message) = when (subscription.category) {
                SubscriptionCategory.ANNIVERSARY -> {
                    val t = applicationContext.getString(R.string.notification_anniversary_title)
                    val m = applicationContext.getString(R.string.notification_anniversary_message, subscription.name)
                    t to m
                }
                SubscriptionCategory.FAMILY -> {
                    val t = applicationContext.getString(R.string.notification_family_title)
                    val m = applicationContext.getString(R.string.notification_family_message, subscription.name)
                    t to m
                }
                SubscriptionCategory.TRIAL -> {
                    val t = applicationContext.getString(R.string.notification_trial_title)
                    val m = applicationContext.getString(R.string.notification_trial_message, subscription.name, amountFormatted)
                    t to m
                }
                SubscriptionCategory.NOTES -> {
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

            sendNotification(subscriptionId.toInt(), title, message)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "NotificationWorker failed", e)
            Result.retry()
        }
    }

    private fun sendNotification(notificationId: Int, title: String, message: String) {
        val channelId = "renewal_reminder_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Fallback channel creation in case app class didn't initialize yet
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

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            setPackage("com.gbao86.sub_lazy")
            setClassName("com.gbao86.sub_lazy", "com.gbao86.sub_lazy.MainActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // TODO: Use dedicated small notification icon when available
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // codeql[java/android/implicit-pendingintents]
        notificationManager.notify(notificationId, notification)
    }
}
