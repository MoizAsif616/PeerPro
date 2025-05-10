package com.example.peerpro

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.databinding.FragmentNotesBinding
import com.example.peerpro.models.Note
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.view.Gravity
import java.text.SimpleDateFormat
import java.util.Date
import androidx.core.view.isVisible



class NotesFragment : Fragment() {

  private var _binding: FragmentNotesBinding? = null
  private val binding get() = _binding!!
  private lateinit var adapter: NotesAdapter
  private var lastVisible: DocumentSnapshot? = null
  private val pageSize = 10
  private var isLoading = false
  private var isEndReached = false

  private val firestore = FirebaseFirestore.getInstance()
  private val notes = mutableListOf<Note>()

  private inner class HorizontalSpacingDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
      val spacing = (resources.displayMetrics.widthPixels * 0.01).toInt()
      if (parent.getChildAdapterPosition(view) % 2 == 0) outRect.right = spacing else outRect.left = spacing
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentNotesBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupRecyclerView()
    setupListeners()
    loadInitialNotes()
  }

  private fun setupRecyclerView() {
    adapter = NotesAdapter(notes) { note ->
      displayNoteDialog(note)
    }
    binding.notesCardsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
    binding.notesCardsRecyclerView.addItemDecoration(HorizontalSpacingDecoration())
    binding.notesCardsRecyclerView.adapter = adapter

    binding.notesCardsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val totalItemCount = layoutManager.itemCount
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

        if (!isLoading && !isEndReached && totalItemCount <= lastVisibleItem + 4) {
          loadMoreNotes()
        }
      }
    })
  }

  private fun setupListeners() {
    binding.notesSwipeRefreshLayout.setOnRefreshListener {
      refreshNotes()
    }
  }

  private fun loadInitialNotes() {
    binding.notesSwipeRefreshLayout.isRefreshing = true
    refreshNotes()
  }

  private fun refreshNotes() {
    lifecycleScope.launch {
      try {
        lastVisible = null
        isEndReached = false
        notes.clear()

        val query = firestore.collection("notes")
          .orderBy("createdAt", Query.Direction.DESCENDING)
          .limit(pageSize.toLong())

        val snapshot = query.get().await()
        val fetchedNotes = snapshot.documents.mapNotNull { it.toObject<Note>() }

        if (fetchedNotes.isNotEmpty()) {
          lastVisible = snapshot.documents.last()
          adapter.clearAndSetItems(fetchedNotes)
          binding.notesViewSwitcher.displayedChild = 0
        } else {
          binding.notesViewSwitcher.displayedChild = 1
        }

        if (fetchedNotes.size < pageSize) {
          isEndReached = true
        }
      } catch (e: Exception) {
        Toast.makeText(requireContext(), "Failed to load notes", Toast.LENGTH_SHORT).show()
        if (notes.isEmpty()) {
          binding.notesViewSwitcher.displayedChild = 1
        }
      } finally {
        binding.notesSwipeRefreshLayout.isRefreshing = false
      }
    }
  }

  private fun loadMoreNotes() {
    if (isLoading || isEndReached) return

    lifecycleScope.launch {
      isLoading = true
      try {
        val query = firestore.collection("notes")
          .orderBy("createdAt", Query.Direction.DESCENDING)
          .startAfter(lastVisible!!)
          .limit(pageSize.toLong())

        val snapshot = query.get().await()
        val fetchedNotes = snapshot.documents.mapNotNull { it.toObject<Note>() }

        if (fetchedNotes.isNotEmpty()) {
          lastVisible = snapshot.documents.last()
          adapter.addItems(fetchedNotes)
        }

        if (fetchedNotes.size < pageSize) {
          isEndReached = true
        }
      } catch (e: Exception) {
        Toast.makeText(requireContext(), "Failed to load more notes", Toast.LENGTH_SHORT).show()
      } finally {
        isLoading = false
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun displayNoteDialog(note: Note) {
    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.notes_details, null)
    val dialog = android.app.AlertDialog.Builder(requireContext())
      .setView(dialogView)
      .setCancelable(true)
      .create()

    val metrics = Resources.getSystem().displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels
    val windowHeight = (screenHeight * 0.6).toInt()
    val textSizeMedium = (windowHeight * 0.017f)
    val textSizeSmall = (windowHeight * 0.015f)
    val textSizeExtraSmall = (windowHeight * 0.01f)
    val textSizeLarge = (windowHeight * 0.02f)

    dialog.setOnShowListener {
      val window = dialog.window
      window?.setBackgroundDrawableResource(android.R.color.transparent)
      window?.setLayout((screenWidth * 0.80).toInt(), windowHeight)
      window?.setGravity(Gravity.CENTER)
      dialogView.minimumHeight = windowHeight
    }

    // Initialize views
    val name = dialogView.findViewById<TextView>(R.id.name)
    val roll = dialogView.findViewById<TextView>(R.id.rollno)
    val subject = dialogView.findViewById<TextView>(R.id.subject)
    val type = dialogView.findViewById<TextView>(R.id.tnotes)
    val typeLabel = dialogView.findViewById<TextView>(R.id.type)
    val cost = dialogView.findViewById<TextView>(R.id.cost)
    val description = dialogView.findViewById<TextView>(R.id.description)
    val instructorLabel = dialogView.findViewById<TextView>(R.id.instructorLabel)
    val instructor = dialogView.findViewById<TextView>(R.id.instructorname)
    val text = dialogView.findViewById<TextView>(R.id.text)
    val requestBtn = dialogView.findViewById<TextView>(R.id.requestButton)

    // Fetch user data
    firestore.collection("users").document(note.peerId).get()
      .addOnSuccessListener { userDoc ->
        name.text = userDoc.getString("name") ?: "Unknown"
        roll.text = userDoc.getString("rollno") ?: "Unknown"
      }
      .addOnFailureListener {
        name.text = "Unknown"
        roll.text = "Unknown"
      }

    // Set note data

    description.text = if (note.description.isNullOrEmpty()) {
      "No description".also {
        description.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_1))
      }
    } else {
      note.description
    }
    subject.text = note.name
    subject.textSize = textSizeMedium
    type.text = note.notesType
    type.textSize = textSizeSmall
    instructor.text = note.instructorName
    instructor.textSize = textSizeSmall
    cost.text = if (note.cost == 0) "Free" else "Rs. ${note.cost}"
    cost.textSize = textSizeSmall
    description.textSize = textSizeSmall
    name.textSize = textSizeLarge
    roll.textSize = textSizeLarge
    instructorLabel.textSize = textSizeSmall
    typeLabel.textSize = textSizeSmall
    text.textSize = textSizeMedium
    requestBtn.textSize = textSizeExtraSmall


    binding.notesSwipeRefreshLayout.isRefreshing = false
    dialog.show()
  }

  fun searchNotes(query: String) {
//    if (query.isBlank()) {
//      refreshNotes()
//      return
//    }
//
//    lifecycleScope.launch {
//      try {
//        val snapshot = firestore.collection("notes")
//          .orderBy("name")
//          .startAt(query)
//          .endAt("$query\uf8ff")
//          .get()
//          .await()
//
//        val results = snapshot.documents.mapNotNull { it.toObject<Note>() }
//        adapter.clearAndSetItems(results)
//
//        if (results.isEmpty()) {
//          binding.notesViewSwitcher.displayedChild = 1
//        } else {
//          binding.notesViewSwitcher.displayedChild = 0
//        }
//      } catch (e: Exception) {
//        Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show()
//      }
//    }

  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}