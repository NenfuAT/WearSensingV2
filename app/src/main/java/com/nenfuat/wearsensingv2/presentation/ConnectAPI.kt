package com.nenfuat.wearsensingv2.presentation

import com.google.gson.Gson
import com.nenfuat.wearsensingv2.BuildConfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.Base64

class ConnectAPI {
    suspend fun getBuckets(): List<String>? = withContext(Dispatchers.IO) {

        data class BucketListResponse(val buckets: List<String>)
        try {
            // ログインターセプターの設定
            val logging = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BASIC)
            }

            // OkHttpClientの作成
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            // リクエストの作成
            val request = Request.Builder()
                .url(BuildConfig.API_LINK + "api/bucket/list")
                .header("Authorization", encodeCredentials())
                .build()

            // リクエストの送信とレスポンスの取得
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                // レスポンスのボディを取得
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    // Gsonを使ってJSONをパース
                    val gson = Gson()
                    val bucketListResponse = gson.fromJson(responseBody, BucketListResponse::class.java)
                    bucketListResponse.buckets
                } else {
                    throw IOException("Response body is null")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun encodeCredentials(): String {
        val credentials = "${BuildConfig.USERNAME}:${BuildConfig.PASSWORD}"
        val credentialsBytes = credentials.toByteArray()
        return Base64.getEncoder().encodeToString(credentialsBytes)
    }
}