package com.example.testapi.api

import com.example.testapi.models.ApiResponse
import com.example.testapi.models.Category
import com.example.testapi.models.CategoryRequest
import com.example.testapi.models.LoginRequest
import com.example.testapi.models.LoginResponse
import com.example.testapi.models.RegisterRequest
import com.example.testapi.models.TodoItem
import com.example.testapi.models.TodoItemRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // ========== AUTH APIs ==========
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<Unit>>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ========== CATEGORY APIs ==========
    @GET("api/category")
    suspend fun getCategories(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("search") search: String? = null,
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<Category>>>

    @GET("api/category/{id}")
    suspend fun getCategoryById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Category>>

    @POST("api/category")
    suspend fun createCategory(
        @Body request: CategoryRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Category>>

    @PUT("api/category/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: CategoryRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Category>>

    @DELETE("api/category/{id}")
    suspend fun deleteCategory(@Path("id") id: Int, @Header("Authorization") token: String): Response<Void>

    // ========== TODOITEM APIs ==========
    @GET("api/todoitem")
    suspend fun getTodoItems(
        @Query("categoryId") categoryId: Int,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("search") search: String? = null,
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<TodoItem>>>

    @GET("api/todoitem/{id}")
    suspend fun getTodoItemById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<ApiResponse<TodoItem>>

    @POST("api/todoitem")
    suspend fun createTodoItem(
        @Body request: TodoItemRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<TodoItem>>

    @PUT("api/todoitem/{id}")
    suspend fun updateTodoItem(
        @Path("id") id: Int,
        @Body request: TodoItemRequest,
        @Header("Authorization") token: String
    ): Response<ApiResponse<TodoItem>>

    @DELETE("api/todoitem/{id}")
    suspend fun deleteTodoItem(@Path("id") id: Int, @Header("Authorization") token: String): Response<Void>

    @PATCH("api/todoitem/{id}/toggle-complete")
    suspend fun toggleComplete(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<ApiResponse<Map<String, Boolean>>>
}