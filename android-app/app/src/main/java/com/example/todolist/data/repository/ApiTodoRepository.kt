package com.example.todolist.data.repository

import android.content.Context
import com.example.todolist.data.api.RetrofitClient
import com.example.todolist.data.api.TodoItem
import com.example.todolist.data.api.TodoItemRequest
import com.example.todolist.ui.home.HomeUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ApiTodoRepository(private val context: Context) {

    private val apiService = RetrofitClient.apiService

    private fun getToken(): String {
        val token = TokenManager.getAccessToken(context) ?: ""
        return "Bearer $token"
    }

    private fun mapToHomeUiState(item: TodoItem): HomeUiState {
        return HomeUiState(
            id = item.id,
            title = item.title,
            subtitle = item.description ?: "",
            createdate = item.createdAt?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
            } ?: LocalDateTime.now(),
            duedate = item.dueDate?.let {
                LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
            } ?: LocalDateTime.now(),
            priority = item.priority,
            isDone = item.isCompleted,
            category = "Default"
        )
    }

    suspend fun getTodos(): List<HomeUiState> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTodoItems(
                categoryId = 1,
                token = getToken()
            )

            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.map { mapToHomeUiState(it) } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addTask(task: HomeUiState): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = TodoItemRequest(
                title = task.title,
                description = task.subtitle,
                dueDate = task.duedate.format(DateTimeFormatter.ISO_DATE_TIME),
                priority = task.priority,
                categoryId = 1,
                date = task.createdate.toLocalDate().toString(),
                isCompleted = task.isDone
            )

            val response = apiService.createTodoItem(request, getToken())
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateTask(task: HomeUiState): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = TodoItemRequest(
                title = task.title,
                description = task.subtitle,
                dueDate = task.duedate.format(DateTimeFormatter.ISO_DATE_TIME),
                priority = task.priority,
                categoryId = 1,
                date = task.createdate.toLocalDate().toString(),
                isCompleted = task.isDone
            )

            val response = apiService.updateTodoItem(task.id, request, getToken())
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteTask(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteTodoItem(id, getToken())
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun toggleStatus(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = apiService.toggleComplete(id, getToken())
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}