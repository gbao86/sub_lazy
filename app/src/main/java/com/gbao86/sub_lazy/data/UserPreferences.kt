package com.gbao86.sub_lazy.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, value) }

    var userBalance: Double
        get() {
            val strVal = prefs.getString(KEY_BALANCE_STR, null)
            if (strVal != null) {
                val parsed = strVal.toDoubleOrNull()
                if (parsed != null) return parsed
            }
            val legacyStr = prefs.getString(KEY_BALANCE_LONG, null)
            return legacyStr?.toDoubleOrNull() ?: DEFAULT_BALANCE
        }
        set(value) = prefs.edit { putString(KEY_BALANCE_STR, value.toString()) }

    var budgetResetDay: Int
        get() = prefs.getInt(KEY_BUDGET_RESET_DAY, 1)
        set(value) = prefs.edit { putInt(KEY_BUDGET_RESET_DAY, value) }

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    companion object {
        const val PREFS_NAME = "app_prefs"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_BALANCE_STR = "user_balance_str"
        const val KEY_BALANCE_LONG = "user_balance_long"
        const val KEY_BUDGET_RESET_DAY = "budget_reset_day"
        const val DEFAULT_BALANCE = 2000000.0
    }
}
