package com.example.peerpro

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.peerpro.databinding.ActivityPostNoteBinding
import com.example.peerpro.models.Note
import com.example.peerpro.utils.ButtonLoadingUtils
import com.example.peerpro.utils.DialogUtils
import com.example.peerpro.utils.UserCache
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PostNoteActivity : AppCompatActivity() {
  private lateinit var binding: ActivityPostNoteBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityPostNoteBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupClickListeners()
  }

  private fun setupClickListeners() {
    binding.notesType.setOnClickListener {
      showSelectionDialog(
        listOf("Handwritten", "Printed"),
        binding.notesType
      )
    }

    binding.backBtn.setOnClickListener {
      onBackPressed()
    }

    binding.notesPostButton.setOnClickListener {
      validateAndPostNote()
    }
  }

  private fun validateAndPostNote() {
    val notesName = binding.notesName.text.toString().trim()
    val notesType = binding.notesType.text.toString().trim()
    val instructorName = binding.instructorName.text.toString().trim()
    val notesCost = binding.notesCost.text.toString().trim()
    val description = binding.descriptionEditText.text.toString().trim()

    // Validate fields
    if (notesName .isEmpty() || notesType.isEmpty() || instructorName.isEmpty() || notesCost.isEmpty()) {
      Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
      return
    }

    val cost = notesCost.toIntOrNull()
    if (cost == null || cost % 50 != 0) {
      Toast.makeText(this, "Cost must be a multiple of 50", Toast.LENGTH_LONG).show()
      return
    }

    ButtonLoadingUtils.setLoadingState(binding.notesPostButton, true)

    postNewNote(
      notesType, instructorName, cost, description, notesName
    )
  }

  private fun postNewNote(
    notesType: String,
    instructorName: String,
    cost: Int,
    description: String,
    notesName: String
  ) {
    val firestore = FirebaseFirestore.getInstance()
    val peerId = UserCache.getId() ?: return

    // Create a Note object
    val newNote = Note(
      skillName = notesName,
      notesType = notesType,
      instructorName = instructorName,
      cost = cost,
      description = description,
      peerId = peerId,
      createdAt = Timestamp.now()
    )

    firestore.collection("notes").add(newNote)
      .addOnSuccessListener { documentReference ->
        val noteId = documentReference.id

        val userRef = firestore.collection("users").document(peerId)
        userRef.update("notesIds", FieldValue.arrayUnion(noteId))
          .addOnSuccessListener {
            val user = UserCache.getUser()
            user?.notesIds = user.notesIds + noteId
            UserCache.setUser(user!!)

            Toast.makeText(this, "Note posted successfully", Toast.LENGTH_LONG).show()

            Handler(Looper.getMainLooper()).postDelayed({
              ButtonLoadingUtils.setLoadingState(binding.notesPostButton, false)
              finish()
            }, 1000)
          }
          .addOnFailureListener { e ->
            Toast.makeText(this, "Failed to update user data: ${e.message}", Toast.LENGTH_LONG).show()
            ButtonLoadingUtils.setLoadingState(binding.notesPostButton, false)
          }
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Failed to post note: ${e.message}", Toast.LENGTH_LONG).show()
        ButtonLoadingUtils.setLoadingState(binding.notesPostButton, false)
      }
      .addOnCompleteListener {
        ButtonLoadingUtils.setLoadingState(binding.notesPostButton, false)
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