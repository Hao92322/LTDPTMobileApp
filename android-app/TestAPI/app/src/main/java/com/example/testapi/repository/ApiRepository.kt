package com.example.testapi.repository

import com.example.testapi.api.RetrofitClient
import com.example.testapi.models.CategoryRequest
import com.example.testapi.models.LoginRequest
import com.example.testapi.models.RegisterRequest
import com.example.testapi.models.TodoItemRequest

class ApiRepository {
    private val apiService = RetrofitClient.apiService

    // ========== AUTH ==========
    suspend fun login(email: String, password: String) =
        apiService.login(LoginRequest(email, password))

    suspend fun register(email: String, password: String) =
        apiService.register(RegisterRequest(email, password))

    // ========== CATEGORIES ==========
    suspend fun getCategories(token: String, page: Int = 1, pageSize: Int = 20, search: String? = null) =
        apiService.getCategories(page, pageSize, search, "Bearer $token")

    suspend fun getCategoryById(token: String, id: Int) =
        apiService.getCategoryById(id, "Bearer $token")

    suspend fun createCategory(token: String, name: String) =
        apiService.createCategory(CategoryRequest(name), "Bearer $token")

    suspend fun updateCategory(token: String, id: Int, name: String) =
        apiService.updateCategory(id, CategoryRequest(name), "Bearer $token")

    suspend fun deleteCategory(token: String, id: Int) =
        apiService.deleteCategory(id, "Bearer $token")

    // ========== TODO ITEMS ==========
    suspend fun getTodoItems(token: String, categoryId: Int, page: Int = 1, pageSize: Int = 20, search: String? = null) =
        apiService.getTodoItems(categoryId, page, pageSize, search, "Bearer $token")

    suspend fun getTodoItemById(token: String, id: Int) =
        apiService.getTodoItemById(id, "Bearer $token")

    suspend fun createTodoItem(token: String, request: TodoItemRequest) =
        apiService.createTodoItem(request, "Bearer $token")

    suspend fun updateTodoItem(token: String, id: Int, request: TodoItemRequest) =
        apiService.updateTodoItem(id, request, "Bearer $token")

    suspend fun deleteTodoItem(token: String, id: Int) =
        apiService.deleteTodoItem(id, "Bearer $token")

    suspend fun toggleComplete(token: String, id: Int) =
        apiService.toggleComplete(id, "Bearer $token")
}