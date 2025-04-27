package com.example.peerpro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

  private var _binding: FragmentProfileBinding? = null
  private val binding get() = _binding!!

  class CardAdapter(private val items: List<CardItem>) :
  RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.profile_cards, parent, false)
    return CardViewHolder(view)
  }

  override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
    val item = items[position]
    holder.title.text = item.title
    holder.date.text = item.date
    holder.cost.text = item.cost
  }

  override fun getItemCount(): Int = items.size

  class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.cardTitle)
    val date: TextView = view.findViewById(R.id.cardDate)
    val cost: TextView = view.findViewById(R.id.cardCost)
  }
}

  private val tutoringList = listOf(
    CardItem("Math Tutoring", "March 14, 2025", "FREE"),
    CardItem("Django Rest framework and MVT", "March 14, 2025", "500"),

  )

  private val notesList : List<CardItem>? = listOf(
    CardItem("Math Notes", "March 14, 2025", "FREE"),

    )

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    _binding = FragmentProfileBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.peerTutoringButton.setOnClickListener {
      selectTutoring()
    }

    binding.peerNotesButton.setOnClickListener {
      selectNotes()
    }

    selectTutoring() // Default selection
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
    if (tutoringList?.isEmpty() ?: true) {
      binding.viewSwitcher.visibility = View.GONE
      binding.emptyState.visibility = View.VISIBLE
    } else {
      binding.viewSwitcher.visibility = View.VISIBLE
      binding.emptyState.visibility = View.GONE
      binding.peerNotesRecyclerView.visibility = View.GONE
      binding.peerTutoringRecyclerView.visibility = View.VISIBLE
      binding.peerTutoringRecyclerView.layoutManager = LinearLayoutManager(requireContext())
      binding.peerTutoringRecyclerView.adapter = CardAdapter(tutoringList)
    }
  }

  private fun showNotesCards() {
    if (notesList?.isEmpty() ?: true) {
      binding.viewSwitcher.visibility = View.GONE
      binding.emptyState.visibility = View.VISIBLE
    } else {
      binding.viewSwitcher.visibility = View.VISIBLE
      binding.emptyState.visibility = View.GONE
      binding.peerTutoringRecyclerView.visibility = View.GONE
      binding.peerNotesRecyclerView.visibility = View.VISIBLE
      binding.peerNotesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
      binding.peerNotesRecyclerView.adapter = CardAdapter(notesList)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

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

data class CardItem(val title: String, val date: String, val cost: String)