package com.example.peerpro

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.models.Note
import com.example.peerpro.models.TutorSession
import java.text.SimpleDateFormat
import java.util.Date

class SessionNotesAdapter<T>(
  private var items: List<T>,
  private val onItemClick: (T) -> Unit
) : RecyclerView.Adapter<SessionNotesAdapter.ProfileSessionViewHolder>() {

  fun updateData(newItems: List<T>) {
    items = newItems
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileSessionViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.profile_cards, parent, false)
    return ProfileSessionViewHolder(view)
  }

  @SuppressLint("SimpleDateFormat")
  override fun onBindViewHolder(holder: ProfileSessionViewHolder, position: Int) {
    val item = items[position]
    if (item is TutorSession) {
      holder.title.text = item.skillName
      holder.date.text = SimpleDateFormat("yyyy-MM-dd").format(Date(item.createdAt.seconds * 1000))
      holder.cost.text = if (item.cost == 0) "Free" else item.cost.toString()
    } else if (item is Note) {
      holder.title.text = item.name
      holder.date.text = SimpleDateFormat("yyyy-MM-dd").format(Date(item.createdAt.seconds * 1000))
      holder.cost.text = if (item.cost == 0) "Free" else item.cost.toString()
    }

    // Set click listener
    holder.itemView.setOnClickListener {
      onItemClick(item)
    }
  }

  override fun getItemCount(): Int = items.size

  class ProfileSessionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.cardTitle)
    val date: TextView = view.findViewById(R.id.cardDate)
    val cost: TextView = view.findViewById(R.id.cardCost)
  }
}
