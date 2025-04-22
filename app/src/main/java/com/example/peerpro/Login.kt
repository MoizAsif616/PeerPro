package com.example.peerpro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.peerpro.databinding.FragmentLoginBinding

class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

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

        binding.loginButton.setOnClickListener {
            validateAndLogin()
        }
        binding.forgotPasswordText.setOnClickListener {
            (activity as? Auth)?.selectGetLink()
        }
    }

    private fun validateAndLogin() {
//        val email = binding.emailEditText.text.toString()
//        val password = binding.passwordEditText.text.toString()
//
//        if (email.isEmpty()) {
//            binding.emailInputLayout.error = "Email cannot be empty"
//            return
//        } else {
//            binding.emailInputLayout.error = null
//        }
//
//        if (password.isEmpty()) {
//            binding.passwordInputLayout.error = "Password cannot be empty"
//            return
//        } else {
//            binding.passwordInputLayout.error = null
//        }

        // Handle successful login
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
