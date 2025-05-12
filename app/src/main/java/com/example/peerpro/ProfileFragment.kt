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
import android.app.Dialog
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.view.Gravity
import android.widget.ImageView
import androidx.core.content.ContentProviderCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.peerpro.databinding.NotesProfileDetailsBinding
import com.example.peerpro.databinding.TutorProfileDetailsBinding
import com.google.firebase.storage.FirebaseStorage
import com.example.peerpro.models.TutorSession
import com.example.peerpro.models.User
import com.example.peerpro.models.Note
import java.net.HttpURLConnection
import java.net.URL
import com.squareup.picasso.Picasso


class ProfileFragment : Fragment() {

  public val auth = FirebaseAuth.getInstance()
  public val firestore = FirebaseFirestore.getInstance()

  private var _binding: FragmentProfileBinding? = null
  private val binding get() = _binding!!

  private lateinit var tutorSessionAdapter: SessionNotesAdapter<TutorSession>
  private lateinit var notesAdapter: SessionNotesAdapter<Note>

  // Add these constants at the top of your ProfileFragment
  private companion object {
    const val PICK_IMAGE_REQUEST = 1001
    const val CLOUDINARY_UPLOAD_PRESET = "PeerPro" // Set this in Cloudinary Dashboard
    const val MAX_IMAGE_SIZE_KB = 1024 // 1MB max image size
  }



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

    refreshProfile()
    Log.d("user data: ", UserCache.getUser().toString())
  }

  private fun refreshProfile() {
    binding.profileSwipeRefreshLayout.isRefreshing = true
    binding.peerBio.text = UserCache.getUser()?.bio
    binding.peerEmail.text = UserCache.getUser()?.email
    val imageUrl = UserCache.getUser()?.profilePicUrl

    // Only load the profile image if the URL is not null or empty
    if (!imageUrl.isNullOrEmpty()) {
      binding.tutorImage?.let { imageView ->
          loadProfileImage(imageUrl, imageView)
      }
    } else {
          binding.tutorImage.setImageResource(R.color.black)
    }


      while (UserCache.getId() == null) {
      Log.d("L6", "Waiting for user ID to be set")
    }
    val userId = UserCache.getId() ?: return
    Log.d("L6", "Fetching user data for UID: $userId")

    firestore.collection("users").document(userId).get()
      .addOnSuccessListener { document ->
        Log.d("L6", "User data fetched successfully")
        val user = document.toObject(User::class.java)
        user?.let {
          // Update tutor sessions
          val tutorSessionIds = it.tutorSessionIds
          if (tutorSessionIds.isEmpty()) {
            binding.tutorSessionsViewSwitcher.displayedChild = 1 // Show empty state
          } else {
            Log.d("l6", "Fetched ${tutorSessionIds.size} tutor session IDs")
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
        selectTutoring()
      }
      .addOnFailureListener {
        Log.e("L6", "Error fetching user data: ${it.message}")
        Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_LONG).show()
      }
  }

  private fun selectTutoring() {
    Log.d("L6", "Selecting tutoring")
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
    id.let {
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
          Toast.makeText(requireContext(), "Failed to save bio: ${e.message}", Toast.LENGTH_LONG)
            .show()
        }
    }
  }


  fun changeProfilePic() {

    val intent = Intent().apply {
      type = "image/*"
      action = Intent.ACTION_GET_CONTENT
      putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
    }
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)

  }


  // Updated onActivityResult
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
      data?.data?.let { uri ->
        // Check image size before uploading
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val sizeBytes = inputStream?.available() ?: 0
        inputStream?.close()

        if (sizeBytes > MAX_IMAGE_SIZE_KB * 1024) {
          showImageSizeError()
          return
        }

        uploadProfilePicture(uri)
      } ?: showImageSelectionError()
    }
  }
  private fun uploadProfilePicture(imageUri: Uri) {
    Toast.makeText(requireContext(), "Uploading profile picture...", Toast.LENGTH_SHORT).show()

    try {
      MediaManager.get().upload(imageUri)
        .unsigned(CLOUDINARY_UPLOAD_PRESET)
        .callback(object : UploadCallback {
          // Required method 1
          override fun onSuccess(requestId: String, resultData: Map<*, *>) {
            val imageUrl = resultData["secure_url"] as? String ?: run {
              showUploadError("Invalid server response")
              return
            }
            updateProfilePictureUrl(imageUrl)
            Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show()
          }

          override fun onError(requestId: String?, error: ErrorInfo?) {
            showUploadError("Error: $error")
          }

          override fun onReschedule(requestId: String?, error: ErrorInfo?) {
            TODO("Not yet implemented")
          }
          // Optional method (can be empty if not needed)
          override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
            // You could update progress here if needed
          }

          // Optional method (can be empty if not needed)
          override fun onStart(requestId: String) {
            // Upload started
          }
        })
        .dispatch()
    } catch (e: Exception) {
      showUploadError(e.message ?: "Unknown error")
    }
  }

