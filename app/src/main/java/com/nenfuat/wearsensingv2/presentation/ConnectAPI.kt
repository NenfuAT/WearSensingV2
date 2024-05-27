package com.nenfuat.wearsensingv2.presentation

import android.content.Context
import android.os.Environment
import android.util.Log
import com.google.gson.Gson
import com.nenfuat.wearsensingv2.BuildConfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Base64

class ConnectAPI(var globalVariable: GlobalVariable) {
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

    suspend fun sendCsv(context: Context, fileName:String,path:String): String = withContext(Dispatchers.IO) {
        val bucket = globalVariable.bucket ?: ""
        val file = File(context.applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString().plus("/").plus(fileName).plus(".csv"))
        if (!file.exists()) {
            throw FileNotFoundException("File not found: ${file.absolutePath}")
        }

        data class SendFileResponse(
            val bucket: String,
            val file: String,
            val path: String
        )

        try {
            // ログインターセプターの設定
            val logging = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BASIC)
            }

            // OkHttpClientの作成
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()


            Log.d("bucket",bucket)
            Log.d("path",path)
            Log.d("file",file.name)
            // マルチパートボディを構築
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("bucket", bucket)
                .addFormDataPart("path",path)
                .addFormDataPart("file", file.name, file.asRequestBody("text/csv".toMediaTypeOrNull()))
                .build()

            // リクエストの作成
            val request = Request.Builder()
                .url(BuildConfig.API_LINK + "api/object/upload")
                .header("Authorization", encodeCredentials())
                .post(requestBody)
                .build()

            // リクエストの送信とレスポンスの取得
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            // レスポンスのボディを取得
            val responseBody = response.body?.string()
            if (responseBody != null) {
                // Gsonを使ってJSONをパース
                file.delete()
                val gson = Gson()
                val sendFileResponse = gson.fromJson(responseBody, SendFileResponse::class.java)
                sendFileResponse.bucket
                sendFileResponse.file
                sendFileResponse.path
                // レスポンスを返す
                responseBody

            } else {
                throw IOException("Response body is null")
            }


        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }
    }


    fun encodeCredentials(): String {
        val credentials = "${BuildConfig.USERNAME}:${BuildConfig.PASSWORD}"
        val credentialsBytes = credentials.toByteArray()
        return Base64.getEncoder().encodeToString(credentialsBytes)
    }
}