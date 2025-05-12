package com.example.peerpro.models

data class Chat(
  val messageIds: MutableList<String> // References to messages in "Messages" collection
)