package com.gbao86.sub_lazy.data

import android.content.Context
import android.net.Uri
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRestoreManager @Inject constructor(
    private val subscriptionDao: SubscriptionDao
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(BackupData::class.java)

    suspend fun exportData(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val subscriptions = subscriptionDao.getAllSubscriptionsOnce()
            val paymentHistory = subscriptionDao.getAllPaymentHistoryOnce()
            val sharedMembers = subscriptionDao.getAllSharedMembersOnce()

            val backupData = BackupData(
                timestamp = System.currentTimeMillis(),
                subscriptions = subscriptions,
                paymentHistory = paymentHistory,
                sharedMembers = sharedMembers
            )

            val json = adapter.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            } ?: throw Exception("Cannot open output stream")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importData(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: throw Exception("Cannot open input stream")

            val backupData = adapter.fromJson(json) ?: throw Exception("Invalid JSON format")

            // Simply replace/insert all to DB
            backupData.subscriptions.forEach { subscriptionDao.insertSubscription(it) }
            backupData.paymentHistory.forEach { subscriptionDao.insertPaymentHistory(it) }
            backupData.sharedMembers.forEach { subscriptionDao.insertSharedMember(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBackupJsonString(): String = withContext(Dispatchers.IO) {
        val subscriptions = subscriptionDao.getAllSubscriptionsOnce()
        val paymentHistory = subscriptionDao.getAllPaymentHistoryOnce()
        val sharedMembers = subscriptionDao.getAllSharedMembersOnce()

        val backupData = BackupData(
            timestamp = System.currentTimeMillis(),
            subscriptions = subscriptions,
            paymentHistory = paymentHistory,
            sharedMembers = sharedMembers
        )
        adapter.toJson(backupData)
    }
}
