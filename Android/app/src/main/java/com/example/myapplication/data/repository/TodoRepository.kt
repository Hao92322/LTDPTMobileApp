package com.example.todoapp.data.repository

import com.example.todoapp.data.api.RetrofitClient
import com.example.todoapp.data.mock.MockDataSource
import com.example.todoapp.models.*

class TodoRepository {

    private val useMockData = true

    suspend fun getTodosByCategory(categoryId: Int): Result<List<TodoItem>> {
        return try {
            if (useMockData) {
                Result.Success(MockDataSource.getTodoByCategory(categoryId))
            } else {
                val response = RetrofitClient.api.getToDoByCategory(categoryId)
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.Success(response.body()?.data ?: emptyList())
                } else {
                    Result.Error(response.body()?.message ?: "Lỗi tải todos")
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun createTodo(request: CreateTodoRequest): Result<TodoItem> {
        return try {
            if (useMockData) {
                Result.Success(
                    MockDataSource.createTodo(
                        request.title, request.description, request.categoryId
                    )
                )
            } else {
                val response = RetrofitClient.api.createToDo(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.Success(response.body()!!.data!!)
                } else {
                    Result.Error(response.body()?.message ?: "Lỗi tạo todo")
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun updateTodo(id: Int, request: CreateTodoRequest, isCompleted: Boolean): Result<Unit> {
        return try {
            if (useMockData) {
                val success = MockDataSource.updateTodo(
                    id, request.title, request.description, isCompleted
                )
                if (success) Result.Success(Unit) else Result.Error("Không tìm thấy todo")
            } else {
                val response = RetrofitClient.api.updateTodo(id, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.Success(Unit)
                } else {
                    Result.Error(response.body()?.message ?: "Lỗi cập nhật")
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun deleteTodo(id: Int): Result<Unit> {
        return try {
            if (useMockData) {
                val success = MockDataSource.deleteTodo(id)
                if (success) Result.Success(Unit) else Result.Error("Không tìm thấy todo")
            } else {
                val response = RetrofitClient.api.deleteTodo(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.Success(Unit)
                } else {
                    Result.Error(response.body()?.message ?: "Lỗi xóa")
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }
}