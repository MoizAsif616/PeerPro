package com.example.peerpro.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties


data class Message(
  val senderId: String,        // Sender's user ID
  val chatId: String,
  val text: String,            // Message content
  val timestamp: Timestamp = Timestamp.now(), // Current date and time
  val isSeen: Boolean = false,  // Is message read by receiver?
  val deletedBy: String, // User ID of the person who deleted the message
){
  constructor() : this("", "", "", Timestamp.now(), false,"") // Empty constructor
}