package com.example.moodflow

import android.app.AlertDialog
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moodflow.ui.WaveProgressView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import com.example.moodflow.water.WaterReminderReceiver

/**
 * StatsFragment - Water intake tracking and statistics
 * 
 * Features:
 * - Daily water intake goal setting and tracking
 * - Visual progress display with custom wave animation
 * - Water reminder notifications with AlarmManager
 * - Data persistence using SharedPreferences
 * - Water intake history with delete functionality
 * 
 * Advanced Features:
 * - SharedPreferences for data persistence (water_prefs)
 * - AlarmManager for scheduled water reminders
 * - Custom notification channels for Android O+
 * - JSON serialization for complex data storage
 * - File system operations for data management
 * 
 * @author MoodFlow Development Team
 * @version 1.0
 */

class StatsFragment : Fragment() {
    private lateinit var waveProgressView: WaveProgressView
    private lateinit var textDate: TextView
    private lateinit var textIntake: TextView
    private lateinit var textCupsSummary: TextView
    private lateinit var recordsRecycler: RecyclerView
    private lateinit var floatingAddButton: ImageView

    private val gson = Gson()
    private var records = mutableListOf<WaterRecord>()
    private var dailyGoalMl: Int = 2000
    private var consumedMl: Int = 0
    private var reminderIntervalMs: Long? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)
        setupBottomNavigation(view)
        bindViews(view)
        loadState()
        renderDate()
        setupRecycler()
        updateProgressUI()
        setupFab()
        setupEditGoal(view)
        return view
    }
    
    private fun setupBottomNavigation(view: View) {
        // Get navigation items - these are included layouts
        val navHabits = view.findViewById<View>(R.id.navHabits)
        val navMood = view.findViewById<View>(R.id.navMood)
        val navStats = view.findViewById<View>(R.id.navStats)
        val navSettings = view.findViewById<View>(R.id.navSettings)
        val floatingAddButton = view.findViewById<ImageView>(R.id.floatingAddButton)
        
        // Setup navigation items with icons and labels
        setupNavItem(navHabits, R.drawable.ic_habits, getString(R.string.habits), false)
        setupNavItem(navMood, R.drawable.ic_mood, getString(R.string.mood), false)
        setupNavItem(navStats, R.drawable.ic_stats, getString(R.string.log_water), true) // Current page
        setupNavItem(navSettings, R.drawable.ic_settings, getString(R.string.settings), false)
        
        // Setup center FAB
        floatingAddButton.setOnClickListener {
            // Navigate to habits to add new habit
            (requireActivity() as MainActivity).replaceFragment(HabitsFragment())
        }
        
        // Setup click listeners for navigation
        navHabits.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(HabitsFragment())
        }
        
        navMood.setOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(ModernMoodFragment())
        }
        
        navStats.setOnClickListener {
            // Already on stats page
            updateNavSelection(navHabits, false)
            updateNavSelection(navMood, false)
            updateNavSelection(navStats, true)
            updateNavSelection(navSettings, false)
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

    private fun bindViews(view: View) {
        waveProgressView = view.findViewById(R.id.waveProgress)
        textDate = view.findViewById(R.id.textCurrentDate)
        textIntake = view.findViewById(R.id.textIntake)
        textCupsSummary = view.findViewById(R.id.textCupsSummary)
        recordsRecycler = view.findViewById(R.id.rvRecords)
        floatingAddButton = view.findViewById(R.id.floatingAddButton)
    }

    private fun renderDate() {
        val cal = Calendar.getInstance()
        val dayName = DateFormat.format("EEEE", cal)
        val day = DateFormat.format("d", cal)
        val month = DateFormat.format("MMM", cal)
        textDate.text = "$dayName, $day $month"
    }

    private fun setupRecycler() {
        recordsRecycler.layoutManager = LinearLayoutManager(requireContext())
        recordsRecycler.adapter = WaterRecordAdapter(records) { removeIndex ->
            records.removeAt(removeIndex)
            saveState()
            refreshSummary()
            updateProgressFromRecords()
            recordsRecycler.adapter?.notifyItemRemoved(removeIndex)
        }
        refreshSummary()
    }

    private fun setupFab() {
        floatingAddButton.setOnClickListener {
            promptAddRecord()
        }
    }

    private fun setupEditGoal(root: View) {
        root.findViewById<ImageView>(R.id.btnEditGoal).setOnClickListener {
            val container = LinearLayout(requireContext())
            container.orientation = LinearLayout.VERTICAL
            container.setPadding(40, 20, 40, 0)

            val input = EditText(requireContext())
            input.hint = getString(R.string.daily_goal_ml)
            input.setText(dailyGoalMl.toString())
            container.addView(input)

            val rg = RadioGroup(requireContext())
            val opt5s = RadioButton(requireContext()); opt5s.text = getString(R.string.every_1_minute); rg.addView(opt5s)
            val opt30 = RadioButton(requireContext()); opt30.text = getString(R.string.every_30_min); rg.addView(opt30)
            val opt60 = RadioButton(requireContext()); opt60.text = getString(R.string.every_1_hour); rg.addView(opt60)
            val opt120 = RadioButton(requireContext()); opt120.text = getString(R.string.every_2_hours); rg.addView(opt120)
            container.addView(rg)

            // Preselect saved reminder interval
            when (reminderIntervalMs) {
                60_000L -> rg.check(opt5s.id)
                30L * 60_000L -> rg.check(opt30.id)
                60L * 60_000L -> rg.check(opt60.id)
                120L * 60_000L -> rg.check(opt120.id)
            }

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.set_daily_goal_reminders))
                .setView(container)
                .setPositiveButton(getString(R.string.save)) { _, _ ->
                    val newGoal = input.text.toString().toIntOrNull()
                    if (newGoal != null && newGoal > 0) {
                        val goalChanged = newGoal != dailyGoalMl
                        dailyGoalMl = newGoal
                        if (goalChanged) {
                            // Reset ONLY when the goal has changed
                            consumedMl = 0
                            records.clear()
                            saveState()
                            (recordsRecycler.adapter as? WaterRecordAdapter)?.notifyDataSetChanged()
                            refreshSummary()
                            updateProgressUI()
                        } else {
                            saveState()
                        }
                    }

                    val intervalMillisChosen: Long? = when (rg.checkedRadioButtonId) {
                        opt5s.id -> 60_000L
                        opt30.id -> 30L * 60_000L
                        opt60.id -> 60L * 60_000L
                        opt120.id -> 120L * 60_000L
                        else -> null
                    }
                    val finalInterval = intervalMillisChosen ?: reminderIntervalMs
                    scheduleRemindersForToday(finalInterval)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    private fun scheduleRemindersForToday(intervalMillis: Long?) {
        val alarm = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // If nothing selected and no previous interval, keep current schedule
        if (intervalMillis == null) return

        // Cancel existing
        val intent = Intent(requireContext(), WaterReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(requireContext(), 1001, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarm.cancel(pending)

        // Runtime permission for Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (requireContext().checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2003)
            }
        }

        // Save chosen interval
        reminderIntervalMs = intervalMillis
        prefs().edit().putLong("reminder_ms", intervalMillis).apply()

        // Fire first reminder exactly after the selected interval when possible
        val now = System.currentTimeMillis()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, now + intervalMillis, pending)
            } else {
                alarm.setExact(AlarmManager.RTC_WAKEUP, now + intervalMillis, pending)
            }
        } catch (_: SecurityException) {
            // Exact alarms may be restricted on Android 12+. Fall back to windowed alarm.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarm.setWindow(AlarmManager.RTC_WAKEUP, now + intervalMillis, intervalMillis, pending)
            } else {
                alarm.set(AlarmManager.RTC_WAKEUP, now + intervalMillis, pending)
            }
        }

        // Also schedule repeating reminders (system may batch, but will continue thereafter)
        try {
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, now + intervalMillis, intervalMillis, pending)
        } catch (_: SecurityException) {
            // If repeating fails due to restrictions, at least the first exact/windowed alarm will fire.
        }

        // Schedule a cancel at midnight
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val cancelIntent = Intent(requireContext(), WaterReminderReceiver::class.java)
        val cancelPending = PendingIntent.getBroadcast(requireContext(), 1001, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        // Use an alarm to cancel repeating at midnight via AlarmManager.OnAlarmListener is not available; instead we'll rely on next app open or user reschedule.
        // To keep simple, we clear reminders on next day loadState by detecting date change could be added.
    }

    private fun refreshSummary() {
        textCupsSummary.text = "${records.size} cups"
    }

    private fun promptAddRecord() {
        val input = EditText(requireContext())
        input.hint = getString(R.string.amount_ml)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_water_intake))
            .setView(input)
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                val amount = input.text.toString().toIntOrNull()
                if (amount != null && amount > 0) {
                    val record = WaterRecord(amount, System.currentTimeMillis())
                    records.add(0, record)
                    consumedMl += amount
                    saveState()
                    (recordsRecycler.adapter as? WaterRecordAdapter)?.notifyItemInserted(0)
                    refreshSummary()
                    updateProgressUI()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun updateProgressFromRecords() {
        consumedMl = records.sumOf { it.amountMl }
        updateProgressUI()
        saveState()
    }

    private fun updateProgressUI() {
        val percent = if (dailyGoalMl == 0) 0f else (consumedMl.toFloat() / dailyGoalMl.toFloat())
        waveProgressView.setProgressFraction(percent)
        textIntake.text = "$consumedMl/$dailyGoalMl ml"
    }

    /**
     * Gets SharedPreferences instance for water tracking data
     * Uses "water_prefs" to store:
     * - Daily goal (goal)
     * - Current consumption (consumed)
     * - Reminder interval (reminder_ms)
     * - Water intake records (records) - JSON serialized
     * 
     * @return SharedPreferences instance for water data
     */
    private fun prefs() = requireContext().getSharedPreferences("water_prefs", Context.MODE_PRIVATE)

    /**
     * Saves current water tracking state to SharedPreferences
     * Persists goal, consumption, records, and reminder settings
     */
    private fun saveState() {
        val p = prefs().edit()
        p.putInt("goal", dailyGoalMl)
        p.putInt("consumed", consumedMl)
        p.putString("records", gson.toJson(records))
        p.apply()
    }

    /**
     * Loads water tracking state from SharedPreferences
     * Restores goal, consumption, records, and reminder settings
     */
    private fun loadState() {
        val p = prefs()
        dailyGoalMl = p.getInt("goal", 2000)
        consumedMl = p.getInt("consumed", 0)
        if (p.contains("reminder_ms")) {
            reminderIntervalMs = p.getLong("reminder_ms", 0L).let { if (it == 0L) null else it }
        }
        val json = p.getString("records", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<MutableList<WaterRecord>>() {}.type
            records = gson.fromJson(json, type) ?: mutableListOf()
        }
    }
}

data class WaterRecord(val amountMl: Int, val timeMillis: Long)

class WaterRecordAdapter(
    private val items: List<WaterRecord>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<WaterRecordViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaterRecordViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_water_record, parent, false)
        return WaterRecordViewHolder(v, onDelete)
    }
    override fun onBindViewHolder(holder: WaterRecordViewHolder, position: Int) {
        holder.bind(items[position])
    }
    override fun getItemCount(): Int = items.size
}

class WaterRecordViewHolder(itemView: View, private val onDelete: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {
    private val amount: TextView = itemView.findViewById(R.id.textAmount)
    private val time: TextView = itemView.findViewById(R.id.textTime)
    private val deleteBtn: ImageView = itemView.findViewById(R.id.btnDelete)

    fun bind(record: WaterRecord) {
        amount.text = "${record.amountMl} ml"
        val formatted = DateFormat.format("hh:mm a", record.timeMillis)
        time.text = formatted
        deleteBtn.setOnClickListener { onDelete(adapterPosition) }
    }
}