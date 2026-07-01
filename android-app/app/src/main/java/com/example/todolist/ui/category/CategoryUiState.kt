package com.example.todolist.ui.category

import com.example.todolist.ui.home.HomeUiState

data class CategoryUiState (
    val id : String,
    val name : String,
    val todos : List<HomeUiState>
)