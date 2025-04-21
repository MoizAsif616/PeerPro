package com.example.peerpro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.peerpro.databinding.FragmentSendLinkBinding


class SendLink : Fragment() {

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
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Reset link sent to your email", Toast.LENGTH_LONG).show()
                (activity as? Auth)?.selectLogIn()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
