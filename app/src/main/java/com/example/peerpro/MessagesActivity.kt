package com.example.peerpro

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.databinding.ActivityMessagesBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MessagesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMessagesBinding
    private lateinit var messagesAdapter: MessagesAdapter
    private val messagesList = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        val peerName = intent.getStringExtra("peer_name") ?: "Peer"
        val profile_pic = intent.getIntExtra("peer_image_res", R.color.black)

        // Setup UI
        setupToolbar(peerName,profile_pic)
        setupRecyclerView()

        // Load messages (you would typically load from database/API)
        loadMessages()
        binding.sendBtn.setOnClickListener {
            val message = binding.messageInput.text.toString()
            if (message.isNotEmpty()) {
                // 1. Add message to RecyclerView
                addNewMessage(message)

                // 2. Clear input field
                binding.messageInput.text.clear()

                // 3. Optional: Close keyboard
                hideKeyboard()
            }
        }
    }

    private fun setupToolbar(peerName: String, profile_pic: Int) {
        binding.peerName.text = peerName
        binding.peerImage.setImageResource(profile_pic)
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        messagesAdapter = MessagesAdapter(messagesList)
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MessagesActivity)
            adapter = messagesAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.top = 2.dpToPx()
                    outRect.bottom = 2.dpToPx()
                }
            })
        }
    }

    private fun addNewMessage(message: String) {
        // Implement your logic to add message to RecyclerView
        // Example:
        val newMessage = Message(
            text = message,
            isSent = true,
            time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        )
        messagesList.add(newMessage)
        binding.messagesRecyclerView.adapter?.notifyItemInserted(messagesList.size - 1)
        binding.messagesRecyclerView.scrollToPosition(messagesList.size - 1)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.messageInput.windowToken, 0)
    }

    private fun sendMessage(text: String) {
        // Create and add new message
        val newMessage = Message(
            text = text,
            isSent = true,
            time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        )

        messagesList.add(newMessage)
        messagesAdapter.notifyItemInserted(messagesList.size - 1)
        binding.messagesRecyclerView.scrollToPosition(messagesList.size - 1)

        // Here you would typically send the message to your backend
    }

    private fun loadMessages() {
        // Sample messages - in real app you'd load from database/API
        val sampleMessages = listOf(
            Message(
                text = "What course are you struggling with?",
                isSent = false,
                time = "10:45 AM"
            ),
            Message(
                text = "I am looking for a tutor.",
                isSent = true,
                time = "10:46 AM"
            ),
            Message(
                text = "How may i help you?",
                isSent = false,
                time = "10:45 AM"
            ),
            Message(
                text = "I am looking for a tutor.",
                isSent = true,
                time = "10:46 AM"
            )
        )

        messagesList.addAll(sampleMessages)
        messagesAdapter.notifyDataSetChanged()
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    data class Message(
        val text: String,
        val isSent: Boolean,
        val time: String
    )
}