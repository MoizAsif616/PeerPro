package com.example.peerpro.utils

import com.example.peerpro.models.User
import java.util.concurrent.locks.ReentrantLock

object UserCache {
  private var currentUser: User? = null
  private val lock = ReentrantLock()  // For thread-safety

  // Get user (thread-safe)
  fun getUser(): User? {
    lock.lock()
    try {
      return currentUser
    } finally {
      lock.unlock()
    }
  }

  // Set user (thread-safe)
  fun setUser(user: User) {
    lock.lock()
    try {
      currentUser = user
    } finally {
      lock.unlock()
    }
  }

  // Clear cache (thread-safe)
  fun clear() {
    lock.lock()
    try {
      currentUser = null
    } finally {
      lock.unlock()
    }
  }
}