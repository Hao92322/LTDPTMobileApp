package com.example.testapi

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.testapi.utils.ApiTester
import com.example.testapi.utils.TokenManager

class MainActivity : AppCompatActivity() {

    private lateinit var apiTester: ApiTester

    companion object {
        private const val TAG = "MAIN_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Khởi tạo ApiTester với context
        apiTester = ApiTester(this)

        Log.d(TAG, "🚀 App started")
        Log.d(TAG, "====================================")

        // Kiểm tra trạng thái login
        if (TokenManager.isLoggedIn(this)) {
            Log.d(TAG, "✅ User already logged in")
            val token = TokenManager.getAccessToken(this)
            Log.d(TAG, "🔑 Token (first 30 chars): ${token?.take(30)}...")

            if (TokenManager.isTokenExpiringSoon(this)) {
                Log.d(TAG, "⚠️ Token expiring soon")
            }
        } else {
            Log.d(TAG, "❌ No login session - will login")
        }

        // Bắt đầu test API
        Log.d(TAG, "📡 Starting API tests...")
        apiTester.runAllTests()
    }

    override fun onDestroy() {
        super.onDestroy()
        // KHÔNG xóa token khi đóng app (persistent)

    }
}