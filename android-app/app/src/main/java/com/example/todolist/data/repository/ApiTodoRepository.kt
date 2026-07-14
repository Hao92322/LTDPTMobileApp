package com.example.todolist.data.repository

import android.content.Context
import com.example.todolist.data.api.CategoryRequest
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

    // ✅ Tự tạo category mặc định nếu user chưa có category nào
    suspend fun ensureDefaultCategory(): Int = withContext(Dispatchers.IO) {
        try {
            val catResponse = apiService.getCategories(token = getToken())
            if (catResponse.isSuccessful && catResponse.body()?.success == true) {
                val cats = catResponse.body()?.data ?: emptyList()
                if (cats.isNotEmpty()) {
                    return@withContext cats.first().id
                }
            }
            // Chưa có → tạo category mặc định
            val createResp = apiService.createCategory(CategoryRequest("Công việc chung"), getToken())
            if (createResp.isSuccessful && createResp.body()?.success == true) {
                return@withContext createResp.body()?.data?.id ?: 1
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext 1
    }

    private fun mapToHomeUiState(item: TodoItem, categoryName: String = "Default"): HomeUiState {
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
            category = categoryName,
            categoryId = item.categoryId ?: 1
        )
    }

    // ✅ Lấy tất cả todos (không cần categoryId - backend hỗ trợ nullable categoryId)
    suspend fun getTodos(): List<HomeUiState> = withContext(Dispatchers.IO) {
        try {
            // Load categories để map tên
            val catResponse = apiService.getCategories(token = getToken())
            val categoryMap: Map<Int, String> = if (catResponse.isSuccessful) {
                catResponse.body()?.data?.associate { it.id to it.name } ?: emptyMap()
            } else emptyMap()

            // Load tất cả todos (không truyền categoryId)
            val response = apiService.getTodoItems(token = getToken())

            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.data?.map { item ->
                    val catName = categoryMap[item.categoryId] ?: "Default"
                    mapToHomeUiState(item, catName)
                } ?: emptyList()
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
            // ✅ Đảm bảo có category trước khi tạo task
            val categoryId = if (task.categoryId > 0) task.categoryId
                             else ensureDefaultCategory()

            val request = TodoItemRequest(
                title = task.title,
                description = task.subtitle,
                dueDate = task.duedate.format(DateTimeFormatter.ISO_DATE_TIME),
                priority = task.priority,
                categoryId = categoryId,
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
                categoryId = task.categoryId,
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