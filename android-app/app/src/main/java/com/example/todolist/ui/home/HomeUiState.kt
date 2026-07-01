package com.example.todolist.ui.home

import java.time.LocalDateTime

data class HomeUiState(
    val id: Int = 0,
    val title: String,
    val subtitle: String,
    val createdate: LocalDateTime,
    val duedate: LocalDateTime,
    val priority: Int,
    val isDone: Boolean,
    val category: String = "Daily routine"
)
