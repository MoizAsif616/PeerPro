package com.example.peerpro

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.databinding.TutorCardBinding
import com.example.peerpro.models.TutorSession
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

internal class TutorsAdapter(private val tutors: MutableList<TutorSession> , private val onTutorClick: (TutorSession) -> Unit) :
  RecyclerView.Adapter<TutorsAdapter.TutorViewHolder>() {

  // Cached size calculations
  private var itemHeight: Int = 0
  private var itemWidth: Int = 0
  private var itemMarginBottom: Int = 0
  private var dateSize: Float = 0f
  private var textSizeMedium: Float = 0f
  private var textSizeSmall: Float = 0f
  private var imageSize: Int = 0
  private var firestore = FirebaseFirestore.getInstance()

  fun addItems(newItems: List<TutorSession>) {
    // Adds new tutor sessions to the adapter and updates RecyclerView
    // Shuffles the new items before appending to the existing list
    // Notifies RecyclerView about the newly inserted items
    val startPosition = tutors.size
    tutors.addAll(newItems.shuffled())
    notifyItemRangeInserted(startPosition, newItems.size)
  }

  fun clearAndSetItems(newItems: List<TutorSession>) {
    // Clears existing tutor sessions and sets new ones
    // Shuffles the new items before setting them
    // Notifies RecyclerView about the data change
    tutors.clear()
    tutors.addAll(newItems.shuffled())
    notifyDataSetChanged()
  }

  inner class TutorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val item = TutorCardBinding.bind(itemView)

    @SuppressLint("SimpleDateFormat")
    fun bind(tutor: TutorSession, name: String, rollNumber: String, profilePicUrl: String?) {
      // Apply cached sizes
      val layoutParams = itemView.layoutParams as RecyclerView.LayoutParams
      layoutParams.width = itemWidth
      layoutParams.height = itemHeight
      layoutParams.bottomMargin = itemMarginBottom
      itemView.layoutParams = layoutParams

      itemView.setOnClickListener {
        onTutorClick(tutor)
      }

      item.tutorImage.layoutParams.width = (1.01 * imageSize).toInt()
      item.tutorImage.layoutParams.height = imageSize

      (item.tutorName.layoutParams as ViewGroup.MarginLayoutParams).topMargin = (itemHeight * 0.005).toInt()
      (item.tutorRoll.layoutParams as ViewGroup.MarginLayoutParams).topMargin = -(itemHeight * 0.005).toInt()

      // Set text sizes
      item.tutorName.textSize = textSizeSmall
      item.tutorRoll.textSize = textSizeSmall
      item.tutorSubject.textSize = textSizeMedium
      item.genderLabel.textSize = textSizeSmall
      item.tutorGender.textSize = textSizeSmall
      item.sessionTypeLabel.textSize = textSizeSmall
      item.tutorSessionType.textSize = textSizeSmall
      item.availableDaysLabel.textSize = textSizeSmall
      item.tutorAvailableDays.textSize = textSizeSmall
      item.timeWindowLabel.textSize = textSizeSmall
      item.tutorTimeWindow.textSize = textSizeSmall
      item.costLabel.textSize = textSizeSmall
      item.tutorCost.textSize = textSizeSmall
      item.tutorDate.textSize = dateSize

      // Bind data
      item.tutorName.text = name
      item.tutorRoll.text = rollNumber
      item.tutorSubject.text = tutor.skillName
      item.tutorGender.text = tutor.preferredGender
      item.tutorSessionType.text = tutor.sessionType
      item.tutorAvailableDays.text = tutor.availableDays
      item.tutorTimeWindow.text = tutor.timeWindow

      if (tutor.cost == 0) {
        item.tutorCost.text = "Free"
        item.costLabel.visibility = View.GONE
      } else {
        item.tutorCost.text = "Rs. ${tutor.cost}"
        item.costLabel.text = tutor.sessionPricing
      }

      item.tutorDate.text = SimpleDateFormat("yyyy-MM-dd").format(Date(tutor.createdAt.seconds * 1000))

      // Set profile picture
      if (profilePicUrl.isNullOrEmpty()) {
        item.tutorImage.setImageResource(R.color.black)
      } else {
        // Load image using a library like Glide or Picasso
        // Example using Glide:
        // Glide.with(itemView.context).load(profilePicUrl).into(item.tutorImage)
      }
      item.mainContainer.visibility = View.VISIBLE
    }
  }

  @SuppressLint("ServiceCast")
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorViewHolder {
    // Calculate sizes only once when first ViewHolder is created
    if (itemHeight == 0) {
      val displayMetrics = DisplayMetrics()
      val windowManager = parent.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
      windowManager.defaultDisplay.getMetrics(displayMetrics)

      val screenWidth = displayMetrics.widthPixels
      val screenHeight = displayMetrics.heightPixels

      itemHeight = (screenHeight * 0.33).toInt()
      itemWidth = (screenWidth * 0.47).toInt()
      itemMarginBottom = (screenWidth * 0.02).toInt()

      dateSize = (itemHeight * 0.021f)
      textSizeMedium = (itemHeight * 0.03f)
      textSizeSmall = (itemHeight * 0.027f)
      imageSize = (itemHeight * 0.2).toInt()
    }

    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.tutor_card, parent, false)

    return TutorViewHolder(view)
  }

  override fun onBindViewHolder(holder: TutorViewHolder, position: Int) {
    val tutor = tutors[position]
    firestore.collection("users").document(tutor.peerId).get()
      .addOnSuccessListener { userDoc ->
        val name = userDoc.getString("name") ?: "Unknown"
        val rollNumber = userDoc.getString("rollno") ?: "Unknown"
        val profilePicUrl = userDoc.getString("profilePicUrl")
        holder.bind(tutor, name, rollNumber, profilePicUrl)
      }
      .addOnFailureListener {
        holder.bind(tutor, "Unknown", "Unknown", null)
      }
  }

  override fun getItemCount(): Int = tutors.size
}