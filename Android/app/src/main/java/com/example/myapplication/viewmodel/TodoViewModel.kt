package com.example.todoapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.repository.TodoRepository
import com.example.todoapp.models.CreateTodoRequest
import com.example.todoapp.models.Result
import com.example.todoapp.models.TodoItem
import kotlinx.coroutines.launch

class TodoViewModel : ViewModel() {

    private val repository = TodoRepository()

    private val _todos = MutableLiveData<Result<List<TodoItem>>>()
    val todos: LiveData<Result<List<TodoItem>>> = _todos

    private val _operationResult = MutableLiveData<Result<String>>()
    val operationResult: LiveData<Result<String>> = _operationResult

    private var currentCategoryId: Int = 0

    fun loadTodos(categoryId: Int) {
        currentCategoryId = categoryId
        _todos.value = Result.Loading
        viewModelScope.launch {
            _todos.value = repository.getTodosByCategory(categoryId)
        }
    }

    fun addTodo(title: String, description: String?) {
        if (title.isBlank()) {
            _operationResult.value = Result.Error("Tiêu đề không được để trống")
            return
        }

        viewModelScope.launch {
            _operationResult.value = Result.Loading

            val request = CreateTodoRequest(
                title = title,
                description = description,
                priority = 0,
                categoryId = currentCategoryId,
                isCompleted = false
            )

            when (val result = repository.createTodo(request)) {
                is Result.Success -> {
                    _operationResult.value = Result.Success("Đã thêm công việc")
                    loadTodos(currentCategoryId)
                }
                is Result.Error -> _operationResult.value = result
                is Result.Loading -> {}
            }
        }
    }

    fun updateTodo(id: Int, title: String, description: String?, isCompleted: Boolean) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            val request = CreateTodoRequest(
                title = title,
                description = description,
                priority = 0,
                categoryId = currentCategoryId,
                isCompleted = isCompleted
            )
            when (val result = repository.updateTodo(id, request, isCompleted)) {
                is Result.Success -> {
                    _operationResult.value = Result.Success("Đã cập nhật")
                    loadTodos(currentCategoryId)
                }
                is Result.Error -> _operationResult.value = result
                is Result.Loading -> {}
            }
        }
    }

    fun toggleTodo(todo: TodoItem) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading

            val request = CreateTodoRequest(
                title = todo.title,
                description = todo.description,
                priority = todo.priority,
                categoryId = todo.categoryId,
                isCompleted = !todo.isCompleted
            )

            when (val result = repository.updateTodo(todo.id, request, !todo.isCompleted)) {
                is Result.Success -> {
                    val message = if (!todo.isCompleted) "Đã đánh dấu hoàn thành" else "Đã bỏ đánh dấu"
                    _operationResult.value = Result.Success(message)
                    loadTodos(currentCategoryId)
                }
                is Result.Error -> _operationResult.value = result
                is Result.Loading -> {}
            }
        }
    }

    fun deleteTodo(id: Int) {
        viewModelScope.launch {
            _operationResult.value = Result.Loading
            when (val result = repository.deleteTodo(id)) {
                is Result.Success -> {
                    _operationResult.value = Result.Success("Đã xóa")
                    loadTodos(currentCategoryId)
                }
                is Result.Error -> _operationResult.value = result
                is Result.Loading -> {}
            }
        }
    }
}