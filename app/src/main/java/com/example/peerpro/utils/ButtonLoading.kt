package com.example.peerpro.utils

import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import com.example.peerpro.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors

object ButtonLoadingUtils {

  fun setLoadingState(button: View, isLoading: Boolean) {
    val context = button.context
    Log.d("ButtonLoadingUtils", "isLoading: $isLoading")

    // Get your custom textPrimary color from theme
    val normalColor = MaterialColors.getColor(
      button,
      R.attr.buttonTextPrimary // Your custom theme attribute
    )

    // Get disabled color (grey_1 from colors.xml)
    val disabledColor = ContextCompat.getColor(context, R.color.grey_1)

    when (button) {
      is MaterialButton -> {
        button.isClickable = !isLoading
        button.setTextColor(
          if (isLoading) disabledColor else normalColor
        )
        button.iconTint = ColorStateList.valueOf(
          if (isLoading) disabledColor else normalColor
        )
      }
      is Button -> {
        button.isClickable = !isLoading
        button.setTextColor(
          if (isLoading) disabledColor else normalColor
        )
      }
    }
  }
}