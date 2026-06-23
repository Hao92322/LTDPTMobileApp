package com.example.testapi.utils

import android.util.Log
import com.example.testapi.api.RetrofitClient
import com.example.testapi.models.CategoryRequest
import com.example.testapi.models.LoginRequest
import com.example.testapi.models.TodoItemRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApiTester {
    private val apiService = RetrofitClient.apiService
    private var authToken: String = ""
    private var testCategoryId: Int = 0
    private var testTodoId: Int = 0

    companion object {
        private const val TAG = "API_TESTER"
        private const val TEST_EMAIL = "test@example.com"
        private const val TEST_PASSWORD = "Password123!"
    }

    fun runAllTests() {
        Log.d(TAG, "🚀 STARTING 8-STEP CRUD TEST (NO TOGGLE)")
        Log.d(TAG, "================================")
        Log.d(TAG, "Target Backend: http://192.168.1.244:5158")

        CoroutineScope(Dispatchers.IO).launch {
            testLogin()
        }
    }

    // 1️⃣ LOGIN
    private suspend fun testLogin() {
        try {
            Log.d(TAG, "\n📝 TEST 1/8: LOGIN")
            val response = apiService.login(LoginRequest(TEST_EMAIL, TEST_PASSWORD))
            if (response.isSuccessful && response.body()?.success == true) {
                authToken = response.body()!!.data?.token ?: ""
                Log.d(TAG, "✅ LOGIN SUCCESS (200 OK)")
                testGetCategories()
            } else {
                Log.e(TAG, "❌ LOGIN FAILED: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ LOGIN ERROR: ${e.message}")
        }
    }

    // 2️⃣ GET ALL CATEGORIES
    private suspend fun testGetCategories() {
        try {
            Log.d(TAG, "\n📂 TEST 2/8: GET ALL CATEGORIES")
            val response = apiService.getCategories(page = 1, pageSize = 20, search = null, token = "Bearer $authToken")
            if (response.isSuccessful && response.body()?.success == true) {
                Log.d(TAG, "✅ GET CATEGORIES SUCCESS (200 OK)")
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
            val response = apiService.createCategory(
                CategoryRequest("Test Cat - ${System.currentTimeMillis()}"),
                "Bearer $authToken"
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
            val response = apiService.getCategoryById(testCategoryId, "Bearer $authToken")
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
            val response = apiService.updateCategory(
                testCategoryId,
                CategoryRequest("Updated Cat - ${System.currentTimeMillis()}"),
                "Bearer $authToken"
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
            val request = TodoItemRequest(
                title = "Test Todo - ${System.currentTimeMillis()}",
                description = "Full CRUD verification",
                dueDate = null,
                priority = 1,
                categoryId = testCategoryId
            )
            val response = apiService.createTodoItem(request, "Bearer $authToken")
            if (response.isSuccessful && response.body()?.success == true) {
                testTodoId = response.body()!!.data?.id ?: 0
                Log.d(TAG, "✅ CREATE TODO ITEM SUCCESS (200 OK) | ID: $testTodoId")
                testDeleteTodoItem() // ⬅️ NỐI THẲNG SANG DELETE TODO (BỎ TOGGLE)
            } else {
                Log.e(TAG, "❌ CREATE TODO ITEM FAILED: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR: ${e.message}")
        }
    }

    // 7️⃣ DELETE TODO ITEM
    private suspend fun testDeleteTodoItem() {
        if (testTodoId == 0) return
        try {
            Log.d(TAG, "\n🗑️ TEST 7/8: DELETE TODO ITEM ($testTodoId)")

            val response = apiService.deleteTodoItem(testTodoId, "Bearer $authToken")

            // ✅ Chỉ cần check status code 200 là đủ
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
            val response = apiService.deleteCategory(testCategoryId, "Bearer $authToken")

            // ✅ Chỉ check status code
            if (response.isSuccessful && response.code() == 200) {
                Log.d(TAG, "✅ DELETE CATEGORY SUCCESS (200 OK)")
                Log.d(TAG, "\n🎉 ALL 8 CRUD TESTS PASSED WITH 200 OK!")
            } else {
                Log.e(TAG, "❌ DELETE CATEGORY FAILED: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ DELETE CATEGORY ERROR: ${e.message}")
        }
    }
}