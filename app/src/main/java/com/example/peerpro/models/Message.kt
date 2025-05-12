package com.example.peerpro.models

data class Message(
  val senderId: String,        // Sender's user ID
  val text: String,            // Message content
  val timestamp: Long = System.currentTimeMillis(),
  val isSeen: Boolean = false  // Is message read by receiver?
)