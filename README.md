# Brahma Muhurta Android

A native Android app for exploring Brahma Muhurta based on local sunrise, with a date calendar, location settings, and a daily wake-up alarm.

## Build

Open this project in Android Studio, allow Gradle sync, and run the `app` configuration on an Android device. The GitHub Actions workflow builds a debug APK and uploads it as an artifact.

## Calculation

The default convention is a 48-minute window beginning 96 minutes before sunrise and ending 48 minutes before sunrise. Times are calculated offline from the selected latitude and longitude.

## Alarm

The app schedules an alarm with Android alarm APIs. Depending on Android version, exact alarm and full-screen notification access may need to be granted in system settings.

## Project

- Kotlin and Jetpack Compose
- Offline solar calculator
- Month/date navigation
- Location configuration
- Daily alarm, snooze, dismiss, and test alarm
