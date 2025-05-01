package com.example.peerpro

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.view.marginTop
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.databinding.FragmentSessionsBinding
import com.example.peerpro.databinding.SessionCardBinding

class SessionsFragment : Fragment() {

  private var _binding: FragmentSessionsBinding? = null
  private val binding get() = _binding!!

  data class Card(
    val imageRes: Any,
    val name: String,
    val rollNumber: String,
    val subject: String,
  )

  private inner class SessionsAdapter(private val sessions: List<Card>) :
    RecyclerView.Adapter<SessionsAdapter.SessionViewHolder>() {

    // Cached size calculations
    private var itemHeight: Int = 0
    private var itemWidth: Int = 0
    private var itemMarginBottom: Int = 0
    private var textSizeMedium: Float = 0f
    private var textSizeSmall: Float = 0f
    private var imageSize: Int = 0

    inner class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      private val binding = SessionCardBinding.bind(itemView)

      fun bind(session: Card) {
        // Apply cached sizes
        binding.mainContainer.layoutParams = LinearLayout.LayoutParams(
          itemWidth,
          itemHeight
        ).apply {
          bottomMargin = itemMarginBottom
        }

        binding.peerImage.layoutParams.width = (1.01 * imageSize).toInt()
        binding.peerImage.layoutParams.height = imageSize

        (binding.peerName.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
          (itemHeight * 0.005).toInt()
        (binding.peerRoll.layoutParams as ViewGroup.MarginLayoutParams).topMargin =
          -(itemHeight * 0.005).toInt()

        binding.peerName.textSize = textSizeMedium
        binding.peerRoll.textSize = textSizeSmall
        binding.Subject.textSize = textSizeMedium

        binding.peerName.text = session.name
        binding.peerRoll.text = session.rollNumber
        binding.Subject.text = session.subject
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
      // Calculate sizes only once when first ViewHolder is created
      if (itemHeight == 0) {
        val displayMetrics = DisplayMetrics()
        val windowManager = parent.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        itemHeight = (screenHeight * 0.10).toInt()
        itemWidth = (screenWidth * 0.96).toInt()
        itemMarginBottom = (screenWidth * 0.02).toInt()

        textSizeMedium = (itemHeight * 0.09f)
        textSizeSmall = (itemHeight * 0.08f)
        imageSize = (itemHeight * 0.77).toInt()
      }

      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.session_card, parent, false)
      return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
      holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size

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
    // Sample data
    val sessions = listOf(
      Card(
        imageRes = R.color.black,
        name = "Moiz Asif",
        rollNumber = "l22-6720",
        subject = "Digital Logic Design",
      ),
      Card(
        imageRes = R.color.black,
        name = "Huria Ali",
        rollNumber = "l22-6629",
        subject = "Object Oriented Programming",

      ),

      )
    binding.sessionCardsRecyclerView.layoutManager = GridLayoutManager(context, 1)
    binding.sessionCardsRecyclerView.adapter = SessionsAdapter(sessions)

    // Setup refresh listener
    binding.sessionsSwipeRefreshLayout.setOnRefreshListener {
      refreshSessions()
    }
    // Auto-refresh when fragment is created
    binding.sessionsSwipeRefreshLayout.isRefreshing = true
    refreshSessions()
  }

  private fun refreshSessions() {
    // Simulate network delay
    Handler(Looper.getMainLooper()).postDelayed({
      // This is where you would normally fetch new data
      // For now we'll just update the existing data

      // Stop the refreshing animation
      binding.sessionsSwipeRefreshLayout.isRefreshing = false

      // You would typically update your adapter data here
      // adapter.updateData(newData)

    }, 10000) // 10 second delay
  }

  fun searchSessions(query: String) {
    // Implement search functionality here
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}