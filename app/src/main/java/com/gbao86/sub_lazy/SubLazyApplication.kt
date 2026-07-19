package com.gbao86.sub_lazy

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.gbao86.sub_lazy.ui.ExchangeRateManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SubLazyApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var exchangeRateManager: ExchangeRateManager
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        ExchangeRateManager.instance = exchangeRateManager
        createNotificationChannel()
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "renewal_reminder_channel",
                "Renewal Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for subscription renewal notifications"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
