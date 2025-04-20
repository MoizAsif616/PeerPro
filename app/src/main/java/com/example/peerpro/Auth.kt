package com.example.peerpro

import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.commit
import com.example.peerpro.databinding.ActivityAuthBinding
import com.google.android.material.color.MaterialColors

class Auth : AppCompatActivity() {

  private lateinit var binding: ActivityAuthBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityAuthBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Set initial state
    selectSignUp()

    // Click listeners
    binding.llb1.setOnClickListener { selectSignUp() }
    binding.llb2.setOnClickListener { selectLogIn() }
  }

  private fun selectSignUp() {
    // Update button states
    updateButtonStates(true)

    // Load SignUp fragment
    loadSignUpFragment()
  }

  fun selectLogIn() {
    // Update button states
    updateButtonStates(false)

    // Load Login fragment
    binding.form.post { // Ensure UI thread execution
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.form, Login())
        }
    }
  }

  private fun updateButtonStates(isSignUpActive: Boolean) {
    val activeTextColor = MaterialColors.getColor(this, R.attr.buttonTextPrimary, Color.WHITE)
    val inactiveTextColor = MaterialColors.getColor(this, R.attr.textSecondary, Color.GRAY)

    if (isSignUpActive) {
      binding.suBtn.setTextColor(activeTextColor)
      binding.liBtn.setTextColor(inactiveTextColor)
      binding.llb1.setBackgroundResource(R.drawable.rounded_button)
      binding.llb2.setBackgroundResource(android.R.color.transparent)
    } else {
      binding.liBtn.setTextColor(activeTextColor)
      binding.suBtn.setTextColor(inactiveTextColor)
      binding.llb2.setBackgroundResource(R.drawable.rounded_button)
      binding.llb1.setBackgroundResource(android.R.color.transparent)
    }
  }

  private fun loadSignUpFragment() {
    try {
      // 1. Calculate width first
      val displayMetrics = DisplayMetrics()
      windowManager.defaultDisplay.getMetrics(displayMetrics)
      val frameWidth = (displayMetrics.widthPixels * 0.85).toInt()
        .coerceAtMost(resources.getDimensionPixelSize(R.dimen.max_fragment_width))

      // 2. Update FrameLayout width
      binding.form.post { // Ensure UI thread execution
        binding.form.updateLayoutParams<ViewGroup.LayoutParams> {
          width = frameWidth
        }

        // 3. Perform fragment transaction after layout update
        supportFragmentManager.commit {
          setReorderingAllowed(true)
          replace(R.id.form, Signup())
          // Optional: addToBackStack(null) if you want back navigation
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      // Handle error appropriately
    }
  }
}