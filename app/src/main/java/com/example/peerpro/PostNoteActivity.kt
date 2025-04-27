package com.example.peerpro

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.peerpro.databinding.ActivityPostNoteBinding
import com.example.peerpro.utils.DialogUtils

class PostNoteActivity : AppCompatActivity() {
  private lateinit var binding: ActivityPostNoteBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityPostNoteBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupClickListeners()
  }

  private fun setupClickListeners() {

    binding.notesType.setOnClickListener {
      showSelectionDialog(
        listOf("Handwritten", "Printed"),
        binding.notesType
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
    val textColor = typedArray.getColor(0, Color.BLACK)
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