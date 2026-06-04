# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.3] - 2026-06-04

### Added
- **Lifestyle Subscription Templates**: Expanded presets to include offline everyday activities (e.g., Motorbike Oil Change, Pet Deworming, Water Filter Replacement) with default frequencies.
- **VietQR Code Generator**: Integrated standard VietQR payment generator (`VietQRGenerator`) utilizing Napas format. Added a VietQR quick-scan action button and popup for subscriptions with bank transfer information.
- **Full Form Pre-filling**: Extended the navigation graph and `AddEditSubscriptionScreen` to pass and pre-fill all template details (name, cost, cycle, category, custom color, bank details) seamlessly from the template picker.
- **Flexible Billing Cycles**: Introduced support for `Daily`, `Every 3 Months`, and `Every 6 Months` frequencies, fully integrated into SQL aggregations (`getTotalMonthlyCost`, `getSpendingByCategory`) and calendar renewal rollouts.

### Changed
- **Privacy & Security Focus**: Removed high-risk Google APIs/Gmail Read permissions to avoid costly CASA audits and secure absolute offline user privacy.
- **Time-based Maintenance Logic**: Replaced complex odometer/kilometer-based logging with time-based calendar reminders (e.g., every 6 months) for vehicle maintenance.

### Fixed
- **Onboarding Renewal Dates**: Fixed initial billing date projections during onboarding templates to respect target cycles (using `Calendar` offsets) rather than hardcoding a generic 30-day offset.
- **Notification Rollover Logic**: Aligned background notification service rollover calculations with the newly added billing cycles.

## [0.0.2] - 2026-06-02

### Added
- **Cashflow & Subscription Runway Forecasting Chart**:
  - Implemented a custom canvas-based curve path Line Chart with gradient fills displaying projected expenditure runway for the next 6 months.
  - Added a peak-spending indicator displaying the month containing the highest forecasted spending total.
- **Manual Payment Tracking (Mark as Paid) & History**:
  - Created a database table `payment_history` (with schema migration `MIGRATION_3_4`) to track completed subscription payments.
  - Added a "Mark as Paid" button inside the Dashboard's timeline selection card that logs payment info and advances the renewal date (or deletes the subscription if one-time/expired).
  - Designed a "Payment History" feed displaying the last 5 payment transactions with check badges on the Dashboard.
- **Weekly Billing Cycle Support**:
  - Expanded subscription frequency options to include "Weekly" (alongside Monthly, Yearly, and One-time).
  - Integrated weekly cycle calculation in total monthly spending and category breakdown (using `amount * 52.0 / 12.0`).
  - Added a "Weekly" cost comparison column inside the Dashboard's Billing Cycle Chart.
- **Auto-Delete Limit (1-time / N-times count)**:
  - Added the ability to specify a maximum payment count (1 time, custom N times, or unlimited) to auto-delete subscriptions once they expire.
  - Implemented startup checks and an auto-rollover mechanism inside `SubscriptionViewModel` which decrements remaining counts, moves billing dates forward, or deletes expired subscriptions automatically on launch.
  - Designed a modern, elegant FilterChip selection UI for Auto-Deletion Limits in `AddEditSubscriptionScreen`.
  - Added a remaining times counter/badge (e.g., "Còn 3 lần") below the subscription cost in `SubscriptionListScreen` and inside the Dashboard's upcoming timeline card.
- **Offline ML Kit OCR Enhancements**:
  - Enabled automatic detection of weekly cycles from screenshot text recognition (supporting keywords like "weekly", "tuan", "7 days", etc.).
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
- **Donut Chart Segment Overlap**: Changed segment `StrokeCap` from `Round` to `Butt` and introduced a 2-degree gap between slices. This prevents rounded caps from overlapping and completely covering smaller spending category segments, resulting in a cleaner and more premium chart visualization.
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
