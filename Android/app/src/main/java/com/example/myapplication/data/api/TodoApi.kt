package com.example.todoapp.data.api

import com.example.todoapp.models.*
import retrofit2.Response
import retrofit2.http.*

interface TodoApi {

    @GET("api/category")
    suspend fun getCategories(): Response<ApiResponse<List<Category>>>

    @POST("api/category")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<ApiResponse<Category>>

    @PUT("api/category/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: CreateCategoryRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("api/category/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @GET("api/todoitem/category/{categoryId}")
    suspend fun getToDoByCategory(@Path("categoryId") categoryId: Int): Response<ApiResponse<List<TodoItem>>>

    @POST("api/todoitem")
    suspend fun createToDo(@Body request: CreateTodoRequest): Response<ApiResponse<TodoItem>>

    @PUT("api/todoitem/{id}")
    suspend fun updateTodo(
        @Path("id") id: Int,
        @Body request: CreateTodoRequest
    ): Response<ApiResponse<Unit>>

    @DELETE("api/todoitem/{id}")
    suspend fun deleteTodo(@Path("id") id: Int): Response<ApiResponse<Unit>>
}