package com.example.peerpro

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.marginTop
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.TutorsFragment.Card
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
    val description: String ,
    val cost: String,
  )

  private inner class NotesAdapter(private val notes: List<Card>, private val onNoteClick: (Card) -> Unit) :
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
        itemView.setOnClickListener {
          onNoteClick(note)
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


        if (note.cost == "Rs. 0") {
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
        description = "detailed annotated notes",
        cost = "Rs. 2000",
      ),
      Card(
        imageRes = R.color.black,
        name = "Huria Ali",
        rollNumber = "l22-6629",
        subject = "Object Oriented Programming",
        notesType = "Printed",
        instructor = "Samin Iftikhar",
        description = "oop in easy words",
        cost = "Rs. 0",
      ),

      )
    binding.notesCardsRecyclerView.layoutManager = GridLayoutManager(context, 2)
    binding.notesCardsRecyclerView.adapter = NotesAdapter(notes) {note ->
    displayNoteDialog(note)
    }
    binding.notesCardsRecyclerView.addItemDecoration(HorizontalSpacingDecoration())

  }

  fun searchNotes(query: String) {
    // Implement search functionality here
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
  @SuppressLint("SetTextI18n")
  private fun displayNoteDialog(note: com.example.peerpro.NotesFragment.Card) {
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
      window?.setLayout((screenWidth * 0.75).toInt(), (screenHeight * 0.6).toInt())
      window?.setGravity(Gravity.CENTER)
      dialogView.minimumHeight = windowHeight

    }

    // Assign values to your text views (as you already did)
    val name = dialogView.findViewById<TextView>(R.id.name)
    val roll = dialogView.findViewById<TextView>(R.id.rollno)
    val subject = dialogView.findViewById<TextView>(R.id.subject)
    val type = dialogView.findViewById<TextView>(R.id.tnotes)
    val typeLabel = dialogView.findViewById<TextView>(R.id.type)
    val cost = dialogView.findViewById<TextView>(R.id.cost)
    val description = dialogView.findViewById<TextView>(R.id.description)
    val instructorLabel = dialogView.findViewById<TextView>(R.id.instructorLabel)
    val instructorname = dialogView.findViewById<TextView>(R.id.instructorname)
    val text = dialogView.findViewById<TextView>(R.id.text)
    val requestBtn= dialogView.findViewById<TextView>(R.id.requestButton)

    //requestBtn.layoutParams.height = (windowHeight * 0.08f).toInt()
    name.text = note.name
    name.textSize = textSizeLarge
    roll.text = note.rollNumber
    roll.textSize = textSizeLarge
    subject.text = note.subject
    subject.textSize = textSizeMedium
    type.text = note.notesType
    type.textSize = textSizeSmall
    instructorname.textSize = textSizeSmall
    instructorname.text = note.instructor
    cost.text = if (note.cost == "Rs. 0") "Free" else note.cost
    cost.textSize = textSizeSmall
    description.text = note.description
    description.textSize = textSizeSmall
    instructorLabel.textSize = textSizeSmall
    typeLabel.textSize = textSizeSmall
    text.textSize = textSizeMedium
    requestBtn.textSize = textSizeExtraSmall
    dialog.show()
  }


}