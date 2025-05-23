package com.example.peerpro.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.R
import com.example.peerpro.models.Session
import com.example.peerpro.models.User
import com.example.peerpro.utils.UserCache
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class SessionsAdapter(private val sessions: MutableList<Session>) :
  RecyclerView.Adapter<SessionsAdapter.SessionViewHolder>() {

  private val firestore = FirebaseFirestore.getInstance()
  var onItemClick: ((Session, String, String, String, String) -> Unit)? = null

  inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val peerImage: ShapeableImageView = itemView.findViewById(R.id.peerImage)
    private val peerName: TextView = itemView.findViewById(R.id.peerName)
    private val peerRoll: TextView = itemView.findViewById(R.id.peerRoll)
    private val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
    private val time: TextView = itemView.findViewById(R.id.time)
    private val status: ImageView = itemView.findViewById(R.id.status)
    fun bind(session: Session) {
      lastMessage.text = session.lastMessage
      // Convert Firestore Timestamp to Date
      val date = session.timestamp.toDate()

      // Format to show just time (e.g., "2:30 PM")
      val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
      time.text = timeFormat.format(date)
      // Set last message color
      val myId = UserCache.getId() ?: ""
      if (session.sender == myId) {
        lastMessage.setTextColor(itemView.context.getColor(R.color.grey_1))
      } else if (!session.isSeen) {
        lastMessage.setTextColor(itemView.context.getColor(R.color.peerLight))
      } else {
        lastMessage.setTextColor(itemView.context.getColor(R.color.grey_1))
      }

      if (!session.isSeen) {
        if (session.sender != myId) {
          Log.d("L6", "Sender is you and message is unseen, sender id is ${session.sender.toString()}")
          status.setImageResource(R.drawable.green)
        }
      }
      else{
        status.setImageResource(0)
      }


      // Fetch and set user details
      val otherUserId = if (session.chatId.startsWith(myId)) {
        session.chatId.removePrefix(myId)
      } else {
        session.chatId.removeSuffix(myId)
      }

      firestore.collection("users").document(otherUserId).get()
        .addOnSuccessListener { userDoc ->
          val user = userDoc.toObject(User::class.java)
          user?.let {
            peerName.text = it.name
            peerRoll.text = it.rollno
            if (it.profilePicUrl.isNullOrEmpty()) {
              peerImage.setImageResource(R.drawable.default_peer)
            } else {
              Picasso.get()
                .load(it.profilePicUrl)
                .placeholder(R.drawable.default_peer)
                .into(peerImage)
            }

            // Pass data to the click listener
            itemView.setOnClickListener {
              onItemClick?.invoke(session, otherUserId, user.name, user.rollno, session.chatId)
            }
          }
        }
        .addOnFailureListener {
          peerName.text = "Unknown"
          peerRoll.text = "N/A"
          peerImage.setImageResource(R.drawable.default_peer)
        }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.session_card, parent, false)

    return SessionViewHolder(view)
  }

  override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
    holder.bind(sessions[position])
  }

  override fun getItemCount(): Int = sessions.size

  fun updateSessions(newSessions: List<Session>) {
    sessions.clear()
    sessions.addAll(newSessions)
    notifyDataSetChanged()
  }

  // In SessionsAdapter
  fun addSession(session: Session) {
    sessions.removeAll { it.chatId == session.chatId } // Remove if exists
    sessions.add(0, session) // Add at top
    sessions.sortByDescending { it.timestamp }
    notifyDataSetChanged()
  }

  fun updateSession(updatedSession: Session) {
    val index = sessions.indexOfFirst { it.chatId == updatedSession.chatId }
    if (index != -1) {
      sessions[index] = updatedSession
      sessions.sortByDescending { it.timestamp }
      notifyDataSetChanged()
    }
  }

  fun removeSession(chatId: String) {
    sessions.removeAll { it.chatId == chatId }
    notifyDataSetChanged()
  }
}
