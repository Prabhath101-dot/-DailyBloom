package com.example.moodflow

/**
 * Data classes and object for mood tracking functionality
 * 
 * This file contains:
 * - Mood data class representing individual mood states
 * - MoodEntry data class for user mood logs
 * - MoodTag data class for categorizing moods
 * - MoodData object with predefined moods and tags
 * 
 * @author MoodFlow Development Team
 * @version 1.0
 */

/**
 * Represents a mood state with visual and descriptive information
 * @param id Unique identifier for the mood
 * @param name Human-readable name of the mood
 * @param emoji Emoji representation of the mood
 * @param backgroundColor Hex color code for mood background
 * @param description Detailed description of the mood state
 */
data class Mood(
    val id: Int,
    val name: String,
    val emoji: String,
    val backgroundColor: String,
    val description: String
)

/**
 * Represents a user's mood entry with additional context
 * @param mood The selected mood
 * @param note Optional user note about the mood
 * @param tags List of associated mood tags
 * @param timestamp When the mood was recorded
 */
data class MoodEntry(
    val mood: Mood,
    val note: String = "",
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Represents a tag for categorizing moods
 * @param id Unique identifier for the tag
 * @param name Display name of the tag
 * @param color Hex color code for the tag
 */
data class MoodTag(
    val id: String,
    val name: String,
    val color: String
)

/**
 * Object containing predefined mood data and utility functions
 * Provides a comprehensive set of moods and tags for mood tracking
 */
object MoodData {
    /**
     * List of predefined moods with emoji representations and descriptions
     * Covers a wide range of emotional states for comprehensive mood tracking
     */
    val moods = listOf(
        Mood(0, "Happy / Joyful", "😄", "#EAF8FF", "Bright open smile — signals positive energy and dopamine release."),
        Mood(1, "Calm / Relaxed", "😌", "#EAFDF2", "Eyes closed, soft smile — reflects inner peace and parasympathetic activation."),
        Mood(2, "Excited / Energetic", "🤩", "#FFF0E6", "Stars in eyes — logical symbol of high arousal and positive anticipation."),
        Mood(3, "Focused / Productive", "🧠", "#F0F8FF", "Brain — represents cognitive engagement and mental clarity."),
        Mood(4, "Motivated / Confident", "💪", "#FFF5E6", "Flexed arm — symbol of strength and self-efficacy."),
        Mood(5, "Neutral / Okay", "😐", "#F8F8F8", "Straight face — balanced emotion, neither positive nor negative."),
        Mood(6, "Tired / Low Energy", "😴", "#F0F0F0", "Sleeping face — indicates physiological rest need."),
        Mood(7, "Stressed / Anxious", "😰", "#F0F0FF", "Blue face with sweat — shows sympathetic activation (fight or flight)."),
        Mood(8, "Sad / Down", "😔", "#FFECEC", "Downcast eyes — classic sign of serotonin drop or sadness."),
        Mood(9, "Angry / Irritated", "😠", "#FFE5E5", "Red face — biologically linked to anger and adrenaline."),
        Mood(10, "Lonely / Empty", "😞", "#FFF0E6", "Slight frown and downward gaze — signals social disconnection."),
        Mood(11, "Overwhelmed / Burnt out", "😫", "#FFE5E5", "Exhausted face — logical symbol of cognitive/emotional overload."),
        Mood(12, "Grateful / Content", "🥰", "#F5FFFA", "Hearts around face — linked to oxytocin and gratitude."),
        Mood(13, "Hopeful / Positive Outlook", "🌤️", "#E6F3FF", "Sun peeking through clouds — symbolic of optimism after challenge."),
        Mood(14, "Sick / Unwell", "🤒", "#FFF5E6", "Thermometer — direct representation of physical discomfort."),
        Mood(15, "Confused / Uncertain", "🤔", "#F0F0F0", "Thinking face — represents cognitive uncertainty or contemplation.")
    )
    
    /**
     * List of available mood tags for categorizing mood entries
     * Helps users organize and filter their mood data
     */
    val availableTags = listOf(
        MoodTag("work", "Work", "#FF6B6B"),
        MoodTag("friends", "Friends", "#4ECDC4"),
        MoodTag("health", "Health", "#45B7D1"),
        MoodTag("study", "Study", "#96CEB4"),
        MoodTag("family", "Family", "#FFEAA7"),
        MoodTag("exercise", "Exercise", "#DDA0DD"),
        MoodTag("sleep", "Sleep", "#98D8C8"),
        MoodTag("food", "Food", "#F7DC6F")
    )
    
    /**
     * Retrieves a mood by its unique ID
     * @param id The mood ID to search for
     * @return The mood if found, null otherwise
     */
    fun getMoodById(id: Int): Mood? = moods.find { it.id == id }
    
    /**
     * Retrieves a mood by its emoji representation
     * @param emoji The emoji string to search for
     * @return The mood if found, null otherwise
     */
    fun getMoodByEmoji(emoji: String): Mood? = moods.find { it.emoji == emoji }
    
    /**
     * Retrieves a mood tag by its unique ID
     * @param id The tag ID to search for
     * @return The tag if found, null otherwise
     */
    fun getTagById(id: String): MoodTag? = availableTags.find { it.id == id }
}
