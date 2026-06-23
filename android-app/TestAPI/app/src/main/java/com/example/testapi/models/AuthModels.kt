package com.example.testapi.models

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData?
)

data class AuthData(
    val token: String,
    val expiresIn: Int
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
    val statusCode: Int = 200
)