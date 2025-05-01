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
  val chatIds: List<String> = emptyList(),
  var tutorSessionIds: List<String> = emptyList(),
  val notesIds: List<String> = emptyList()
)