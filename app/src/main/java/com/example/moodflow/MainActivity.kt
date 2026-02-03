package com.example.moodflow

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * MainActivity - The main activity that hosts all fragments in the MoodFlow app
 * 
 * Features:
 * - Theme management (dark/light mode)
 * - Fragment navigation
 * - Handles water reminder deep links
 * 
 * @author MoodFlow Development Team
 * @version 1.0
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved theme preference
        applyThemePreference()
        
        setContentView(R.layout.activity_main)

        // Initialize with appropriate fragment based on intent extras
        if (savedInstanceState == null) {
            initializeFragment()
        }
    }
    
    /**
     * Applies the saved theme preference from SharedPreferences
     */
    private fun applyThemePreference() {
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val isDarkTheme = prefs.getBoolean("dark_theme", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    /**
     * Initializes the appropriate fragment based on intent extras
     * Handles deep linking from water reminder notifications
     */
    private fun initializeFragment() {
        val openLogWater = intent?.getBooleanExtra("open_log_water", false) == true
        if (openLogWater) {
            replaceFragment(StatsFragment())
        } else {
            replaceFragment(HabitsFragment())
        }
    }

    /**
     * Replaces the current fragment with a new one
     * @param fragment The fragment to display
     */
    fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}