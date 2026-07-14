package com.example.todolist.data.repository

import android.content.Context
import com.example.todolist.data.api.LoginRequest
import com.example.todolist.data.api.RetrofitClient

class ApiAuthRepository(private val context: Context) {

    private val apiService = RetrofitClient.apiService

    suspend fun login(email: String, password: String): Boolean {
        return try {
            val response = apiService.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body()?.success == true) {
                val accessToken = response.body()?.data?.accessToken ?: ""
                val refreshToken = response.body()?.data?.refreshToken ?: ""
                val expiresIn = response.body()?.data?.expiresIn?.toLong() ?: 3600L

                TokenManager.saveToken(context, accessToken, refreshToken, expiresIn)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isLoggedIn(): Boolean {
        return TokenManager.isLoggedIn(context)
    }

    fun logout() {
        TokenManager.clearToken(context)
    }

    fun getToken(): String? {
        return TokenManager.getAccessToken(context)
    }
}