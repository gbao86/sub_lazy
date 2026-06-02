/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

Unauthorized copying of this file, via any medium, is strictly prohibited.
Proprietary and confidential.
This source code is provided for reference purposes only and may not be copied, 
modified, or distributed without the express written permission of the author.
*/

package com.gbao86.sub_lazy.ui

import java.util.concurrent.TimeUnit

object DateUtils {
    fun getDaysLeft(nextBillingDate: Long): Long {
        val diff = nextBillingDate - System.currentTimeMillis()
        return if (diff < 0) 0L else TimeUnit.MILLISECONDS.toDays(diff)
    }
}
