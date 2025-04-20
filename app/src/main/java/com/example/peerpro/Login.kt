package com.example.peerpro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class Login : Fragment() {

  private lateinit var emailEditText: TextInputEditText
  private lateinit var passwordEditText: TextInputEditText
  private lateinit var loginButton: MaterialButton
  private lateinit var emailInputLayout: TextInputLayout
  private lateinit var passwordInputLayout: TextInputLayout
  private lateinit var forgotPasswordText: TextView

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_login, container, false)

    // Initialize views
    emailEditText = view.findViewById(R.id.emailEditText)
    passwordEditText = view.findViewById(R.id.passwordEditText)
    loginButton = view.findViewById(R.id.loginButton)
    emailInputLayout = view.findViewById(R.id.emailInputLayout)
    passwordInputLayout = view.findViewById(R.id.passwordInputLayout)
    forgotPasswordText = view.findViewById(R.id.forgotPasswordText)

    setupLoginButton()
    setupForgotPasswordText()

    return view
  }

  private fun setupLoginButton() {
    loginButton.setOnClickListener {
      validateAndLogin()
    }
  }

  private fun setupForgotPasswordText() {
    forgotPasswordText.setOnClickListener {
      Toast.makeText(context, "Reset clicked", Toast.LENGTH_SHORT).show()
    }
  }

  private fun validateAndLogin() {
    val email = emailEditText.text.toString()
    val password = passwordEditText.text.toString()

    if (email.isEmpty()) {
      emailInputLayout.error = "Email cannot be empty"
      return
    } else {
      emailInputLayout.error = null
    }

    if (password.isEmpty()) {
      passwordInputLayout.error = "Password cannot be empty"
      return
    } else {
      passwordInputLayout.error = null
    }

    // Handle successful login
  }
}
