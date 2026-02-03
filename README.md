# MoodFlow - Mobile Application Development Lab Exam Project

## Project Overview
MoodFlow is a comprehensive mood and habit tracking Android application built with Kotlin. The app helps users track their daily moods, manage habits, monitor water intake, and maintain overall well-being through data visualization and reminder systems.

## Features

### Core Features
- **Mood Tracking**: Log daily moods with emoji selection, notes, and tags
- **Habit Management**: Create, track, and complete daily habits with progress visualization
- **Water Intake Tracking**: Monitor daily water consumption with visual progress indicators
- **Settings Management**: Customize app preferences including theme, notifications, and profile

### Advanced Features
- **Data Persistence**: Extensive use of SharedPreferences for data storage
- **Notification System**: Water reminder notifications with AlarmManager
- **Theme Support**: Dark/Light mode switching with persistent preferences
- **Profile Management**: User profile with image upload and editing
- **Multi-language Support**: English, Sinhala, and Tamil language options
- **Custom Animations**: Wave progress animations and smooth transitions

## Technical Implementation

### Architecture
- **Language**: Kotlin
- **UI Framework**: Android Views with Material Design components
- **Data Storage**: SharedPreferences with JSON serialization using Gson
- **Notifications**: AlarmManager with custom notification channels
- **File Management**: Internal storage for profile images

### SharedPreferences Usage
The application extensively uses SharedPreferences for data persistence:

1. **`habits_prefs`**: Stores habit data including progress, targets, and completion status
2. **`mood_prefs`**: Persists mood entries with timestamps, notes, and tags
3. **`water_prefs`**: Manages water intake goals, consumption records, and reminder settings
4. **`settings_prefs`**: Stores user preferences including theme, language, notifications, and profile data

### Advanced Features Implementation

#### 1. AlarmManager Integration
- Scheduled water reminder notifications
- Custom notification channels for Android O+
- Vibration patterns and custom sounds
- Deep linking to water tracking page

#### 2. File System Operations
- Profile image persistence to internal storage
- Image compression and format handling
- URI management for image display

#### 3. JSON Serialization
- Complex data structures serialized using Gson
- Type-safe deserialization with TypeToken
- Efficient data storage and retrieval

#### 4. Custom UI Components
- WaveProgressView for water intake visualization
- LiquidProgressView for habit progress
- Custom adapters for RecyclerView components
- Material Design card layouts and animations

## Project Structure

```
app/src/main/java/com/example/moodflow/
├── MainActivity.kt                 # Main activity with fragment management
├── MoodData.kt                     # Data classes and predefined mood data
├── HabitsFragment.kt               # Habit tracking functionality
├── ModernMoodFragment.kt           # Mood logging with advanced features
├── StatsFragment.kt                 # Water intake tracking and statistics
├── SettingsFragment.kt              # App settings and preferences
├── AddHabitActivity.kt              # Habit creation and editing
├── EditProfileFragment.kt           # Profile management
├── water/
│   └── WaterReminderReceiver.kt    # Notification broadcast receiver
└── ui/
    ├── LiquidProgressView.kt       # Custom progress animation
    └── WaveProgressView.kt          # Custom wave animation
```

## Lab Exam Requirements Compliance

### 1. Functionality (3 marks) ✅
- **Core Features**: Comprehensive mood tracking, habit management, water intake monitoring
- **Bonus Features**: Notifications, profile management, theme switching, multi-language support

### 2. Creativity & User Interface Design (2 marks) ✅
- Modern Material Design implementation
- Custom animations and progress indicators
- Intuitive navigation with bottom navigation bar
- Responsive layouts with proper spacing and colors

### 3. Code Quality & Organization (2 marks) ✅
- Well-documented code with comprehensive comments
- Proper separation of concerns
- Clean architecture with reusable components
- Error handling and validation

### 4. Advanced Features & Data Persistence (3 marks) ✅
- **SharedPreferences Usage**: Extensive use across multiple features
- **Advanced Features**: AlarmManager, file operations, JSON serialization
- **Data Persistence**: Complex data structures stored and retrieved efficiently

## Installation & Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on Android device or emulator

## Dependencies

- AndroidX Core KTX
- Material Design Components
- Gson for JSON serialization
- MPAndroidChart for statistics
- FlexboxLayout for tag management

## Author
MoodFlow Development Team - SLIIT Mobile Application Development Course

## Version
1.0 - Lab Exam Submission

---

**Note**: This project demonstrates comprehensive Android development skills including data persistence, notification systems, custom UI components, and modern Android development practices.
