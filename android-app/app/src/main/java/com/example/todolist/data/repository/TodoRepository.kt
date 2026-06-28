package com.example.todolist.data.repository
import com.example.todolist.ui.home.HomeUiState
interface TodoRepository{
    fun getTodos() : List<HomeUiState>
    fun toggleStatus(index : Int)
    fun addTask(task : HomeUiState)
    fun updateTask(index: Int, task: HomeUiState)
    fun deleteTask(index: Int)
}
