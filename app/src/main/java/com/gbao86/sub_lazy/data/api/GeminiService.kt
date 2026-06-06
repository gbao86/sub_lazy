/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational, 
and non-commercial purposes. 
Commercial exploitation, sale, or distribution of this software or any derivative works 
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy.data.api

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import java.util.Calendar
import com.gbao86.sub_lazy.ui.DateUtils

class GeminiService(private val context: Context) {

    interface GeminiCallback {
        fun onSuccess(result: ParsedSubscription)
        fun onError(message: String)
    }

    interface GeminiBatchCallback {
        fun onSuccess(results: List<ParsedSubscription>)
        fun onError(message: String)
    }

    data class ParsedSubscription(
        val name: String,
        val amount: Double,
        val cycle: String, // "Weekly", "Monthly", "Every 3 Months", "Every 6 Months", "Yearly" or "One-time"
        val category: String, // "Entertainment", "Utilities", "Work", "Cloud", "Music", "Food", "Other"
        val nextBillingDate: Long, // Timestamp
        val currency: String = "VND"
    )

    fun analyzeImage(imageUri: Uri, callback: GeminiCallback) {
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    val parsed = parseTextLocally(text)
                    if (parsed != null) {
                        callback.onSuccess(parsed)
                    } else {
                        callback.onError("Không thể nhận diện hóa đơn hoặc dịch vụ từ ảnh chụp màn hình này.")
                    }
                }
                .addOnFailureListener { e ->
                    callback.onError(e.message ?: "Lỗi nhận diện văn bản trên ảnh.")
                }
        } catch (e: Exception) {
            callback.onError(e.message ?: "Lỗi xử lý tệp ảnh.")
        }
    }

    fun analyzeText(emailContent: String, callback: GeminiCallback) {
        val parsed = parseTextLocally(emailContent)
        if (parsed != null) {
            callback.onSuccess(parsed)
        } else {
            callback.onError("Không tìm thấy thông tin thanh toán dịch vụ trong email này.")
        }
    }

    fun analyzeEmailsBatch(emailsText: String, callback: GeminiBatchCallback) {
        val list = parseMultipleSubscriptionsLocally(emailsText)
        if (list.isNotEmpty()) {
            callback.onSuccess(list)
        } else {
            callback.onError("Không tìm thấy thông tin thanh toán hóa đơn mới nào.")
        }
    }

    private fun parseTextLocally(text: String): ParsedSubscription? {
        if (text.isBlank()) return null
        val lowerText = text.lowercase(Locale.getDefault())
        val unaccentedLowerText = removeAccents(lowerText)

        // 1. Detect price first
        val price = detectAmount(text) ?: 0.0

        // 2. Detect service name
        var serviceName = detectService(unaccentedLowerText)
        
        // Fallback service name logic:
        if (serviceName == null) {
            if (price > 0.0) {
                // If we found a price, we can proceed with a fallback service name
                serviceName = "Hóa đơn mới"
            } else {
                // If we found neither a price nor a service name, return null to show error
                return null
            }
        }

        // 3. Detect cycle
        val cycle = if (unaccentedLowerText.contains("nam") || 
            unaccentedLowerText.contains("yearly") || 
            unaccentedLowerText.contains("annual") || 
            unaccentedLowerText.contains("1 year") || 
            unaccentedLowerText.contains("1 nam")) {
            "Yearly"
        } else if (unaccentedLowerText.contains("3 thang") ||
            unaccentedLowerText.contains("3 months") ||
            unaccentedLowerText.contains("quy") ||
            unaccentedLowerText.contains("quarterly")) {
            "Every 3 Months"
        } else if (unaccentedLowerText.contains("6 thang") ||
            unaccentedLowerText.contains("6 months") ||
            unaccentedLowerText.contains("nua nam") ||
            unaccentedLowerText.contains("half yearly") ||
            unaccentedLowerText.contains("semi-annual")) {
            "Every 6 Months"
        } else if (unaccentedLowerText.contains("tuan") ||
            unaccentedLowerText.contains("weekly") ||
            unaccentedLowerText.contains("7 ngay") ||
            unaccentedLowerText.contains("7 days") ||
            unaccentedLowerText.contains("1 week") ||
            unaccentedLowerText.contains("1 tuan")) {
            "Weekly"
        } else if (unaccentedLowerText.contains("ngay") ||
            unaccentedLowerText.contains("daily") ||
            unaccentedLowerText.contains("every day") ||
            unaccentedLowerText.contains("moi ngay") ||
            unaccentedLowerText.contains("1 day")) {
            "Daily"
        } else {
            "Monthly"
        }

        // 4. Map category
        val category = getCategoryForService(serviceName)

        val defaultNextBilling = DateUtils.getNextBillingDate(System.currentTimeMillis(), cycle)
        val billingDate = detectDate(unaccentedLowerText) ?: defaultNextBilling

        // 6. Detect currency
        val currency = if (lowerText.contains("$") || lowerText.contains("usd")) "USD" else "VND"

        return ParsedSubscription(serviceName, price, cycle, category, billingDate, currency)
    }

    private fun parseMultipleSubscriptionsLocally(text: String): List<ParsedSubscription> {
        val results = mutableListOf<ParsedSubscription>()
        val lines = text.split("\n---", "\nEmail #")
        for (emailText in lines) {
            val parsed = parseTextLocally(emailText)
            if (parsed != null) {
                // Deduplicate items in the same batch by service name
                if (results.none { it.name.equals(parsed.name, ignoreCase = true) }) {
                    results.add(parsed)
                }
            }
        }
        return results
    }

    private fun removeAccents(src: String): String {
        val nfdNormalizedString = java.text.Normalizer.normalize(src, java.text.Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("")
            .replace('đ', 'd')
            .replace('Đ', 'D')
    }

    private fun detectService(text: String): String? {
        // Predefined list sorted by length descending to match longer keywords first (e.g. "fpt play" before "fpt")
        val services = listOf(
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
        for (service in services) {
            if (text.contains(service)) {
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
                    "momo billing", "momo" -> "MoMo"
                    "grab subscription", "grab" -> "Grab"
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

        // Detect banking apps and e-wallets
        val banksAndWallets = listOf(
            "zalopay", "shopeepay", "vnpay", "vietcombank", "techcombank", "vietinbank", "bidv",
            "agribank", "mb bank", "mbbank", "tpbank", "vpbank", "sacombank", "acb"
        )
        for (bw in banksAndWallets) {
            if (text.contains(bw)) {
                return when (bw) {
                    "zalopay" -> "ZaloPay"
                    "shopeepay" -> "ShopeePay"
                    "vnpay" -> "VNPay"
                    "vietcombank" -> "Vietcombank"
                    "techcombank" -> "Techcombank"
                    "vietinbank" -> "Vietinbank"
                    "bidv" -> "BIDV"
                    "agribank" -> "Agribank"
                    "mb bank", "mbbank" -> "MB Bank"
                    "tpbank" -> "TPBank"
                    "vpbank" -> "VPBank"
                    "sacombank" -> "Sacombank"
                    "acb" -> "ACB"
                    else -> bw.uppercase()
                }
            }
        }
        return null
    }

    private fun detectAmount(text: String): Double? {
        val cleanText = text.replace(Regex("[\\u00A0\\u2007\\u202F]"), " ")
        val candidates = mutableListOf<AmountCandidate>()
        val lines = cleanText.split("\n")
        
        // 1. Patterns with Currency Symbols
        val suffixPattern = Pattern.compile("(?i)(\\d{1,3}(?:[.,\\s]\\d{3})+|\\d+(?:[.,]\\d{2})?)\\s*(đ|d|vnd|vnđ|usd|\\$|đ|đ|vnd|vnđ)")
        val prefixPattern = Pattern.compile("(?i)(\\$|usd|vnd|vnđ|đ|d)\\s*(\\d{1,3}(?:[.,\\s]\\d{3})+|\\d+(?:[.,]\\d{2})?)")
        val kPattern = Pattern.compile("(?i)(\\d+(?:[.,]\\d{1,3})?)\\s*(k)\\b")
        
        // 2. Base separator & plain patterns
        val separatorPattern = Pattern.compile("\\b(\\d{1,3}(?:[.,]\\d{3})+)\\b")
        val plainPattern = Pattern.compile("\\b(\\d{4,8})\\b")
        val usdDecimalPattern = Pattern.compile("\\b(\\d+\\.\\d{2})\\b")

        val priceKeywords = listOf("so tien", "thanh toan", "tong cong", "tong tien", "tien", "amount", "total", "price", "gia", "phi", "fee", "tri gia", "value", "cuoc")

        fun isLikelyNoise(valueStr: String, lineText: String): Boolean {
            val cleanVal = valueStr.replace(Regex("[.,\\s]"), "")
            if (cleanVal.length >= 9) return true // Account numbers/serial numbers are usually long
            
            // Filter out dates and times
            if (lineText.contains("/") || lineText.contains("-") || lineText.contains(":")) {
                val dateRegex = Regex("\\d{1,4}[/-]\\d{1,4}[/-]\\d{2,4}")
                if (dateRegex.containsMatchIn(lineText)) {
                    val dateMatch = dateRegex.find(lineText)?.value ?: ""
                    if (dateMatch.contains(valueStr)) return true
                }
                val timeRegex = Regex("\\d{1,2}:\\d{2}")
                if (timeRegex.containsMatchIn(lineText)) {
                    val timeMatch = timeRegex.find(lineText)?.value ?: ""
                    if (timeMatch.contains(valueStr)) return true
                }
            }
            
            // Filter out years (e.g. 2024..2027) if they look like a year rather than an amount
            val numericVal = cleanVal.toDoubleOrNull() ?: 0.0
            if (numericVal in 2020.0..2035.0) {
                val normalizedLine = removeAccents(lineText.lowercase(Locale.getDefault()))
                if (!normalizedLine.contains("đ") && !normalizedLine.contains("vnd") && 
                    !normalizedLine.contains("d") && !normalizedLine.contains("$") &&
                    !normalizedLine.contains("gia") && !normalizedLine.contains("tien")) {
                    return true
                }
            }
            return false
        }

        fun parseAmountValue(str: String): Double? {
            var temp = str.trim()
            if (temp.endsWith(".00") || temp.endsWith(",00")) {
                temp = temp.substring(0, temp.length - 3)
            }
            
            val hasDots = temp.contains(".")
            val hasCommas = temp.contains(",")
            val hasSpaces = temp.contains(" ")
            
            if (hasDots && hasCommas) {
                val dotIndex = temp.indexOf(".")
                val commaIndex = temp.indexOf(",")
                temp = if (dotIndex < commaIndex) {
                    temp.replace(".", "").replace(",", ".")
                } else {
                    temp.replace(",", "").replace(".", ".")
                }
            } else if (hasDots) {
                val parts = temp.split(".")
                if (parts.size > 2) {
                    temp = temp.replace(".", "")
                } else if (parts.size == 2) {
                    val decimalPart = parts[1]
                    if (decimalPart.length == 3) {
                        temp = temp.replace(".", "")
                    }
                }
            } else if (hasCommas) {
                val parts = temp.split(",")
                if (parts.size > 2) {
                    temp = temp.replace(",", "")
                } else if (parts.size == 2) {
                    val decimalPart = parts[1]
                    temp = if (decimalPart.length == 3) {
                        temp.replace(",", "")
                    } else {
                        temp.replace(",", ".")
                    }
                }
            } else if (hasSpaces) {
                temp = temp.replace(" ", "")
            }
            
            return temp.toDoubleOrNull()
        }

        // Match suffix patterns (45.000đ, etc.)
        val suffixMatcher = suffixPattern.matcher(cleanText)
        while (suffixMatcher.find()) {
            val numStr = suffixMatcher.group(1) ?: continue
            val parsedVal = parseAmountValue(numStr)
            if (parsedVal != null) {
                candidates.add(AmountCandidate(parsedVal, score = 100))
            }
        }

        // Match k pattern (100k, 25.5k)
        val kMatcher = kPattern.matcher(cleanText)
        while (kMatcher.find()) {
            val numStr = kMatcher.group(1) ?: continue
            val parsedVal = parseAmountValue(numStr)
            if (parsedVal != null) {
                candidates.add(AmountCandidate(parsedVal * 1000.0, score = 95))
            }
        }

        // Match prefix patterns ($9.99, etc.)
        val prefixMatcher = prefixPattern.matcher(cleanText)
        while (prefixMatcher.find()) {
            val numStr = prefixMatcher.group(2) ?: continue
            val parsedVal = parseAmountValue(numStr)
            if (parsedVal != null) {
                candidates.add(AmountCandidate(parsedVal, score = 100))
            }
        }

        // Line-by-line keyword context detection
        for (i in lines.indices) {
            val line = removeAccents(lines[i].lowercase(Locale.getDefault()))
            var containsKeyword = false
            for (keyword in priceKeywords) {
                if (line.contains(keyword)) {
                    containsKeyword = true
                    break
                }
            }
            
            if (containsKeyword) {
                val linesToSearch = mutableListOf<String>()
                linesToSearch.add(lines[i])
                if (i + 1 < lines.size) {
                    linesToSearch.add(lines[i + 1])
                }
                
                for (searchLine in linesToSearch) {
                    val sepMatcher = separatorPattern.matcher(searchLine)
                    while (sepMatcher.find()) {
                        val numStr = sepMatcher.group(1) ?: continue
                        if (!isLikelyNoise(numStr, searchLine)) {
                            val parsedVal = parseAmountValue(numStr)
                            if (parsedVal != null) {
                                candidates.add(AmountCandidate(parsedVal, score = 80))
                            }
                        }
                    }
                    
                    val plainMatcher = plainPattern.matcher(searchLine)
                    while (plainMatcher.find()) {
                        val numStr = plainMatcher.group(1) ?: continue
                        if (!isLikelyNoise(numStr, searchLine)) {
                            val parsedVal = parseAmountValue(numStr)
                            if (parsedVal != null) {
                                candidates.add(AmountCandidate(parsedVal, score = 70))
                            }
                        }
                    }

                    val usdMatcher = usdDecimalPattern.matcher(searchLine)
                    while (usdMatcher.find()) {
                        val numStr = usdMatcher.group(1) ?: continue
                        if (!isLikelyNoise(numStr, searchLine)) {
                            val parsedVal = parseAmountValue(numStr)
                            if (parsedVal != null) {
                                candidates.add(AmountCandidate(parsedVal, score = 80))
                            }
                        }
                    }
                }
            }
        }

        // Fallback match of independent separator/plain numbers anywhere in the text
        val sepMatcher = separatorPattern.matcher(cleanText)
        while (sepMatcher.find()) {
            val numStr = sepMatcher.group(1) ?: continue
            val matchedIndex = sepMatcher.start()
            val surroundingLine = getLineAtCharIndex(cleanText, matchedIndex)
            if (!isLikelyNoise(numStr, surroundingLine)) {
                val parsedVal = parseAmountValue(numStr)
                if (parsedVal != null) {
                    candidates.add(AmountCandidate(parsedVal, score = 40))
                }
            }
        }

        val plainMatcher = plainPattern.matcher(cleanText)
        while (plainMatcher.find()) {
            val numStr = plainMatcher.group(1) ?: continue
            val matchedIndex = plainMatcher.start()
            val surroundingLine = getLineAtCharIndex(cleanText, matchedIndex)
            if (!isLikelyNoise(numStr, surroundingLine)) {
                val parsedVal = parseAmountValue(numStr)
                if (parsedVal != null) {
                    candidates.add(AmountCandidate(parsedVal, score = 30))
                }
            }
        }

        val usdMatcher = usdDecimalPattern.matcher(cleanText)
        while (usdMatcher.find()) {
            val numStr = usdMatcher.group(1) ?: continue
            val matchedIndex = usdMatcher.start()
            val surroundingLine = getLineAtCharIndex(cleanText, matchedIndex)
            if (!isLikelyNoise(numStr, surroundingLine)) {
                val parsedVal = parseAmountValue(numStr)
                if (parsedVal != null) {
                    candidates.add(AmountCandidate(parsedVal, score = 40))
                }
            }
        }

        val validCandidates = candidates.filter { it.value > 0.0 && it.value <= 100000000.0 }
        if (validCandidates.isEmpty()) return null
        
        val bestCandidates = validCandidates.groupBy { it.value }
            .map { entry -> entry.value.maxByOrNull { it.score }!! }
            .sortedByDescending { it.score }
            
        val topScore = bestCandidates.first().score
        val topScored = bestCandidates.filter { it.score == topScore }
        
        return topScored.maxByOrNull { it.value }?.value
    }

    private fun getLineAtCharIndex(text: String, charIndex: Int): String {
        val startIndex = Math.max(0, text.lastIndexOf('\n', charIndex))
        val nextNewline = text.indexOf('\n', charIndex)
        val endIndex = if (nextNewline == -1) text.length else nextNewline
        return text.substring(startIndex, endIndex).trim()
    }

    private fun detectDate(text: String): Long? {
        val datePattern1 = Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})")
        val datePattern2 = Pattern.compile("(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})")
        val vnDatePattern = Pattern.compile("ngay (\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})")

        var matcher = vnDatePattern.matcher(text)
        if (matcher.find()) {
            try {
                val day = matcher.group(1)!!.toInt()
                val month = matcher.group(2)!!.toInt()
                var year = matcher.group(3)!!.toInt()
                if (year < 100) year += 2000
                val cal = java.util.Calendar.getInstance().apply {
                    set(year, month - 1, day)
                }
                return cal.timeInMillis
            } catch (e: Exception) {
                // ignore
            }
        }

        matcher = datePattern1.matcher(text)
        if (matcher.find()) {
            try {
                val day = matcher.group(1)!!.toInt()
                val month = matcher.group(2)!!.toInt()
                var year = matcher.group(3)!!.toInt()
                if (year < 100) year += 2000
                val cal = java.util.Calendar.getInstance().apply {
                    set(year, month - 1, day)
                }
                return cal.timeInMillis
            } catch (e: Exception) {
                // ignore
            }
        }

        matcher = datePattern2.matcher(text)
        if (matcher.find()) {
            try {
                val year = matcher.group(1)!!.toInt()
                val month = matcher.group(2)!!.toInt()
                val day = matcher.group(3)!!.toInt()
                val cal = java.util.Calendar.getInstance().apply {
                    set(year, month - 1, day)
                }
                return cal.timeInMillis
            } catch (e: Exception) {
                // ignore
            }
        }

        val relativePattern = Pattern.compile("gia han sau (\\d+) ngay|renew in (\\d+) days")
        matcher = relativePattern.matcher(text)
        if (matcher.find()) {
            val days = (matcher.group(1) ?: matcher.group(2))?.toLong() ?: 0L
            return System.currentTimeMillis() + days * 86400000L
        }

        return null
    }

    private fun getCategoryForService(service: String): String {
        return when (service) {
            "Netflix", "YouTube Premium", "VieON", "FPT Play", "K+" -> "Entertainment"
            "Spotify", "Zing MP3" -> "Music"
            "iCloud", "Google One", "AWS" -> "Cloud"
            "Viettel", "VNPT", "FPT Telecom", "Vinaphone", "Mobifone" -> "Utilities"
            "GitHub", "GitHub Copilot", "ChatGPT Plus", "OpenAI API", "Zoom", "Canva" -> "Work"
            "Grab" -> "Food"
            else -> "Other"
        }
    }

    private data class AmountCandidate(val value: Double, val score: Int)
}
