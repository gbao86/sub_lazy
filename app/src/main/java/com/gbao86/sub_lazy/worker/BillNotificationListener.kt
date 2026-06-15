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
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

class BillNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val BANKING_PACKAGES = setOf(
        "com.VCB",                    // Vietcombank
        "com.mbmobile",               // MB Bank
        "com.vnpay.hdbank",           // HD Bank
        "vn.com.techcombank.bb.app",  // Techcombank
        "com.tpb.mb.gprsandroid",     // TPBank
        "com.VietinBank",             // VietinBank
        "com.ftOS.momo",              // MoMo
        "com.zalopay",                // ZaloPay
        "vn.momo.platform"            // MoMo alternate
    )

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        if (sbn.packageName !in BANKING_PACKAGES) return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: ""
        val text = extras.getString("android.text") ?: ""

        val combinedText = "$title $text".lowercase(Locale.getDefault())

        val detectedBalance = detectBalance(combinedText)
        if (detectedBalance != null) {
            val sharedPref = applicationContext.getSharedPreferences("app_prefs", MODE_PRIVATE)
            sharedPref.edit()
                .putString("user_balance_str", detectedBalance.toString())
                .apply()
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

        serviceScope.launch {
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
                    if (matchedSub.cycle == BillingCycle.ONE_TIME) {
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
                    showAutoPaidNotification(matchedSub.name, amount, matchedSub.currency.code)
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
        val rawServices = listOf(
            "github copilot", "chatgpt plus", "youtube premium", "fpt telecom", "fpt play", 
            "google one", "microsoft 365", "apple services", "grab subscription", "momo billing",
            "netflix", "spotify", "youtube", "icloud", "google", "microsoft", "office", "apple",
            "vieon", "k+", "netnam", "viettel", "vnpt", "aws", "github", "copilot", "chatgpt", 
            "openai", "momo", "grab", "mobi", "vina", "zalo", "shopee", "tiki", "canva", "capcut",
            "zoom", "medium", "notion", "galaxy play", "clip tv", "vtvcab on", "danet", "fpt camera",
            "kplus", "beamin", "be app", "gojek", "tinder gold", "tinder platinum", "spotify premium",
            "adobe creative cloud", "dropbox", "canva pro", "duolingo plus", "grammarly", "babbel",
            "elsa speak", "monkey stories"
        )
        val sortedServices = rawServices.sortedByDescending { it.length }

        for (service in sortedServices) {
            val pattern = Pattern.compile("\\b${Pattern.quote(service)}\\b")
            if (pattern.matcher(text).find()) {
                return when (service) {
                    "galaxy play" -> "Galaxy Play"
                    "clip tv" -> "Clip TV"
                    "vtvcab on" -> "VTVcab ON"
                    "danet" -> "Danet"
                    "fpt camera" -> "FPT Camera"
                    "kplus" -> "K+"
                    "beamin" -> "Baemin"
                    "be app" -> "Be"
                    "gojek" -> "Gojek"
                    "tinder gold" -> "Tinder Gold"
                    "tinder platinum" -> "Tinder Platinum"
                    "adobe creative cloud" -> "Adobe CC"
                    "canva pro" -> "Canva Pro"
                    "duolingo plus" -> "Duolingo Plus"
                    "elsa speak" -> "ELSA Speak"
                    "monkey stories" -> "Monkey Stories"
                    "fpt play" -> "FPT Play"
                    "vieon" -> "VieON"
                    "k+" -> "K+"
                    "netnam" -> "Netnam"
                    "viettel" -> "Viettel"
                    "vnpt" -> "VNPT"
                    "fpt telecom" -> "FPT Telecom"
                    "aws" -> "AWS"
                    "github" -> "GitHub"
                    "github copilot", "copilot" -> "GitHub Copilot"
                    "chatgpt plus", "chatgpt" -> "ChatGPT Plus"
                    "openai" -> "OpenAI API"
                    "netflix" -> "Netflix"
                    "spotify", "spotify premium" -> "Spotify"
                    "youtube premium", "youtube" -> "YouTube Premium"
                    "icloud" -> "iCloud"
                    "google one", "google" -> "Google One"
                    "microsoft 365", "microsoft", "office" -> "Microsoft 365"
                    "apple services", "apple" -> "Apple Services"
                    "momo billing", "momo" -> "MoMo Billing"
                    "grab subscription", "grab" -> "Grab Subscription"
                    "mobi" -> "Mobifone"
                    "vina" -> "Vinaphone"
                    "zalo" -> "ZaloPay"
                    "shopee" -> "ShopeePay"
                    "tiki" -> "Tiki"
                    "canva" -> "Canva"
                    "capcut" -> "CapCut"
                    "zoom" -> "Zoom"
                    "medium" -> "Medium"
                    "notion" -> "Notion"
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
            setPackage("com.gbao86.sub_lazy")
            setClassName("com.gbao86.sub_lazy", "com.gbao86.sub_lazy.MainActivity")
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

        val notificationId = serviceName.hashCode()
        // codeql[java/android/implicit-pendingintents]
        notificationManager.notify(notificationId, notification)
    }



    private fun showAutoPaidNotification(serviceName: String, amount: Double, currency: String) {
        val channelId = "detected_bills"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val locale = resources.configuration.locales[0]
        val amountFormatted = CurrencyFormatter.format(amount, currency, locale)

        val message = getString(R.string.notification_auto_paid_message, serviceName, amountFormatted)

        val intent = Intent(this, MainActivity::class.java).apply {
            setPackage("com.gbao86.sub_lazy")
            setClassName("com.gbao86.sub_lazy", "com.gbao86.sub_lazy.MainActivity")
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
            .setContentTitle(getString(R.string.notification_auto_paid_title))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = serviceName.hashCode() + 1
        // codeql[java/android/implicit-pendingintents]
        notificationManager.notify(notificationId, notification)
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
