package com.example.testapi.models

data class TodoItem(
    val id: Int,
    val title: String,
    val description: String?,
    val isCompleted: Boolean,
    val dueDate: String?,
    val priority: Int,
    val categoryId: Int,
    val createdAt: String
)

data class TodoItemRequest(
    val title: String,
    val description: String?,
    val dueDate: String?,
    val priority: Int,
    val categoryId: Int
)