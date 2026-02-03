package com.example.moodflow

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class ModernMoodFragment : Fragment() {
    
    private lateinit var viewPagerEmojis: ViewPager2
    private lateinit var btnSaveMood: MaterialButton
    private lateinit var etMoodReason: com.google.android.material.textfield.TextInputEditText
    private lateinit var layoutMoodReason: com.google.android.material.textfield.TextInputLayout
    private lateinit var recyclerRecentMoods: RecyclerView
    private lateinit var fabAddMood: FloatingActionButton
    private lateinit var modalOverlay: View
    private lateinit var moodEntryModal: MaterialCardView
    private lateinit var recyclerMoodSelector: RecyclerView
    private lateinit var recyclerTags: RecyclerView
    private lateinit var etMoodNote: TextInputEditText
    private lateinit var btnSaveMoodEntry: MaterialButton
    private lateinit var btnCloseModal: MaterialButton
    private lateinit var tvMonthTitle: MaterialTextView
    
    private lateinit var emojiCardAdapter: EmojiCardAdapter
    private lateinit var moodAdapter: MoodAdapter
    private lateinit var tagAdapter: MoodTagAdapter
    private lateinit var recentMoodAdapter: RecentMoodAdapter
    
    private var selectedMood: Mood? = null
    private val moodEntries = mutableListOf<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood_modern, container, false)
        
        initializeViews(view)
        setupEmojiSwipeCard()
        setupMoodSelector()
        setupTagSelector()
        setupRecentMoods()
        setupFAB()
        setupModal()
        setupBottomNavigation(view)
        loadMoodData()
        
        // Update adapter after loading data
        recentMoodAdapter.updateMoodEntries(moodEntries)
        
        return view
    }
    
    private fun initializeViews(view: View) {
        try {
            // Views from emoji_swipe_container (included layout)
            viewPagerEmojis = view.findViewById(R.id.viewPagerEmojis)
            btnSaveMood = view.findViewById(R.id.btnSaveMood)
            etMoodReason = view.findViewById(R.id.etMoodReason)
            layoutMoodReason = view.findViewById(R.id.layoutMoodReason)
            
            // Views from main layout
            recyclerRecentMoods = view.findViewById(R.id.recyclerRecentMoods)
            fabAddMood = view.findViewById(R.id.fabAddMood)
            modalOverlay = view.findViewById(R.id.modalOverlay)
            moodEntryModal = view.findViewById(R.id.moodEntryModal)
            recyclerMoodSelector = view.findViewById(R.id.recyclerMoodSelector)
            recyclerTags = view.findViewById(R.id.recyclerTags)
            etMoodNote = view.findViewById(R.id.etMoodNote)
            btnSaveMoodEntry = view.findViewById(R.id.btnSaveMoodEntry)
            btnCloseModal = view.findViewById(R.id.btnCloseModal)
            tvMonthTitle = view.findViewById(R.id.tvMonthTitle)
        } catch (e: Exception) {
            android.util.Log.e("ModernMoodFragment", "Error initializing views: ${e.message}")
            throw e
        }
    }
    
    private fun setupEmojiSwipeCard() {
        try {
            // Setup ViewPager2 for emoji cards
            emojiCardAdapter = EmojiCardAdapter(MoodData.moods) { mood ->
                android.util.Log.d("ModernMoodFragment", "Mood selected: ${mood.name}")
                selectedMood = mood
                btnSaveMood.isEnabled = true
                // Show the reason input field when emoji is selected
                layoutMoodReason.visibility = View.VISIBLE
                android.util.Log.d("ModernMoodFragment", "Save button enabled: ${btnSaveMood.isEnabled}")
            }
            
            viewPagerEmojis.adapter = emojiCardAdapter
            
            // Setup save and skip buttons
            btnSaveMood.setOnClickListener {
                android.util.Log.d("ModernMoodFragment", "Save Mood button clicked!")
                android.util.Log.d("ModernMoodFragment", "Selected mood: $selectedMood")
                
                selectedMood?.let { mood ->
                    android.util.Log.d("ModernMoodFragment", "Calling saveMoodForToday with mood: ${mood.name}")
                    saveMoodForToday(mood)
                } ?: run {
                    android.util.Log.w("ModernMoodFragment", "No mood selected when save button clicked!")
                    android.widget.Toast.makeText(requireContext(), getString(R.string.please_select_mood_first), android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("ModernMoodFragment", "Error setting up emoji swipe card: ${e.message}")
            throw e
        }
    }
    
    
    
    private fun saveMoodForToday(mood: Mood) {
        android.util.Log.d("ModernMoodFragment", "saveMoodForToday called with mood: ${mood.name}")
        
        val reason = etMoodReason.text?.toString()?.trim() ?: ""
        android.util.Log.d("ModernMoodFragment", "Reason: '$reason'")
        
        val moodEntry = MoodEntry(
            mood = mood,
            note = reason,
            tags = emptyList(),
            timestamp = System.currentTimeMillis()
        )
        
        android.util.Log.d("ModernMoodFragment", "Created moodEntry: $moodEntry")
        android.util.Log.d("ModernMoodFragment", "Current moodEntries size before add: ${moodEntries.size}")
        
        // Add to mood entries
        moodEntries.add(0, moodEntry) // Add to beginning for recent display
        
        android.util.Log.d("ModernMoodFragment", "Current moodEntries size after add: ${moodEntries.size}")
        
        // Save to SharedPreferences
        saveMoodData()
        
        // Update recent moods display
        recentMoodAdapter.updateMoodEntries(moodEntries)
        
        android.util.Log.d("ModernMoodFragment", "Adapter notified, item count: ${recentMoodAdapter.itemCount}")
        
        // Show success feedback
        android.widget.Toast.makeText(requireContext(), getString(R.string.mood_saved_successfully), android.widget.Toast.LENGTH_SHORT).show()
        
        // Reset selection and clear reason input
        selectedMood = null
        btnSaveMood.isEnabled = false
        etMoodReason.text?.clear()
        // Hide the reason input field
        layoutMoodReason.visibility = View.GONE
    }
    
    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        android.util.Log.d("ModernMoodFragment", "Deleting mood entry: ${moodEntry.mood.name}")
        
        // Remove from list
        moodEntries.remove(moodEntry)
        
        // Save updated list to SharedPreferences
        saveMoodData()
        
        // Update adapter
        recentMoodAdapter.updateMoodEntries(moodEntries)
        
        // Show success feedback
        android.widget.Toast.makeText(requireContext(), getString(R.string.mood_deleted_successfully), android.widget.Toast.LENGTH_SHORT).show()
    }
    
    
    private fun setupMoodSelector() {
        moodAdapter = MoodAdapter(MoodData.moods) { mood ->
            selectedMood = mood
            btnSaveMoodEntry.isEnabled = true
        }
        
        recyclerMoodSelector.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerMoodSelector.adapter = moodAdapter
    }
    
    private fun setupTagSelector() {
        tagAdapter = MoodTagAdapter(MoodData.availableTags, true) { tag, isSelected ->
            // Tag selection handled by adapter
        }
        
        recyclerTags.layoutManager = LinearLayoutManager(
            requireContext(), 
            LinearLayoutManager.HORIZONTAL, 
            false
        )
        recyclerTags.adapter = tagAdapter
    }
    
    private fun setupRecentMoods() {
        android.util.Log.d("ModernMoodFragment", "setupRecentMoods called with ${moodEntries.size} entries")
        
        recentMoodAdapter = RecentMoodAdapter(moodEntries, 
            onMoodClick = { moodEntry ->
                // Handle mood entry click (could open edit dialog)
                android.util.Log.d("ModernMoodFragment", "Mood entry clicked: ${moodEntry.mood.name}")
            },
            onMoodDelete = { moodEntry ->
                // Handle mood entry delete
                deleteMoodEntry(moodEntry)
            }
        )
        
        recyclerRecentMoods.layoutManager = LinearLayoutManager(requireContext())
        recyclerRecentMoods.adapter = recentMoodAdapter
        
        android.util.Log.d("ModernMoodFragment", "RecentMoodAdapter setup complete")
    }
    
    private fun setupFAB() {
        fabAddMood.setOnClickListener {
            openMoodEntryModal()
        }
    }
    
    private fun setupModal() {
        btnCloseModal.setOnClickListener {
            closeMoodEntryModal()
        }
        
        modalOverlay.setOnClickListener {
            closeMoodEntryModal()
        }
        
        btnSaveMoodEntry.setOnClickListener {
            selectedMood?.let { mood ->
                val note = etMoodNote.text?.toString()?.trim() ?: ""
                val selectedTags = tagAdapter.getSelectedTags()
                
                val moodEntry = MoodEntry(
                    mood = mood,
                    note = note,
                    tags = selectedTags,
                    timestamp = System.currentTimeMillis()
                )
                
                // Add to mood entries
                moodEntries.add(0, moodEntry)
                
                // Save to SharedPreferences
                saveMoodData()
                
                // Update recent moods display
                recentMoodAdapter.updateMoodEntries(moodEntries)
                
                // Show success feedback
                android.widget.Toast.makeText(requireContext(), getString(R.string.mood_saved_successfully), android.widget.Toast.LENGTH_SHORT).show()
                
                closeMoodEntryModal()
            }
        }
    }
    
    private fun openMoodEntryModal(day: Int? = null) {
        // Reset form
        selectedMood = null
        etMoodNote.text?.clear()
        tagAdapter.clearSelection()
        btnSaveMoodEntry.isEnabled = false
        
        // Show modal with animation
        modalOverlay.visibility = View.VISIBLE
        moodEntryModal.visibility = View.VISIBLE
        
        // Slide up animation
        val slideUp = ObjectAnimator.ofFloat(moodEntryModal, "translationY", 1000f, 0f)
        slideUp.duration = 300
        slideUp.interpolator = DecelerateInterpolator()
        slideUp.start()
        
        // Fade in overlay
        val fadeIn = ObjectAnimator.ofFloat(modalOverlay, "alpha", 0f, 1f)
        fadeIn.duration = 300
        fadeIn.start()
    }
    
    private fun closeMoodEntryModal() {
        // Slide down animation
        val slideDown = ObjectAnimator.ofFloat(moodEntryModal, "translationY", 0f, 1000f)
        slideDown.duration = 300
        slideDown.interpolator = DecelerateInterpolator()
        slideDown.start()
        
        // Fade out overlay
        val fadeOut = ObjectAnimator.ofFloat(modalOverlay, "alpha", 1f, 0f)
        fadeOut.duration = 300
        fadeOut.start()
        
        // Hide views after animation
        view?.postDelayed({
            modalOverlay.visibility = View.GONE
            moodEntryModal.visibility = View.GONE
        }, 300)
    }
    
    
    
    
    private fun saveMoodData() {
        android.util.Log.d("ModernMoodFragment", "saveMoodData called with ${moodEntries.size} entries")
        
        val prefs = requireContext().getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        // Convert mood entries to JSON and save
        val gson = Gson()
        val moodEntriesJson = gson.toJson(moodEntries)
        
        android.util.Log.d("ModernMoodFragment", "JSON to save: $moodEntriesJson")
        
        editor.putString("MOOD_ENTRIES", moodEntriesJson)
        val result = editor.commit() // Use commit() instead of apply() for immediate feedback
        
        android.util.Log.d("ModernMoodFragment", "Save result: $result")
    }
    
    private fun loadMoodData() {
        android.util.Log.d("ModernMoodFragment", "loadMoodData called")
        
        val prefs = requireContext().getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
        moodEntries.clear()
        
        // Load mood entries from JSON
        val moodEntriesJson = prefs.getString("MOOD_ENTRIES", null)
        android.util.Log.d("ModernMoodFragment", "Loaded JSON: $moodEntriesJson")
        
        if (moodEntriesJson != null) {
            try {
                val gson = Gson()
                val type = object : TypeToken<List<MoodEntry>>() {}.type
                val loadedEntries = gson.fromJson<List<MoodEntry>>(moodEntriesJson, type)
                android.util.Log.d("ModernMoodFragment", "Loaded ${loadedEntries.size} entries")
                moodEntries.addAll(loadedEntries)
            } catch (e: Exception) {
                android.util.Log.e("ModernMoodFragment", "Error loading mood entries: ${e.message}")
            }
        } else {
            android.util.Log.d("ModernMoodFragment", "No mood entries found in SharedPreferences")
        }
        
        // Sort by timestamp (most recent first)
        moodEntries.sortByDescending { it.timestamp }
        
        android.util.Log.d("ModernMoodFragment", "Final moodEntries size: ${moodEntries.size}")
        
        // Update recent moods adapter
        if (::recentMoodAdapter.isInitialized) {
            recentMoodAdapter.updateMoodEntries(moodEntries)
            android.util.Log.d("ModernMoodFragment", "Adapter updated in loadMoodData")
        }
    }
    
    
    private fun setupBottomNavigation(view: View) {
        // Get navigation items - these are included layouts, so we need to find them differently
        val navHabits = view.findViewById<View>(R.id.navHabits)
        val navMood = view.findViewById<View>(R.id.navMood)
        val navStats = view.findViewById<View>(R.id.navStats)
        val navSettings = view.findViewById<View>(R.id.navSettings)
        
        // Setup navigation items with icons and labels
        setupNavItem(navHabits, R.drawable.ic_habits, "Habits", false)
        setupNavItem(navMood, R.drawable.ic_mood, "Mood", true) // Current page
        setupNavItem(navStats, R.drawable.ic_stats, "Log Water", false)
        setupNavItem(navSettings, R.drawable.ic_settings, "Settings", false)
        
        // Setup click listeners for navigation
        navHabits.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(HabitsFragment())
        }
        
        navMood.setOnClickListener {
            // Already on mood page
            updateNavSelection(navHabits, false)
            updateNavSelection(navMood, true)
            updateNavSelection(navStats, false)
            updateNavSelection(navSettings, false)
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
}
