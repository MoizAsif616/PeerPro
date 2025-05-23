package com.example.peerpro.utils

import android.content.Context
import com.example.peerpro.models.Session
import com.example.peerpro.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.google.firebase.Timestamp

object ChatUtils {
  private const val TAG = "L6"

  fun startNewChat(
    context: Context,
    myId: String,
    peerId: String,
    message: String,
    onSuccess: () -> Unit = {},
    onError: (Exception) -> Unit = {}
  ) {
    val sessionId = listOf(myId, peerId).sorted().joinToString("")
    val db = FirebaseFirestore.getInstance()

    Log.d(TAG, "Starting new chat process for sessionId: $sessionId")

    // First check if session exists
    db.collection("sessions").document(sessionId).get()
      .addOnSuccessListener { sessionDoc ->
        if (sessionDoc.exists()) {
          Log.d(TAG, "Session exists - updating existing session")
          updateExistingSessionFlow(db, sessionId, myId, peerId, message, onSuccess, onError)
        } else {
          Log.d(TAG, "Session doesn't exist - creating new session")
          createNewSessionFlow(db, context, myId, peerId, message, sessionId, onSuccess, onError)
        }
      }
      .addOnFailureListener { e ->
        Log.e(TAG, "Error checking session existence", e)
        onError(e)
      }
  }

  private fun updateExistingSessionFlow(
    db: FirebaseFirestore,
    sessionId: String,
    myId: String,
    peerId: String,
    message: String,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
  ) {
    // Update session document
    val sessionUpdates = hashMapOf<String, Any>(
      "lastMessage" to message,
      "sender" to myId,
      "isSeen" to false,
      "timestamp" to Timestamp.now(),
      "chatId" to sessionId
    )

    db.collection("sessions").document(sessionId)
      .update(sessionUpdates)
      .addOnSuccessListener {
        Log.d(TAG, "Session updated successfully")
        createMessageForSession(db, sessionId, myId, message, onSuccess, onError)
      }
      .addOnFailureListener { e ->
        Log.e(TAG, "Error updating session", e)
        onError(e)
      }
  }

  private fun createNewSessionFlow(
    db: FirebaseFirestore,
    context: Context,
    myId: String,
    peerId: String,
    message: String,
    sessionId: String,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
  ) {
    Log.d(TAG, "Creating new session with ID: $sessionId")

    val messageId = db.collection("messages").document().id
    val newMessage = hashMapOf(
      "senderId" to myId,
      "text" to message,
      "isSeen" to false,
      "timestamp" to Timestamp.now(), // Use current date and time
      "deletedBy" to "",
      "chatId" to sessionId // Add chatId attribute
    )

    val batch = db.batch()

    // Create message
    batch.set(db.collection("messages").document(messageId), newMessage)

    // Create session document
    batch.set(db.collection("sessions").document(sessionId),
      hashMapOf(
        "lastMessage" to message,
        "sender" to myId,
        "isSeen" to false,
        "timestamp" to Timestamp.now(), // Use current date and time
        "chatId" to sessionId
      )
    )

    batch.commit()
      .addOnSuccessListener {
        Log.d(TAG, "New session created successfully")
        safelyAddToChatIds(db, myId, peerId, sessionId, onSuccess, onError)
      }
      .addOnFailureListener { e ->
        Log.e(TAG, "Error creating new session", e)
        onError(e)
      }
  }

  private fun createMessageForSession(
    db: FirebaseFirestore,
    sessionId: String,
    myId: String,
    message: String,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
  ) {
    Log.d(TAG, "Creating new message for session: $sessionId")

    val messageId = db.collection("messages").document().id
    val newMessage = hashMapOf(
      "senderId" to myId,
      "text" to message,
      "isSeen" to false,
      "timestamp" to Timestamp.now(), // Use current date and time
      "deletedBy" to "",
      "chatId" to sessionId // Add chatId attribute
    )

    db.collection("messages").document(messageId)
      .set(newMessage)
      .addOnSuccessListener {
        onSuccess()
        Log.d(TAG, "Message created successfully")
      }
      .addOnFailureListener { e ->
        Log.e(TAG, "Error creating message", e)
        onError(e)
      }
  }

  private fun safelyAddToChatIds(
    db: FirebaseFirestore,
    myId: String,
    peerId: String,
    sessionId: String,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
  ) {
    Log.d(TAG, "Safely updating chatIds for users: $myId and $peerId")

    // Function to safely add chatId to a user's document
    fun addChatIdToUser(userId: String, onComplete: (Boolean) -> Unit) {
      db.collection("users").document(userId).get()
        .addOnSuccessListener { userDoc ->

          if (userDoc.exists()) {
            Log.d(TAG, "User doc found for $userId")
            // Check if chatIds field exists and contains the sessionId
            val currentChatIds = userDoc.get("chatIds") as? List<String> ?: emptyList()
            if (currentChatIds.contains(sessionId)) {
              Log.d(TAG, "Session $sessionId already exists in chatIds for user $userId")
              onComplete(true)
              return@addOnSuccessListener
            }

            // Update existing document
            db.collection("users").document(userId)
              .update("chatIds", FieldValue.arrayUnion(sessionId))
              .addOnSuccessListener {
                Log.d(TAG, "Successfully added $sessionId to chatIds for $userId")
                if (userId == UserCache.getId()) {
                  updateUserCache(db, userId)
                }
                onComplete(true)
              }
              .addOnFailureListener { e ->
                Log.e(TAG, "Error updating chatIds for $userId", e)
                onComplete(false)
              }
          }
        }
        .addOnFailureListener { e ->
          Log.e(TAG, "Error checking user document for $userId", e)
          onComplete(false)
        }
    }

    // Update both users
    addChatIdToUser(myId) { mySuccess ->
      if (mySuccess) {
        addChatIdToUser(peerId) { peerSuccess ->
          if (peerSuccess) {
            onSuccess()
          } else {
            onError(Exception("Failed to update peer's chatIds"))
          }
        }
      } else {
        onError(Exception("Failed to update my chatIds"))
      }
    }
  }

  private fun updateUserCache(db: FirebaseFirestore, userId: String) {
    db.collection("users").document(userId).get()
      .addOnSuccessListener { userDoc ->
        val user = userDoc.toObject(User::class.java)
        if (user != null) {
          UserCache.setUser(user)
          Log.d(TAG, "UserCache updated for current user")
        }
      }
  }
}