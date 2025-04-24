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
import android.widget.LinearLayout
import androidx.core.view.marginTop
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.databinding.FragmentNotesBinding
import com.example.peerpro.databinding.NoteCardBinding

class NotesFragment : Fragment() {

  private var _binding: FragmentNotesBinding? = null
  private val binding get() = _binding!!

  data class Card(
    val imageRes: Any,
    val name: String,
    val rollNumber: String,
    val subject: String,
    val notesType: String,
    val instructor: String,
    val cost: String,
  )

  private inner class NotesAdapter(private val notes: List<Card>) :
    RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    // Cached size calculations
    private var itemHeight: Int = 0
    private var itemWidth: Int = 0
    private var itemMarginBottom: Int = 0
    private var dateSize: Float = 0f
    private var textSizeMedium: Float = 0f
    private var textSizeSmall: Float = 0f
    private var imageSize: Int = 0

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
      private val binding = NoteCardBinding.bind(itemView)

      fun bind(note: Card) {
        // Apply cached sizes
        binding.mainContainer.layoutParams = LinearLayout.LayoutParams(
          itemWidth,
          itemHeight
        ).apply {
          bottomMargin = itemMarginBottom
        }

        binding.peerImage.layoutParams.width = (1.01 * imageSize).toInt()
        binding.peerImage.layoutParams.height = imageSize

        (binding.peerName.layoutParams as ViewGroup.MarginLayoutParams).topMargin = (itemHeight * 0.005).toInt()
        (binding.peerRoll.layoutParams as ViewGroup.MarginLayoutParams).topMargin = -(itemHeight * 0.005).toInt()

        // Set text sizes
        binding.peerName.textSize = textSizeSmall
        binding.peerRoll.textSize = textSizeSmall
        binding.notesSubject.textSize = textSizeMedium
        binding.notesTypeLabel.textSize = textSizeSmall
        binding.notesType.textSize = textSizeSmall
        binding.instructorLabel.textSize = textSizeSmall
        binding.instructorName.textSize = textSizeSmall
        binding.notesCost.textSize = textSizeSmall
        binding.notesDate.textSize = dateSize

        // Bind data
        binding.peerName.text = note.name
        binding.peerRoll.text = note.rollNumber
        binding.notesSubject.text = note.subject
        binding.notesType.text = note.notesType
        binding.instructorName.text = note.instructor


        if (note.cost == "RS 0") {
          binding.notesCost.text = "Free"
        } else {
          binding.notesCost.text = note.cost
        }
        // Set date if needed
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
      // Calculate sizes only once when first ViewHolder is created
      if (itemHeight == 0) {
        val displayMetrics = DisplayMetrics()
        val windowManager = parent.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        itemHeight = (screenHeight * 0.29).toInt()
        itemWidth = (screenWidth * 0.47).toInt()
        itemMarginBottom = (screenWidth * 0.02).toInt()

        dateSize = (itemHeight * 0.024f)
        textSizeMedium = (itemHeight * 0.032f)
        textSizeSmall = (itemHeight * 0.03f)
        imageSize = (itemHeight * 0.23).toInt()
      }

      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.note_card, parent, false)
      return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
      holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size
  }

  private inner class HorizontalSpacingDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
      val spacing = (resources.displayMetrics.widthPixels * 0.01).toInt() // 1% spacing
      if (parent.getChildAdapterPosition(view) % 2 == 0) outRect.right = spacing else outRect.left = spacing
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentNotesBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    // Sample data
    val notes = listOf(
      Card(
        imageRes = R.color.black,
        name = "Moiz Asif",
        rollNumber = "l22-6720",
        subject = "Digital Logic Design",
        notesType = "Handwritten",
        instructor = "Amjad Hussain",
        cost = "RS 2000",
      ),
      Card(
        imageRes = R.color.black,
        name = "Huria Ali",
        rollNumber = "l22-6629",
        subject = "Object Oriented Programming",
        notesType = "Printed",
        instructor = "Samin Iftikhar",
        cost = "RS 0",
      ),

      )
    binding.notesCardsRecyclerView.layoutManager = GridLayoutManager(context, 2)
    binding.notesCardsRecyclerView.adapter = NotesAdapter(notes)
    binding.notesCardsRecyclerView.addItemDecoration(HorizontalSpacingDecoration())

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