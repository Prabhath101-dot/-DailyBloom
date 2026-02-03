package com.example.moodflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView

class EmojiCardAdapter(
    private val moods: List<Mood>,
    private val onMoodSelected: (Mood) -> Unit
) : RecyclerView.Adapter<EmojiCardAdapter.EmojiCardViewHolder>() {

    private var selectedMood: Mood? = null

    class EmojiCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: MaterialTextView = itemView.findViewById(R.id.tvEmoji)
        val tvMoodName: MaterialTextView = itemView.findViewById(R.id.tvMoodName)
        val selectionIndicator: View = itemView.findViewById(R.id.selectionIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emoji_card, parent, false)
        return EmojiCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmojiCardViewHolder, position: Int) {
        val mood = moods[position]
        
        holder.tvEmoji.text = mood.emoji
        holder.tvMoodName.text = mood.name
        
        // Update selection state
        val isSelected = selectedMood?.id == mood.id
        holder.selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
        
        // Set click listener
        holder.itemView.setOnClickListener {
            selectedMood = mood
            onMoodSelected(mood)
            notifyDataSetChanged() // Refresh all items to update selection indicators
        }
    }

    override fun getItemCount(): Int = moods.size

    fun getSelectedMood(): Mood? = selectedMood
}
