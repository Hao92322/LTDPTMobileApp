package com.example.todolist.ui.home

import androidx.lifecycle.ViewModel
import com.example.todolist.data.repository.MockTodoRepositoryImpl
import com.example.todolist.data.repository.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
class HomeViewModel(
    private val repository: TodoRepository = MockTodoRepositoryImpl()
) : ViewModel() {
    private val _todoList = MutableStateFlow<List<HomeUiState>>(emptyList())
    val todoList = _todoList.asStateFlow()

    init {
        // Use toList() to ensure we have a static snapshot, not a reference to a mutable list
        _todoList.value = repository.getTodos().toList()
    }

    fun toggleTodoStatus(index: Int) {
        repository.toggleStatus(index)
        // Refresh the flow with the latest data from the repository
        _todoList.value = repository.getTodos().toList()
    }

    fun updateTodo(index: Int, task: HomeUiState) {
        repository.updateTask(index, task)
        _todoList.value = repository.getTodos().toList()
    }

    fun deleteTodo(index: Int) {
        repository.deleteTask(index)
        _todoList.value = repository.getTodos().toList()
    }
}
