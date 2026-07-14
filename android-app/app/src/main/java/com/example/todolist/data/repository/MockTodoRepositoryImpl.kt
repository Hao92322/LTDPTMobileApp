package com.example.todolist.data.repository

import com.example.todolist.ui.home.HomeUiState
import java.time.LocalDateTime

class MockTodoRepositoryImpl : TodoRepository {
    private val now = LocalDateTime.now()
    
    private val todoList = mutableListOf<HomeUiState>(
        HomeUiState(
            0, "Uống Nước Sông", "Lượng nước: 500ml", 
            now, now.withHour(8).withMinute(0), 
            1, false, "Morning Routine"
        ),
        HomeUiState(
            0, "Thiền tích nội công", "Thời gian: 15 phút", 
            now, now.withHour(9).withMinute(0), 
            2, false, "Morning Routine"
        ),
        HomeUiState(
            0, "Check Email", "Xử lý inbox công việc",
            now, now.withHour(10).withMinute(30), 
            0, false, "Work"
        )
    )

    override fun getTodos(): List<HomeUiState> {
        return todoList
    }

    override fun addTask(task: HomeUiState) {
        todoList.add(task)
    }

    override fun toggleStatus(index: Int) {
        if (index in todoList.indices) {
            todoList[index] = todoList[index].copy(isDone = !todoList[index].isDone)
        }
    }

    override fun updateTask(index: Int, task: HomeUiState) {
        if (index in todoList.indices) {
            todoList[index] = task
        }
    }

    override fun deleteTask(index: Int) {
        if (index in todoList.indices) {
            todoList.removeAt(index)
        }
    }
}
