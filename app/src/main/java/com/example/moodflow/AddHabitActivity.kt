package com.example.moodflow

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class AddHabitActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etNumber: TextInputEditText
    private lateinit var spinnerUnit: Spinner
    private lateinit var btnSave: Button
    private var editingHabitId: String? = null
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_habit)

        etName = findViewById(R.id.etHabitName)
        etNumber = findViewById(R.id.etTargetNumber)
        spinnerUnit = findViewById(R.id.spinnerUnit)
        btnSave = findViewById(R.id.btnSaveHabit)

        // Back button (MaterialTextView in layout)
        findViewById<View>(R.id.btnBack).setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Setup spinner entries with custom adapter
        val units = resources.getStringArray(R.array.habit_units)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.habit_units,
            R.layout.spinner_item
        ).apply {
            setDropDownViewResource(R.layout.spinner_item)
        }
        spinnerUnit.adapter = adapter

        // If launched for editing, prefill
        editingHabitId = intent.getStringExtra("habit_id")
        if (editingHabitId != null) {
            title = "Edit Habit"
            prefillForEdit(editingHabitId!!)
        }

        btnSave.setOnClickListener { saveHabit() }
    }

    private fun prefillForEdit(id: String) {
        val prefs = getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
        val existingJson = prefs.getString("habits", null)
        val type = object : TypeToken<MutableList<HabitItem>>() {}.type
        val list: MutableList<HabitItem> = if (existingJson != null) Gson().fromJson(existingJson, type) else mutableListOf()
        val habit = list.find { it.id == id } ?: return
        etName.setText(habit.name)
        etNumber.setText(habit.targetProgress.toString())
        val unitIndex = when (habit.unit.lowercase()) { "month" -> 1; "year" -> 2; else -> 0 }
        spinnerUnit.setSelection(unitIndex)
    }

    private fun saveHabit() {
        val name = etName.text?.toString()?.trim().orEmpty()
        val numberStr = etNumber.text?.toString()?.trim().orEmpty()
        val unit = spinnerUnit.selectedItem?.toString()?.lowercase() ?: "day"

        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_enter_habit_name), Toast.LENGTH_SHORT).show()
            return
        }
        val number = numberStr.toIntOrNull()
        if (number == null || number <= 0) {
            Toast.makeText(this, getString(R.string.please_enter_valid_number), Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
        val existingJson = prefs.getString("habits", null)
        val type = object : TypeToken<MutableList<HabitItem>>() {}.type
        val list: MutableList<HabitItem> = if (existingJson != null) gson.fromJson(existingJson, type) else mutableListOf()

        if (editingHabitId != null) {
            val idx = list.indexOfFirst { it.id == editingHabitId }
            if (idx >= 0) {
                val old = list[idx]
                list[idx] = old.copy(
                    name = name,
                    targetProgress = number,
                    unit = unit
                )
            }
        } else {
            val newHabit = HabitItem(
                id = UUID.randomUUID().toString(),
                name = name,
                iconRes = R.drawable.ic_habits,
                iconBackgroundRes = R.drawable.icon_unselected_background,
                colorRes = R.color.habit_blue,
                currentProgress = 0,
                targetProgress = number,
                unit = unit,
                isNew = true,
                streak = 0,
                duration = null,
                timeOfDay = "All Day",
                repeatDaily = true,
                lastMarkedDate = null
            )
            list.add(0, newHabit) // Add new habit at the beginning (top) of the list
        }
        prefs.edit().putString("habits", gson.toJson(list)).apply()

        Toast.makeText(this, getString(R.string.habit_added_successfully), Toast.LENGTH_SHORT).show()
        finish()
    }
}


