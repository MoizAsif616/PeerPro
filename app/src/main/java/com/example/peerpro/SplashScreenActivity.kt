package com.example.peerpro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.peerpro.utils.SharedPrefHelper

class SplashScreenActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.splashscreen)

    // Navigate to Auth activity after 5 seconds
    Handler(Looper.getMainLooper()).postDelayed({
//      val intent = Intent(this, Auth::class.java)
//      startActivity(intent)
//      finish()
      checkAuthAndRedirect()
    }, 2000)
  }

  private fun checkAuthAndRedirect() {
    val token = SharedPrefHelper(this).getToken()

    val destination = if (token != null) {
      // Token exists - go to MainActivity
      Intent(this, MainActivity::class.java)
    } else {
      // No token - go to AuthActivity
      Intent(this, Auth::class.java)
    }

    startActivity(destination)
    finish()
  }
}
