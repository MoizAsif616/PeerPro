package com.example.peerpro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.splashscreen)

    // Navigate to Auth activity after 5 seconds
    Handler(Looper.getMainLooper()).postDelayed({
      val intent = Intent(this, Auth::class.java)
      startActivity(intent)
      finish()
    }, 5000)
  }
}
