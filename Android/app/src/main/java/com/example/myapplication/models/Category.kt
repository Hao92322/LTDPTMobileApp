package com.example.todoapp.models

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("todoItems") val todoItems: List<TodoItem>? = null
)

data class CreateCategoryRequest(
    @SerializedName("name") val name: String
)