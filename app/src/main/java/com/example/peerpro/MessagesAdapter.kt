package com.example.peerpro

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.models.Message
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
  private val myId: String
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

  companion object {
    private const val VIEW_TYPE_SENT = 1
    private const val VIEW_TYPE_RECEIVED = 2
    private const val TAG = "L6"
  }

  private val messages = mutableListOf<Message>()

  // Main update function
  fun updateMessages(newMessages: MutableList<Message>) {
    Log.d(TAG, "Updating adapter with ${newMessages.size} messages")

    messages.addAll(0, newMessages.reversed())
    notifyDataSetChanged()
  }

  // For real-time messages
  fun addMessage(message: Message) {
    messages.add(message)
    notifyItemInserted(messages.size - 1)
    Log.d(TAG, "Added new message: ${message.text}")
  }

  // For pagination
  fun prependMessages(newMessages: List<Message>) {
    if (newMessages.isNotEmpty()) {
      messages.addAll(0, newMessages)
      notifyItemRangeInserted(0, newMessages.size)
      Log.d(TAG, "Prepended ${newMessages.size} messages")
    }
  }

  override fun getItemViewType(position: Int): Int {
    return if (messages[position].senderId == myId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
    return when (viewType) {
      VIEW_TYPE_SENT -> {
        val view = LayoutInflater.from(parent.context)
          .inflate(R.layout.send_msg, parent, false)
        SentMessageViewHolder(view)
      }
      else -> {
        val view = LayoutInflater.from(parent.context)
          .inflate(R.layout.recv_msg, parent, false)
        ReceivedMessageViewHolder(view)
      }
    }
  }

  override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
    holder.bind(messages[position])
  }

  override fun getItemCount(): Int = messages.size

  abstract class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(message: Message)
  }

  inner class SentMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
    private val messageBox: TextView = itemView.findViewById(R.id.send_message_box)
    private val messageTime: TextView = itemView.findViewById(R.id.send_message_time)

    override fun bind(message: Message) {
      messageBox.text = message.text
      messageTime.text = SimpleDateFormat("h:mm a", Locale.getDefault())
        .format(Date(message.timestamp.seconds * 1000))
    }
  }

  inner class ReceivedMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
    private val messageBox: TextView = itemView.findViewById(R.id.recv_message_box)
    private val messageTime: TextView = itemView.findViewById(R.id.recv_message_time)

    override fun bind(message: Message) {
      messageBox.text = message.text
      messageTime.text = SimpleDateFormat("h:mm a", Locale.getDefault())
        .format(Date(message.timestamp.seconds * 1000))
    }
  }
}