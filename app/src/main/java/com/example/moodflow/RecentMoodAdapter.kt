package com.example.moodflow

import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.*

class RecentMoodAdapter(
    private var moodEntries: List<MoodEntry>,
    private val onMoodClick: (MoodEntry) -> Unit,
    private val onMoodDelete: (MoodEntry) -> Unit
) : RecyclerView.Adapter<RecentMoodAdapter.RecentMoodViewHolder>() {

    inner class RecentMoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardMainContent: MaterialCardView = itemView.findViewById(R.id.cardMainContent)
        private val layoutDeleteButton: LinearLayout = itemView.findViewById(R.id.layoutDeleteButton)
        private val tvMoodDate: MaterialTextView = itemView.findViewById(R.id.tvMoodDate)
        private val tvMoodDay: MaterialTextView = itemView.findViewById(R.id.tvMoodDay)
        private val tvMoodEmoji: TextView = itemView.findViewById(R.id.tvMoodEmoji)
        private val tvMoodName: MaterialTextView = itemView.findViewById(R.id.tvMoodName)
        private val tvMoodTime: MaterialTextView = itemView.findViewById(R.id.tvMoodTime)
        private val tvMoodNote: MaterialTextView = itemView.findViewById(R.id.tvMoodNote)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)
        
        private var isSwipeRevealed = false
        private var startX = 0f
        private var startY = 0f

        fun bind(moodEntry: MoodEntry) {
            val date = Date(moodEntry.timestamp)
            val calendar = Calendar.getInstance().apply { time = date }
            
            // Set date and day
            tvMoodDate.text = calendar.get(Calendar.DAY_OF_MONTH).toString()
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
            tvMoodDay.text = dayFormat.format(date).uppercase()
            
            // Set mood emoji and name
            tvMoodEmoji.text = moodEntry.mood.emoji
            tvMoodName.text = moodEntry.mood.name
            
            // Format time
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            tvMoodTime.text = timeFormat.format(date)
            
            // Show/hide note
            if (moodEntry.note.isNotEmpty()) {
                tvMoodNote.text = moodEntry.note
                tvMoodNote.visibility = View.VISIBLE
            } else {
                tvMoodNote.visibility = View.GONE
            }
            
            // Reset swipe state
            isSwipeRevealed = false
            cardMainContent.translationX = 0f
            layoutDeleteButton.visibility = View.GONE
            
            // Setup click listeners
            cardMainContent.setOnClickListener {
                if (!isSwipeRevealed) {
                    onMoodClick(moodEntry)
                }
            }
            
            // Long press to reveal delete button (backup method)
            cardMainContent.setOnLongClickListener {
                android.util.Log.d("RecentMoodAdapter", "Long press detected")
                if (!isSwipeRevealed) {
                    revealDeleteButton()
                } else {
                    hideDeleteButton()
                }
                true
            }
            
            layoutDeleteButton.setOnClickListener {
                android.util.Log.d("RecentMoodAdapter", "Delete button clicked")
                onMoodDelete(moodEntry)
            }
            
            // Setup swipe gesture
            setupSwipeGesture(moodEntry)
        }
        
        private fun setupSwipeGesture(moodEntry: MoodEntry) {
            var startX = 0f
            var startY = 0f
            
            cardMainContent.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                        startY = event.rawY
                        android.util.Log.d("RecentMoodAdapter", "Touch down at: $startX, $startY")
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - startX
                        val deltaY = event.rawY - startY
                        
                        android.util.Log.d("RecentMoodAdapter", "Touch move deltaX: $deltaX, deltaY: $deltaY")
                        
                        // Check if it's a horizontal swipe
                        if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 50) {
                            // Swipe left to reveal delete button
                            if (deltaX < -50) {
                                android.util.Log.d("RecentMoodAdapter", "Swipe left detected")
                                revealDeleteButton()
                                true
                            }
                            // Swipe right to hide delete button
                            else if (deltaX > 50 && isSwipeRevealed) {
                                android.util.Log.d("RecentMoodAdapter", "Swipe right detected")
                                hideDeleteButton()
                                true
                            } else {
                                false
                            }
                        } else {
                            false
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        android.util.Log.d("RecentMoodAdapter", "Touch up")
                        true
                    }
                    else -> false
                }
            }
        }
        
        private fun revealDeleteButton() {
            android.util.Log.d("RecentMoodAdapter", "Revealing delete button")
            isSwipeRevealed = true
            layoutDeleteButton.visibility = View.VISIBLE
            cardMainContent.animate()
                .translationX(-layoutDeleteButton.width.toFloat())
                .setDuration(200)
                .start()
        }
        
        private fun hideDeleteButton() {
            android.util.Log.d("RecentMoodAdapter", "Hiding delete button")
            isSwipeRevealed = false
            cardMainContent.animate()
                .translationX(0f)
                .setDuration(200)
                .withEndAction {
                    layoutDeleteButton.visibility = View.GONE
                }
                .start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentMoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_mood, parent, false)
        return RecentMoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentMoodViewHolder, position: Int) {
        holder.bind(moodEntries[position])
    }

    override fun getItemCount(): Int = moodEntries.size
    
    fun updateMoodEntries(newMoodEntries: List<MoodEntry>) {
        moodEntries = newMoodEntries
        notifyDataSetChanged()
    }
}
