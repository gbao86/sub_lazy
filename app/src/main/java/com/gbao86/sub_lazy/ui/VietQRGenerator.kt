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

import java.net.URLEncoder

object VietQRGenerator {
    /**
     * Generates the VietQR URL based on the input parameters.
     * Standard VietQR API format:
     * https://img.vietqr.io/image/<BANK_ID>-<ACCOUNT_NUMBER>-<TEMPLATE>.png?amount=<AMOUNT>&addInfo=<DESCRIPTION>&accountName=<ACCOUNT_NAME>
     */
    fun generateQrUrl(
        bankName: String,
        accountNumber: String,
        amount: Double,
        description: String,
        accountHolder: String? = null,
        template: String = "compact2"
    ): String {
        val cleanBank = bankName.trim()
        val cleanAccount = accountNumber.trim().replace(" ", "")
        
        if (cleanBank.isEmpty() || cleanAccount.isEmpty() || cleanAccount.length < 4 || amount <= 0) {
            return ""
        }

        val cleanBankId = cleanBank.lowercase()
            .replace(" ", "")
            .replace("nganhang", "")
            .replace("bank", "")
        
        val bankId = mapToStandardBankId(cleanBankId)
        val amountVal = amount.toLong()
        
        return try {
            val encodedInfo = URLEncoder.encode(description, "UTF-8")
            val encodedName = accountHolder?.let { URLEncoder.encode(it, "UTF-8") } ?: ""
            "https://img.vietqr.io/image/$bankId-$cleanAccount-$template.png?amount=$amountVal&addInfo=$encodedInfo&accountName=$encodedName"
        } catch (e: Exception) {
            "https://img.vietqr.io/image/$bankId-$cleanAccount-$template.png?amount=$amountVal"
        }
    }

    private fun mapToStandardBankId(input: String): String {
        return when {
            input.contains("vietcom") || input.contains("vcb") -> "vietcombank"
            input.contains("techcom") || input.contains("tcb") -> "techcombank"
            input.contains("vietin") || input.contains("vtb") -> "vietinbank"
            input.contains("bidv") -> "bidv"
            input.contains("agribank") || input.contains("agri") -> "agribank"
            input.contains("mbbank") || input == "mb" || input.contains("quandoi") -> "mb"
            input.contains("sacom") || input.contains("stb") -> "sacombank"
            input.contains("acb") || input.contains("achau") -> "acb"
            input.contains("vpbank") || input.contains("vp") -> "vpbank"
            input.contains("tpbank") || input.contains("tp") || input.contains("tienphong") -> "tpbank"
            input.contains("shb") -> "shb"
            input.contains("hdbank") || input.contains("hd") -> "hdbank"
            input.contains("scb") -> "scb"
            input.contains("vib") -> "vib"
            input.contains("seabank") || input.contains("seab") -> "seabank"
            input.contains("ocb") || input.contains("phuongdong") -> "ocb"
            input.contains("eximbank") || input.contains("exim") -> "eximbank"
            input.contains("msb") || input.contains("hanghai") -> "msb"
            input.contains("shanhank") || input.contains("shinhan") -> "shinhan"
            input.contains("woori") -> "woori"
            input.contains("cbbank") || input.contains("cb") -> "cbbank"
            else -> input
        }
    }
}
