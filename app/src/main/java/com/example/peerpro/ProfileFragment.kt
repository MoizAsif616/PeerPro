package com.example.peerpro

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.databinding.FragmentProfileBinding
import com.example.peerpro.databinding.NotesProfileDisplayBinding
import com.example.peerpro.databinding.TutorProfileDisplayBinding
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {

  private var _binding: FragmentProfileBinding? = null
  private val binding get() = _binding!!

  // Adapter for tutoring cards
  inner class TutoringAdapter(private val items: List<TutoringItem>) :
    RecyclerView.Adapter<TutoringAdapter.TutoringViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutoringViewHolder {
      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.profile_cards, parent, false)
      return TutoringViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutoringViewHolder, position: Int) {
      val item = items[position]
      holder.title.text = item.subject
      holder.date.text = item.date
      holder.cost.text = item.cost

      holder.itemView.setOnClickListener {
        showTutoringDetailsDialog(item)
      }
    }

    override fun getItemCount(): Int = items.size

    inner class TutoringViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      val title: TextView = view.findViewById(R.id.cardTitle)
      val date: TextView = view.findViewById(R.id.cardDate)
      val cost: TextView = view.findViewById(R.id.cardCost)
    }
  }

  // Adapter for notes cards
  inner class NotesAdapter(private val items: List<NotesItem>) :
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.profile_cards, parent, false)
      return NotesViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
      val item = items[position]
      holder.title.text = item.subject
      holder.date.text = item.date
      holder.cost.text = item.cost

      holder.itemView.setOnClickListener {
        showNotesDetailsDialog(item)
      }
    }

    override fun getItemCount(): Int = items.size

    inner class NotesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      val title: TextView = view.findViewById(R.id.cardTitle)
      val date: TextView = view.findViewById(R.id.cardDate)
      val cost: TextView = view.findViewById(R.id.cardCost)
    }
  }

  // Data classes
  data class TutoringItem(
    val subject: String ,
    val date: String,
    val cost: String,
    val description: String = "Default tutoring description",
    val preferredGender: String = "Any",
    val sessionType: String = "Online",
    val availableDays: String = "Flexible",
    val timeWindow: String = "Morning",
    val costType: String = "One Time"
  )

  data class NotesItem(
    val subject: String ,
    val instructor: String,
    val type: String,
    val date: String,
    val cost: String,
    val description: String = "Default notes description",
  )

  // Sample data
  private val tutoringList = listOf(
    TutoringItem(
      "Math Tutoring",
      "March 14, 2025",
      "FREE",
      "Comprehensive math tutoring covering algebra, calculus, and geometry.",
      "Female",
      "Online",
      "Flexible",
      "Morning",
      "One Time"
    ),
    TutoringItem(
      "Django Rest Framework",
      "March 15, 2025",
      "Rs. 500",
      "Learn Django's Model-View-Template architecture and REST framework.",
      "Male",
      "On campus",
      "Mon,Wed,Fri",
      "Morning",
      "Per session"
    )
  )

  private val notesList = listOf(
    NotesItem(
      "Mathematics",
      "John Doe",
      "Printed",
      "March 14, 2025",
      "FREE",
      "Detailed notes covering all major math concepts from algebra to calculus.",

    ),
    NotesItem(
      "Physics",
      "Jane Smith",
      "Hanwritten",
      "March 16, 2025",
      "Rs. 200",
      "Complete collection of physics formulas with explanations.",
    )
  )

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentProfileBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // Setup button listeners
    binding.peerTutoringButton.setOnClickListener { selectTutoring() }
    binding.peerNotesButton.setOnClickListener { selectNotes() }

    // Default selection
    selectTutoring()
  }

  private fun selectTutoring() {
    binding.peerTutoringButton.setBackgroundResource(R.color.peerLight_30)
    binding.peerNotesButton.setBackgroundResource(android.R.color.transparent)
    showTutoringCards()
  }

  private fun selectNotes() {
    binding.peerNotesButton.setBackgroundResource(R.color.peerLight_30)
    binding.peerTutoringButton.setBackgroundResource(android.R.color.transparent)
    showNotesCards()
  }

  private fun showTutoringCards() {
    if (tutoringList.isEmpty()) {
      binding.viewSwitcher.visibility = View.GONE
      binding.emptyState.visibility = View.VISIBLE
    } else {
      binding.viewSwitcher.visibility = View.VISIBLE
      binding.emptyState.visibility = View.GONE
      binding.peerNotesRecyclerView.visibility = View.GONE
      binding.peerTutoringRecyclerView.visibility = View.VISIBLE
      binding.peerTutoringRecyclerView.layoutManager = LinearLayoutManager(requireContext())
      binding.peerTutoringRecyclerView.adapter = TutoringAdapter(tutoringList)
    }
  }

  private fun showNotesCards() {
    if (notesList.isEmpty()) {
      binding.viewSwitcher.visibility = View.GONE
      binding.emptyState.visibility = View.VISIBLE
    } else {
      binding.viewSwitcher.visibility = View.VISIBLE
      binding.emptyState.visibility = View.GONE
      binding.peerTutoringRecyclerView.visibility = View.GONE
      binding.peerNotesRecyclerView.visibility = View.VISIBLE
      binding.peerNotesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
      binding.peerNotesRecyclerView.adapter = NotesAdapter(notesList)
    }
  }

  private fun showTutoringDetailsDialog(tutoring: TutoringItem) {
    val dialog = Dialog(requireContext())
    val dialogBinding = TutorProfileDisplayBinding.inflate(layoutInflater)
    dialog.setContentView(dialogBinding.root)

    val metrics = Resources.getSystem().displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels
    val windowHeight = (screenHeight * 0.5).toInt()
    val textSizeMedium = (windowHeight * 0.022f)
    val textSizeSmall = (windowHeight * 0.015f)

    // Set tutoring data
    dialogBinding.subject.text = tutoring.subject
    dialogBinding.tgender.text = tutoring.preferredGender
    dialogBinding.type.text = tutoring.sessionType
    dialogBinding.days.text = tutoring.availableDays
    dialogBinding.timeWindow.text = tutoring.timeWindow
    dialogBinding.description.text = tutoring.description
    dialogBinding.costType.text = tutoring.costType
    dialogBinding.cost.text = tutoring.cost


    dialogBinding.subject.textSize = textSizeMedium
    dialogBinding.tgender.textSize = textSizeSmall
    dialogBinding.type.textSize = textSizeSmall
    dialogBinding.days.textSize = textSizeSmall
    dialogBinding.timeWindow.textSize = textSizeSmall
    dialogBinding.description.textSize = textSizeSmall
    dialogBinding.costType.textSize = textSizeSmall
    dialogBinding.cost.textSize = textSizeSmall
    dialogBinding.deleteButton.textSize = textSizeSmall
    dialogBinding.sessionTypeLabel.textSize = textSizeSmall
    dialogBinding.availableDaysLabel.textSize = textSizeSmall
    dialogBinding.timeLabel.textSize = textSizeSmall
    dialogBinding.genderLabel.textSize = textSizeSmall

    dialogBinding.deleteButton.setOnClickListener {
      Toast.makeText(requireContext(), "Delete tutoring: ${tutoring.subject}", Toast.LENGTH_SHORT).show()
      dialog.dismiss()
    }

    dialog.window?.apply {
      dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
      dialog.window?.setLayout((screenWidth * 0.75).toInt(), (screenHeight * 0.5).toInt())
      dialog.window?.setGravity(Gravity.CENTER)
    }
    dialog.show()
  }

  private fun showNotesDetailsDialog(note: NotesItem) {
    val dialog = Dialog(requireContext())
    val dialogBinding = NotesProfileDisplayBinding.inflate(layoutInflater)
    dialog.setContentView(dialogBinding.root)

    val metrics = Resources.getSystem().displayMetrics
    val screenWidth = metrics.widthPixels
    val screenHeight = metrics.heightPixels
    val windowHeight = (screenHeight * 0.5).toInt()
    val textSizeMedium = (windowHeight * 0.022f)
    val textSizeSmall = (windowHeight * 0.017f)

    dialogBinding.subject.text = note.subject
    dialogBinding.description.text = note.description
    dialogBinding.cost.text = note.cost
    dialogBinding.tnotes.text = note.type
    dialogBinding.instructorname.text = note.instructor

    dialogBinding.instructorname.textSize = textSizeSmall
    dialogBinding.subject.textSize = textSizeMedium
    dialogBinding.description.textSize = textSizeSmall
    dialogBinding.cost.textSize = textSizeSmall
    dialogBinding.deleteButton.textSize = textSizeSmall
    dialogBinding.tnotes.textSize = textSizeSmall
    dialogBinding.instructorLabel.textSize = textSizeSmall
    dialogBinding.type.textSize = textSizeSmall
    dialogBinding.deleteButton.setOnClickListener {
      Toast.makeText(requireContext(), "Delete note: ${note.subject}", Toast.LENGTH_SHORT).show()
      dialog.dismiss()
    }

    dialog.window?.apply {
      dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
      dialog.window?.setLayout((screenWidth * 0.75).toInt(), (screenHeight * 0.5).toInt())
      dialog.window?.setGravity(Gravity.CENTER)
    }
    dialog.show()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  // Profile action methods
  fun editBio() {
    Toast.makeText(requireContext(), "Edit Bio clicked", Toast.LENGTH_SHORT).show()
  }

  fun changeProfilePic() {
    Toast.makeText(requireContext(), "Change Profile Pic clicked", Toast.LENGTH_SHORT).show()
  }

  fun logout() {
    Toast.makeText(requireContext(), "Logout clicked", Toast.LENGTH_SHORT).show()
  }

  fun deleteAccount() {
    Toast.makeText(requireContext(), "Delete Account clicked", Toast.LENGTH_SHORT).show()
  }
}