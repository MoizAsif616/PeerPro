// com.example.peerpro.utils.DialogUtils
package com.example.peerpro.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.peerpro.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogUtils {
  @SuppressLint("ResourceType")
  fun showSelectionDialog(
    context: Context,
    items: List<String>,
    onItemSelected: (String) -> Unit
  ) {
    val dialog = MaterialAlertDialogBuilder(context)
      .setCancelable(true)
      .create()

    // Get theme colors
    val typedArray = context.obtainStyledAttributes(
      intArrayOf(
        R.attr.bgSecondary,
        R.attr.textPrimary
      )
    )
    val bgColor = typedArray.getColor(0, Color.TRANSPARENT)
    val textColor = typedArray.getColor(1, Color.BLACK)
    typedArray.recycle()

    val layout = LinearLayout(context).apply {
      orientation = LinearLayout.VERTICAL
      setBackgroundColor(bgColor)
      setPadding(5.dpToPx(context), 15.dpToPx(context), 5.dpToPx(context), 15.dpToPx(context))
      layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
    }

    items.forEach { item ->
      TextView(context).apply {
        text = item
        textSize = 16f
        setPadding(32, 13, 32, 13)
        setTextColor(textColor)
        setOnClickListener {
          onItemSelected(item)
          dialog.dismiss()
        }
        layout.addView(this)
      }
    }

    dialog.setView(layout)
    dialog.show()
  }

  fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
  }
}