private fun updateProfilePictureUrl(imageUrl: String) {
  val userId = UserCache.getId() ?: run {
    Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
    return
  }

  firestore.collection("users").document(userId)
    .update("profilePicUrl", imageUrl)
    .addOnSuccessListener {
      // Update local cache
      UserCache.getUser()?.let { user ->
        user.profilePicUrl = imageUrl
        UserCache.setUser(user)
        // Update UI immediately
        binding.tutorImage?.let { imageView ->
          // Load image without Glide
          loadProfileImage(imageUrl, imageView)
        }
      }
      Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show()
    }
    .addOnFailureListener { e ->
      Toast.makeText(requireContext(), "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

  private fun loadProfileImage(imageUrl: String, imageView: ImageView) {
    Picasso.get()
      .load(imageUrl)
      .resize(250, 250) // Resize if needed
      .centerCrop()
      .into(imageView)
  }


  // Error handling functions
private fun showImageSizeError() {
    AlertDialog.Builder(requireContext())
        .setTitle("Image Too Large")
        .setMessage("Please select an image smaller than 1MB")
        .setPositiveButton("OK", null)
        .show()
}

private fun showImageSelectionError() {
    Toast.makeText(requireContext(), "Failed to select image", Toast.LENGTH_SHORT).show()
}

private fun showUploadError(message: String) {
    Toast.makeText(requireContext(), "Upload failed: $message", Toast.LENGTH_LONG).show()
    Log.e("ProfilePic", "Upload error: $message")
}
 /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
*/

  fun logout() {
    val localStorage = SharedPrefHelper(requireContext())
    localStorage.clearToken()

    val intent = Intent(requireContext(), Auth::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    auth.signOut()
    startActivity(intent)
  }

  fun deleteAccount() {
    Toast.makeText(requireContext(), "Delete Account clicked", Toast.LENGTH_LONG).show()
  }

  private fun tutorSessionClicked(tutorSession: TutorSession) {
    showTutoringDetailsDialog(tutorSession)
  }

  private fun notesClicked(note: Note) {
    showNotesDetailsDialog(note)
  }

  private fun showTutoringDetailsDialog(tutoring: TutorSession) {
    val binding = TutorProfileDetailsBinding.inflate(LayoutInflater.from(requireContext()))
    val dialog = AlertDialog.Builder(requireContext())
      .setView(binding.root)
      .create()

    val metrics = Resources.getSystem().displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels
    val windowHeight = (screenHeight * 0.6).toInt()
    val textSizeMedium = (windowHeight * 0.017f)
    val textSizeSmall = (windowHeight * 0.013f)

    // Set tutoring data
    binding.subject.text = tutoring.skillName
    binding.tgender.text = tutoring.preferredGender
    binding.type.text = tutoring.sessionType
    binding.days.text = tutoring.availableDays
    binding.timeWindow.text = tutoring.timeWindow
    if (tutoring.description.isEmpty()) {
      binding.description.text = "No description"
      binding.description.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_1))
    } else {
      binding.description.text = tutoring.description
    }
    binding.costType.text = if (tutoring.cost == 0) "" else tutoring.sessionPricing
    binding.cost.text = if(tutoring.cost == 0) "FREE" else "Rs." + tutoring.cost.toString()


    binding.subject.textSize = textSizeMedium
    binding.tgender.textSize = textSizeSmall
    binding.type.textSize = textSizeSmall
    binding.days.textSize = textSizeSmall
    binding.timeWindow.textSize = textSizeSmall
    binding.description.textSize = textSizeSmall
    binding.costType.textSize = textSizeSmall
    binding.cost.textSize = textSizeMedium
    binding.sessionTypeLabel.textSize = textSizeSmall
    binding.availableDaysLabel.textSize = textSizeSmall
    binding.timeLabel.textSize = textSizeSmall
    binding.genderLabel.textSize = textSizeSmall

    binding.tutorDeleteButton.setOnClickListener {
      Toast.makeText(requireContext(), "Delete tutoring: ${tutoring.skillName}", Toast.LENGTH_SHORT).show()
      dialog.dismiss()
    }

    dialog.window?.apply {
      dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
      dialog.window?.setLayout((screenWidth * 0.90).toInt(), (screenHeight * 0.5).toInt())
      dialog.window?.setGravity(Gravity.CENTER)
    }

    binding.root.minimumHeight = (screenHeight * 0.5).toInt()

    dialog.show()
  }

  private fun showNotesDetailsDialog(note: Note) {
    val dialogBinding = NotesProfileDetailsBinding.inflate(LayoutInflater.from(requireContext()))
    val dialog = AlertDialog.Builder(requireContext())
      .setView(dialogBinding.root)
      .create()

    val metrics = Resources.getSystem().displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels
    val textSizeMedium = (screenHeight * 0.6f * 0.017f)
    val textSizeSmall = (screenHeight * 0.6f * 0.013f)

    dialogBinding.subject.text = note.name
    if (note.description.isEmpty()) {
      dialogBinding.description.text = "No description"
      dialogBinding.description.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_1))
    } else {
      dialogBinding.description.text = note.description
    }
    dialogBinding.cost.text = if(note.cost ==0) "FREE" else "Rs." + note.cost.toString()
    dialogBinding.tnotes.text = note.notesType
    dialogBinding.instructorname.text = note.instructorName

    dialogBinding.instructorname.textSize = textSizeSmall
    dialogBinding.subject.textSize = textSizeMedium
    dialogBinding.description.textSize = textSizeSmall
    dialogBinding.cost.textSize = textSizeMedium
    dialogBinding.tnotes.textSize = textSizeSmall
    dialogBinding.instructorLabel.textSize = textSizeSmall
    dialogBinding.type.textSize = textSizeSmall
    dialogBinding.notesDeleteButton.setOnClickListener {
      Toast.makeText(requireContext(), "Delete note: ${note.name}", Toast.LENGTH_SHORT).show()
      dialog.dismiss()
    }

    dialog.window?.apply {
      dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
      dialog.window?.setLayout((screenWidth * 0.90).toInt(), (screenHeight * 0.5).toInt())
      dialog.window?.setGravity(Gravity.CENTER)
    }
    dialogBinding.root.minimumHeight = (screenHeight * 0.5).toInt()
    dialog.show()
  }

}

data class CardItem(val title: String, val date: String, val cost: String)