package com.example.moodflow

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip

class MoodTagAdapter(
    private val tags: List<MoodTag>,
    private val isSelectable: Boolean = true,
    private val onTagSelected: (MoodTag, Boolean) -> Unit
) : RecyclerView.Adapter<MoodTagAdapter.TagViewHolder>() {

    private val selectedTags = mutableSetOf<String>()

    inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chipTag: Chip = itemView.findViewById(R.id.chipTag)

        fun bind(tag: MoodTag) {
            chipTag.text = tag.name
            chipTag.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                Color.parseColor(tag.color)
            )
            
            if (isSelectable) {
                chipTag.isCheckable = true
                chipTag.isChecked = selectedTags.contains(tag.id)
                
                chipTag.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedTags.add(tag.id)
                    } else {
                        selectedTags.remove(tag.id)
                    }
                    onTagSelected(tag, isChecked)
                }
            } else {
                chipTag.isCheckable = false
                chipTag.isClickable = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_tag, parent, false)
        return TagViewHolder(view)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(tags[position])
    }

    override fun getItemCount(): Int = tags.size

    fun getSelectedTags(): List<String> = selectedTags.toList()
    
    fun clearSelection() {
        selectedTags.clear()
        notifyDataSetChanged()
    }
}
