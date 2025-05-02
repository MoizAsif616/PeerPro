package com.example.peerpro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.databinding.FragmentProfileBinding
import android.content.Intent
import com.example.peerpro.utils.SharedPrefHelper
import kotlin.jvm.java
import android.app.AlertDialog
import android.util.Log
import android.widget.EditText
import com.example.peerpro.utils.ButtonLoadingUtils
import com.google.android.material.button.MaterialButton
import com.example.peerpro.utils.UserCache
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.Activity
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.example.peerpro.models.TutorSession
import com.example.peerpro.models.User
import com.example.peerpro.models.Note

class ProfileFragment : Fragment() {

  public val auth = FirebaseAuth.getInstance()
  public val firestore = FirebaseFirestore.getInstance()

  private var _binding: FragmentProfileBinding? = null
  private val binding get() = _binding!!

  private lateinit var tutorSessionAdapter: SessionNotesAdapter<TutorSession>
  private lateinit var notesAdapter: SessionNotesAdapter<Note>

  private val PICK_IMAGE_REQUEST = 1

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentProfileBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    tutorSessionAdapter = SessionNotesAdapter(emptyList<TutorSession>()) {
        tutorSessionClicked(it)
    }
    notesAdapter = SessionNotesAdapter(emptyList<Note>()) {
        notesClicked(it)
    }

    binding.tutorSessionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    binding.tutorSessionsRecyclerView.adapter = tutorSessionAdapter

    binding.notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    binding.notesRecyclerView.adapter = notesAdapter

    binding.peerTutoringButton.setOnClickListener {
      selectTutoring()
    }

    binding.peerNotesButton.setOnClickListener {
      selectNotes()
    }

    // Setup refresh listener
    binding.profileSwipeRefreshLayout.setOnRefreshListener {
      refreshProfile()
    }

