package com.example.todoapp.models

import com.google.gson.annotations.SerializedName

data class TodoItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("isCompleted") val isCompleted: Boolean = false,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("priority") val priority: Int = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("category") val category: Category? = null
)

data class CreateTodoRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("priority") val priority: Int = 0,
    @SerializedName("categoryId") val categoryId: Int,
    @SerializedName("isCompleted") val isCompleted: Boolean = false
)