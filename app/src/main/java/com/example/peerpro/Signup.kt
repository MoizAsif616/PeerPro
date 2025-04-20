package com.example.peerpro

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

class Signup : Fragment() {

  private lateinit var passwordEditText: TextInputEditText
  private lateinit var confirmPasswordEditText: TextInputEditText
  private lateinit var showPasswordCheckbox: CheckBox
  private lateinit var emailEditText: TextInputEditText
  private lateinit var submitButton: MaterialButton
  private lateinit var passwordInputLayout: TextInputLayout
  private lateinit var confirmPasswordInputLayout: TextInputLayout
  private lateinit var usernameEditText: TextInputEditText
  private lateinit var usernameInputLayout: TextInputLayout

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_signup, container, false)

//     Initialize views
    passwordEditText = view.findViewById(R.id.passwordEditText)
    confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
    showPasswordCheckbox = view.findViewById(R.id.showPasswordCheckbox)
    emailEditText = view.findViewById(R.id.emailEditText)
    submitButton = view.findViewById(R.id.submitButton)
    passwordInputLayout = view.findViewById(R.id.passwordInputLayout)
    confirmPasswordInputLayout = view.findViewById(R.id.confirmPasswordInputLayout)
    usernameEditText = view.findViewById(R.id.usernameEditText)
    usernameInputLayout = view.findViewById(R.id.usernameInputLayout)

    setupPasswordFields()
    setupSubmitButton()

    return view
  }

  private fun setupPasswordFields() {
    // Configure password toggle for both fields
    listOf(passwordInputLayout, confirmPasswordInputLayout).forEach { layout ->
      layout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
      // Get color from theme attribute with fallback
      val colorStateList = context?.let { ctx ->
        MaterialColors.getColorStateList(
          ctx,
          R.attr.peerMain,  // Theme attribute
          ctx.getColorStateList(R.color.grey_1) // Fallback
        )
      }
      colorStateList?.let { layout.setEndIconTintList(it) }
    }

    showPasswordCheckbox.setOnCheckedChangeListener { _, isChecked ->
      val inputType = if (isChecked) {
        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
      } else {
        InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
      }

      passwordEditText.inputType = inputType
      confirmPasswordEditText.inputType = inputType

      // Maintain cursor position
      passwordEditText.setSelection(passwordEditText.text?.length ?: 0)
      confirmPasswordEditText.setSelection(confirmPasswordEditText.text?.length ?: 0)
    }
  }

  private fun setupSubmitButton() {
    submitButton.setOnClickListener {
      validateAndSubmit()
    }
  }

  private fun validateAndSubmit() {
    val username = usernameEditText.text.toString()
    val email = emailEditText.text.toString()
    val password = passwordEditText.text.toString()
    val confirmPassword = confirmPasswordEditText.text.toString()

    if (username.isEmpty()) {
      usernameInputLayout.error = "Username cannot be empty"
      return
    } else if (username.length < 4) {
      usernameInputLayout.error = "Username must be at least 4 characters"
      return
    } else {
      usernameInputLayout.error = null
    }

    if (password != confirmPassword) {
      confirmPasswordInputLayout.error = "Passwords don't match"
      return
    }

    // Handle successful submission
  }

  override fun onDestroyView() {
    super.onDestroyView()
    // No binding to clear in this version
  }
}