    selectTutoring()
    refreshProfile()
    Log.d("user data: ", UserCache.getUser().toString())
  }

  private fun refreshProfile() {
    binding.profileSwipeRefreshLayout.isRefreshing = true

    val userId = UserCache.getId() ?: return

    firestore.collection("users").document(userId).get()
      .addOnSuccessListener { document ->
        val user = document.toObject(User::class.java)
        user?.let {
          UserCache.setUser(it)
          binding.peerBio.text = it.bio
          binding.peerEmail.text = it.email

          // Update tutor sessions
          val tutorSessionIds = it.tutorSessionIds
          if (tutorSessionIds.isEmpty()) {
            binding.tutorSessionsViewSwitcher.displayedChild = 1 // Show empty state
          } else {
            val tutorSessions = mutableListOf<TutorSession>()
            tutorSessionIds.forEach { sessionId ->
              firestore.collection("tutor_sessions").document(sessionId).get()
                .addOnSuccessListener { sessionDoc ->
                  val session = sessionDoc.toObject(TutorSession::class.java)
                  session?.let { tutorSessions.add(it) }
                  tutorSessionAdapter.updateData(tutorSessions)
                  binding.tutorSessionsViewSwitcher.displayedChild = 0 // Show list
                }
            }
          }

          val notesIds = it.notesIds
          if (notesIds.isEmpty()) {
            binding.notesViewSwitcher.displayedChild = 1 // Show empty state
          } else {
            val notes = mutableListOf<Note>()
            notesIds.forEach { noteId ->
              firestore.collection("notes").document(noteId).get()
                .addOnSuccessListener { noteDoc ->
                  val note = noteDoc.toObject(Note::class.java)
                  note?.let { notes.add(it) }
                  notesAdapter.updateData(notes)
                  binding.notesViewSwitcher.displayedChild = 0 // Show list
                }
            }
          }
        }
        binding.profileSwipeRefreshLayout.isRefreshing = false
      }
      .addOnFailureListener {
        Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_LONG).show()
      }
  }

  private fun selectTutoring() {
    binding.peerTutoringButton.setBackgroundResource(R.color.peerLight_30)
    binding.peerNotesButton.setBackgroundResource(android.R.color.transparent)
    binding.tutorSessionsViewSwitcher.visibility = View.VISIBLE
    binding.notesViewSwitcher.visibility = View.GONE
  }

  private fun selectNotes() {
    binding.peerNotesButton.setBackgroundResource(R.color.peerLight_30)
    binding.peerTutoringButton.setBackgroundResource(android.R.color.transparent)
    binding.notesViewSwitcher.visibility = View.VISIBLE
    binding.tutorSessionsViewSwitcher.visibility = View.GONE
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  fun editBio() {
    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_bio, null)
    val editText = dialogView.findViewById<EditText>(R.id.bioEditText)
    val saveButton = dialogView.findViewById<MaterialButton>(R.id.saveBioButton)

    // Pre-fill the EditText with the current bio
    editText.setText(UserCache.getUser()?.bio ?: "")

    val dialog = AlertDialog.Builder(requireContext())
      .setView(dialogView)
      .setCancelable(true) // Allow dismissal when clicking outside
      .create()

    saveButton.setOnClickListener {
      ButtonLoadingUtils.setLoadingState(saveButton, true)
      saveBio(editText.text.toString())
      ButtonLoadingUtils.setLoadingState(saveButton, false)
      dialog.dismiss()
    }

    dialog.show()
  }

  private fun saveBio(bio: String) {
    var id = UserCache.getId()
    id.let{
      val userRef = firestore.collection("users").document(it.toString())
      userRef.update("bio", bio)
        .addOnSuccessListener {
          Toast.makeText(requireContext(), "Bio saved successfully", Toast.LENGTH_LONG).show()
          var user = UserCache.getUser()
          user?.bio = bio
          UserCache.setUser(user!!)
          binding.peerBio.text = bio
        }
        .addOnFailureListener { e ->
          Toast.makeText(requireContext(), "Failed to save bio: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
  }

  fun changeProfilePic() {
    // Open file picker to select an image
    val intent = Intent(Intent.ACTION_GET_CONTENT)
    intent.type = "image/*"
    startActivityForResult(intent, PICK_IMAGE_REQUEST)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
      val imageUri: Uri = data.data!!

    
      val storageRef = FirebaseStorage.getInstance().reference
        .child("profile_pictures/${UserCache.getId()}.jpg")

      storageRef.putFile(imageUri)
        .addOnSuccessListener {
          // Get the download URL
          storageRef.downloadUrl.addOnSuccessListener { uri ->
            val profilePicUrl = uri.toString()

            // Update Firestore with the new profile picture URL
            val userRef = firestore.collection("users").document(UserCache.getId()!!)
            userRef.update("profilePicUrl", profilePicUrl)
              .addOnSuccessListener {
                // Update UserCache
                val user = UserCache.getUser()
                user?.profilePicUrl = profilePicUrl
                UserCache.setUser(user!!)

                Toast.makeText(requireContext(), "Profile picture updated successfully", Toast.LENGTH_LONG).show()
              }
              .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to update profile picture: ${e.message}", Toast.LENGTH_LONG).show()
              }
          }
        }
        .addOnFailureListener { e ->
          Toast.makeText(requireContext(), "Failed to upload profile picture: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
  }

  fun logout() {
    val localStorage = SharedPrefHelper(requireContext())
    localStorage.clearToken()

    val intent = Intent(requireContext(), Auth::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    startActivity(intent)
  }

  fun deleteAccount() {
    Toast.makeText(requireContext(), "Delete Account clicked", Toast.LENGTH_LONG).show()
  }

  private fun tutorSessionClicked(tutorSession: TutorSession) {
    Toast.makeText(requireContext(), "Tutor session clicked: ${tutorSession.skillName}", Toast.LENGTH_LONG).show()
  }

  private fun notesClicked(note: Note) {
    Toast.makeText(requireContext(), "Notes item clicked: ${note.name}", Toast.LENGTH_LONG).show()
  }
}

data class CardItem(val title: String, val date: String, val cost: String)