package com.example.testapi.models

data class Category(
    val id: Int,
    val name: String,
    val userId: String,
    val todoCount: Int
)

data class CategoryRequest(
    val name: String
)