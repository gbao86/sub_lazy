package com.gbao86.sub_lazy.data.api

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GoogleDriveService(private val context: Context) {
    private val client = OkHttpClient()

    suspend fun uploadBackup(jsonContent: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context) ?: throw Exception("Not signed in")
            if (account.account == null) throw Exception("No account found")
            
            val token = GoogleAuthUtil.getToken(context, account.account!!, "oauth2:https://www.googleapis.com/auth/drive.file")
            
            val metadata = """{"name": "sub_lazy_backup.json"}"""
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("metadata", null, metadata.toRequestBody("application/json; charset=UTF-8".toMediaType()))
                .addFormDataPart("file", "sub_lazy_backup.json", jsonContent.toRequestBody("application/json".toMediaType()))
                .build()

            val request = Request.Builder()
                .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                .addHeader("Authorization", "Bearer ${"$token"}")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Failed to upload: ${"$response.code"}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
