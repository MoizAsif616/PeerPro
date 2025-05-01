package com.example.peerpro.models

import com.google.firebase.Timestamp

data class TutorSession(
  val skillName: String = "",
  val preferredGender: String = "",
  val sessionType: String = "",
  val availableDays: String = "",
  val timeWindow: String = "",
  val sessionPricing: String = "",
  val cost: Int = 0,
  val description: String = "",
  val tutorId: String = "",
  val createdAt: Timestamp = Timestamp.now()
)
