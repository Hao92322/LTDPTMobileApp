package com.example.todolist.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.api.LoginRequest
import com.example.todolist.data.api.RegisterRequest
import com.example.todolist.data.api.RetrofitClient
import com.example.todolist.data.repository.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService
    private val context = application.applicationContext

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginResult = MutableStateFlow<LoginResult?>(null)
    val loginResult: StateFlow<LoginResult?> = _loginResult.asStateFlow()

    private val _registerResult = MutableStateFlow<RegisterResult?>(null)
    val registerResult: StateFlow<RegisterResult?> = _registerResult.asStateFlow()

    private fun parseErrorMsg(response: retrofit2.Response<*>): String {
        return try {
            val errorStr = response.errorBody()?.string()
            if (!errorStr.isNullOrBlank()) {
                val json = org.json.JSONObject(errorStr)
                json.optString("message", "Có lỗi xảy ra")
            } else {
                "Có lỗi xảy ra"
            }
        } catch (e: Exception) {
            "Có lỗi xảy ra"
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginResult.value = null

            withContext(Dispatchers.IO) {
                try {
                    val response = apiService.login(LoginRequest(email, password))

                    if (response.isSuccessful && response.body()?.success == true) {
                        val accessToken = response.body()?.data?.accessToken ?: ""
                        val refreshToken = response.body()?.data?.refreshToken ?: ""
                        val expiresIn = response.body()?.data?.expiresIn?.toLong() ?: 3600L

                        // Lưu token
                        TokenManager.saveToken(context, accessToken, refreshToken, expiresIn)

                        _loginResult.value = LoginResult(true, "Đăng nhập thành công")
                    } else {
                        val errorMsg = parseErrorMsg(response).ifBlank { "Email hoặc mật khẩu không đúng" }
                        _loginResult.value = LoginResult(false, errorMsg)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _loginResult.value = LoginResult(false, "Lỗi kết nối: ${e.message}")
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _registerResult.value = null

            withContext(Dispatchers.IO) {
                try {
                    val response = apiService.register(
                        RegisterRequest(
                            email = email,
                            password = password,
                            userName = name,
                            confirmPassword = confirmPassword
                        )
                    )

                    if (response.isSuccessful && response.body()?.success == true) {
                        _registerResult.value = RegisterResult(true, "Đăng ký thành công! Vui lòng đăng nhập.")
                    } else {
                        val errorMsg = parseErrorMsg(response).ifBlank { "Đăng ký thất bại" }
                        _registerResult.value = RegisterResult(false, errorMsg)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _registerResult.value = RegisterResult(false, "Lỗi kết nối: ${e.message}")
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return TokenManager.isLoggedIn(context)
    }

    fun clearResults() {
        _loginResult.value = null
        _registerResult.value = null
    }
}

data class LoginResult(val success: Boolean, val message: String)
data class RegisterResult(val success: Boolean, val message: String)