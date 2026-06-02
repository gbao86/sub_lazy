# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.2] - 2026-06-02

### Added
- **Offline ML Kit OCR Scanning**: Completely removed online Gemini API requirements and replaced it with on-device Google ML Kit Text Recognition. Screenshots are now parsed locally, offline, and privately.
- **Bank/MoMo Notification Parsing**: Created a `BillNotificationListener` service to automatically parse banking and MoMo transaction alerts to pre-fill billing alerts.
- **Google Sign-In Account Management**: Implemented simple Google account linkage inside Settings, allowing users to log in or out to view their profile email (no sensitive permissions required).
- **Multi-Currency & Exchange Rates**:
  - Database upgraded to Schema v2 with safe Room migration (`MIGRATION_1_2`) that adds the `currency` column while preserving user data.
  - Added a currency selector dropdown (VND/USD) in the Add/Edit form utilizing Material 3 card controls.
  - Automated currency pre-fill based on amount thresholds (>1000 VND, otherwise USD).
- **Metadata OCR Auto-download**: Configured `AndroidManifest.xml` tags to instruct Google Play Services to pre-download the OCR module on app install.

### Changed
- **Unified Currency Aggregation**: Updated `SubscriptionViewModel` and chart components to convert all currencies into a unified base currency (VND) before summing, resolving total cost calculation errors.
- **Active Locale Conversion**: Enabled dynamic locale-based currency conversion (e.g. converting `260,000₫` to `$10.24` when app is switched to English, and `$9.99` to `253.746₫` when switched to Vietnamese) based on an exchange rate of `1 USD = 25,400 VND`.

### Fixed
- **OCR Scan Orientation & Tolerance**: Rebuilt OCR price and keyword scanning algorithms to make them highly tolerant to horizontal layout variations and diacritical variations.
- **Empty Scan Errors**: Configured fallback naming (`Hóa đơn mới`) to allow successful scanning and pre-filling even if a service logo/text isn't in our predefined service database.

## [0.0.1] - 2026-06-01

### Added
- **Core Architecture**: Configured MVVM architecture with Room Database, Repository Pattern, and Android WorkManager.
- **Onboarding Setup**: Created onboarding screen with subscription templates (Netflix, Spotify, YouTube Premium, etc.) to quickly bootstrap user data.
- **Subscription Tracking**: Enabled adding, editing, and deleting tracked subscriptions. Added currency formatting in VND and days-remaining counters.
- **Dashboard Visualization**: Implemented custom canvas-drawn animated Pie Chart for category cost distribution and a premium gradient total spending card.
- **Interactive Dashboard Charts**: 
  - Added an interactive **Donut Chart** with selection highlights (exploding segment outward offset and thickness) and dynamic center text (category name, amount, percentage).
  - Added an interactive **Category Breakdown Legend** where tapping legend items expands to display the full list of subscriptions under that category with slide animations.
  - Added an animated **Billing Cycle Comparison** vertical bar chart showing Monthly vs. Yearly spending impact.
  - Added an interactive **Upcoming Renewals Timeline** with pulsing circles indicating urgent renewals (<= 3 days) and tap tooltips to view subscription dates.
- **Dynamic Localization**: Added a dynamic language switcher (dropdown menu in top app bar) allowing users to switch between English and Vietnamese.
- **Multi-language Support**: Created localized resource files (`strings.xml`) for English and Vietnamese locales.
- **Local Reminders**: Set up `NotificationWorker` via WorkManager to trigger high-priority alerts 2 days before renewals.

### Changed
- **Branding**: Updated screen titles and configuration to display the app name as **Sub Lazy** (removing underscores).
- **Core Dependency**: Updated `MainActivity` base class to `AppCompatActivity` and added `androidx.appcompat` to project dependencies to support runtime locale changes.
- **Dynamic Localized Currency**: Migrated from hardcoded VND formatting to dynamic currency formatting (VND/₫ for Vietnamese language, USD/$ for English) using `CurrencyFormatter`.
- **System-Adaptive Theme (UI/UX)**: Created a customized Material 3 Light Color Scheme (`PremiumLightColorScheme`) and updated `Sub_lazyTheme` to automatically toggle between light and dark modes based on the user\'s Android device settings.
- **Swipe to Delete (UX Confirmation)**: Intercepted immediate swipe-to-delete and added an `AlertDialog` confirmation popup with dynamic localized text (English/Vietnamese) showing the specific service name before performing database delete. Cancel transitions the swiped item back smoothly.

### Fixed
- **Gradle wrapper startup**: Restructured Gradle wrapper jar and settings to compile correctly on JDK 21 environment.
- **Code Health / Warnings**: Resolved compiler warnings by migrating to `Icons.AutoMirrored` and suppressing legacy status bar color modifications in `Theme.kt`.
- **State Persistence (UX Bug Fix)**: Fixed a bug where data appeared to be lost upon app exit by storing the onboarding completion state in `SharedPreferences`. The app now automatically loads directly into the `Dashboard` screen on subsequent launches instead of forcing the user through onboarding repeatedly.
