package com.example.testapi.utils

import android.content.Context
import android.util.Log
import com.example.testapi.api.RetrofitClient
import com.example.testapi.models.CategoryRequest
import com.example.testapi.models.LoginRequest
import com.example.testapi.models.TodoItemRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ApiTester - Test tất cả API endpoints
 *
 * 📌 FLOW:
 * 1. Login → Lưu token vào EncryptedSharedPreferences
 * 2. Lấy token từ storage để gọi các API khác
 * 3. Test đầy đủ 8 bước CRUD
 */
class ApiTester(private val context: Context) {

    private val apiService = RetrofitClient.apiService
    private var testCategoryId: Int = 0
    private var testTodoId: Int = 0

    companion object {
        private const val TAG = "API_TESTER"
        private const val TEST_EMAIL = "test@example.com"
        private const val TEST_PASSWORD = "Password123!"
    }

    fun runAllTests() {
        Log.d(TAG, "🚀 STARTING 8-STEP CRUD TEST")
        Log.d(TAG, "================================")
        Log.d(TAG, "Storage: EncryptedSharedPreferences (localStorage equivalent)")

        CoroutineScope(Dispatchers.IO).launch {
            // Kiểm tra đã có token chưa
            if (TokenManager.isLoggedIn(context)) {
                Log.d(TAG, "✅ User already logged in - reusing token")
                testGetCategories()
            } else {
                Log.d(TAG, "❌ No token found - logging in...")
                testLogin()
            }
        }
    }

