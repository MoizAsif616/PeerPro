package com.example.peerpro

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.marginTop
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.databinding.FragmentTutorsBinding
import com.example.peerpro.databinding.TutorCardBinding
import com.example.peerpro.models.TutorSession
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date

class TutorsFragment : Fragment() {

  private var _binding: FragmentTutorsBinding? = null
  private lateinit var adapter: TutorsAdapter
  private var lastVisible: DocumentSnapshot? = null
  private val pageSize = 10
  private var isLoading = false
  private var isEndReached = false
  private val binding get() = _binding!!


  private inner class HorizontalSpacingDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
      val spacing = (resources.displayMetrics.widthPixels * 0.01).toInt() // 1% spacing
      if (parent.getChildAdapterPosition(view) % 2 == 0) outRect.right = spacing else outRect.left = spacing
    }
  }

  private val firestore = FirebaseFirestore.getInstance()
  private val tutorSessions = mutableListOf<TutorSession>()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentTutorsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    adapter = TutorsAdapter(mutableListOf())
    binding.tutorsCardsRecyclerView.layoutManager = GridLayoutManager(context, 2)
    binding.tutorsCardsRecyclerView.addItemDecoration(HorizontalSpacingDecoration())
    binding.tutorsCardsRecyclerView.adapter = adapter

    binding.tutorsCardsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val totalItemCount = layoutManager.itemCount
        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

        // Load more when reaching the 4th item
        if (!isLoading && !isEndReached && totalItemCount > 0 && lastVisibleItem >= 4) {
          loadTutors()
        }
      }
    })

    binding.tutorsSwipeRefreshLayout.setOnRefreshListener {
      refreshTutors()
    }

    refreshTutors()
  }

  private fun refreshTutors() {
    if (binding.tutorsSearchViewSwitcher.visibility == View.VISIBLE) {
      binding.tutorsSwipeRefreshLayout.isRefreshing = false
      return
    }
    binding.tutorsSwipeRefreshLayout.isRefreshing = true
    lastVisible = null
    isEndReached = false
    lifecycleScope.launch {
      loadTutors(isRefresh = true)
    }
  }

  private fun loadTutors(isRefresh: Boolean = false) = lifecycleScope.launch {
    if (isLoading || isEndReached) return@launch
    isLoading = true

    try {
      var query = firestore.collection("tutor_sessions")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(pageSize.toLong())

      if (!isRefresh && lastVisible != null) {
        query = query.startAfter(lastVisible!!)
      }

      val snapshot = query.get().await()
      val fetchedSessions = snapshot.documents.mapNotNull { it.toObject<TutorSession>() }
      // Toast.makeText(requireContext(), "Fetched ${fetchedSessions.size} sessions", Toast.LENGTH_SHORT).show()

      lastVisible = if (snapshot.documents.isNotEmpty()) {
        snapshot.documents[snapshot.documents.size - 1]
      } else {
        null
      }

      if (fetchedSessions.size < pageSize) {
        isEndReached = true
      }

      if (fetchedSessions.isNotEmpty()) {
        binding.tutorsViewSwitcher.displayedChild = 0
        if (isRefresh) {
          adapter.clearAndSetItems(fetchedSessions)
        } else {
          adapter.addItems(fetchedSessions)
        }
      } else if (isRefresh && adapter.itemCount == 0) {
        // Toast.makeText(requireContext(), "No session found", Toast.LENGTH_LONG).show()
        binding.tutorsViewSwitcher.displayedChild = 1
        isEndReached = true
      }
      if (isRefresh) {
        binding.tutorsSwipeRefreshLayout.isRefreshing = false
      }

    } catch (e: Exception) {
      Toast.makeText(requireContext(), "Failed to fetch tutor sessions", Toast.LENGTH_LONG).show()
      if (isRefresh && adapter.itemCount == 0) {
        binding.tutorsViewSwitcher.displayedChild = 1
      }
    } finally {
      isLoading = false
      // binding.tutorsSwipeRefreshLayout.isRefreshing = false
    }
  }

  fun searchTutors(query: String) {
    // Implement search functionality here
    // For example, filter a list of tutors based on the query
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}