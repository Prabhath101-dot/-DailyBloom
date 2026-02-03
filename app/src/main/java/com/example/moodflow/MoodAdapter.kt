package com.example.moodflow

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class MoodAdapter(
    private val moods: List<Mood>,
    private val onMoodSelected: (Mood) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    private var selectedPosition = -1

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardMood)
        private val emojiText: TextView = itemView.findViewById(R.id.tvEmoji)
        private val moodName: TextView = itemView.findViewById(R.id.tvMoodName)

        fun bind(mood: Mood, position: Int) {
            emojiText.text = mood.emoji
            moodName.text = mood.name
            
            // Set background color
            cardView.setCardBackgroundColor(Color.parseColor(mood.backgroundColor))
            
            // Handle selection state
            val isSelected = position == selectedPosition
            cardView.strokeWidth = if (isSelected) 4 else 0
            cardView.strokeColor = if (isSelected) Color.parseColor("#4CAF50") else Color.TRANSPARENT
            
            // Set elevation based on selection
            cardView.cardElevation = if (isSelected) 12f else 6f
            
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                
                // Notify changes for animation
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition)
                }
                notifyItemChanged(position)
                
                onMoodSelected(mood)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moods[position], position)
    }

    override fun getItemCount(): Int = moods.size

    fun getSelectedMood(): Mood? {
        return if (selectedPosition != -1) moods[selectedPosition] else null
    }
}
