package com.example.peerpro.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
  // val id: String = "",
  val rollno: String = "",
  val email: String = "",
  var bio: String = "",
  var profilePicUrl: String = "",
  val name: String = "",
  var chatIds: List<String> = emptyList(),
  var tutorSessionIds: List<String> = emptyList(),
  var notesIds: List<String> = emptyList()
)