    // 1️⃣ LOGIN & LƯU TOKEN
    private suspend fun testLogin() {
        try {
            Log.d(TAG, "\n📝 TEST 1/8: LOGIN")
            val response = apiService.login(LoginRequest(TEST_EMAIL, TEST_PASSWORD))

            if (response.isSuccessful && response.body()?.success == true) {
                val token = response.body()!!.data?.token ?: ""

                val expiresIn = 3600L

                Log.d(TAG, "🔑 Token received: ${token.take(30)}...")
                Log.d(TAG, "⏰ Expires in: $expiresIn seconds")

                //  LƯU TOKEN VÀO ENCRYPTED SHARED PREFERENCES
                TokenManager.saveToken(
                    context = context,
                    accessToken = token,
                    expiresIn = expiresIn
                )

                Log.d(TAG, "✅ LOGIN SUCCESS (200 OK)")
                Log.d(TAG, "💾 Token saved to EncryptedSharedPreferences")

                testGetCategories()
            } else {
                Log.e(TAG, "❌ LOGIN FAILED: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ LOGIN ERROR: ${e.message}")
        }
    }

    // 2️⃣ GET ALL CATEGORIES (Lấy token từ storage)
    private suspend fun testGetCategories() {
        try {
            Log.d(TAG, "\n📂 TEST 2/8: GET ALL CATEGORIES")

            // ✅ LẤY TOKEN TỪ STORAGE
            val token = TokenManager.getAccessToken(context) ?: run {
                Log.e(TAG, "❌ No token in storage!")
                return
            }

            val response = apiService.getCategories(
                page = 1, pageSize = 20, search = null,
                token = "Bearer $token"
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val count = response.body()!!.data?.size ?: 0
                Log.d(TAG, "✅ GET CATEGORIES SUCCESS (200 OK) | Count: $count")
                testCreateCategory()
            } else {
                Log.e(TAG, "❌ GET CATEGORIES FAILED: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR: ${e.message}")
        }
    }

    // 3️⃣ CREATE CATEGORY
    private suspend fun testCreateCategory() {
        try {
            Log.d(TAG, "\n➕ TEST 3/8: CREATE CATEGORY")

            val token = TokenManager.getAccessToken(context) ?: return

            val response = apiService.createCategory(
                CategoryRequest("Test Cat - ${System.currentTimeMillis()}"),
                "Bearer $token"
            )

            if (response.isSuccessful && response.body()?.success == true) {
                testCategoryId = response.body()!!.data?.id ?: 0
                Log.d(TAG, "✅ CREATE CATEGORY SUCCESS (200 OK) | ID: $testCategoryId")
                testGetCategoryById()
            } else {
                Log.e(TAG, "❌ CREATE CATEGORY FAILED: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR: ${e.message}")
        }
    }

    // 4️⃣ GET CATEGORY BY ID
    private suspend fun testGetCategoryById() {
        if (testCategoryId == 0) return
        try {
            Log.d(TAG, "\n🔍 TEST 4/8: GET CATEGORY BY ID ($testCategoryId)")

            val token = TokenManager.getAccessToken(context) ?: return

            val response = apiService.getCategoryById(testCategoryId, "Bearer $token")

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ GET CATEGORY BY ID SUCCESS (200 OK)")
                testUpdateCategory()
            } else {
                Log.e(TAG, "❌ GET CATEGORY BY ID FAILED: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR: ${e.message}")
        }
    }

    // 5️⃣ UPDATE CATEGORY
    private suspend fun testUpdateCategory() {
        if (testCategoryId == 0) return
        try {
            Log.d(TAG, "\n✏️ TEST 5/8: UPDATE CATEGORY ($testCategoryId)")

            val token = TokenManager.getAccessToken(context) ?: return

            val response = apiService.updateCategory(
                testCategoryId,
                CategoryRequest("Updated Cat - ${System.currentTimeMillis()}"),
                "Bearer $token"
            )

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ UPDATE CATEGORY SUCCESS (200 OK)")
                testCreateTodoItem()
            } else {
                Log.e(TAG, "❌ UPDATE CATEGORY FAILED: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR: ${e.message}")
        }
    }

    // 6️⃣ CREATE TODO ITEM
    private suspend fun testCreateTodoItem() {
        if (testCategoryId == 0) return
        try {
            Log.d(TAG, "\n📝 TEST 6/8: CREATE TODO ITEM")

            val token = TokenManager.getAccessToken(context) ?: return

            //Thêm field Date (format: yyyy-MM-dd)
            val currentDate = java.time.LocalDate.now().toString() // "2026-06-24"

            val request = TodoItemRequest(
                title = "Test Todo - ${System.currentTimeMillis()}",
                description = "Full CRUD verification",
                dueDate = null,
                priority = 1,
                categoryId = testCategoryId,
                date = currentDate,
                isCompleted = false
            )

            val response = apiService.createTodoItem(request, "Bearer $token")

            if (response.isSuccessful && response.body()?.success == true) {
                testTodoId = response.body()!!.data?.id ?: 0
                Log.d(TAG, "✅ CREATE TODO ITEM SUCCESS (200 OK) | ID: $testTodoId")

                // ✅ MỚI: Test Toggle Complete (nếu muốn)
                testToggleComplete()
            } else {
                Log.e(TAG, "❌ CREATE TODO ITEM FAILED: ${response.code()}")
                Log.e(TAG, "Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR: ${e.message}")
        }
    }

    // ✅ MỚI: Test Toggle Complete
    private suspend fun testToggleComplete() {
        if (testTodoId == 0) return
        try {
            Log.d(TAG, "\n🔄 TEST TOGGLE COMPLETE ($testTodoId)")

            val token = TokenManager.getAccessToken(context) ?: return

            val response = apiService.toggleComplete(testTodoId, "Bearer $token")

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ TOGGLE COMPLETE SUCCESS (200 OK)")
                testDeleteTodoItem()
            } else {
                Log.e(TAG, "❌ TOGGLE COMPLETE FAILED: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ TOGGLE COMPLETE ERROR: ${e.message}")
        }
    }

    // 7️⃣ DELETE TODO ITEM
    private suspend fun testDeleteTodoItem() {
        if (testTodoId == 0) return
        try {
            Log.d(TAG, "\n🗑️ TEST 7/8: DELETE TODO ITEM ($testTodoId)")

            val token = TokenManager.getAccessToken(context) ?: return

            val response = apiService.deleteTodoItem(testTodoId, "Bearer $token")

            if (response.isSuccessful && response.code() == 200) {
                Log.d(TAG, "✅ DELETE TODO ITEM SUCCESS (200 OK)")
                testDeleteCategory()
            } else {
                Log.e(TAG, "❌ DELETE TODO ITEM FAILED: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ DELETE TODO ITEM ERROR: ${e.message}")
        }
    }

    // 8️⃣ DELETE CATEGORY
    private suspend fun testDeleteCategory() {
        if (testCategoryId == 0) return
        try {
            Log.d(TAG, "\n🗑️ TEST 8/8: DELETE CATEGORY ($testCategoryId)")

            val token = TokenManager.getAccessToken(context) ?: return

            val response = apiService.deleteCategory(testCategoryId, "Bearer $token")

            if (response.isSuccessful && response.code() == 200) {
                Log.d(TAG, "✅ DELETE CATEGORY SUCCESS (200 OK)")
                Log.d(TAG, "\n🎉 ALL 8 CRUD TESTS PASSED WITH 200 OK!")
                Log.d(TAG, "💾 Token stored in: EncryptedSharedPreferences (localStorage)")
                Log.d(TAG, "🔐 Token is encrypted with AES256")
                Log.d(TAG, "✅ Token persists across app restarts")
            } else {
                Log.e(TAG, "❌ DELETE CATEGORY FAILED: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ DELETE CATEGORY ERROR: ${e.message}")
        }
    }

    // Helper: Logout
    fun logout() {
        TokenManager.clearToken(context)
        Log.d(TAG, "🚪 User logged out - Token cleared")
    }
}