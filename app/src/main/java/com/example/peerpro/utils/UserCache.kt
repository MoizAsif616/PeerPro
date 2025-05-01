package com.example.peerpro.utils

import com.example.peerpro.models.User
import java.util.concurrent.locks.ReentrantLock

object UserCache {
  private var currentUser: User? = null
  private var id: String? = null
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

  fun getId(): String? {
    lock.lock()
    try {
      return id
    } finally {
      lock.unlock()
    }
  }

  fun setId(id: String) {
    lock.lock()
    try {
      this.id = id
    } finally {
      lock.unlock()
    }
  }

  // Clear cache (thread-safe)
  fun clear() {
    lock.lock()
    try {
      currentUser = null
      id = null
    } finally {
      lock.unlock()
    }
  }
}