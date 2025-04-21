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

class Verify : Fragment() {

    private lateinit var otpEditText: TextInputEditText
    private lateinit var verifyButton: MaterialButton
    private lateinit var resendOtpText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_verify, container, false)

        otpEditText = view.findViewById(R.id.otpEditText)
        verifyButton = view.findViewById(R.id.verifyButton)
        resendOtpText = view.findViewById(R.id.resendOtpText)

        setupListeners()

        return view
    }

    private fun setupListeners() {
        resendOtpText.setOnClickListener {
            Toast.makeText(context, "OTP has been sent to your email", Toast.LENGTH_SHORT).show()
        }

        verifyButton.setOnClickListener {
            (activity as? Auth)?.selectLogIn()
        }
    }
}
