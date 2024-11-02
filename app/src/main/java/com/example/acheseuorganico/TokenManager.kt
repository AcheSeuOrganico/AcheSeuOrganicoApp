package com.example.acheseuorganico;

import android.content.Context;
import android.util.Base64;
import org.json.JSONObject;

class TokenManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveTokens(accessToken: String, refreshToken: String) {
        with(sharedPreferences.edit()) {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    fun getUserIdFromToken(): String? {
        val accessToken = getAccessToken() ?: return null

        return try {
            val tokenParts = accessToken.split(".")
            if (tokenParts.size != 3) return null

            val payloadBytes = Base64.decode(tokenParts[1], Base64.URL_SAFE)
            val payloadString = String(payloadBytes, Charsets.UTF_8)

            val payloadJson = JSONObject(payloadString)
            payloadJson.getString("user_id")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearTokens() {
        with(sharedPreferences.edit()) {
            remove("access_token")
            remove("refresh_token")
            apply()
        }
    }
}
