package com.example.testapi.api

import com.example.testapi.models.*
import retrofit2.Response
import retrofit2.http.*

// ========== ENDPOINT API ==========
interface ApiService {

    // ========== AUTH API ==========
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<Unit>>

    // ========== CATEGORY API ==========
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
    suspend fun deleteCategory(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Void>  //Backend trả về string, dùng Void để bỏ qua parse

    // ========== TODO ITEM API ==========
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
    suspend fun deleteTodoItem(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Void>

    @PATCH("api/todoitem/{id}/toggle")
    suspend fun toggleComplete(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<ApiResponse<TodoItem>>
}