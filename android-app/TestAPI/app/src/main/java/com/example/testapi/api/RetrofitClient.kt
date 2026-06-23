package com.example.testapi.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // ⚠️ QUAN TRỌNG:
    // - Android Emulator: dùng 10.0.2.2
    // - Device thật (cùng WiFi): dùng IP máy tính (ví dụ: 192.168.1.100)
    // - Cần sửa lại base_url cho khớp với backend
//    private const val BASE_URL = "http://192.168.1.244:5158/" dùng để test điện thoại thật
//    private const val BASE_URL = "http://10.0.2.2:5158/" // dùng để test điện thoại ảo
    private const val BASE_URL = "http://192.168.1.244:5158/"
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

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}