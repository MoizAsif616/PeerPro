package com.example.peerpro

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText
import androidx.fragment.app.commit
import com.example.peerpro.models.User
import com.example.peerpro.utils.ButtonLoadingUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import java.util.regex.Pattern

class Signup : Fragment() {

  private val firestore = FirebaseFirestore.getInstance()
  private val auth = FirebaseAuth.getInstance()
  private lateinit var passwordEditText: TextInputEditText
  private lateinit var confirmPasswordEditText: TextInputEditText
  private lateinit var showPasswordCheckbox: CheckBox
  private lateinit var emailEditText: TextInputEditText
  private lateinit var submitButton: MaterialButton
  private lateinit var passwordInputLayout: TextInputLayout
  private lateinit var confirmPasswordInputLayout: TextInputLayout
  private lateinit var usernameEditText: TextInputEditText
  private lateinit var usernameInputLayout: TextInputLayout

  companion object {
    private val NU_EMAIL_REGEX = Pattern.compile("^l\\d{6}@lhr\\.nu\\.edu\\.pk$", Pattern.CASE_INSENSITIVE)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_signup, container, false)

    auth.apply {
      firebaseAuthSettings.setAppVerificationDisabledForTesting(false) // Keep silent reCAPTCHA
    }
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
      ButtonLoadingUtils.setLoadingState(submitButton, true)
      validateAndSubmit()
    }
  }

  private fun validateAndSubmit() {
    val username = usernameEditText.text.toString()
    val email = emailEditText.text.toString().trim()
    val password = passwordEditText.text.toString()
    val confirmPassword = confirmPasswordEditText.text.toString()

    // Validate username
    if (username.isEmpty()) {
      usernameInputLayout.error = "Username cannot be empty"
      ButtonLoadingUtils.setLoadingState(submitButton, false)
      return
    } else if (username.length < 4) {
      usernameInputLayout.error = "Username must be at least 4 characters"
      ButtonLoadingUtils.setLoadingState(submitButton, false)
      return
    }

    // Validate email format (lYYXXXX@lhr.nu.edu.pk)
    val matcher = NU_EMAIL_REGEX.matcher(email)
    if (!matcher.matches()) {
      emailEditText.error = "Invalid NU email format (lYYXXXX@lhr.nu.edu.pk)"
      ButtonLoadingUtils.setLoadingState(submitButton, false)
      return
    }

    // Extract =roll numbe rof the peer
    val rollno = email.substring(1, 3) + "L-" + email.substring(3, 7)

    // Validate password match
    if (password != confirmPassword) {
      confirmPasswordInputLayout.error = "Passwords don't match"
      ButtonLoadingUtils.setLoadingState(submitButton, false)
      return
    }

    // Create user in Firebase Auth + Firestore
    registerUser(rollno, email, password, username)
  }

  private fun registerUser(rollno: String, email: String, password: String, username: String) {
    auth.createUserWithEmailAndPassword(email, password)
      .addOnCompleteListener(requireActivity()) { task ->
        if (task.isSuccessful) {
          val user = auth.currentUser
          val userId = user?.uid ?: return@addOnCompleteListener

          // Create user object with additional fields
          val newUser = User(
            rollno = rollno,
            name = username,
            email = email,
            bio = "",
            profilePicUrl = "",
            chatIds = emptyList(),
            tutorSessionIds = emptyList(),
            notesIds = emptyList(),
          )

          // Save user to Firestore
          firestore.collection("users").document(userId).set(newUser)
            .addOnSuccessListener {
              user.sendEmailVerification()
                .addOnSuccessListener {
                  (activity as? Auth)?.selectLogIn()
                  Toast.makeText(context, "Verification email sent", Toast.LENGTH_SHORT).show()
                  ButtonLoadingUtils.setLoadingState(submitButton, false)
                }
            }
        } else {
          when (task.exception) {
            is FirebaseAuthInvalidCredentialsException -> {
              Toast.makeText(context, "Invalid credentials\n Password length must be at least 6 characters", Toast.LENGTH_SHORT).show()
              ButtonLoadingUtils.setLoadingState(submitButton, false)
            }
            is FirebaseAuthUserCollisionException -> {
              Toast.makeText(context, "Email already exists", Toast.LENGTH_SHORT).show()
              ButtonLoadingUtils.setLoadingState(submitButton, false)
            }
            else -> {
              Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT)
                .show()
              ButtonLoadingUtils.setLoadingState(submitButton, false)
            }
          }
        }
      }
  }

  fun delayForTenSeconds(callback: () -> Unit) {
    Handler(Looper.getMainLooper()).postDelayed(callback, 10000)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    // No binding to clear in this version
  }
}