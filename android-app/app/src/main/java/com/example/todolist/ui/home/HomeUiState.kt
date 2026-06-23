package com.example.todolist.ui.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class HomeUiState(
    val title: String,
    val subtitle: String,
    val time: String,
    val icon: ImageVector,
    val iconBg: Color,
    val streakDays: Int,
    val isDone: Boolean,
    val category: String = "Daily routine"
)
