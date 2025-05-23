package com.example.peerpro

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.peerpro.adapters.SessionsAdapter
import com.example.peerpro.databinding.FragmentSessionsBinding
import com.example.peerpro.databinding.SessionCardBinding
import com.example.peerpro.models.Session
import com.example.peerpro.models.User
import com.example.peerpro.utils.UserCache
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration

class SessionsFragment : Fragment() {

  private var _binding: FragmentSessionsBinding? = null
  private val binding get() = _binding!!

  private lateinit var sessionsAdapter: SessionsAdapter
  private val firestore: FirebaseFirestore = Firebase.firestore

  companion object {
    private var instance: SessionsFragment? = null

    fun refreshIfVisible() {
      instance?.refreshSessions()
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentSessionsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    instance = this
    sessionsAdapter = SessionsAdapter(mutableListOf()).apply {
      onItemClick = { session, otherUserId, peerName, peerRoll, chatId ->
        val intent = Intent(requireContext(), MessagesActivity::class.java).apply {
          putExtra("receiver_id", otherUserId)
          putExtra("receiver_name", peerName)
          putExtra("receiver_roll", peerRoll)
          putExtra("chat_id", chatId)
        }
        startActivity(intent)
      }
    }

    binding.sessionCardsRecyclerView.apply {
      layoutManager = GridLayoutManager(context, 1)
      adapter = sessionsAdapter
    }

    binding.sessionsSwipeRefreshLayout.setOnRefreshListener {
      binding.sessionsSwipeRefreshLayout.isRefreshing = true
      refreshSessions()
    }
    binding.sessionsSwipeRefreshLayout.isRefreshing = true
    refreshSessions()
    setupSessionListener()
  }

  internal fun refreshSessions() {
    Log.d("L6", "Refreshing sessions")
    val myId = UserCache.getId() ?: return
    val sessions = mutableListOf<Session>()

    firestore.collection("sessions")
      .get()
      .addOnSuccessListener { querySnapshot ->
        Log.d("L6", "Fetched sessions: ${querySnapshot.documents.size}")
        val filteredDocuments = querySnapshot.documents.filter { doc ->
          val chatId = doc.getString("chatId")
          chatId != null && (chatId.startsWith(myId) || chatId.endsWith(myId))
        }
        Log.d("L6", "Filtered sessions: ${filteredDocuments.size}")

        filteredDocuments.forEach { document ->
          val chatId = document.getString("chatId") ?: ""
          sessions.add(
            Session(
              sender = document.getString("sender") ?: "",
              lastMessage = document.getString("lastMessage") ?: "",
              chatId = chatId,
              isSeen = document.getBoolean("isSeen") == true,
              timestamp = document.getTimestamp("timestamp") ?: Timestamp.now() // Use current date and time
            )
          )
        }

        sessions.sortByDescending { it.timestamp }
        sessionsAdapter.updateSessions(sessions)

        // Show appropriate child of ViewSwitcher
        if (sessions.isEmpty()) {
          binding.sessionViewSwitcher.displayedChild = 1 // Show "no chats found" view
        } else {
          binding.sessionViewSwitcher.displayedChild = 0 // Show RecyclerView
        }
        binding.sessionsSwipeRefreshLayout.isRefreshing = false
      }
      .addOnFailureListener {
        Toast.makeText(requireContext(), "Failed to fetch sessions", Toast.LENGTH_SHORT).show()
        binding.sessionsSwipeRefreshLayout.isRefreshing = false
      }
  }

  private var sessionListener: ListenerRegistration? = null

  private fun setupSessionListener() {
    val myId = UserCache.getId() ?: return

    sessionListener = firestore.collection("sessions")
      .addSnapshotListener { snapshot, error ->
        error?.let {
          Log.e("L6", "Session sync error: ${it.message}")
          Toast.makeText(requireContext(), "Session sync error", Toast.LENGTH_SHORT).show()
          return@addSnapshotListener
        }

        snapshot?.documentChanges?.forEach { change ->
          Log.d("L6", "Session change: ${change.type} for document: ${change.document.id}")
          val chatId = change.document.getString("chatId") ?: ""
          if (chatId.startsWith(myId) || chatId.endsWith(myId)) {
            when (change.type) {
              DocumentChange.Type.ADDED -> {
                val session = change.document.toObject(Session::class.java)
                sessionsAdapter.addSession(session)
              }
              DocumentChange.Type.MODIFIED -> {
                Log.d("L6", "Session modified: ${change.document.id}")
                val session = change.document.toObject(Session::class.java)
                sessionsAdapter.updateSession(session)
              }
              DocumentChange.Type.REMOVED -> {
                sessionsAdapter.removeSession(chatId)
              }
            }
          }
        }
      }
  }

  private fun showDeleteDialog(view: View, chat: Session, position: Int) {
    Log.d("DialogDebug", "Attempting to show dialog at specific position")

    // Inflate the dialog layout
    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.delete_session_popup, null)

    // Create the popup window
    val popupWindow = PopupWindow(dialogView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

    // Get the location of the clicked item
    val location = IntArray(2)
    view.getLocationOnScreen(location)
    val x = location[0]
    val y = location[1]
    val width = view.width
    val height = view.height

    // Offset the popup to align to the bottom-right
    val offsetX = width - popupWindow.contentView.measuredWidth
    val offsetY = height

    popupWindow.showAtLocation(view, 0, x + offsetX, y + offsetY)


    dialogView.setOnClickListener {
      //popupWindow.dismiss()
    }
  }

  fun searchSessions(query: String) {
    // Implement search functionality here
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
    instance = null
  }
}