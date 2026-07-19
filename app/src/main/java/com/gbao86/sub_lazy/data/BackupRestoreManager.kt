package com.gbao86.sub_lazy.data

import android.content.Context
import android.net.Uri
import android.os.Build
import com.gbao86.sub_lazy.BuildConfig
import androidx.room.withTransaction
import com.squareup.moshi.Moshi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

sealed interface BackupResult {
    object Success : BackupResult
    data class InvalidBackupFile(val message: String) : BackupResult
    data class PermissionDenied(val message: String) : BackupResult
    data class OutOfStorage(val message: String) : BackupResult
    data class UnsupportedVersion(val message: String) : BackupResult
    data class UnknownError(val exception: Exception) : BackupResult
}

@Singleton
class BackupRestoreManager @Inject constructor(
    private val appDatabase: AppDatabase,
    private val subscriptionDao: SubscriptionDao
) {
    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(BackupData::class.java)

    suspend fun exportData(context: Context, uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val subscriptions = subscriptionDao.getAllSubscriptionsOnce()
            val paymentHistory = subscriptionDao.getAllPaymentHistoryOnce()
            val sharedMembers = subscriptionDao.getAllSharedMembersOnce()

            val currentTime = System.currentTimeMillis()
            val backupData = BackupData(
                appVersion = BuildConfig.VERSION_NAME,
                exportSource = "MANUAL",
                timestamp = currentTime,
                createdAt = getIso8601DateString(currentTime),
                device = "${Build.MANUFACTURER} ${Build.MODEL}",
                backupType = "FULL",
                defaultCurrency = "VND",
                totalSubscriptions = subscriptions.size,
                subscriptions = subscriptions,
                paymentHistory = paymentHistory,
                sharedMembers = sharedMembers
            )

            val json = adapter.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            } ?: return@withContext BackupResult.PermissionDenied("Cannot open output stream")

            BackupResult.Success
        } catch (e: SecurityException) {
            BackupResult.PermissionDenied("Permission denied: ${e.message}")
        } catch (e: Exception) {
            BackupResult.UnknownError(e)
        }
    }

    suspend fun importData(context: Context, uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return@withContext BackupResult.PermissionDenied("Cannot open input stream")

            val backupData = try {
                adapter.fromJson(json)
            } catch (e: Exception) {
                return@withContext BackupResult.InvalidBackupFile("Invalid JSON format: ${e.message}")
            } ?: return@withContext BackupResult.InvalidBackupFile("JSON parsed to null")

            if (backupData.totalSubscriptions != null && backupData.totalSubscriptions != backupData.subscriptions.size) {
                return@withContext BackupResult.InvalidBackupFile("Backup file corrupted: totalSubscriptions mismatch")
            }

            appDatabase.withTransaction {
                // Option A: Delete existing data -> Import clean
                subscriptionDao.deleteAllSubscriptions()

                // Insert all to DB
                backupData.subscriptions.forEach { subscriptionDao.insertSubscription(it) }
                backupData.paymentHistory.forEach { subscriptionDao.insertPaymentHistory(it) }
                backupData.sharedMembers.forEach { subscriptionDao.insertSharedMember(it) }
            }

            BackupResult.Success
        } catch (e: SecurityException) {
            BackupResult.PermissionDenied("Permission denied: ${e.message}")
        } catch (e: Exception) {
            BackupResult.UnknownError(e)
        }
    }

    suspend fun getBackupJsonString(): String = withContext(Dispatchers.IO) {
        val subscriptions = subscriptionDao.getAllSubscriptionsOnce()
        val paymentHistory = subscriptionDao.getAllPaymentHistoryOnce()
        val sharedMembers = subscriptionDao.getAllSharedMembersOnce()

        val currentTime = System.currentTimeMillis()
        val backupData = BackupData(
            appVersion = BuildConfig.VERSION_NAME,
            exportSource = "MANUAL",
            timestamp = currentTime,
            createdAt = getIso8601DateString(currentTime),
            device = "${Build.MANUFACTURER} ${Build.MODEL}",
            backupType = "FULL",
            defaultCurrency = "VND",
            totalSubscriptions = subscriptions.size,
            subscriptions = subscriptions,
            paymentHistory = paymentHistory,
            sharedMembers = sharedMembers
        )
        adapter.toJson(backupData)
    }

    private fun getIso8601DateString(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }
}
