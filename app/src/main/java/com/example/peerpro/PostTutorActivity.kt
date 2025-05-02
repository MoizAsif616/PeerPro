package com.example.peerpro

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.peerpro.databinding.ActivityPostTutorBinding
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.example.peerpro.utils.DialogUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.example.peerpro.utils.UserCache
import com.example.peerpro.utils.ButtonLoadingUtils
import android.os.Handler
import android.os.Looper
import com.example.peerpro.models.TutorSession
import com.google.firebase.firestore.FieldValue

class PostTutorActivity : AppCompatActivity() {
  private lateinit var binding: ActivityPostTutorBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityPostTutorBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupClickListeners()
  }

  private fun setupClickListeners() {
    binding.preferredGender.setOnClickListener {
      showSelectionDialog(
        listOf("Any", "Male", "Female"),
        binding.preferredGender
      )
    }

    binding.sessionType.setOnClickListener {
      showSelectionDialog(
        listOf("Flexible", "Online", "On-Campus"),
        binding.sessionType
      )
    }

    binding.availableDays.setOnClickListener {
      showSelectionDialog(
        listOf("Flexible", "Weekdays", "Weekends", "Mon,wed,fri", "Tue,thu", "Mon-sat", "Mon-sun"),
        binding.availableDays
      )
    }

    binding.timeWindow.setOnClickListener {
      showSelectionDialog(
        listOf("Flexible", "Morning", "Afternoon", "Evening", "Night"),
        binding.timeWindow
      )
    }

    binding.sessionPricing.setOnClickListener {
      showSelectionDialog(
        listOf("One-time", "Per session", "Per week", "Per month"),
        binding.sessionPricing
      )
    }

    binding.backBtn.setOnClickListener {
      onBackPressed()
    }

    binding.tutotPostButton.setOnClickListener {
      validateAndPostSession()
    }
  }

  private fun validateAndPostSession() {
    val skillName = binding.emailEditText.text.toString().trim()
    val preferredGender = binding.preferredGender.text.toString().trim()
    val sessionType = binding.sessionType.text.toString().trim()
    val availableDays = binding.availableDays.text.toString().trim()
    val timeWindow = binding.timeWindow.text.toString().trim()
    val sessionPricing = binding.sessionPricing.text.toString().trim()
    val sessionCost = binding.sessionCost.text.toString().trim()
    val description = binding.descriptionEditText.text.toString().trim()

    // Validate fields
    if (skillName.isEmpty() || preferredGender.isEmpty() || sessionType.isEmpty() ||
      availableDays.isEmpty() || timeWindow.isEmpty() || sessionPricing.isEmpty() || sessionCost.isEmpty()
    ) {
      Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
      return
    }

    val cost = sessionCost.toIntOrNull()
    if (cost == null || cost % 50 != 0) {
      Toast.makeText(this, "Cost must be a multiple of 50", Toast.LENGTH_LONG).show()
      return
    }

    ButtonLoadingUtils.setLoadingState(binding.tutotPostButton, true)

    postNewTutorSession(
      skillName, preferredGender, sessionType, availableDays, timeWindow,
      sessionPricing, cost, description
    )
  }

  private fun postNewTutorSession(
    skillName: String,
    preferredGender: String,
    sessionType: String,
    availableDays: String,
    timeWindow: String,
    sessionPricing: String,
    cost: Int,
    description: String
  ) {
    val firestore = FirebaseFirestore.getInstance()
    val peerId = UserCache.getId() ?: return

    // Create a TutorSession object
    val newSession = TutorSession(
      skillName = skillName,
      preferredGender = preferredGender,
      sessionType = sessionType,
      availableDays = availableDays,
      timeWindow = timeWindow,
      sessionPricing = sessionPricing,
      cost = cost,
      description = description,
      peerId = peerId, // Updated field name
      createdAt = Timestamp.now()
    )

    firestore.collection("tutor_sessions").add(newSession)
      .addOnSuccessListener { documentReference ->
        val sessionId = documentReference.id

        val userRef = firestore.collection("users").document(peerId)
        userRef.update("tutorSessionIds", FieldValue.arrayUnion(sessionId))
          .addOnSuccessListener {
            val user = UserCache.getUser()
            user?.tutorSessionIds = user.tutorSessionIds + sessionId
            UserCache.setUser(user!!)

            Toast.makeText(this, "Session posted successfully", Toast.LENGTH_LONG).show()

            Handler(Looper.getMainLooper()).postDelayed({
              ButtonLoadingUtils.setLoadingState(binding.tutotPostButton, false)
              finish()
            }, 1000)
          }
          .addOnFailureListener { e ->
            Toast.makeText(this, "Failed to update user data: ${e.message}", Toast.LENGTH_LONG).show()
            ButtonLoadingUtils.setLoadingState(binding.tutotPostButton, false)
          }
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Failed to post session: ${e.message}", Toast.LENGTH_LONG).show()
        ButtonLoadingUtils.setLoadingState(binding.tutotPostButton, false)
      }
      .addOnCompleteListener {
        ButtonLoadingUtils.setLoadingState(binding.tutotPostButton, false)
      }
  }

  @SuppressLint("ResourceType")
  private fun showSelectionDialog(items: List<String>, textView: TextView) {

    val typedArray = theme.obtainStyledAttributes(
      intArrayOf(
        R.attr.textPrimary
      )
    )
    val textColor = typedArray.getColor(0, Color.BLACK)
    typedArray.recycle()

    DialogUtils.showSelectionDialog(
      context = this,
      items = items
    ) { selectedItem ->
      textView.text = selectedItem
      textView.setTextColor(textColor)
    }
  }
}