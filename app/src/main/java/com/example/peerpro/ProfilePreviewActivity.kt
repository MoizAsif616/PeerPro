package com.example.peerpro

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.view.WindowManager
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peerpro.databinding.ActivityProfilePreviewBinding
import com.example.peerpro.databinding.NotesProfileDetailsBinding
import com.example.peerpro.databinding.TutorProfileDetailsBinding
import com.example.peerpro.models.Note
import com.example.peerpro.models.TutorSession
import com.example.peerpro.models.User
import com.example.peerpro.utils.ButtonLoadingUtils
import com.example.peerpro.utils.ChatUtils
import com.example.peerpro.utils.RatingsUtils
import com.example.peerpro.utils.UserCache
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilePreviewActivity : AppCompatActivity() {

  private lateinit var binding: ActivityProfilePreviewBinding
  private val firestore = FirebaseFirestore.getInstance()

  private lateinit var tutorSessionAdapter: SessionNotesAdapter<TutorSession>
  private lateinit var notesAdapter: SessionNotesAdapter<Note>
  lateinit var peerId: String;

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityProfilePreviewBinding.inflate(layoutInflater)
    setContentView(binding.root)

    peerId = intent.getStringExtra("peerId").toString()
    if (peerId.isNullOrEmpty()) {
      Toast.makeText(this, "Invalid peer ID", Toast.LENGTH_LONG).show()
      finish()
      return
    }
    binding.profilePreviewSwipeRefreshLayout.isEnabled = false
    binding.profilePreviewSwipeRefreshLayout.isRefreshing = true
    binding.profilePreviewContainer.visibility = View.GONE

    setupUI()
    fetchProfileData(peerId)
    binding.profilePreviewSwipeRefreshLayout.isRefreshing = false
    binding.profilePreviewContainer.visibility = View.VISIBLE
    if (UserCache.getId() != peerId) {
      binding.rateButton.visibility = View.VISIBLE
      binding.rateButton.setOnClickListener {
        showRatingDialog()
      }
    } else {
      binding.rateButton.visibility = View.GONE
    }

  }

  private fun setupUI() {
    tutorSessionAdapter = SessionNotesAdapter(emptyList<TutorSession>()) {
      tutorSessionClicked(it)
    }
    notesAdapter = SessionNotesAdapter(emptyList<Note>()) {
      notesClicked(it)
    }
    val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    val padding = (screenWidth * 0.02).toInt()
    binding.profilePreviewContainer.setPadding(padding, padding / 3, padding, padding / 3)
    binding.profilePreviewContainer.clipToPadding = false

    binding.tutorSessionsRecyclerView.layoutManager = LinearLayoutManager(this)
    binding.tutorSessionsRecyclerView.adapter = tutorSessionAdapter

    binding.notesRecyclerView.layoutManager = LinearLayoutManager(this)
    binding.notesRecyclerView.adapter = notesAdapter

    binding.peerTutoringButton.setOnClickListener {
      selectTutoring()
    }

    binding.peerNotesButton.setOnClickListener {
      selectNotes()
    }

    binding.backBtn.setOnClickListener {
      onBackPressed()
    }
    selectTutoring()
  }

  private fun loadProfileImage(imageUrl: String, imageView: ImageView) {
    Picasso.get()
      .load(imageUrl)
      .resize(250, 250) // Resize if needed
      .centerCrop()
      .into(imageView)
  }

  private fun fetchProfileData(peerId: String) {
    binding.profilePreviewSwipeRefreshLayout.isRefreshing = true

    firestore.collection("users").document(peerId).get()
      .addOnSuccessListener { document ->
        val user = document.toObject(User::class.java)
        if (user != null) {
          binding.peerEmail.text = user.email
          binding.peerBio.text = user.bio
          binding.headerText.text = user.name
          val imageUrl = user.profilePicUrl
          if (!imageUrl.isNullOrEmpty()) {
            binding.tutorImage?.let { imageView ->
              loadProfileImage(imageUrl, imageView)
              imageView.setOnClickListener {
                showEnlargedProfileImage(imageUrl)
              }
            }
          } else {
            binding.tutorImage.setImageResource(R.drawable.default_peer)
          }
          fetchAndDisplayRatings()
          fetchTutorSessions(user.tutorSessionIds)
          fetchNotes(user.notesIds)
        } else {
          Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show()
          finish()
        }
      }
      .addOnFailureListener {
        Log.e("ProfilePreview", "Error fetching user data: ${it.message}")
        Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_LONG).show()
        finish()
      }
      .addOnCompleteListener {
        binding.profilePreviewSwipeRefreshLayout.isRefreshing = false
      }
  }

  private fun showEnlargedProfileImage(imageUrl: String) {
    val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
      // Get screen dimensions
      val displayMetrics = DisplayMetrics()
      windowManager?.defaultDisplay?.getMetrics(displayMetrics)
      val screenWidth = displayMetrics.widthPixels
      val screenHeight = displayMetrics.heightPixels

      // Calculate dialog dimensions
      val dialogWidth = (screenWidth * 0.8).toInt()
      val dialogHeight = (screenHeight * 0.5).toInt()

      // Set dialog window dimensions
      window?.apply {
        setLayout(dialogWidth, dialogHeight)
        setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        setGravity(Gravity.CENTER) // Center the dialog
      }
      setCancelable(true)  // Allows dismissal by back button or outside touch
      setCanceledOnTouchOutside(true)
    }

    dialog.setContentView(R.layout.dialog_enlarged_image)

    val imageView = dialog.findViewById<ImageView>(R.id.enlargedImageView)
    Picasso.get()
      .load(imageUrl)
      .resize(0, (resources.displayMetrics.heightPixels * 0.5).toInt())
      .onlyScaleDown()
      .centerInside()
      .into(imageView)
    dialog.show()
  }
  private fun fetchAndDisplayRatings() {
    lifecycleScope.launch {
      try {
        val (average, count) = RatingsUtils.fetchAverageRating(peerId)
        withContext(Dispatchers.Main) {
          binding.peerRating.text = "%.1f".format(average)
          binding.peerRatingCount.text = "$count"
        }
      } catch (e: Exception) {
        Log.e("ProfilePreview", "Error fetching ratings", e)
        withContext(Dispatchers.Main) {
          binding.peerRating.text = "0.0"
          binding.peerRatingCount.text = "0"
        }
      }
    }
  }

  private fun fetchTutorSessions(tutorSessionIds: List<String>) {
    if (tutorSessionIds.isEmpty()) {
      binding.tutorSessionsViewSwitcher.displayedChild = 1 // Show empty state
      return
    }

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

  private fun fetchNotes(notesIds: List<String>) {
    if (notesIds.isEmpty()) {
      binding.notesViewSwitcher.displayedChild = 1 // Show empty state
      return
    }

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
    binding.profilePreviewSwipeRefreshLayout.isRefreshing = false
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

  private fun tutorSessionClicked(tutorSession: TutorSession) {
    displayTutorDialog(tutorSession)
  }

  private fun notesClicked(note: Note) {
    displayNoteDialog(note)
  }

  @SuppressLint("MissingInflatedId")
  public fun displayTutorDialog(tutor: TutorSession) {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.tutor_details, null)
    val dialog = android.app.AlertDialog.Builder(this)
      .setView(dialogView)
      .setCancelable(true)
      .create()

    val metrics = Resources.getSystem().displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels
    val windowHeight = (screenHeight * 0.6).toInt()
    val textSizeMedium = (windowHeight * 0.017f)
    val textSizeSmall = (windowHeight * 0.013f)
    val textSizeExtraSmall = (windowHeight * 0.01f)
    val textSizeLarge = (windowHeight * 0.02f)
    dialog.setOnShowListener {
      val window = dialog.window
      window?.setBackgroundDrawableResource(android.R.color.transparent)
      window?.setLayout((screenWidth * 0.90).toInt(), (screenHeight * 0.5).toInt())
      window?.setGravity(Gravity.CENTER)
      dialogView.minimumHeight = windowHeight

    }

    // Assign values to your text views (as you already did)
    val top = dialogView.findViewById<LinearLayout>(R.id.infoLayout)
    val main = dialogView.findViewById<LinearLayout>(R.id.main)
    val name = dialogView.findViewById<TextView>(R.id.tutor_peer_name)
    val roll = dialogView.findViewById<TextView>(R.id.tutor_peer_rollno)
    val subject = dialogView.findViewById<TextView>(R.id.subject)
    val gender = dialogView.findViewById<TextView>(R.id.tgender)
    val session = dialogView.findViewById<TextView>(R.id.type)
    val days = dialogView.findViewById<TextView>(R.id.days)
    val costType = dialogView.findViewById<TextView>(R.id.costType)
    val timeWindow = dialogView.findViewById<TextView>(R.id.timeWindow)
    val cost = dialogView.findViewById<TextView>(R.id.cost)
    val description = dialogView.findViewById<TextView>(R.id.description)
    val genderLabel = dialogView.findViewById<TextView>(R.id.genderLabel)
    val sessionTypeLabel = dialogView.findViewById<TextView>(R.id.sessionTypeLabel)
    val availableDaysLabel = dialogView.findViewById<TextView>(R.id.availableDaysLabel)
    val timeWindowLabel = dialogView.findViewById<TextView>(R.id.timeLabel)
    val text = dialogView.findViewById<TextView>(R.id.text)
    val requestBtn= dialogView.findViewById<TextView>(R.id.requestButton)
    requestBtn.setOnClickListener {
      ButtonLoadingUtils.setLoadingState(requestBtn, true)
      val myId = UserCache.getId()
      val peerId = tutor.peerId
      if (myId == peerId) {
        Toast.makeText(this, "You cannot send a request to yourself", Toast.LENGTH_SHORT).show()
        ButtonLoadingUtils.setLoadingState(requestBtn, false)
        return@setOnClickListener
      }
      val name = UserCache.getUser()?.name

      ChatUtils.startNewChat(
        context = this,
        myId = myId.toString(),
        peerId = peerId,
        message = "Hi, $name from this side. I want to learn ${tutor.skillName} from you.",
        onSuccess = {
          Toast.makeText(this, "Request sent, check in your sessions", Toast.LENGTH_SHORT).show()
          ButtonLoadingUtils.setLoadingState(requestBtn, false)
          dialog.dismiss()
          SessionsFragment.refreshIfVisible()

        },
        onError = { e ->
          Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
          ButtonLoadingUtils.setLoadingState(requestBtn, false)
          dialog.dismiss()
        }
      )
    }

    top.visibility = View.GONE
    main.weightSum = 9f

    var tutorName: String? = null
    var tutorRollNumber: String? = null
    val userRef = firestore.collection("users").document(tutor.peerId)
    userRef.get().addOnSuccessListener { userDoc ->
      Log.d("L6", "Fetched user data for UID: ${tutor.peerId}")
      tutorName = userDoc.getString("name") ?: "Unknown"
      tutorRollNumber = userDoc.getString("rollno") ?: "Unknown"
      Log.d("L6", "Fetched user data: $tutorName, $tutorRollNumber")

      name.text = tutorName
      roll.text = tutorRollNumber


    }.addOnFailureListener {
      Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
    }

    subject.text = tutor.skillName
    subject.textSize = textSizeMedium
    description.text = if (tutor.description.isNullOrEmpty()) {
      "No description".also {
        description.setTextColor(ContextCompat.getColor(this, R.color.grey_1))
      }
    } else {
      tutor.description
    }
    gender.text = tutor.preferredGender
    gender.textSize = textSizeSmall
    session.text = tutor.sessionType
    session.textSize = textSizeSmall
    days.text = tutor.availableDays
    days.textSize = textSizeSmall
    timeWindow.text = tutor.timeWindow
    timeWindow.textSize = textSizeSmall
    sessionTypeLabel.textSize = textSizeSmall
    availableDaysLabel.textSize = textSizeSmall
    timeWindowLabel.textSize = textSizeSmall
    genderLabel.textSize = textSizeSmall
    roll.textSize = textSizeLarge
    name.textSize = textSizeLarge
    costType.text = tutor.sessionPricing
    costType.textSize = textSizeSmall
    cost.text = if (tutor.cost == 0) "Free" else "Rs." + tutor.cost.toString()
    cost.textSize = textSizeSmall
    description.textSize = textSizeSmall
    text.textSize = textSizeMedium
    requestBtn.textSize = textSizeExtraSmall


    dialog.show()
  }

  @SuppressLint("SetTextI18n")
  private fun displayNoteDialog(note: Note) {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.notes_details, null)
    val dialog = android.app.AlertDialog.Builder(this)
      .setView(dialogView)
      .setCancelable(true)
      .create()

    val metrics = Resources.getSystem().displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels
    val windowHeight = (screenHeight * 0.6).toInt()
    val textSizeMedium = (windowHeight * 0.017f)
    val textSizeSmall = (windowHeight * 0.015f)
    val textSizeExtraSmall = (windowHeight * 0.01f)
    val textSizeLarge = (windowHeight * 0.02f)
    dialog.setOnShowListener {
      val window = dialog.window
      window?.setBackgroundDrawableResource(android.R.color.transparent)
      window?.setLayout((screenWidth * 0.9).toInt(), (screenHeight * 0.5).toInt())
      window?.setGravity(Gravity.CENTER)
      dialogView.minimumHeight = windowHeight

    }

    // Assign values to your text views (as you already did)

    val name = dialogView.findViewById<TextView>(R.id.name)
    val roll = dialogView.findViewById<TextView>(R.id.rollno)
    val subject = dialogView.findViewById<TextView>(R.id.subject)
    val type = dialogView.findViewById<TextView>(R.id.tnotes)
    val typeLabel = dialogView.findViewById<TextView>(R.id.type)
    val cost = dialogView.findViewById<TextView>(R.id.cost)
    val description = dialogView.findViewById<TextView>(R.id.description)
    val instructorLabel = dialogView.findViewById<TextView>(R.id.instructorLabel)
    val instructorname = dialogView.findViewById<TextView>(R.id.instructorname)
    val text = dialogView.findViewById<TextView>(R.id.text)
    val requestBtn= dialogView.findViewById<TextView>(R.id.requestButton)
    val top = dialogView.findViewById<LinearLayout>(R.id.infoLayout)
    val main = dialogView.findViewById<LinearLayout>(R.id.main)
    requestBtn.setOnClickListener {
      ButtonLoadingUtils.setLoadingState(requestBtn, true)
      val myId = UserCache.getId()
      val peerId = note.peerId
      if (myId == peerId) {
        Toast.makeText(this, "You cannot send a request to yourself", Toast.LENGTH_SHORT).show()
        ButtonLoadingUtils.setLoadingState(requestBtn, false)
        return@setOnClickListener
      }
      val name = UserCache.getUser()?.name

      ChatUtils.startNewChat(
        context = this,
        myId = myId.toString(),
        peerId = peerId,
        message = "Hi, $name from this side. I want to get ${note.name} notes from you.",
        onSuccess = {
          Toast.makeText(this, "Request sent, check in your sessions", Toast.LENGTH_SHORT).show()
          ButtonLoadingUtils.setLoadingState(requestBtn, false)
          dialog.dismiss()
          SessionsFragment.refreshIfVisible()
        },
        onError = { e ->
          Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
          ButtonLoadingUtils.setLoadingState(requestBtn, false)
          dialog.dismiss()
        }
      )
    }


    //requestBtn.layoutParams.height = (windowHeight * 0.08f).toInt()
    top.visibility = View.GONE
    main.weightSum = 9.1f
    name.textSize = textSizeLarge
    roll.textSize = textSizeLarge
    subject.text = note.name
    subject.textSize = textSizeMedium
    type.text = note.notesType
    type.textSize = textSizeSmall
    instructorname.textSize = textSizeSmall
    instructorname.text = note.instructorName
    cost.text = if (note.cost == 0) "Free" else "Rs." + note.cost.toString()
    cost.textSize = textSizeSmall
    description.text = note.description
    description.textSize = textSizeSmall
    instructorLabel.textSize = textSizeSmall
    typeLabel.textSize = textSizeSmall
    text.textSize = textSizeMedium
    requestBtn.textSize = textSizeExtraSmall

    dialog.show()
  }

  private var selectedRating = 0

  private fun showRatingDialog() {
    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null)
    val dialog = AlertDialog.Builder(this)
      .setView(dialogView)
      .setCancelable(true)
      .create()

    // Get references to all star views
    val stars = listOf(
      dialogView.findViewById<ImageView>(R.id.star1),
      dialogView.findViewById<ImageView>(R.id.star2),
      dialogView.findViewById<ImageView>(R.id.star3),
      dialogView.findViewById<ImageView>(R.id.star4),
      dialogView.findViewById<ImageView>(R.id.star5)
    )

    // Set click listeners for stars
    stars.forEach { star ->
      star.setOnClickListener {
        val rating = star.tag.toString().toInt()
        selectedRating = rating
        updateStars(stars, rating)
      }
    }

    // Submit button click listener
    dialogView.findViewById<Button>(R.id.submitButton).setOnClickListener {
      if (selectedRating > 0) {
        submitRating(selectedRating)
        dialog.dismiss()
      } else {
        Toast.makeText(this, "Please select a rating first", Toast.LENGTH_SHORT).show()
      }
    }
    dialog.apply {
      window?.setBackgroundDrawableResource(R.drawable.display_cardbg)
    }
    dialog.show()
  }

  private fun updateStars(stars: List<ImageView>, rating: Int) {
    stars.forEachIndexed { index, star ->
      if (index < rating) {
        star.setImageResource(R.drawable.star_filled)
      } else {
        star.setImageResource(R.drawable.star_outline)
      }
    }
  }

  private fun submitRating(rating: Int) {
    val userId = UserCache.getId() ?: return
    val ratingId = userId.toString() + peerId.toString()
    val ratingsCollection = firestore.collection("ratings")

    // First check if rating exists
    ratingsCollection.document(ratingId).get()
      .addOnSuccessListener { document ->
        if (document.exists()) {
          // Update existing rating
          ratingsCollection.document(ratingId)
            .update("rating", rating)
            .addOnSuccessListener {
              Toast.makeText(this, "Rating updated", Toast.LENGTH_SHORT).show()
              fetchAndDisplayRatings()
            }
            .addOnFailureListener { e ->
              Toast.makeText(this, "Failed to update rating: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
          // Create new rating
          val newRating = hashMapOf(
            "rating" to rating,
          )

          ratingsCollection.document(ratingId)
            .set(newRating)
            .addOnSuccessListener {
              Toast.makeText(this, "Rating submitted", Toast.LENGTH_SHORT).show()
              fetchAndDisplayRatings()
            }
            .addOnFailureListener { e ->
              Toast.makeText(this, "Failed to submit rating: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Error checking rating: ${e.message}", Toast.LENGTH_SHORT).show()
      }
  }
}