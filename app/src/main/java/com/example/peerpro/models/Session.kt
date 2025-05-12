package com.example.peerpro.models

data class Session(
  val lastMessage: String,     // Last message text
  val sender: String, // Sender of last message
  val isSeen: Boolean = false,  // Is last message seen?
  val timestamp: Long = System.currentTimeMillis() // For sorting
)