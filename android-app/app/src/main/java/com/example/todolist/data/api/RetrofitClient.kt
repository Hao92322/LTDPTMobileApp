package com.example.todolist.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var currentBaseUrl = "http://127.0.0.1:5158/"
    private var _apiService: ApiService? = null

    val apiService: ApiService
        get() {
            if (_apiService == null) {
                _apiService = buildApiService(currentBaseUrl)
            }
            return _apiService!!
        }

    fun initialize(context: android.content.Context) {
        val prefs = context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
        val savedIp = prefs.getString("backend_ip", "127.0.0.1") ?: "127.0.0.1"
        currentBaseUrl = "http://$savedIp:5158/"
        _apiService = buildApiService(currentBaseUrl)
    }

    fun updateIpAddress(context: android.content.Context, ipAddress: String) {
        val prefs = context.getSharedPreferences("app_settings", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("backend_ip", ipAddress).apply()
        currentBaseUrl = "http://$ipAddress:5158/"
        _apiService = buildApiService(currentBaseUrl)
    }

    private fun buildApiService(baseUrl: String): ApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
}