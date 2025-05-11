package com.example.peerpro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessagesAdapter(private val messages: List<MessagesActivity.Message>) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSent) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
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
        abstract fun bind(message: MessagesActivity.Message)
    }

    inner class SentMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val messageBox: TextView = itemView.findViewById(R.id.send_message_box)
        private val messageTime: TextView = itemView.findViewById(R.id.send_message_time)

        override fun bind(message: MessagesActivity.Message) {
            messageBox.text = message.text
            messageTime.text = message.time
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : MessageViewHolder(itemView) {
        private val messageBox: TextView = itemView.findViewById(R.id.recv_message_box)
        private val messageTime: TextView = itemView.findViewById(R.id.recv_message_time)

        override fun bind(message: MessagesActivity.Message) {
            messageBox.text = message.text
            messageTime.text = message.time
        }
    }
}