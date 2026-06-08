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
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.gbao86.sub_lazy.MainActivity
import com.gbao86.sub_lazy.R
import com.gbao86.sub_lazy.data.AppDatabase
import com.gbao86.sub_lazy.data.PaymentHistory
import com.gbao86.sub_lazy.ui.CurrencyFormatter
import com.gbao86.sub_lazy.ui.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
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

        val detectedBalance = detectBalance(combinedText)
        if (detectedBalance != null) {
            val sharedPref = applicationContext.getSharedPreferences("app_prefs", MODE_PRIVATE)
            sharedPref.edit().putFloat("user_balance", detectedBalance.toFloat()).apply()
        }

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

        // Check if subscription exists in local Room DB to auto-mark as paid
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.subscriptionDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val subs = dao.getAllSubscriptions().first()
                val matchedSub = subs.find { it.name.equals(serviceName, ignoreCase = true) }

                if (matchedSub != null) {
                    // Record payment history
                    val record = PaymentHistory(
                        subscriptionId = matchedSub.id,
                        subscriptionName = matchedSub.name,
                        amount = amount,
                        currency = matchedSub.currency,
                        paymentDate = System.currentTimeMillis(),
                        cycle = matchedSub.cycle
                    )
                    dao.insertPaymentHistory(record)

                    // Rollover renewal date
                    if (matchedSub.cycle == "One-time") {
                        dao.deleteSubscription(matchedSub)
                        NotificationScheduler(applicationContext).cancelNotification(matchedSub.id)
                    } else {
                        var currentSub = matchedSub
                        var shouldDelete = false

                        val limit = currentSub.remainingTimes
                        if (limit != null && limit > 0) {
                            val newLimit = limit - 1
                            if (newLimit <= 0) {
                                shouldDelete = true
                            } else {
                                currentSub = currentSub.copy(remainingTimes = newLimit)
                            }
                        }

                        if (shouldDelete) {
                            dao.deleteSubscription(matchedSub)
                            NotificationScheduler(applicationContext).cancelNotification(matchedSub.id)
                        } else {
                            val finalNextDate = DateUtils.getNextBillingDate(currentSub.nextBillingDate, currentSub.cycle)
                            currentSub = currentSub.copy(nextBillingDate = finalNextDate)
                            dao.updateSubscription(currentSub)
                            NotificationScheduler(applicationContext).scheduleNotification(currentSub)
                        }
                    }
                    showAutoPaidNotification(matchedSub.name, amount, matchedSub.currency)
                } else {
                    // Pre-fill screen if it's a new subscription
                    showDetectedNotification(serviceName, amount)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

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

        // Format amount
        val locale = resources.configuration.locales[0]
        val amountFormatted = CurrencyFormatter.format(amount, "VND", locale)

        val message = getString(R.string.notification_detected_message, serviceName, amountFormatted)

        // PendingIntent to launch MainActivity with pre-fill parameters
        val intent = Intent(this, MainActivity::class.java).apply {
            setPackage(packageName)
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

        // codeql[java/android/implicit-pendingintents]
        notificationManager.notify(200, notification)
    }



    private fun showAutoPaidNotification(serviceName: String, amount: Double, currency: String) {
        val channelId = "detected_bills"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val locale = resources.configuration.locales[0]
        val amountFormatted = CurrencyFormatter.format(amount, currency, locale)

        val message = "Đã tự động xác nhận thanh toán hóa đơn $serviceName ($amountFormatted). Chúc bạn lười vui vẻ! 🐱"

        val intent = Intent(this, MainActivity::class.java).apply {
            setPackage(packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tự động thanh toán thành công!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // codeql[java/android/implicit-pendingintents]
        notificationManager.notify(201, notification)
    }

    private fun detectBalance(text: String): Double? {
        val lower = text.lowercase()
        val keyword = listOf("so du vi", "số dư ví", "so du", "số dư", "balance").find { lower.contains(it) } ?: return null
        val startIndex = lower.indexOf(keyword) + keyword.length
        val textAfter = lower.substring(startIndex)

        val matcher = Pattern.compile("[:\\s]*([0-9.,\\s]+)").matcher(textAfter)
        if (matcher.find()) {
            val numberStr = matcher.group(1)?.replace("\\s".toRegex(), "") ?: ""
            var cleaned = numberStr
            if (cleaned.contains(".") && cleaned.contains(",")) {
                val lastDot = cleaned.lastIndexOf(".")
                val lastComma = cleaned.lastIndexOf(",")
                if (lastDot > lastComma) {
                    cleaned = cleaned.replace(",", "")
                } else {
                    cleaned = cleaned.replace(".", "").replace(",", ".")
                }
            } else if (cleaned.contains(".")) {
                val parts = cleaned.split(".")
                if (parts.size == 2 && parts[1].length <= 2) {
                    // Decimal point
                } else {
                    cleaned = cleaned.replace(".", "")
                }
            } else if (cleaned.contains(",")) {
                val parts = cleaned.split(",")
                if (parts.size == 2 && parts[1].length <= 2) {
                    cleaned = cleaned.replace(",", ".")
                } else {
                    cleaned = cleaned.replace(",", "")
                }
            }
            return cleaned.toDoubleOrNull()
        }
        return null
    }
}
