package com.example.moodflow

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class HabitsFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitAdapter
    private lateinit var floatingAddButton: ImageView
    private lateinit var layoutCalendarDays: LinearLayout
    
    private var habits = mutableListOf<HabitItem>()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews(view)
        setupSharedPreferences()
        loadHabits()
        setupCalendar()
        setupRecyclerView()
        // Removed filter chips per new UI; also remove function reference/definition
        setupFAB()
        setupBottomNavigation(view)
    }

    override fun onResume() {
        super.onResume()
        // Reload habits when returning from AddHabitActivity
        loadHabits()
        if (this::adapter.isInitialized) {
            adapter.updateHabits(habits)
        }
    }
    
    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.rvHabits)
        floatingAddButton = view.findViewById(R.id.floatingAddButton)
        layoutCalendarDays = view.findViewById(R.id.layoutCalendarDays)
    }
    
    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
    }
    
    private fun loadHabits() {
        val habitsJson = sharedPreferences.getString("habits", null)
        if (habitsJson != null) {
            val type = object : TypeToken<List<HabitItem>>() {}.type
            habits = gson.fromJson(habitsJson, type) ?: mutableListOf()
        } else {
            // Load sample habits for first time
            habits = SampleHabits.getSampleHabits().toMutableList()
            saveHabits()
        }
    }
    
    private fun saveHabits() {
        val habitsJson = gson.toJson(habits)
        sharedPreferences.edit().putString("habits", habitsJson).apply()
    }
    
    private fun setupCalendar() {
        layoutCalendarDays.removeAllViews()
        
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Start from Monday (Calendar.MONDAY = 2)
        val startOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY
        calendar.add(Calendar.DAY_OF_YEAR, -startOfWeek)
        
        val dayNames = arrayOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        
        for (i in 0..6) {
            val dayLayout = createDayLayout(calendar, dayNames[i], i == today - Calendar.MONDAY)
            layoutCalendarDays.addView(dayLayout)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }
    
    private fun createDayLayout(calendar: Calendar, dayName: String, isToday: Boolean): LinearLayout {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size),
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 6
            }
            setPadding(
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 6,
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 6,
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 6,
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 6
            )
            
            if (isToday) {
                setBackgroundResource(R.drawable.day_background_selected)
            }
        }
        
        val dayNameText = TextView(requireContext()).apply {
            text = dayName
            textSize = 12f
            setTextColor(if (isToday) resources.getColor(R.color.habit_text_primary, null) 
                        else resources.getColor(R.color.habit_text_secondary, null))
        }
        
        val dayNumberText = TextView(requireContext()).apply {
            text = calendar.get(Calendar.DAY_OF_MONTH).toString()
            textSize = 16f
            setTextColor(if (isToday) resources.getColor(R.color.habit_text_primary, null) 
                        else resources.getColor(R.color.habit_text_secondary, null))
            if (isToday) {
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
        }
        
        layout.addView(dayNameText)
        layout.addView(dayNumberText)
        
        return layout
    }
    
    private fun setupRecyclerView() {
        adapter = HabitAdapter(habits) { habit, action ->
            when (action) {
                HabitAction.COMPLETE -> completeHabit(habit)
                HabitAction.EDIT -> editHabit(habit)
                HabitAction.DELETE -> deleteHabit(habit)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }
    
    // Filter chips removed per new UI
    
    private fun setupFAB() {
        floatingAddButton.setOnClickListener {
            val intent = android.content.Intent(requireContext(), AddHabitActivity::class.java)
            startActivity(intent)
        }
    }
    
    // Old add/edit dialog removed; using AddHabitActivity instead
    
    private fun createIconButton(iconRes: Int, iconName: String, isSelected: Boolean): ImageButton {
        val button = ImageButton(requireContext())
        val size = (48 * resources.displayMetrics.density).toInt()
        val layoutParams = LinearLayout.LayoutParams(size, size)
        layoutParams.setMargins(8, 0, 8, 0)
        button.layoutParams = layoutParams
        
        button.setImageResource(iconRes)
        button.scaleType = ImageView.ScaleType.CENTER_INSIDE
        button.background = if (isSelected) {
            ContextCompat.getDrawable(requireContext(), R.drawable.icon_selected_background)
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.icon_unselected_background)
        }
        
        button.contentDescription = iconName
        return button
    }
    
    private fun createColorButton(colorRes: Int, colorName: String, isSelected: Boolean): ImageButton {
        val button = ImageButton(requireContext())
        val size = (48 * resources.displayMetrics.density).toInt()
        val layoutParams = LinearLayout.LayoutParams(size, size)
        layoutParams.setMargins(8, 0, 8, 0)
        button.layoutParams = layoutParams
        
        button.setColorFilter(ContextCompat.getColor(requireContext(), colorRes))
        button.setImageResource(R.drawable.ic_color_circle)
        button.scaleType = ImageView.ScaleType.CENTER_INSIDE
        button.background = if (isSelected) {
            ContextCompat.getDrawable(requireContext(), R.drawable.color_selected_background)
        } else {
            ContextCompat.getDrawable(requireContext(), R.drawable.color_unselected_background)
        }
        
        button.contentDescription = colorName
        return button
    }
    
    private fun createTimeChip(timeOption: String, isSelected: Boolean): com.google.android.material.chip.Chip {
        val chip = com.google.android.material.chip.Chip(requireContext())
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        chip.layoutParams = layoutParams
        
        chip.text = timeOption
        chip.isCheckable = true
        chip.isChecked = isSelected
        chip.chipBackgroundColor = if (isSelected) {
            android.content.res.ColorStateList.valueOf(resources.getColor(R.color.md_theme_primary, null))
        } else {
            android.content.res.ColorStateList.valueOf(resources.getColor(R.color.habit_card_background, null))
        }
        chip.setTextColor(if (isSelected) {
            resources.getColor(R.color.habit_text_primary, null)
        } else {
            resources.getColor(R.color.habit_text_secondary, null)
        })
        
        return chip
    }
    
    private fun updateIconSelection(layout: LinearLayout, icons: List<Pair<Int, String>>, selectedIndex: Int) {
        for (i in 0 until layout.childCount) {
            val button = layout.getChildAt(i) as ImageButton
            button.background = if (i == selectedIndex) {
                ContextCompat.getDrawable(requireContext(), R.drawable.icon_selected_background)
            } else {
                ContextCompat.getDrawable(requireContext(), R.drawable.icon_unselected_background)
            }
        }
    }
    
    private fun updateColorSelection(layout: LinearLayout, colors: List<Pair<Int, String>>, selectedIndex: Int) {
        for (i in 0 until layout.childCount) {
            val button = layout.getChildAt(i) as ImageButton
            button.background = if (i == selectedIndex) {
                ContextCompat.getDrawable(requireContext(), R.drawable.color_selected_background)
            } else {
                ContextCompat.getDrawable(requireContext(), R.drawable.color_unselected_background)
            }
        }
    }
    
    private fun updateTimeSelection(layout: LinearLayout, timeOptions: List<String>, selectedIndex: Int) {
        for (i in 0 until layout.childCount) {
            val chip = layout.getChildAt(i) as com.google.android.material.chip.Chip
            chip.isChecked = i == selectedIndex
            chip.chipBackgroundColor = if (i == selectedIndex) {
                android.content.res.ColorStateList.valueOf(resources.getColor(R.color.md_theme_primary, null))
            } else {
                android.content.res.ColorStateList.valueOf(resources.getColor(R.color.habit_card_background, null))
            }
            chip.setTextColor(if (i == selectedIndex) {
                resources.getColor(R.color.habit_text_primary, null)
            } else {
                resources.getColor(R.color.habit_text_secondary, null)
            })
        }
    }
    
    // Old preview update removed with dialog
    
    private fun setupBottomNavigation(view: View) {
        // Get navigation items - these are included layouts
        val navHabits = view.findViewById<View>(R.id.navHabits)
        val navMood = view.findViewById<View>(R.id.navMood)
        val navStats = view.findViewById<View>(R.id.navStats)
        val navSettings = view.findViewById<View>(R.id.navSettings)
        
        // Setup navigation items with icons and labels
        setupNavItem(navHabits, R.drawable.ic_habits, "Habits", true) // Current page
        setupNavItem(navMood, R.drawable.ic_mood, "Mood", false)
        setupNavItem(navStats, R.drawable.ic_stats, "Log Water", false)
        setupNavItem(navSettings, R.drawable.ic_settings, "Settings", false)
        
        // Setup click listeners for navigation
        navHabits.setOnClickListener {
            // Already on habits page, just update selection
            updateNavSelection(navHabits, true)
            updateNavSelection(navMood, false)
            updateNavSelection(navStats, false)
            updateNavSelection(navSettings, false)
        }
        
        navMood.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(ModernMoodFragment())
        }
        
        navStats.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(StatsFragment())
        }
        
        navSettings.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(SettingsFragment())
        }
    }
    
    private fun setupNavItem(navItem: View?, iconRes: Int, label: String, isSelected: Boolean) {
        if (navItem == null) return
        
        val icon = navItem.findViewById<ImageView>(R.id.navIcon)
        val labelView = navItem.findViewById<TextView>(R.id.navLabel)
        
        icon?.setImageResource(iconRes)
        labelView?.text = label
        navItem.isSelected = isSelected
        icon?.isSelected = isSelected
        labelView?.isSelected = isSelected
    }
    
    private fun updateNavSelection(navItem: View?, isSelected: Boolean) {
        if (navItem == null) return
        
        val icon = navItem.findViewById<ImageView>(R.id.navIcon)
        val label = navItem.findViewById<TextView>(R.id.navLabel)
        
        navItem.isSelected = isSelected
        icon?.isSelected = isSelected
        label?.isSelected = isSelected
    }
    
    private fun completeHabit(habit: HabitItem) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            val today = getTodayString()
            val current = habits[index]
            if (current.lastMarkedDate == today) {
                Toast.makeText(requireContext(), "Already marked today", Toast.LENGTH_SHORT).show()
                return
            }
            
            val newProgress = current.currentProgress + 1
            
            // Calculate target days based on unit
            val targetDays = when (current.unit.lowercase(Locale.getDefault())) {
                "day" -> current.targetProgress
                "month" -> current.targetProgress * 30
                "year" -> current.targetProgress * 365
                else -> current.targetProgress
            }
            
            if (newProgress >= targetDays) {
                // Habit completed! Show congratulation dialog and remove it from the list
                showCongratulationDialog(current)
                habits.removeAt(index)
            } else {
                // Update progress
                habits[index] = current.copy(
                    currentProgress = newProgress,
                    lastMarkedDate = today
                )
            }
            
            saveHabits()
            adapter.updateHabits(habits)
        }
    }
    
    private fun getTodayString(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar months are 0-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }
    
    private fun editHabit(habit: HabitItem) {
        val intent = android.content.Intent(requireContext(), AddHabitActivity::class.java)
        intent.putExtra("habit_id", habit.id)
        startActivity(intent)
    }
    
    private fun deleteHabit(habit: HabitItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                habits.removeAll { it.id == habit.id }
                saveHabits()
                adapter.updateHabits(habits)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showCongratulationDialog(habit: HabitItem) {
        val messages = listOf(
            "Amazing work! 🎉 You completed '${habit.name}'!",
            "Fantastic! 🎉 You finished '${habit.name}' successfully!",
            "Well done! 🎉 You achieved your '${habit.name}' goal!",
            "Outstanding! 🎉 You mastered '${habit.name}'!",
            "Brilliant! 🎉 You conquered '${habit.name}'!",
            "Excellent! 🎉 You nailed '${habit.name}'!",
            "Superb! 🎉 You completed '${habit.name}' perfectly!",
            "Wonderful! 🎉 You did it! '${habit.name}' is complete!"
        )
        
        val randomMessage = messages.random()
        
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("🎉 Congratulations!")
            .setMessage(randomMessage)
            .setPositiveButton("Awesome!") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }
}



