package com.example.todolist.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenManager {

    private const val PREFS_NAME = "secure_auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_TOKEN_EXPIRY = "token_expiry_time"

    private var encryptedPrefs: SharedPreferences? = null

    private fun getEncryptedPrefs(context: Context): SharedPreferences {
        if (encryptedPrefs == null) {
            try {
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
            } catch (e: Exception) {
                context.deleteSharedPreferences(PREFS_NAME)
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
        }
        return encryptedPrefs!!
    }

    fun saveToken(
        context: Context,
        accessToken: String,
        refreshToken: String = "",
        expiresIn: Long = 3600
    ) {
        val prefs = getEncryptedPrefs(context)
        val expiryTime = System.currentTimeMillis() + (expiresIn * 1000)

        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .commit()
    }

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

    fun isLoggedIn(context: Context): Boolean {
        return getAccessToken(context) != null
    }

    fun clearToken(context: Context) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit().clear().commit()
    }

    // ===== GIẢI MÃ JWT ĐỂ LẤY THÔNG TIN USER =====
    private fun getJwtPayloadField(token: String, fieldKey: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT))
            val json = org.json.JSONObject(payload)
            if (json.has(fieldKey)) {
                json.getString(fieldKey)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getUsername(context: Context): String {
        val token = getAccessToken(context) ?: return "User"
        return getJwtPayloadField(token, "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name")
            ?: getJwtPayloadField(token, "unique_name")
            ?: "User"
    }

    fun getEmail(context: Context): String {
        val token = getAccessToken(context) ?: return "user@gmail.com"
        return getJwtPayloadField(token, "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress")
            ?: getJwtPayloadField(token, "email")
            ?: "user@gmail.com"
    }
}