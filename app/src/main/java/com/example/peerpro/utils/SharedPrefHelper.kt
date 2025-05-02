package com.example.peerpro.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.concurrent.TimeUnit

class SharedPrefHelper(context: Context) {
  private val sharedPref: SharedPreferences =
    context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)

  // Save token + expiry timestamp (e.g., 1 hour later)
  fun saveToken(token: String) {
    sharedPref.edit {
      putString("access_token", token)
      putLong("expiry_time", System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)) // 1 hour expiry
    }
  }

  // Retrieve token if not expired
  fun getToken(): String? {
    val token = sharedPref.getString("access_token", null)
    val expiryTime = sharedPref.getLong("expiry_time", 0)
    return if (token != null && System.currentTimeMillis() < expiryTime) token else null
  }

  // Clear on logout
  fun clearToken() {
    sharedPref.edit {
      remove("access_token")
      remove("expiry_time")
    }
  }
}