package com.example.testapi.models

data class Category(
    val id: Int,
    val name: String,
    val userId: String? = null,
    val todoCount: Int = 0
)

data class CategoryRequest(
    val name: String
)