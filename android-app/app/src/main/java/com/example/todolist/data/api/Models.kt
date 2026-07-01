package com.example.todolist.data.api

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val userName: String,
    val confirmPassword: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData?
)

data class AuthData(
    val token: String,
    val expiresIn: Int = 3600
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class Category(
    val id: Int,
    val name: String,
    val userId: String? = null,
    val todoCount: Int = 0
)

data class CategoryRequest(
    val name: String
)

data class TodoItem(
    val id: Int,
    val title: String,
    val description: String?,
    val isCompleted: Boolean = false,
    val dueDate: String? = null,
    val priority: Int = 0,
    val categoryId: Int?,
    val date: String,
    val createdAt: String? = null
)

data class TodoItemRequest(
    val title: String,
    val description: String?,
    val dueDate: String? = null,
    val priority: Int = 0,
    val categoryId: Int?,
    val date: String,
    val isCompleted: Boolean? = null
)