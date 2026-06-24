package com.example.testapi.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
//  ======= Gọi API =======
object RetrofitClient {
    // ===================== TESTER CHÚ Ý =================================
    //   Đổi IP nếu cần
    // - Android Emulator: http://10.0.2.2:5158/
    // - Thiết bị thật: http://192.168.1.244:5158/
    // ===================== 🫩🫩🫩 =================================
    private const val BASE_URL = "http://192.168.1.244:5158/"

    // Logging interceptor để xem request/response
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