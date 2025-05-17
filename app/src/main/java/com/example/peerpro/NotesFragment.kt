package com.example.peerpro

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.core.content.ContentProviderCompat.requireContext
import java.text.SimpleDateFormat
import java.util.Date
import androidx.core.view.isVisible
import com.example.peerpro.models.TutorSession
import com.example.peerpro.utils.ButtonLoadingUtils
import com.example.peerpro.utils.ChatUtils
import com.example.peerpro.utils.UserCache


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
  lateinit var searchedAdapter: NotesAdapter;


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
    binding.notesSearchViewSwitcher.visibility = View.GONE
    binding.notesViewSwitcher.visibility = View.VISIBLE
    adapter = NotesAdapter(notes) { note ->
      displayNoteDialog(note)
    }
    binding.notesCardsRecyclerView.layoutManager = GridLayoutManager(context, 2)
    binding.notesCardsRecyclerView.addItemDecoration(HorizontalSpacingDecoration())
    binding.notesCardsRecyclerView.adapter = adapter

    searchedAdapter = NotesAdapter(mutableListOf()) { note->
      displayNoteDialog(note)
    }

    binding.notesSearchedRecyclerView.layoutManager = GridLayoutManager(context, 2)
    binding.notesSearchedRecyclerView.adapter = searchedAdapter
    binding.notesSearchedRecyclerView.addItemDecoration(HorizontalSpacingDecoration())
    binding.notesSearchedRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val totalItemCount = layoutManager.itemCount
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

        // Load more when reaching the 4th item
        if (!isLoading && !isEndReached && totalItemCount > 0 && lastVisibleItem >= 4) {
          loadNotes()
        }
      }
    })
    binding.notesSwipeRefreshLayout.setOnRefreshListener {
      refreshNotes()
    }

    refreshNotes()
  }




  private fun refreshNotes() {
    if (binding.notesSearchViewSwitcher.isVisible) {
      binding.notesSwipeRefreshLayout.isRefreshing = false
      return
    }
    binding.notesSwipeRefreshLayout.isRefreshing = true
    lastVisible = null
    isEndReached = false
    lifecycleScope.launch {
      loadNotes(isRefresh = true)
    }

  }

  private fun loadNotes(isRefresh: Boolean = false)  = lifecycleScope.launch {
    if (isLoading || isEndReached) return@launch
    isLoading = true

    try {
      var query = firestore.collection("notes")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(pageSize.toLong())

      if (!isRefresh && lastVisible != null) {
        query = query.startAfter(lastVisible!!)
      }

      val snapshot = query.get().await()
      val fetchedNotes = snapshot.documents.mapNotNull { it.toObject<Note>() }


      lastVisible = if (snapshot.documents.isNotEmpty()) {
        snapshot.documents[snapshot.documents.size - 1]
      } else {
        null
      }


      if (fetchedNotes.size < pageSize) {
        isEndReached = true
      }
      if (fetchedNotes.isNotEmpty()) {
        binding.notesViewSwitcher.displayedChild = 0
        if (isRefresh) {
          adapter.clearAndSetItems(fetchedNotes)
        } else {
          adapter.addItems(fetchedNotes)
        }
      } else if (isRefresh && adapter.itemCount == 0) {
        // Toast.makeText(requireContext(), "No session found", Toast.LENGTH_LONG).show()
        binding.notesViewSwitcher.displayedChild = 1
        isEndReached = true
      }
      if (isRefresh) {
        binding.notesSwipeRefreshLayout.isRefreshing = false
      }
    } catch (e: Exception) {
      Toast.makeText(requireContext(), "Failed to load more notes", Toast.LENGTH_SHORT).show()
      if (isRefresh && adapter.itemCount == 0) {
        binding.notesViewSwitcher.displayedChild = 1
      }
    } finally {
      isLoading = false
    }
  }


  fun searchNotes(query: String) {
    binding.notesViewSwitcher.visibility = View.GONE
    binding.notesSearchViewSwitcher.visibility = View.VISIBLE
    binding.notesSearchViewSwitcher.displayedChild = 0
    searchedAdapter.clearAndSetItems(mutableListOf())
    searchedAdapter.notifyDataSetChanged()
    var lastVisible: DocumentSnapshot? = null
    var isEndReached = false
    var isLoading = false

    @SuppressLint("NotifyDataSetChanged")
    fun loadSearchResults(isRefresh: Boolean = false) = lifecycleScope.launch {
      if (isLoading || isEndReached) return@launch
      isLoading = true

      try {
        Log.d("SearchDebug", "Starting search for query: $query")
        var queryRef = firestore.collection("notes")
          .orderBy("createdAt", Query.Direction.DESCENDING)
          .limit(50) // Fetch a larger batch to filter locally

        if (!isRefresh && lastVisible != null) {
          queryRef = queryRef.startAfter(lastVisible!!)
        }

        val snapshot = queryRef.get().await()
        Log.d("L6", "Fetched ${snapshot.documents.size} documents from Firestore")

        val allSessions = snapshot.documents.mapNotNull { it.toObject<Note>() }
        Log.d("L6", "Mapped ${allSessions.size} documents to Note objects")

        val filteredNotes = allSessions.filter { note ->
          val name = note.name?.lowercase() ?: ""
          query.lowercase() in name || name == query.lowercase()
        }.take(pageSize)

        Log.d("L6", "Filtered ${filteredNotes.size} notes matching the query")

        lastVisible = if (snapshot.documents.isNotEmpty()) {
          snapshot.documents[snapshot.documents.size - 1]
        } else {
          null
        }

        if (filteredNotes.size < pageSize) {
          isEndReached = true
          Log.d("L6", "End reached for search results")
        }

        if (filteredNotes.isNotEmpty()) {
          binding.notesSearchViewSwitcher.displayedChild = 0 // Show results
          if (isRefresh) {
            searchedAdapter.clearAndSetItems(filteredNotes)
            Log.d("L6", "Adapter refreshed with ${filteredNotes.size} items")
          } else {
            searchedAdapter.addItems(filteredNotes)
            Log.d("L6", "Adapter added ${filteredNotes.size} items")
          }
          Log.d("L6", "Adapter has ${searchedAdapter.itemCount} items")
          searchedAdapter.notifyDataSetChanged()
        } else if (isRefresh && searchedAdapter.itemCount == 0) {
          binding.notesSearchViewSwitcher.displayedChild = 1 // Show "No results found"
          Log.d("L6", "No results found, showing empty state")
        }

      } catch (e: Exception) {
        Log.e("L6", "Error during search: ${e.message}")
        Toast.makeText(requireContext(), "Failed to fetch search results", Toast.LENGTH_LONG).show()
        if (searchedAdapter.itemCount == 0) {
          binding.notesSearchViewSwitcher.displayedChild = 1 // Show "No results found"
        }
      } finally {
        isLoading = false
      }
    }

    binding.notesSearchedRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val totalItemCount = layoutManager.itemCount
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

        if (!isLoading && !isEndReached && totalItemCount > 0 && lastVisibleItem >= totalItemCount - 4) {
          loadSearchResults()
        }
      }
    })

    loadSearchResults(isRefresh = true)
  }

  fun closeSearchView() {
    binding.notesViewSwitcher.visibility = View.VISIBLE
    binding.notesSearchViewSwitcher.visibility = View.GONE
    searchedAdapter.clearAndSetItems(mutableListOf())
    searchedAdapter.notifyDataSetChanged()
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
      window?.setLayout((screenWidth * 0.90).toInt(), (screenHeight * 0.5).toInt())
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
    requestBtn.setOnClickListener {
      ButtonLoadingUtils.setLoadingState(requestBtn, true)
      val myId = UserCache.getId()
      val peerId = note.peerId
      if (myId == peerId) {
        Toast.makeText(requireContext(), "You cannot send a request to yourself", Toast.LENGTH_SHORT).show()
        ButtonLoadingUtils.setLoadingState(requestBtn, false)
        return@setOnClickListener
      }
      val name = UserCache.getUser()?.name

      ChatUtils.startNewChat(
        context = requireContext(),
        myId = myId.toString(),
        peerId = peerId,
        message = "Hi, $name from this side. I want to get ${note.name} notes from you.",
        onSuccess = {
          Toast.makeText(requireContext(),  "Request sent, check in your sessions", Toast.LENGTH_SHORT).show()
          ButtonLoadingUtils.setLoadingState(requestBtn, false)
          dialog.dismiss()
          SessionsFragment.refreshIfVisible()
        },
        onError = { e ->
          Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
          ButtonLoadingUtils.setLoadingState(requestBtn, false)
          dialog.dismiss()
        }
      )
    }

    // Fetch user data
    val userRef = firestore.collection("users").document(note.peerId)
    userRef.get().addOnSuccessListener { userDoc ->
        name.text = userDoc.getString("name") ?: "Unknown"
        roll.text = userDoc.getString("rollno") ?: "Unknown"

      name.setOnClickListener { showPeerProfile(note.peerId) }
      roll.setOnClickListener { showPeerProfile(note.peerId) }
    }.addOnFailureListener {
      Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show()
    }

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

  private fun showPeerProfile(peerId: String) {
    val intent = Intent(requireContext(), ProfilePreviewActivity::class.java)
    intent.putExtra("peerId", peerId)
    startActivity(intent)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}