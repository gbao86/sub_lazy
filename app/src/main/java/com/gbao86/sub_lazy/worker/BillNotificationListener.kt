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
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.gbao86.sub_lazy.MainActivity
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import java.util.Locale
import java.util.regex.Pattern

class BillNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getString("android.text") ?: ""

        val combinedText = "$title $text".lowercase(Locale.getDefault())

        // Check for payment-related keywords
        val isPayment = combinedText.contains("thanh toan") ||
                combinedText.contains("tru tien") ||
                combinedText.contains("charged") ||
                combinedText.contains("debited") ||
                combinedText.contains("payment") ||
                combinedText.contains("successful") ||
                combinedText.contains("giao dich") ||
                combinedText.contains("gia han") ||
                combinedText.contains("renewed")

        if (!isPayment) return

        // 1. Identify Service Name
        val serviceName = detectService(combinedText) ?: return // If no subscription matching, ignore

        // 2. Identify Amount
        val amount = detectAmount(combinedText) ?: 0.0
        if (amount <= 0.0) return

        // Show a local notification to alert user and pre-fill Add Screen
        showDetectedNotification(serviceName, amount)
    }

    private fun detectService(text: String): String? {
        val services = listOf(
            "netflix", "spotify", "youtube", "icloud", "google", "microsoft", "office", "apple",
            "fpt play", "vieon", "k+", "netnam", "viettel", "vnpt", "fpt telecom", "aws", "github",
            "copilot", "chatgpt", "openai", "momo", "grab", "mobi", "vina"
        )
        for (service in services) {
            if (text.contains(service)) {
                return when (service) {
                    "fpt play" -> "FPT Play"
                    "vieon" -> "VieON"
                    "k+" -> "K+"
                    "netnam" -> "Netnam"
                    "viettel" -> "Viettel"
                    "vnpt" -> "VNPT"
                    "fpt telecom" -> "FPT Telecom"
                    "aws" -> "AWS"
                    "github" -> "GitHub"
                    "copilot" -> "GitHub Copilot"
                    "chatgpt" -> "ChatGPT Plus"
                    "openai" -> "OpenAI API"
                    "netflix" -> "Netflix"
                    "spotify" -> "Spotify"
                    "youtube" -> "YouTube Premium"
                    "icloud" -> "iCloud"
                    "google" -> "Google One"
                    "microsoft" -> "Microsoft 365"
                    "office" -> "Microsoft 365"
                    "apple" -> "Apple Services"
                    "momo" -> "MoMo Billing"
                    "grab" -> "Grab Subscription"
                    "mobi" -> "Mobifone"
                    "vina" -> "Vinaphone"
                    else -> service.replaceFirstChar { it.uppercase() }
                }
            }
        }
        return null
    }

    private fun detectAmount(text: String): Double? {
        // Regex for VND: 100.000, 100,000, 100000 followed by d, đ, vnd, vnđ
        val vndPattern = Pattern.compile("(\\d{1,3}(?:[.,]\\d{3})+)\\s*(?:đ|d|vnd|vnđ|vnds)")
        val usdPattern = Pattern.compile("(?:\\$|usd)\\s*(\\d+(?:\\.\\d{2})?)")

        var matcher = vndPattern.matcher(text)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(".", "")?.replace(",", "")
            return amountStr?.toDoubleOrNull()
        }

        matcher = usdPattern.matcher(text)
        if (matcher.find()) {
            val amountStr = matcher.group(1)
            return amountStr?.toDoubleOrNull()
        }

        // Search for any sequence of numbers that might represent money
        val genericPattern = Pattern.compile("(\\d{1,3}(?:[.,]\\d{3}){1,2})")
        matcher = genericPattern.matcher(text)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(".", "")?.replace(",", "")
            return amountStr?.toDoubleOrNull()
        }

        return null
    }

    private fun showDetectedNotification(serviceName: String, amount: Double) {
        val channelId = "detected_bills"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.notification_detected_channel_name)
            val channelDesc = getString(R.string.notification_detected_channel_desc)
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDesc
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Format amount
        val locale = resources.configuration.locales[0]
        val amountFormatted = CurrencyFormatter.format(amount, "VND", locale)

        val message = getString(R.string.notification_detected_message, serviceName, amountFormatted)

        // PendingIntent to launch MainActivity with pre-fill parameters
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("is_prefilled", true)
            putExtra("prefill_name", serviceName)
            putExtra("prefill_amount", amount)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.notification_detected_title))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(200, notification)
    }
}
