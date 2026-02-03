package com.example.moodflow

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class SettingsFragment : Fragment() {
    private var profileUri: Uri? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && view != null) {
            val persisted = persistImageToInternal(uri)
            if (persisted != null) {
                profileUri = persisted
                view?.findViewById<ImageView>(R.id.imageProfile)?.setImageURI(persisted)
                saveProfileUri(persisted)
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        
        // Header name placeholder
        val prefsName = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE).getString("full_name", "Ibrahim Ali")
        view.findViewById<TextView>(R.id.textUserName)?.text = prefsName

        // Profile image click to pick
        view.findViewById<ImageView>(R.id.imageProfile)?.setOnClickListener {
            pickImage.launch("image/*")
        }
        view.findViewById<TextView>(R.id.btnEditProfile)?.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(EditProfileFragment())
        }

        // Load saved profile image (only internal file URIs)
        loadProfileUri()?.let { uri ->
            if (uri.scheme == "file") {
                profileUri = uri
                view.findViewById<ImageView>(R.id.imageProfile)?.setImageURI(uri)
            }
        }

        // Language row
        val languageValue = view.findViewById<TextView>(R.id.textLanguageValue)
        view.findViewById<View>(R.id.rowLanguages)?.setOnClickListener {
            val items = arrayOf("English", "සිංහල", "தமிழ்")
            val current = items.indexOf(languageValue?.text?.toString())
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select language")
                .setSingleChoiceItems(items, if (current >= 0) current else 0) { dialog, which ->
                    languageValue?.text = items[which]
                    requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
                        .edit().putString("language", items[which]).apply()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        // Load saved language
        languageValue?.text = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .getString("language", "English")

        // Theme toggle wiring
        val themeSwitch = view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchDarkTheme)
        val prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val dark = prefs.getBoolean("dark_theme", true)
        themeSwitch.isChecked = dark
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_theme", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Notifications toggle (stored only)
        val notifSwitch = view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchNotifications)
        val notifOn = prefs.getBoolean("notifications_on", true)
        notifSwitch.isChecked = notifOn
        notifSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_on", isChecked).apply()
        }

        setupBottomNavigation(view)
        return view
    }

    private fun saveProfileUri(uri: Uri) {
        requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .edit().putString("profile_uri", uri.toString()).apply()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

        // Version label
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            view.findViewById<TextView>(R.id.textVersion)?.text = "v${pInfo.versionName}"
        } catch (_: Exception) { }
    }

    private fun loadProfileUri(): Uri? {
        val s = requireContext().getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
            .getString("profile_uri", null)
        return s?.let { Uri.parse(it) }
    }

    private fun persistImageToInternal(source: Uri): Uri? {
        return try {
            val input: InputStream? = requireContext().contentResolver.openInputStream(source)
            val outFile = File(requireContext().filesDir, "profile.jpg")
            FileOutputStream(outFile).use { out ->
                if (input != null) {
                    input.copyTo(out)
                    input.close()
                }
            }
            Uri.fromFile(outFile)
        } catch (_: Exception) { null }
    }
    
    private fun setupBottomNavigation(view: View) {
        // Get navigation items - these are included layouts
        val navHabits = view.findViewById<View>(R.id.navHabits)
        val navMood = view.findViewById<View>(R.id.navMood)
        val navStats = view.findViewById<View>(R.id.navStats)
        val navSettings = view.findViewById<View>(R.id.navSettings)
        
        // Setup navigation items with icons and labels
        setupNavItem(navHabits, R.drawable.ic_habits, "Habits", false)
        setupNavItem(navMood, R.drawable.ic_mood, "Mood", false)
        setupNavItem(navStats, R.drawable.ic_stats, "Log Water", false)
        setupNavItem(navSettings, R.drawable.ic_settings, "Settings", true) // Current page
        
        // Setup click listeners for navigation
        navHabits.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(HabitsFragment())
        }
        
        navMood.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(ModernMoodFragment())
        }
        
        navStats.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(StatsFragment())
        }
        
        navSettings.setOnClickListener {
            // Already on settings page
            updateNavSelection(navHabits, false)
            updateNavSelection(navMood, false)
            updateNavSelection(navStats, false)
            updateNavSelection(navSettings, true)
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