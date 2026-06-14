package com.gbao86.sub_lazy.data

import androidx.room.TypeConverter
import com.gbao86.sub_lazy.data.model.BillingCycle
import com.gbao86.sub_lazy.data.model.SubscriptionCurrency
import com.gbao86.sub_lazy.data.model.SubscriptionCategory

class Converters {
    @TypeConverter
    fun fromBillingCycle(cycle: BillingCycle): String = cycle.displayName

    @TypeConverter
    fun toBillingCycle(value: String): BillingCycle = BillingCycle.fromDisplayName(value)

    @TypeConverter
    fun fromCurrency(currency: SubscriptionCurrency): String = currency.code

    @TypeConverter
    fun toCurrency(value: String): SubscriptionCurrency = SubscriptionCurrency.fromCode(value)

    @TypeConverter
    fun fromCategory(category: SubscriptionCategory): String = category.displayName

    @TypeConverter
    fun toCategory(value: String): SubscriptionCategory = SubscriptionCategory.fromDisplayName(value)
}
