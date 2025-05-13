package com.example.peerpro

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import androidx.core.view.isVisible
import com.example.peerpro.utils.ButtonLoadingUtils
import com.example.peerpro.utils.ChatUtils
import com.example.peerpro.utils.SharedPrefHelper
import com.example.peerpro.utils.UserCache

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
  lateinit var searchedAdapter: TutorsAdapter;

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentTutorsBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    binding.tutorsSearchViewSwitcher.visibility = View.GONE
    binding.tutorsViewSwitcher.visibility = View.VISIBLE

    adapter = TutorsAdapter(mutableListOf()){ tutor ->
      displayTutorDialog(tutor)
    }
    binding.tutorsCardsRecyclerView.layoutManager = GridLayoutManager(context, 2)
    binding.tutorsCardsRecyclerView.addItemDecoration(HorizontalSpacingDecoration())
    binding.tutorsCardsRecyclerView.adapter = adapter

    searchedAdapter = TutorsAdapter(mutableListOf()) { tutor ->
      displayTutorDialog(tutor)
    }

    binding.tutorsSearchedRecyclerView.layoutManager = GridLayoutManager(context, 2)
    binding.tutorsSearchedRecyclerView.adapter = searchedAdapter
    binding.tutorsSearchedRecyclerView.addItemDecoration(HorizontalSpacingDecoration())
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
    if (binding.tutorsSearchViewSwitcher.isVisible) {
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
    binding.tutorsViewSwitcher.visibility = View.GONE
    binding.tutorsSearchViewSwitcher.visibility = View.VISIBLE
    binding.tutorsSearchViewSwitcher.displayedChild = 0
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
        var queryRef = firestore.collection("tutor_sessions")
          .orderBy("createdAt", Query.Direction.DESCENDING)
          .limit(50) // Fetch a larger batch to filter locally

        if (!isRefresh && lastVisible != null) {
          queryRef = queryRef.startAfter(lastVisible!!)
        }

        val snapshot = queryRef.get().await()
        Log.d("L6", "Fetched ${snapshot.documents.size} documents from Firestore")

        val allSessions = snapshot.documents.mapNotNull { it.toObject<TutorSession>() }
        Log.d("L6", "Mapped ${allSessions.size} documents to TutorSession objects")

        val filteredSessions = allSessions.filter { session ->
          val skillName = session.skillName?.lowercase() ?: ""
          query.lowercase() in skillName || skillName == query.lowercase()
        }.take(pageSize)

        Log.d("L6", "Filtered ${filteredSessions.size} sessions matching the query")

        lastVisible = if (snapshot.documents.isNotEmpty()) {
          snapshot.documents[snapshot.documents.size - 1]
        } else {
          null
        }

        if (filteredSessions.size < pageSize) {
          isEndReached = true
          Log.d("L6", "End reached for search results")
        }

        if (filteredSessions.isNotEmpty()) {
          binding.tutorsSearchViewSwitcher.displayedChild = 0 // Show results
          if (isRefresh) {
            searchedAdapter.clearAndSetItems(filteredSessions)
            Log.d("L6", "Adapter refreshed with ${filteredSessions.size} items")
          } else {
            searchedAdapter.addItems(filteredSessions)
            Log.d("L6", "Adapter added ${filteredSessions.size} items")
          }
          Log.d("L6", "Adapter has ${searchedAdapter.itemCount} items")
          searchedAdapter.notifyDataSetChanged()
        } else if (isRefresh && searchedAdapter.itemCount == 0) {
          binding.tutorsSearchViewSwitcher.displayedChild = 1 // Show "No results found"
          Log.d("L6", "No results found, showing empty state")
        }

      } catch (e: Exception) {
        Log.e("L6", "Error during search: ${e.message}")
        Toast.makeText(requireContext(), "Failed to fetch search results", Toast.LENGTH_LONG).show()
        if (searchedAdapter.itemCount == 0) {
          binding.tutorsSearchViewSwitcher.displayedChild = 1 // Show "No results found"
        }
      } finally {
        isLoading = false
      }
    }

    binding.tutorsSearchedRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
    binding.tutorsViewSwitcher.visibility = View.VISIBLE
    binding.tutorsSearchViewSwitcher.visibility = View.GONE
    searchedAdapter.clearAndSetItems(mutableListOf())
    searchedAdapter.notifyDataSetChanged()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  @SuppressLint("SetTextI18n", "MissingInflatedId")
  public fun displayTutorDialog(tutor: TutorSession) {
    val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.tutor_details, null)
    val dialog = android.app.AlertDialog.Builder(requireContext())
      .setView(dialogView)
      .setCancelable(true)
      .create()

    val metrics = Resources.getSystem().displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels
    val windowHeight = (screenHeight * 0.6).toInt()
    val textSizeMedium = (windowHeight * 0.017f)
    val textSizeSmall = (windowHeight * 0.013f)
    val textSizeExtraSmall = (windowHeight * 0.01f)
    val textSizeLarge = (windowHeight * 0.02f)
    dialog.setOnShowListener {
      val window = dialog.window
      window?.setBackgroundDrawableResource(android.R.color.transparent)
      window?.setLayout((screenWidth * 0.90).toInt(), (screenHeight * 0.5).toInt())
      window?.setGravity(Gravity.CENTER)
      dialogView.minimumHeight = windowHeight

    }

    // Assign values to your text views (as you already did)
    val name = dialogView.findViewById<TextView>(R.id.tutor_peer_name)
    val roll = dialogView.findViewById<TextView>(R.id.tutor_peer_rollno)
    val subject = dialogView.findViewById<TextView>(R.id.subject)
    val gender = dialogView.findViewById<TextView>(R.id.tgender)
    val session = dialogView.findViewById<TextView>(R.id.type)
    val days = dialogView.findViewById<TextView>(R.id.days)
    val costType = dialogView.findViewById<TextView>(R.id.costType)
    val timeWindow = dialogView.findViewById<TextView>(R.id.timeWindow)
    val cost = dialogView.findViewById<TextView>(R.id.cost)
    val description = dialogView.findViewById<TextView>(R.id.description)
    val genderLabel = dialogView.findViewById<TextView>(R.id.genderLabel)
    val sessionTypeLabel = dialogView.findViewById<TextView>(R.id.sessionTypeLabel)
    val availableDaysLabel = dialogView.findViewById<TextView>(R.id.availableDaysLabel)
    val timeWindowLabel = dialogView.findViewById<TextView>(R.id.timeLabel)
    val text = dialogView.findViewById<TextView>(R.id.text)
    val requestBtn= dialogView.findViewById<TextView>(R.id.requestButton)

    requestBtn.setOnClickListener {
      ButtonLoadingUtils.setLoadingState(requestBtn, true)
      val myId = UserCache.getId()
      val peerId = tutor.peerId
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
        message = "Hi, $name from this side. I want to learn ${tutor.skillName} from you.",
        onSuccess = {
          Toast.makeText(requireContext(), "Your request has been sent", Toast.LENGTH_SHORT).show()
          ButtonLoadingUtils.setLoadingState(requestBtn, false)
          // Optional: Navigate to chat screen
        },
        onError = { e ->
          Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
          ButtonLoadingUtils.setLoadingState(requestBtn, false)

        }
      )
    }

    var tutorName: String? = null
    var tutorRollNumber: String? = null
    val userRef = firestore.collection("users").document(tutor.peerId)
    userRef.get().addOnSuccessListener { userDoc ->
      Log.d("L6", "Fetched user data for UID: ${tutor.peerId}")
      tutorName = userDoc.getString("name") ?: "Unknown"
      tutorRollNumber = userDoc.getString("rollno") ?: "Unknown"
      Log.d("L6", "Fetched user data: $tutorName, $tutorRollNumber")

      name.text = tutorName
      roll.text = tutorRollNumber

      // Set click listeners to show peer profile
      name.setOnClickListener { showPeerProfile(tutor.peerId) }
      roll.setOnClickListener { showPeerProfile(tutor.peerId) }
    }.addOnFailureListener {
      Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show()
    }

    subject.text = tutor.skillName
    subject.textSize = textSizeMedium
    description.text = if (tutor.description.isNullOrEmpty()) {
      "No description".also {
        description.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_1))
      }
    } else {
      tutor.description
    }
    gender.text = tutor.preferredGender
    gender.textSize = textSizeSmall
    session.text = tutor.sessionType
    session.textSize = textSizeSmall
    days.text = tutor.availableDays
    days.textSize = textSizeSmall
    timeWindow.text = tutor.timeWindow
    timeWindow.textSize = textSizeSmall
    sessionTypeLabel.textSize = textSizeSmall
    availableDaysLabel.textSize = textSizeSmall
    timeWindowLabel.textSize = textSizeSmall
    genderLabel.textSize = textSizeSmall
    roll.textSize = textSizeLarge
    name.textSize = textSizeLarge
    costType.text = tutor.sessionPricing
    costType.textSize = textSizeSmall
    cost.text = if (tutor.cost == 0) "Free" else "Rs." + tutor.cost.toString()
    cost.textSize = textSizeSmall
    description.textSize = textSizeSmall
    text.textSize = textSizeMedium
    requestBtn.textSize = textSizeExtraSmall

    binding.tutorsSwipeRefreshLayout.isRefreshing = false
    dialog.show()
  }

  private fun showPeerProfile(peerId: String) {
    val intent = Intent(requireContext(), ProfilePreviewActivity::class.java)
    intent.putExtra("peerId", peerId)
    startActivity(intent)
  }
}