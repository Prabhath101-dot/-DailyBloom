package com.example.moodflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt
import java.util.Locale
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.example.moodflow.ui.LiquidProgressView

data class HabitItem(
    val id: String,
    val name: String,
    val iconRes: Int,
    val iconBackgroundRes: Int,
    val colorRes: Int,
    val currentProgress: Int,
    val targetProgress: Int,
    val unit: String,
    val isNew: Boolean = false,
    val streak: Int = 0,
    val duration: String? = null,
    val timeOfDay: String = "All Day",
    val repeatDaily: Boolean = true,
    val lastMarkedDate: String? = null
)

enum class HabitAction {
    COMPLETE, EDIT, DELETE
}

class HabitAdapter(
    private var habits: List<HabitItem>,
    private val onHabitAction: (HabitItem, HabitAction) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    companion object {
        // Available colors for habits
        private val AVAILABLE_COLORS = listOf(
            R.color.habit_blue,
            R.color.habit_orange, 
            R.color.habit_green,
            R.color.habit_pink,
            R.color.habit_purple,
            R.color.habit_red,
            R.color.habit_teal,
            R.color.habit_yellow
        )
        
        // Color mapping for hex values
        private val COLOR_HEX_MAP = mapOf(
            R.color.habit_blue to "#3B82F6",
            R.color.habit_orange to "#F97316", 
            R.color.habit_green to "#10B981",
            R.color.habit_pink to "#EC4899",
            R.color.habit_purple to "#8B5CF6",
            R.color.habit_red to "#EF4444",
            R.color.habit_teal to "#14B8A6",
            R.color.habit_yellow to "#EAB308"
        )
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val liquidProgress: LiquidProgressView = itemView.findViewById(R.id.liquidProgress)
        val tvPercent: TextView = itemView.findViewById(R.id.tvPercent)
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val tvProgressCounter: TextView = itemView.findViewById(R.id.tvProgressCounter)
        val tvProgressLabel: TextView = itemView.findViewById(R.id.tvProgressLabel)
        val btnPlus: ImageView = itemView.findViewById(R.id.btnPlus)
        val btnMenu: ImageView = itemView.findViewById(R.id.btnMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        
        holder.tvHabitName.text = habit.name

        // Calculate target days based on unit
        val targetDays = when (habit.unit.lowercase(Locale.getDefault())) {
            "day" -> habit.targetProgress
            "month" -> habit.targetProgress * 30
            "year" -> habit.targetProgress * 365
            else -> habit.targetProgress
        }
        
        // Calculate percentage
        val percent = if (targetDays <= 0) 0 else ((habit.currentProgress.toDouble() / targetDays) * 100).roundToInt().coerceIn(0, 100)
        holder.tvPercent.text = "$percent%"
        
        // Set progress counter (days completed / total days)
        holder.tvProgressCounter.text = "${habit.currentProgress}/$targetDays"
        
        // Set progress label based on unit
        val progressLabel = when (habit.unit.lowercase(Locale.getDefault())) {
            "day" -> "Days"
            "month" -> "Days"
            "year" -> "Days"
            else -> "Days"
        }
        holder.tvProgressLabel.text = progressLabel

        // Set liquid progress color and percentage
        val colorHex = COLOR_HEX_MAP[habit.colorRes] ?: "#8B5CF6"
        val color = android.graphics.Color.parseColor(colorHex)
        holder.liquidProgress.setLiquidColor(color)
        holder.liquidProgress.setProgress(percent.toFloat())

        // Set click listeners
        holder.btnPlus.setOnClickListener {
            onHabitAction(habit, HabitAction.COMPLETE)
        }

        holder.btnMenu.setOnClickListener {
            showActionDialog(holder.itemView.context, habit)
        }
    }
    
    private fun showActionDialog(context: android.content.Context, habit: HabitItem) {
        val options = arrayOf("Edit", "Delete")
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> onHabitAction(habit, HabitAction.EDIT)
                    1 -> onHabitAction(habit, HabitAction.DELETE)
                }
            }
            .show()
    }
    
    
    fun updateHabits(newHabits: List<HabitItem>) {
        habits = assignUniqueColors(newHabits)
        notifyDataSetChanged()
    }
    
    /**
     * Assigns unique colors to habits, ensuring no two habits have the same color
     */
    private fun assignUniqueColors(habits: List<HabitItem>): List<HabitItem> {
        val usedColors = mutableSetOf<Int>()
        val result = mutableListOf<HabitItem>()
        
        for (habit in habits) {
            val assignedColor = if (habit.colorRes != 0 && !usedColors.contains(habit.colorRes)) {
                // Keep existing color if it's not used yet
                habit.colorRes
            } else {
                // Find next available color
                findNextAvailableColor(usedColors)
            }
            
            usedColors.add(assignedColor)
            result.add(habit.copy(colorRes = assignedColor))
        }
        
        return result
    }
    
    /**
     * Finds the next available color from AVAILABLE_COLORS
     */
    private fun findNextAvailableColor(usedColors: Set<Int>): Int {
        for (color in AVAILABLE_COLORS) {
            if (!usedColors.contains(color)) {
                return color
            }
        }
        // If all colors are used, cycle back to the first one
        return AVAILABLE_COLORS.first()
    }

    override fun getItemCount(): Int = habits.size
}

// Sample data for testing
object SampleHabits {
    fun getSampleHabits(): List<HabitItem> = listOf(
        HabitItem(
            id = "1",
            name = "exercise",
            iconRes = R.drawable.ic_water_drop,
            iconBackgroundRes = R.drawable.circle_background_blue,
            colorRes = R.color.habit_blue, // Blue for exercise
            currentProgress = 15,
            targetProgress = 30,
            unit = "day",
            isNew = true,
            timeOfDay = "All Day",
            repeatDaily = true
        ),
        HabitItem(
            id = "2",
            name = "read a book",
            iconRes = R.drawable.ic_fitness_center,
            iconBackgroundRes = R.drawable.circle_background_orange,
            colorRes = R.color.habit_green, // Green for reading
            currentProgress = 8,
            targetProgress = 2,
            unit = "month",
            isNew = true,
            duration = "30m",
            timeOfDay = "Morning",
            repeatDaily = true
        ),
        HabitItem(
            id = "3",
            name = "meditate",
            iconRes = R.drawable.ic_local_dining,
            iconBackgroundRes = R.drawable.circle_background_green,
            colorRes = R.color.habit_pink, // Pink for meditation
            currentProgress = 350,
            targetProgress = 1,
            unit = "year",
            isNew = true,
            duration = "15m",
            timeOfDay = "Evening",
            repeatDaily = true
        )
    )
}
