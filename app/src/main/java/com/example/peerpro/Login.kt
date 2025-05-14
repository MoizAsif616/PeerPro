package com.example.peerpro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.peerpro.databinding.FragmentLoginBinding
import com.example.peerpro.models.User
import com.example.peerpro.utils.ButtonLoadingUtils
import com.example.peerpro.utils.SharedPrefHelper
import com.example.peerpro.utils.UserCache
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class Login : Fragment() {

  companion object {
    private val NU_EMAIL_REGEX = Pattern.compile("^l\\d{6}@lhr\\.nu\\.edu\\.pk$", Pattern.CASE_INSENSITIVE)
  }

  private var _binding: FragmentLoginBinding? = null
  private val binding get() = _binding!!

  private var onLoginSuccessListener: (() -> Unit)? = null

  private lateinit var sharedPrefHelper: SharedPrefHelper
  private val auth = FirebaseAuth.getInstance()

  fun setOnLoginSuccessListener(listener: () -> Unit) {
    onLoginSuccessListener = listener
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentLoginBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    sharedPrefHelper = SharedPrefHelper(requireContext())

    binding.loginButton.setOnClickListener {
      validateAndLogin()
    }
    binding.forgotPasswordText.setOnClickListener {
      (activity as? Auth)?.selectGetLink()
    }
  }

  private fun validateAndLogin() {
    val email = binding.emailEditText.text.toString().trim()
    val password = binding.passwordEditText.text.toString()

    // Validate email format
    val matcher = NU_EMAIL_REGEX.matcher(email)
    if (!matcher.matches()) {
      binding.emailInputLayout.error = "Invalid NU email format (lYYXXXX@lhr.nu.edu.pk)"
      return
    } else {
      binding.emailInputLayout.error = null
    }

    if (password.isEmpty()) {
      binding.passwordInputLayout.error = "Password cannot be empty"
      return
    } else {
      binding.passwordInputLayout.error = null
    }

    ButtonLoadingUtils.setLoadingState(binding.loginButton, true)
    performLogin(email, password)
  }

  private fun performLogin(email: String, password: String) {
    auth.signInWithEmailAndPassword(email, password)
      .addOnCompleteListener(requireActivity()) { task ->
        if (task.isSuccessful) {
          val user = auth.currentUser
          if (user?.isEmailVerified == true) {
            user?.getIdToken(true)?.addOnSuccessListener { result ->
              val accessToken = result.token
              sharedPrefHelper.saveToken(accessToken ?: "")

              // Fetch user object from Firestore
              val firestore = FirebaseFirestore.getInstance()
              firestore.collection("users").document(user.uid ).get()
                .addOnSuccessListener { document ->
                  if (document.exists()) {
                    val userData = document.toObject(User::class.java)
                    UserCache.setUser(userData!!)
                    UserCache.setId(user.uid)

                    // Navigate to MainActivity
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                  } else {
                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                  }
                }
                .addOnFailureListener {
                  Toast.makeText(context, "Failed to fetch user data: ${it.message}", Toast.LENGTH_SHORT).show()
                }
              ButtonLoadingUtils.setLoadingState(binding.loginButton, false)
            }
          } else {
            user?.sendEmailVerification()
            Toast.makeText(context, "You are not verified. Verification link sent to your email.", Toast.LENGTH_SHORT).show()
            ButtonLoadingUtils.setLoadingState(binding.loginButton, false)
          }
        } else {
          Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
          ButtonLoadingUtils.setLoadingState(binding.loginButton, false)
        }
      }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
