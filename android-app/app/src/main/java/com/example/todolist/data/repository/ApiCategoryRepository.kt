package com.example.todolist.data.repository

import android.content.Context
import com.example.todolist.data.api.Category
import com.example.todolist.data.api.CategoryRequest
import com.example.todolist.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ApiCategoryRepository(private val context: Context) {

    private val apiService = RetrofitClient.apiService

    private fun getToken(): String {
        val token = TokenManager.getAccessToken(context) ?: ""
        return "Bearer $token"
    }

    suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCategories(token = getToken())
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createCategory(name: String): Category? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createCategory(CategoryRequest(name), getToken())
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateCategory(id: Int, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateCategory(id, CategoryRequest(name), getToken())
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteCategory(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteCategory(id, getToken())
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
