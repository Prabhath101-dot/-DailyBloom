package com.example.moodflow

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class MoodPickerFragment : Fragment() {
    
    private lateinit var moodAdapter: MoodAdapter
    private var selectedMood: Mood? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood_picker, container, false)

        setupRecyclerView(view)
        setupButtons(view)

        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerMoods)
        
        moodAdapter = MoodAdapter(MoodData.moods) { mood ->
            selectedMood = mood
            // Enable save button when mood is selected
            view.findViewById<MaterialButton>(R.id.btnSaveMood).isEnabled = true
        }
        
        recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = moodAdapter
        }
    }

    private fun setupButtons(view: View) {
        val btnClose = view.findViewById<MaterialButton>(R.id.btnClose)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveMood)

        btnClose.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(ModernMoodFragment())
        }

        btnSave.setOnClickListener {
            selectedMood?.let { mood ->
                saveMoodForToday(mood)
                (requireActivity() as MainActivity).replaceFragment(ModernMoodFragment())
            }
        }
    }

    private fun saveMoodForToday(mood: Mood) {
        val prefs = requireContext().getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
        val todayKey = getTodayKey()
        
        // Save both the mood ID and emoji for easy retrieval
        prefs.edit()
            .putInt(todayKey, mood.id)
            .putString("${todayKey}_emoji", mood.emoji)
            .putString("${todayKey}_name", mood.name)
            .apply()
    }

    private fun getTodayKey(): String {
        val c = java.util.Calendar.getInstance()
        val y = c.get(java.util.Calendar.YEAR)
        val m = c.get(java.util.Calendar.MONTH) + 1
        val d = c.get(java.util.Calendar.DAY_OF_MONTH)
        return "$y-$m-$d"
    }
}


