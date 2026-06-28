package com.example.todolist.ui.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDateTime

data class HomeUiState(
    val title: String,
    val subtitle: String,
    val createdate: LocalDateTime,
    val duedate: LocalDateTime,
    val priority: Int,
    val isDone: Boolean,
    val category: String = "Daily routine"
)
