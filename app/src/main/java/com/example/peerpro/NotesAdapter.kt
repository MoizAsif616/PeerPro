package com.example.peerpro

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.databinding.NoteCardBinding
import com.example.peerpro.databinding.TutorCardBinding
import com.example.peerpro.models.Note
import com.example.peerpro.models.TutorSession
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

class NotesAdapter(private val notes: MutableList<Note>, private val onNoteClick: (Note) -> Unit) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    // Cached size calculations
    private var itemHeight: Int = 0
    private var itemWidth: Int = 0
    private var itemMarginBottom: Int = 0
    private var dateSize: Float = 0f
    private var textSizeMedium: Float = 0f
    private var textSizeSmall: Float = 0f
    private var imageSize: Int = 0
    private val firestore = FirebaseFirestore.getInstance()

    fun addItems(newItems: List<Note>) {
        val startPosition = notes.size
        notes.addAll(newItems.shuffled())
        notifyItemRangeInserted(startPosition, newItems.size)
    }

    fun clearAndSetItems(newItems: List<Note>) {
        notes.clear()
        notes.addAll(newItems.shuffled())
        notifyDataSetChanged()
    }


    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val item = NoteCardBinding.bind(itemView)
        @SuppressLint("SimpleDateFormat")
        fun bind(note: Note, name: String, rollNumber: String, profilePicUrl: String?) {
            // Apply cached sizes

            val layoutParams = itemView.layoutParams as RecyclerView.LayoutParams
            layoutParams.width = itemWidth
            layoutParams.height = itemHeight
            layoutParams.bottomMargin = itemMarginBottom
            itemView.layoutParams = layoutParams

            itemView.setOnClickListener { onNoteClick(note) }

            item.peerImage.layoutParams.width = (1.01 * imageSize).toInt()
            item.peerImage.layoutParams.height = imageSize

            (item.peerName.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
                (itemHeight * 0.005).toInt()
            (item.peerRoll.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
                -(itemHeight * 0.005).toInt()

            // Set text sizes
            item.peerName.textSize = textSizeSmall
            item.peerRoll.textSize = textSizeSmall
            item.notesSubject.textSize = textSizeMedium
            item.notesTypeLabel.textSize = textSizeSmall
            item.notesType.textSize = textSizeSmall
            item.instructorLabel.textSize = textSizeSmall
            item.instructorName.textSize = textSizeSmall
            item.notesCost.textSize = textSizeSmall
            item.notesDate.textSize = dateSize

            // Fetch user data
            firestore.collection("users").document(note.peerId).get()
                .addOnSuccessListener { userDoc ->
                    item.peerName.text = userDoc.getString("name") ?: "Unknown"
                    item.peerRoll.text = userDoc.getString("rollno") ?: "Unknown"
                }
                .addOnFailureListener {
                    item.peerName.text = "Unknown"
                    item.peerRoll.text = "Unknown"
                }

            // Bind note data
            item.notesSubject.text = note.name
            item.notesType.text = note.notesType
            item.instructorName.text = note.instructorName
            item.notesCost.text = if (note.cost == 0) "Free" else "Rs. ${note.cost}"
            item.notesDate.text = SimpleDateFormat("yyyy-MM-dd").format(note.createdAt.toDate())


            // Set profile picture
            if (profilePicUrl.isNullOrEmpty()) {
                item.peerImage.setImageResource(R.color.black)
            } else {
                // Load image using a library like Glide or Picasso
                // Example using Glide:
                // Glide.with(itemView.context).load(profilePicUrl).into(item.tutorImage)
            }
            item.mainContainer.visibility = View.VISIBLE
        }
    }

    @SuppressLint("ServiceCast")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        // Calculate sizes only once when first ViewHolder is created
        if (itemHeight == 0) {
            val displayMetrics = DisplayMetrics()
            val windowManager = parent.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            itemHeight = (screenHeight * 0.29).toInt()
            itemWidth = (screenWidth * 0.47).toInt()
            itemMarginBottom = (screenWidth * 0.02).toInt()

            dateSize = (itemHeight * 0.024f)
            textSizeMedium = (itemHeight * 0.032f)
            textSizeSmall = (itemHeight * 0.03f)
            imageSize = (itemHeight * 0.23).toInt()
        }


        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_card, parent, false)

        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        firestore.collection("users").document(note.peerId).get()
            .addOnSuccessListener { userDoc ->
                val name = userDoc.getString("name") ?: "Unknown"
                val rollNumber = userDoc.getString("rollno") ?: "Unknown"
                val profilePicUrl = userDoc.getString("profilePicUrl")
                holder.bind(note, name, rollNumber, profilePicUrl)
            }
            .addOnFailureListener {
                holder.bind(note, "Unknown", "Unknown", null)
            }
    }

    override fun getItemCount(): Int = notes.size
}