package com.example.peerpro

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.peerpro.databinding.ActivityPostTutorBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.example.peerpro.utils.DialogUtils

class PostTutorActivity : AppCompatActivity() {
  private lateinit var binding: ActivityPostTutorBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityPostTutorBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupClickListeners()
  }

  private fun setupClickListeners() {
    binding.preferredGender.setOnClickListener {
      showSelectionDialog(
        listOf("Any", "Male", "Female"),
        binding.preferredGender
      )
    }

    binding.sessionType.setOnClickListener {
      showSelectionDialog(
        listOf("Flexible", "Online", "On-Campus"),
        binding.sessionType
      )
    }

    binding.availableDays.setOnClickListener {
      showSelectionDialog(
        listOf("Flexible", "Weekdays", "Weekends", "Mon,wed,fri", "Tue,thu", "Mon-sat", "Mon-sun"),
        binding.availableDays
      )
    }

    binding.timeWindow.setOnClickListener {
      showSelectionDialog(
        listOf("Flexible", "Morning", "Afternoon", "Evening", "Night"),
        binding.timeWindow
      )
    }

    binding.sessionPricing.setOnClickListener {
      showSelectionDialog(
        listOf("One-time", "Per session", "Per week", "Per month"),
        binding.sessionPricing
      )
    }

    binding.backBtn.setOnClickListener {
      onBackPressed()
    }
  }

  @SuppressLint("ResourceType")
  private fun showSelectionDialog(items: List<String>, textView: TextView) {

    val typedArray = theme.obtainStyledAttributes(
      intArrayOf(
        R.attr.textPrimary
      )
    )
    val textColor = typedArray.getColor(1, Color.BLACK)
    typedArray.recycle()

    DialogUtils.showSelectionDialog(
      context = this,
      items = items
    ) { selectedItem ->
      textView.text = selectedItem
      textView.setTextColor(textColor)
    }
  }
}