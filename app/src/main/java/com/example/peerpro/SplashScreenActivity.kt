package com.example.peerpro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.peerpro.models.User
import com.example.peerpro.utils.SharedPrefHelper
import com.example.peerpro.utils.UserCache
import com.google.android.gms.common.util.SharedPreferencesUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

  val auth = FirebaseAuth.getInstance()
  val db = FirebaseFirestore.getInstance()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.splashscreen)
    checkAuthAndRedirect()
  }

  private fun checkAuthAndRedirect() {
    auth.currentUser?.let { user ->
      val uid = user.uid
      Log.d("L6", "User is logged in with UID: $uid")
      fetchUserDataAndRedirect(uid)
    } ?: run {
      Log.d("L6", "No user is logged in")
      redirectToAuth()
    }
  }

  private fun fetchUserDataAndRedirect(uid: String) {
    try {
      Log.d("L6", "Fetching user data for UID: $uid")
      FirebaseFirestore.getInstance()
        .collection("users")
        .document(uid)
        .get()
        .addOnSuccessListener { document ->
          if (document.exists()) {
            val user : User? = document.toObject(User::class.java)
            if (user != null) {
              UserCache.setUser(user)
              UserCache.setId(uid)
            }
          }
          Log.d("L6", "Fetched user.")
          redirectToMain()
        }
        .addOnFailureListener {
          redirectToAuth()
        }
    } catch (e: Exception) {
      redirectToAuth()
    }
  }

  private fun redirectToMain() {
    startActivity(Intent(this, MainActivity::class.java))
    finish()
  }

  private fun redirectToAuth() {
    startActivity(Intent(this, Auth::class.java))
    finish()
  }
}
