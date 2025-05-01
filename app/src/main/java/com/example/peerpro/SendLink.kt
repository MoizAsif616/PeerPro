package com.example.peerpro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.peerpro.databinding.FragmentSendLinkBinding
import com.example.peerpro.utils.ButtonLoadingUtils
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class SendLink : Fragment() {

    companion object {
        private val NU_EMAIL_REGEX = Pattern.compile("^l\\d{6}@lhr\\.nu\\.edu\\.pk$", Pattern.CASE_INSENSITIVE)
    }

    private var _binding: FragmentSendLinkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSendLinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sendLinkButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val matcher = NU_EMAIL_REGEX.matcher(email)

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
            } else if (!matcher.matches()) {
                Toast.makeText(requireContext(), "Invalid NU email format (lYYXXXX@lhr.nu.edu.pk)", Toast.LENGTH_SHORT).show()
            } else {
                ButtonLoadingUtils.setLoadingState(binding.sendLinkButton, true)
                resetPassword(email)
            }
        }
    }

    fun resetPassword(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                ButtonLoadingUtils.setLoadingState(binding.sendLinkButton, false)
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Password reset link sent to $email", Toast.LENGTH_SHORT).show()
                    (activity as? Auth)?.selectLogIn()
                } else {
                    Toast.makeText(requireContext(), "Failed to send reset link: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
