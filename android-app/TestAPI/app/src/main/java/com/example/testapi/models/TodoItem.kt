package com.example.testapi.models

data class TodoItem(
    val id: Int,
    val title: String,
    val description: String?,
    val isCompleted: Boolean = false,
    val dueDate: String? = null,
    val priority: Int = 1,
    val categoryId: Int,
    val createdAt: String? = null
)

data class TodoItemRequest(
    val title: String,
    val description: String?,
    val dueDate: String? = null,
    val priority: Int = 1,
    val categoryId: Int
)