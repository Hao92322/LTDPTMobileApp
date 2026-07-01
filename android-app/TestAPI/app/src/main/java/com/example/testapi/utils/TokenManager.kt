package com.example.testapi.utils

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * TokenManager - Quản lý JWT Token
 */
object TokenManager {

    private const val PREFS_NAME = "secure_auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_TOKEN_EXPIRY = "token_expiry_time"
    private const val KEY_USER_ID = "user_id"

    private var encryptedPrefs: SharedPreferences? = null

    /**
     * EncryptedSharedPreferences
     */
    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        if (encryptedPrefs == null) {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            encryptedPrefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        return encryptedPrefs!!
    }

    /**
     * LƯU TOKEN SAU KHI LOGIN
     */
    fun saveToken(
        context: Context,
        accessToken: String,
        expiresIn: Long = 3600,
        userId: String? = null
    ) {
        try {
            val prefs = getEncryptedPrefs(context)
            // ✅ FIX: Đảm bảo expiresIn luôn >= 3600 (1 giờ)
            val safeExpiresIn = if (expiresIn > 0) expiresIn else 3600L
            val expiryTime = System.currentTimeMillis() + (safeExpiresIn * 1000)

            Log.d(TAG, "💾 Saving token with expiry: $safeExpiresIn seconds")
            Log.d(TAG, "⏰ Expiry time: $expiryTime, Current: ${System.currentTimeMillis()}")

            val editor = prefs.edit()
            editor.putString(KEY_ACCESS_TOKEN, accessToken)
            editor.putLong(KEY_TOKEN_EXPIRY, expiryTime)
            if (userId != null) {
                editor.putString(KEY_USER_ID, userId)
            }

            val success = editor.commit()

            Log.d(TAG, "💾 Save token: success=$success")

            val verifyToken = prefs.getString(KEY_ACCESS_TOKEN, null)
            val verifyExpiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
            Log.d(TAG, "🔍 Verify token after save: ${if (verifyToken != null) "OK" else "FAILED"}")
            Log.d(TAG, "🔍 Verify expiry after save: $verifyExpiry")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving token: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * LẤY ACCESS TOKEN
     */
    fun getAccessToken(context: Context): String? {
        val prefs = getEncryptedPrefs(context)
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0)

        return if (token != null && System.currentTimeMillis() < expiryTime) {
            token
        } else {
            clearToken(context)
            null
        }
    }

    /**
     * KIỂM TRA ĐÃ LOGIN CHƯA
     */
    fun isLoggedIn(context: Context): Boolean {
        return getAccessToken(context) != null
    }

    /**
     * KIỂM TRA TOKEN SẮP HẾT HẠN
     */
    fun isTokenExpiringSoon(context: Context, thresholdMinutes: Long = 5): Boolean {
        val prefs = getEncryptedPrefs(context)
        val expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        val timeUntilExpiry = expiryTime - System.currentTimeMillis()
        return timeUntilExpiry < (thresholdMinutes * 60 * 1000)
    }

    /**
     * XÓA TOKEN (LOGOUT)
     */
    fun clearToken(context: Context) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit().clear().apply()
    }

    /**
     * LẤY USER ID
     */
    fun getUserId(context: Context): String? {
        return getEncryptedPrefs(context).getString(KEY_USER_ID, null)
    }
}