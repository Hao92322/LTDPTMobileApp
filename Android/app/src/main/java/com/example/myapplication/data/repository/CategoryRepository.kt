package com.example.todoapp.data.repository

import com.example.todoapp.data.api.RetrofitClient
import com.example.todoapp.data.mock.MockDataSource
import com.example.todoapp.models.*

class CategoryRepository {

    private val useMockData = true

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            if (useMockData) {
                Result.Success(MockDataSource.getCategories())
            } else {
                val response = RetrofitClient.api.getCategories()
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.Success(response.body()?.data ?: emptyList())
                } else {
                    Result.Error(response.body()?.message ?: "Lỗi ${response.code()}")
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun createCategory(name: String): Result<Category> {
        return try {
            if (useMockData) {
                Result.Success(MockDataSource.createCategory(name))
            } else {
                val response = RetrofitClient.api.createCategory(CreateCategoryRequest(name))
                if (response.isSuccessful && response.body()?.success == true) {
                    Result.Success(response.body()!!.data!!)
                } else {
                    Result.Error(response.body()?.message ?: "Lỗi tạo category")
                }
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Lỗi kết nối")
        }
    }

    suspend fun updateCategory(id: Int, newName: String): Result<Unit> {
        return try {
            if (useMockData) {
                val success = MockDataSource.updateCategory(id, newName)
                if (success) Result.Success(Unit) else Result.Error("Không tìm thấy category")
            } else {
                val response = RetrofitClient.api.updateCategory(id, CreateCategoryRequest(newName))
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

    suspend fun deleteCategory(id: Int): Result<Unit> {
        return try {
            if (useMockData) {
                val success = MockDataSource.deleteCategory(id)
                if (success) Result.Success(Unit) else Result.Error("Không tìm thấy category")
            } else {
                val response = RetrofitClient.api.deleteCategory(id)
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