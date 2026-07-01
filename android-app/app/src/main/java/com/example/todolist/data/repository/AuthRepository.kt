package com.example.todolist.data.repository

interface AuthRepository {
    suspend fun saveToken(token: String)

    suspend fun getToken(): String?

    suspend fun clearToken()
}