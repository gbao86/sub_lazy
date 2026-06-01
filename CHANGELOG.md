# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.1] - 2026-06-01

### Added
- **Core Architecture**: Configured MVVM architecture with Room Database, Repository Pattern, and Android WorkManager.
- **Onboarding Setup**: Created onboarding screen with subscription templates (Netflix, Spotify, YouTube Premium, etc.) to quickly bootstrap user data.
- **Subscription Tracking**: Enabled adding, editing, and deleting tracked subscriptions. Added currency formatting in VND and days-remaining counters.
- **Dashboard Visualization**: Implemented custom canvas-drawn animated Pie Chart for category cost distribution and a premium gradient total spending card.
- **Dynamic Localization**: Added a dynamic language switcher (dropdown menu in top app bar) allowing users to switch between English and Vietnamese.
- **Multi-language Support**: Created localized resource files (`strings.xml`) for English and Vietnamese locales.
- **Local Reminders**: Set up `NotificationWorker` via WorkManager to trigger high-priority alerts 2 days before renewals.

### Changed
- **Branding**: Updated screen titles and configuration to display the app name as **Sub Lazy** (removing underscores).
- **Core Dependency**: Updated `MainActivity` base class to `AppCompatActivity` and added `androidx.appcompat` to project dependencies to support runtime locale changes.
- **Dynamic Localized Currency**: Migrated from hardcoded VND formatting to dynamic currency formatting (VND/₫ for Vietnamese language, USD/$ for English) using `CurrencyFormatter`.
- **System-Adaptive Theme (UI/UX)**: Created a customized Material 3 Light Color Scheme (`PremiumLightColorScheme`) and updated `Sub_lazyTheme` to automatically toggle between light and dark modes based on the user\'s Android device settings.

### Fixed
- **Gradle wrapper startup**: Restructured Gradle wrapper jar and settings to compile correctly on JDK 21 environment.
- **Code Health / Warnings**: Resolved compiler warnings by migrating to `Icons.AutoMirrored` and suppressing legacy status bar color modifications in `Theme.kt`.
- **State Persistence (UX Bug Fix)**: Fixed a bug where data appeared to be lost upon app exit by storing the onboarding completion state in `SharedPreferences`. The app now automatically loads directly into the `Dashboard` screen on subsequent launches instead of forcing the user through onboarding repeatedly.
