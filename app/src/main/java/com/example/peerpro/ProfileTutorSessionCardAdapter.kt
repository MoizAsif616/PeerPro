package com.example.peerpro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.peerpro.models.TutorSession

class ProfileTutorSessionCardAdapter(private var items: List<TutorSession>) :
  RecyclerView.Adapter<ProfileTutorSessionCardAdapter.ProfileTutorSessionViewHolder>() {

  fun updateData(newItems: List<TutorSession>) {
    items = newItems
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileTutorSessionViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.profile_cards, parent, false)
    return ProfileTutorSessionViewHolder(view)
  }

  override fun onBindViewHolder(holder: ProfileTutorSessionViewHolder, position: Int) {
    val item = items[position]
    holder.title.text = item.skillName
    holder.date.text = item.createdAt.toDate().toString()
    holder.cost.text = item.cost.toString()
  }

  override fun getItemCount(): Int = items.size

  class ProfileTutorSessionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val title: TextView = view.findViewById(R.id.cardTitle)
    val date: TextView = view.findViewById(R.id.cardDate)
    val cost: TextView = view.findViewById(R.id.cardCost)
  }
}
