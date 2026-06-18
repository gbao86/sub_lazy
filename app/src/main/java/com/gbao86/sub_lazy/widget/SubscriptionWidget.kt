package com.gbao86.sub_lazy.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import com.gbao86.sub_lazy.MainActivity
import com.gbao86.sub_lazy.data.SubscriptionDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class SubscriptionWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun subscriptionDao(): SubscriptionDao
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
        val dao = entryPoint.subscriptionDao()
        val subscriptions = dao.getAllSubscriptionsOnce().take(4)

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFFF1F5F9))
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>())
            ) {
                Text(
                    text = "Gói sắp đến hạn",
                    style = TextStyle(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = GlanceModifier.height(12.dp))
                if (subscriptions.isEmpty()) {
                    Text("Không có gói nào sắp tới.")
                } else {
                    subscriptions.forEach { sub ->
                        Row(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp)) {
                            Text(text = sub.name, modifier = GlanceModifier.defaultWeight())
                            Text(text = "${sub.amount.toLong()} ${sub.currency.name}", style = TextStyle(fontWeight = FontWeight.Medium))
                        }
                    }
                }
            }
        }
    }
}
