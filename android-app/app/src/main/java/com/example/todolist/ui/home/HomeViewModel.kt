package com.example.todolist.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repository.ApiTodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ApiTodoRepository(application.applicationContext)

    private val _todoList = MutableStateFlow<List<HomeUiState>>(emptyList())
    val todoList: StateFlow<List<HomeUiState>> = _todoList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTodos()
    }

    fun loadTodos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _todoList.value = repository.getTodos()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleTodoStatus(id: Int) {
        viewModelScope.launch {
            try {
                val success = repository.toggleStatus(id)
                if (success) {
                    loadTodos()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTodo(id: Int) {
        viewModelScope.launch {
            try {
                val success = repository.deleteTask(id)
                if (success) {
                    loadTodos()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addTodo(task: HomeUiState) {
        viewModelScope.launch {
            try {
                val success = repository.addTask(task)
                if (success) {
                    loadTodos()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateTodo(task: HomeUiState) {
        viewModelScope.launch {
            try {
                val success = repository.updateTask(task)
                if (success) {
                    loadTodos()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}