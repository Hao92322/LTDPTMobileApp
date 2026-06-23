package com.example.todolist.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.todolist.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val sampleTasks = listOf(
        HomeUiState("Uống Nước Sông", "Streak 3 ngày", "5 phút", Icons.Filled.LocalDrink, SandBg, 3, true, "Morning Routine"),
        HomeUiState("Thiền tích nội công", "Streak 6 ngày", "15 phút", Icons.Filled.SelfImprovement, MintBg, 6, true, "Morning Routine"),
        HomeUiState("Check Email", "Review inbox", "10 phút", Icons.Filled.Email, SkyBg, 0, false, "Work"),
        HomeUiState("Team Meeting", "Weekly sync", "45 phút", Icons.Filled.Groups, LavenderBg, 0, false, "Work"),
        HomeUiState("Dãn cơ", "Streak 5 ngày", "10 phút", Icons.Filled.Accessibility, LavenderBg, 5, false, "Personal"),
        HomeUiState("Đi Bộ", "Streak 3 ngày", "20 phút", Icons.AutoMirrored.Filled.DirectionsWalk, SkyBg, 3, false, "Personal"),
    )
    private val _todoList = MutableStateFlow<List<HomeUiState>>(sampleTasks)
    val todoList = _todoList.asStateFlow()
    fun toggleTodoStatus(index: Int) {
        _todoList.value = _todoList.value.mapIndexed { i, task ->
            if (i == index)
                task.copy(isDone = !task.isDone)
            else
                task
        }
    }
}
