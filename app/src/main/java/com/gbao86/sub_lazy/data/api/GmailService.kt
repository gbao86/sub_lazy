package com.gbao86.sub_lazy.data.api

import android.content.Context
import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.nio.charset.Charset

class GmailService(private val context: Context) {
    private val client = OkHttpClient()

    interface GmailCallback {
        fun onSuccess(emails: List<String>)
        fun onError(message: String)
    }

    fun fetchEmails(accessToken: String, callback: GmailCallback) {
        val query = "subject:receipt OR subject:invoice OR subject:\"thanh toan\" OR Netflix OR Spotify OR YouTube OR iCloud"
        val url = "https://gmail.googleapis.com/gmail/v1/users/me/messages?q=${java.net.URLEncoder.encode(query, "UTF-8")}&maxResults=10"

        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                callback.onError(e.message ?: "Failed to list emails.")
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                try {
                    val responseStr = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        callback.onError("Gmail API error: Code ${response.code}")
                        return
                    }

                    val json = JSONObject(responseStr)
                    val messages = json.optJSONArray("messages")
                    if (messages == null || messages.length() == 0) {
                        callback.onSuccess(emptyList())
                        return
                    }

                    val emailTexts = mutableListOf<String>()
                    val total = messages.length()
                    var completedCount = 0

                    for (i in 0 until total) {
                        val messageId = messages.getJSONObject(i).getString("id")
                        fetchEmailDetail(accessToken, messageId, object : DetailCallback {
                            override fun onDetailLoaded(text: String) {
                                synchronized(emailTexts) {
                                    emailTexts.add(text)
                                    completedCount++
                                    if (completedCount == total) {
                                        callback.onSuccess(emailTexts)
                                    }
                                }
                            }

                            override fun onDetailError() {
                                synchronized(emailTexts) {
                                    completedCount++
                                    if (completedCount == total) {
                                        callback.onSuccess(emailTexts)
                                    }
                                }
                            }
                        })
                    }
                } catch (e: Exception) {
                    callback.onError(e.message ?: "Failed to parse email list.")
                }
            }
        })
    }

    private interface DetailCallback {
        fun onDetailLoaded(text: String)
        fun onDetailError()
    }

    private fun fetchEmailDetail(accessToken: String, id: String, callback: DetailCallback) {
        val url = "https://gmail.googleapis.com/gmail/v1/users/me/messages/$id"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                callback.onDetailError()
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                try {
                    val responseStr = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        callback.onDetailError()
                        return
                    }

                    val json = JSONObject(responseStr)
                    val snippet = json.optString("snippet")
                    val payload = json.optJSONObject("payload")
                    
                    var bodyText = ""
                    if (payload != null) {
                        bodyText = extractTextFromPayload(payload)
                    }

                    val result = if (bodyText.isNotBlank()) bodyText else snippet
                    callback.onDetailLoaded(result)
                } catch (e: Exception) {
                    callback.onDetailError()
                }
            }
        })
    }

    private fun extractTextFromPayload(payload: JSONObject): String {
        val body = payload.optJSONObject("body")
        val data = body?.optString("data")
        if (!data.isNullOrBlank()) {
            return decodeBase64Safe(data)
        }

        val parts = payload.optJSONArray("parts")
        if (parts != null && parts.length() > 0) {
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val mimeType = part.optString("mimeType")
                if (mimeType == "text/plain") {
                    val partBody = part.optJSONObject("body")
                    val partData = partBody?.optString("data")
                    if (!partData.isNullOrBlank()) {
                        return decodeBase64Safe(partData)
                    }
                }
            }
            // If no plain text, look inside sub-parts (nested parts)
            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                val nestedParts = part.optJSONArray("parts")
                if (nestedParts != null) {
                    val result = extractTextFromPayload(part)
                    if (result.isNotBlank()) return result
                }
            }
        }
        return ""
    }

    private fun decodeBase64Safe(encoded: String): String {
        return try {
            val decodedBytes = Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP)
            String(decodedBytes, Charset.forName("UTF-8"))
        } catch (e: Exception) {
            ""
        }
    }
}
