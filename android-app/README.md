# ProductivityPush Android App

An Android app that blocks distracting apps and provides daily motivation to boost smartphone productivity.

## Features

- **App Blocking**: Monitor and block time-wasting apps (YouTube, Facebook, Instagram, etc.)
- **Daily Task Tracking**: Add and track daily productive tasks
- **Motivational Messages**: Receive encouraging messages throughout the day
- **Usage Monitoring**: Track app usage patterns and blocked attempts
- **Persistent Service**: Background service monitors app usage continuously

## Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM with LiveData
- **UI**: Material Design with View Binding
- **Background Processing**: Foreground Service for app monitoring
- **Permissions**: Usage Stats, System Alert Window, Foreground Service

## Setup

1. Open the project in Android Studio
2. Sync Gradle dependencies
3. Build and run on Android device/emulator
4. Grant required permissions:
   - Usage Access Settings
   - Display over other apps
   - Notification permissions

## Key Components

### Activities
- `MainActivity.kt` - Main app interface with blocking controls and task management
- `SettingsActivity.kt` - Configuration for blocked apps and preferences
- `BlockedActivity.kt` - Fullscreen overlay shown when blocked apps are opened

### Services
- `AppMonitoringService.kt` - Background service that monitors active apps

### ViewModels
- `MainViewModel.kt` - Manages app state and business logic

## Permissions Required

- `PACKAGE_USAGE_STATS` - Monitor which apps are currently active
- `SYSTEM_ALERT_WINDOW` - Display blocking overlay over other apps
- `INTERNET` - Sync data with backend (future feature)
- `FOREGROUND_SERVICE` - Run monitoring service in background

## Development Notes

- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Uses AndroidX libraries and Material Design components
- Follows Android architecture best practices with MVVM pattern