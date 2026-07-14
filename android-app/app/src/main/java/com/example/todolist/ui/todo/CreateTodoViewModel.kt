package com.example.todolist.ui.todo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repository.ApiTodoRepository
import com.example.todolist.ui.home.HomeUiState
import kotlinx.coroutines.launch

class CreateTodoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ApiTodoRepository(application.applicationContext)

    fun createTodo(task: HomeUiState, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val success = repository.addTask(task)
                if (success) {
                    onSuccess()
                } else {
                    onError("Không thể tạo công việc")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Lỗi không xác định")
            }
        }
    }
}