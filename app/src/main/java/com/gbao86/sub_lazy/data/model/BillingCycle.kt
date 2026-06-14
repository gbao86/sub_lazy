package com.gbao86.sub_lazy.data.model

enum class BillingCycle(val displayName: String, val monthlyMultiplier: Double) {
    DAILY("Daily", 365.25 / 12.0),
    WEEKLY("Weekly", 52.0 / 12.0),
    MONTHLY("Monthly", 1.0),
    EVERY_3_MONTHS("Every 3 Months", 1.0 / 3.0),
    EVERY_6_MONTHS("Every 6 Months", 1.0 / 6.0),
    YEARLY("Yearly", 1.0 / 12.0),
    ONE_TIME("One-time", 0.0);

    companion object {
        fun fromDisplayName(name: String): BillingCycle =
            entries.find { it.displayName.equals(name, ignoreCase = true) } ?: MONTHLY
    }
}
