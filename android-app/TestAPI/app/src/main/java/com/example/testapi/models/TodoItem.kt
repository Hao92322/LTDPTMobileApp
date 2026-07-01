package com.example.testapi.models

import com.google.gson.annotations.SerializedName

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