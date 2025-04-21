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

    setInitialFrameWidth()

    selectSignUp()

    binding.llb1.setOnClickListener { selectSignUp() }
    binding.llb2.setOnClickListener { selectLogIn() }
  }

  private fun setInitialFrameWidth() {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    val screenWidth = displayMetrics.widthPixels
    val frameWidth = (screenWidth * 0.85).toInt()
      .coerceAtMost(resources.getDimensionPixelSize(R.dimen.max_fragment_width)) // Max width 500dp

    binding.form.updateLayoutParams<ViewGroup.LayoutParams> {
      width = frameWidth
    }
  }

  fun selectSignUp() {
    updateButtonStates("signup")
    loadFragment(Signup())
  }

  fun selectLogIn() {
    updateButtonStates("login")
    loadFragment(Login())
  }

  fun selectVerify() {
    updateButtonStates("verify")
    loadFragment(Verify())
  }

  fun selectGetLink() {
    updateButtonStates("reset")
    loadFragment(SendLink())
  }

  private fun updateButtonStates(state: String) {
    val activeTextColor = MaterialColors.getColor(this, R.attr.buttonTextPrimary, Color.WHITE)
    val inactiveTextColor = MaterialColors.getColor(this, R.attr.textSecondary, Color.GRAY)
    val peerLightColor = resources.getColor(R.color.peerLight, theme)

    when (state) {
      "signup" -> {
        binding.suBtn.setTextColor(activeTextColor)
        binding.liBtn.setTextColor(inactiveTextColor)
        binding.llb1.setBackgroundResource(R.drawable.rounded_button)
        binding.llb2.setBackgroundResource(android.R.color.transparent)
      }
      "login" -> {
        binding.liBtn.setTextColor(activeTextColor)
        binding.suBtn.setTextColor(inactiveTextColor)
        binding.llb2.setBackgroundResource(R.drawable.rounded_button)
        binding.llb1.setBackgroundResource(android.R.color.transparent)
      }
      "verify" -> {
        binding.liBtn.setTextColor(inactiveTextColor)
        binding.suBtn.setTextColor(peerLightColor)
        binding.llb1.setBackgroundResource(R.drawable.rounded_border_only)
        binding.llb2.setBackgroundResource(android.R.color.transparent)
      }
      "reset" -> {
        binding.liBtn.setTextColor(peerLightColor)
        binding.suBtn.setTextColor(inactiveTextColor)
        binding.llb1.setBackgroundResource(android.R.color.transparent)
        binding.llb2.setBackgroundResource(R.drawable.rounded_border_only)
      }
    }
  }

  private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
    supportFragmentManager.commit {
      setReorderingAllowed(true)
      replace(R.id.form, fragment)
    }
  }
}