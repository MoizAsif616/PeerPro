package com.example.peerpro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.peerpro.models.User
import com.example.peerpro.utils.SharedPrefHelper
import com.example.peerpro.utils.UserCache
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashScreenActivity : AppCompatActivity() {

  val auth = FirebaseAuth.getInstance()
  val db = FirebaseFirestore.getInstance()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.splashscreen)

    // Navigate to Auth activity after 5 seconds
    Handler(Looper.getMainLooper()).postDelayed({
//      val intent = Intent(this, Auth::class.java)
//      startActivity(intent)
//      finish()
      checkAuthAndRedirect()
    }, 0)
  }

  private fun checkAuthAndRedirect() {
    val currentUser = auth.currentUser

    if (currentUser != null) {
      // User is already authenticated
      currentUser.getIdToken(true) // Force token refresh
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            // Token is valid (either existing or refreshed)
            val uid = currentUser.uid
            fetchUserDataAndRedirect(uid)
          } else {
            // Token refresh failed
            redirectToAuth()
          }
        }
    } else {
      // No user is signed in
      redirectToAuth()
    }
  }

  private fun fetchUserDataAndRedirect(uid: String) {
    try {
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
