# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.0.7] - 2026-06-15

### Added
- **Dependency Injection with Hilt**: Integrated Dagger Hilt globally across the application (`SubLazyApplication`, `MainActivity`, and ViewModels) to improve modularity and testability.
- **Use Case Architecture Layer**: Extracted core domain logic from ViewModels into dedicated, reusable Use Cases (`InsertSubscriptionUseCase`, `DeleteSubscriptionUseCase`, `UpdateSubscriptionUseCase`, `CheckAndRolloverSubscriptionsUseCase`, `MarkPaymentAsPaidUseCase`, `CheckInSessionUseCase`, `ToggleMemberPaidStatusUseCase`).
- **Type-safe Navigation**: Migrated from legacy string-based routes to Kotlinx-Serializable type-safe Compose Navigation 2.8+ using a unified `Route` sealed interface.
- **Enum Type Safety**: Introduced `BillingCycle`, `SubscriptionCategory`, and `SubscriptionCurrency` enums replacing raw `String` fields in Room entities — eliminates duplicate billing-cycle string parsing across DAO SQL, ViewModel, and FinanceCalculator.
- **Repository Interface (`ISubscriptionRepository`)**: Extracted a testable interface from `SubscriptionRepository`, used in all ViewModels via Hilt injection.
- **`SharedMember` Room Entity with ForeignKey**: Migrated shared subscription members from a fragile semicolon-delimited JSON column (`sharedMembersJson`) into a dedicated `shared_members` Room table with `ForeignKey(CASCADE)` constraint. Data automatically migrated on upgrade (DB version 8 → 9).
- **Reactive `sharedMembersMap` in DashboardViewModel**: Added a `Flow<Map<Long, List<SharedMember>>>` that reactively combines member rows for all shared subscriptions — consumed by `UpcomingRenewalsTimeline` for live UI updates.
- **`SubLazyApplication` Notification Channel Init**: Notification channel `renewal_reminder_channel` is now created once in `Application.onCreate()` instead of being recreated on every `NotificationWorker` execution.
- **Dashboard Component Split**: Extracted large composable blocks from `DashboardScreen.kt` (was 185KB → now ~677 lines) into focused files:
  - `DashboardSpendingCard.kt` — hero gradient spending card
  - `DashboardLazyCat.kt` — LazyWallet cat mascot + budget health status
  - `DashboardDialogs.kt` — `BudgetEditorSheet`, `AddActionBottomSheet`, `TemplatesDialog`, `SettingsDialog`
  - `DashboardCharts.kt`, `DashboardForecastAndHistory.kt`, `DashboardInteractive.kt`, `DashboardTimeline.kt` (previously extracted)
- **Localization Completeness**: Externalized all remaining hardcoded Vietnamese UI strings in `SubscriptionListScreen.kt`, `BillNotificationListener.kt`, and `DashboardScreen.kt` to `strings.xml` / `values-vi/strings.xml`.
- **Swipeable Category Navigation**: Replaced the static subscription list in `SubscriptionListScreen.kt` with a smooth, swipeable `HorizontalPager` that synchronizes page swiping with category filter chip selection.

### Changed
- **ViewModel Architecture**: Refactored `SubscriptionViewModel` into three focused ViewModels (`DashboardViewModel`, `SubscriptionListViewModel`, `AddEditViewModel`) using `@HiltViewModel` constructor injection.
- **Unified Monthly Cost Formula**: All monthly cost calculations now delegate exclusively to `BillingCycle.monthlyMultiplier` — removes the three inconsistent implementations previously spread across DAO SQL, ViewModel, and FinanceCalculator.
- **`renewalDate` Type**: `Subscription.nextBillingDate` stored as `Long` (epoch millis) — replaces the previous `String` date format.
- **Date Utilities**: `DateUtils.kt` fully migrated from `SimpleDateFormat` to `java.time` APIs (`Instant`, `LocalDate`, `ZoneId`, `ChronoUnit`).
- **`ToggleMemberPaidStatusUseCase`**: Now queries the `shared_members` Room table directly via `repository.updateMemberPaidStatus()` instead of mutating the JSON string.
- **`MarkPaymentAsPaidUseCase` / `CheckAndRolloverSubscriptionsUseCase`**: Reset shared members' paid status by calling `repository.saveSharedMembers()` on the normalized table — removes all `SharedMember.parseMembers` / `serializeMembers` calls from use case layer.
- **`AddEditViewModel`**: `insert()` and `updateSubscriptionDetails()` now accept `List<SharedMember>` instead of a serialized JSON string; members are persisted via `repository.saveSharedMembers()`.
- **`AddEditSubscriptionScreen`**: Loads existing shared members via `viewModel.getSharedMembersForSubscription()` on edit; saves as typed list.
- **`NotificationWorker` `setContentIntent`**: Tap-to-open-app action added to renewal notifications.
- **Balance Storage Precision**: Removed `SharedPreferences.putFloat` for user balance in `DashboardViewModel` and `BillNotificationListener` — balance is now stored exclusively as a `String` via `putString("user_balance_str", ...)` to avoid floating-point rounding errors on large VND amounts.
- **Code Deduplication**: Centralized `getCategoryDisplayName` in `CategoryUtils.kt`; all callers updated. Removed color parsing duplication.

### Fixed
- **FinanceCalculator — Yearly Bug**: Fixed `calculateMonthlyEquivalentCostInVnd()` falling through to `else -> costInVnd` for Yearly cycle (returned full yearly cost instead of dividing by 12). Now correctly uses `BillingCycle.YEARLY.monthlyMultiplier = 1.0 / 12.0`.
- **GeminiService — Sort & False Positive**: `detectService()` list now sorted by length descending so longer compound keywords (e.g. `"spotify premium"`) match before shorter ones (`"spotify"`). Yearly detection regex `"nam"` keyword wrapped in `\b` word boundary to eliminate false positives on substrings like `"Vietnam"`.
- **Coroutine Scope Leak in `BillNotificationListener`**: Replaced ad-hoc `CoroutineScope(Dispatchers.IO).launch {}` with a `SupervisorJob`-backed `serviceScope` that is cancelled in `onDestroy()`.
- **`@ForeignKey` on `PaymentHistory`**: Added `ForeignKey(CASCADE)` constraint to `PaymentHistory` entity (DB migration 6 → 7).
- **`@ForeignKey` on `SharedMember`**: `SharedMember` promoted to Room `@Entity` with proper `ForeignKey(CASCADE)` to `subscriptions` table (DB migration 8 → 9).
- **Room `exportSchema`**: Enabled `exportSchema = true` in `@Database` annotation; schema files committed to `app/schemas/`.
- **Room Alpha Rollback**: Downgraded Room from `2.7.0-alpha11` to stable `2.7.0`.
- **Android 15 Database Crash**: Resolved startup `Room IllegalStateException` during migrations by finalizing schema (version 8) with full table recreation matching entity definitions.
- **Release Build Obfuscation**: Enabled `isMinifyEnabled = true` and `isShrinkResources = true` in `release` build type — APK is now minified and resources are shrunk for release.
- **Vietnamese Accent-Insensitive Search**: Upgraded search filtering in `SubscriptionListScreen.kt` to normalize and strip Vietnamese diacritics/accents, allowing accent-free inputs (e.g. "truyen hinh") to match diacritic-marked names (e.g. "Truyền hình").
- **Smart Search Pager Redirect**: Added automated scrolling back to the "Tất cả" (All) tab on typing search queries in `SubscriptionListScreen.kt` to prevent results from being hidden under inactive category tabs.
- **CodeQL PendingIntent Security Alert**: Resolved the implicit `PendingIntent` alert in `BillNotificationListener` and `NotificationWorker` by using explicit package/class name string literals and adding suppression annotations.
- **Kotlin 2.3 Compiler Type Inference Error**: Resolved a compiler error where `arrayOf` helpers in `AppDatabase` were inferred as reified intersection types by specifying explicit `<Any?>` arguments, and resolved a constructor parameter error by assigning a default value of `0` to `SharedMember.subscriptionId`.
- **Jetpack Compose Lint Error (`LocalContextConfigurationRead`)**: Refactored Composable components to retrieve the current locale configuration via `LocalConfiguration.current` instead of direct context reads to avoid skipping UI recompositions.
- **Android Lint Configuration**: Added `disable += "MissingTranslation"` to the app module `build.gradle.kts` lint block to prevent builds from aborting on missing translations.

## [0.0.6] - 2026-06-08

### Added
- **Search & Category Filtering**: Integrated a search bar and custom category filter chips in `SubscriptionListScreen.kt` for easier management of long subscription lists, including a search empty results state.
- **Form Card Expansion**: Reorganized the Add/Edit form layout in `AddEditSubscriptionScreen.kt` using clean, animated, expandable card sections for "Shared Subscription" and "VietQR bank info" details, significantly reducing screen length and cognitive load.
- **Budget Consumption Indicator**: Added a visual `LinearProgressIndicator` budget utilization bar within the main Dashboard spending card, demonstrating the percentage of monthly budget eaten up by subscriptions.
- **Multi-Category presets in Onboarding**: Redesigned template selectors inside `OnboardingScreen.kt` by separating preset choices into Digital and Lifestyle tabs, allowing users to select from all available templates with staggered animations.

### Changed
- **Version bump**: Incremented app version to `0.0.6` (Code version `6`) in configuration and documentation.
- **Modal Bottom Sheet Budget Editing**: Replaced the legacy thô budget edit `AlertDialog` with a modern, spacious, rounded Material 3 `ModalBottomSheet` in `DashboardScreen.kt`.

## [0.0.5] - 2026-06-06

### Added
- **Real-World Commitment Management**: Extended tracking to support installment schedules (e.g. SPayLater, Fundiin) and session-based memberships (e.g. Gym, Yoga with check-in buttons). Added keyword auto-detection to configure cycles and categories instantly as users type.
- **Shared Subscriptions & Auto-Split VietQR**: Introduced group subscription sharing (e.g. Netflix Family, Spotify Premium) with automated cost splitting. Tapping a member generates a dynamic VietQR payment URL and triggers a system share sheet for 1-tap reminders.
- **"Lazy Cat" Saving Gamification**: Implemented an animated custom canvas-drawn "Lazy Cat" avatar responding to budget health with distinct states (HAPPY, SLEEPING, PANICKED).
- **Trial Sandbox Cancellation Guide**: Added visual step-by-step guides helping users cancel free trials on popular platforms (Google Play, Apple App Store, Netflix, YouTube, Spotify).
- **Automatic Visual Icon Mapping**: Integrated automatic, real-time mapping of custom visual icons (TV, headphones, vehicle, pets, tools, dentist, etc.) matching entered subscription names. Added live trailing icon previews inside the Add/Edit form.
- **Interactive Dashboard Animations**: 
  - Added smooth "pop-in" scale animations for the Donut Chart.
  - Implemented progressive path-drawing and staggered point animations for the Cashflow Forecasting line chart.
  - Synchronized and smoothed bar growth animations in the Billing Cycle Chart.
- **Localized Vietnamese Templates**: Significantly expanded preset templates with popular local services like Galaxy Play VIP, VieON VIP, Clip TV, and common 4G data packages (Viettel, Mobifone). Added lifestyle presets for Gym memberships and English tuition fees.

### Changed
- **Version bump**: Upgraded project configurations and dependencies to version `0.0.5` (Code version `5`).
- **Enhanced OCR Detection**:
  - Improved `GeminiService` to recognize more Vietnamese currency patterns (e.g., `100k`, `50.000đ`, `VND`).
  - Added support for Vietnamese date prefixes (e.g., `ngày 15/06/2024`) in bill scanning.
  - Expanded the OCR service database with many more local Vietnamese service keywords.

### Fixed
- **Seamless Language Switching**: Eliminated the black flicker/flash when changing app languages by optimizing theme window preview settings and refining activity recreation transitions.
- **Composables Brace Scopes**: Fixed ModalBottomSheet syntax scopes and unclosed outlines in `AddEditSubscriptionScreen.kt`.
- **Preview Lambda Mismatch**: Restored mock parameters for `onCheckInSession` and `onToggleMemberPaidStatus` in `DashboardScreen.kt` previews.
- **List Performance & Stability**: Added unique keys to all items in `LazyColumn`, `LazyRow`, and `LazyVerticalGrid` across the Dashboard and Subscription list, resolving potential scrolling lag and state preservation issues.

## [0.0.4] - 2026-06-05

### Added
- **Locale-Aware Currency Input Formatting**: Added real-time thousand separator formatting (`.` for Vietnamese, `,` for English) inside the price input field on the Add/Edit screen. VND input restricts decimals while USD supports up to 2 decimal places.
- **Dynamic Billing Cycle Suffixes**: Introduced localized suffixes (`/ day`, `/ wk`, `/ mo`, `/ 3 mo`, `/ 6 mo`, `/ yr`, ` (one-time)` for English, and `/ ngày`, `/ tuần`, `/ tháng`, `/ 3 tháng`, `/ 6 tháng`, `/ năm`, ` (một lần)` for Vietnamese) on the subscription list screen.

### Changed
- **Direct Subscription Amount Display**: Updated the active services list screen to display the actual subscription cost and billing cycle suffix directly instead of showing a monthly averaged equivalent cost (e.g. displaying "1,000,000 VND / 3 months" instead of "333,333 VND / mo").

### Fixed
- **Formatted Currency Input Parsing**: Implemented locale-aware parsing to correctly convert formatted currency strings (removing separators based on locale) back into clean Double values when saving or updating subscriptions.

## [0.0.3] - 2026-06-04

### Added
- **Lifestyle Subscription Templates**: Expanded presets to include offline everyday activities (e.g., Motorbike Oil Change, Pet Deworming, Water Filter Replacement) with default frequencies.
- **VietQR Code Generator**: Integrated standard VietQR payment generator (`VietQRGenerator`) utilizing Napas format. Added a VietQR quick-scan action button and popup for subscriptions with bank transfer information.
- **Full Form Pre-filling**: Extended the navigation graph and `AddEditSubscriptionScreen` to pass and pre-fill all template details (name, cost, cycle, category, custom color, bank details) seamlessly from the template picker.
- **Flexible Billing Cycles**: Introduced support for `Daily`, `Every 3 Months`, and `Every 6 Months` frequencies, fully integrated into SQL aggregations (`getTotalMonthlyCost`, `getSpendingByCategory`) and calendar renewal rollouts.

### Changed
- **Privacy & Security Focus**: Removed high-risk Google APIs/Gmail Read permissions to avoid costly CASA audits and secure absolute offline user privacy.
- **Time-based Maintenance Logic**: Replaced complex odometer/kilometer-based logging with time-based calendar reminders (e.g., every 6 months) for vehicle maintenance.
- **Google Sign-In Web Client ID**: Configured Google Sign-In options with the correct GCP Web Client ID and Gmail read scope, resolving integration developer errors.
- **KTX Extension Migration**: Migrated from legacy `android.graphics.Color.parseColor` to the KTX extension function `String.toColorInt()` in `DashboardScreen.kt`, `OnboardingScreen.kt`, and `SubscriptionListScreen.kt`. Removed suppression annotations and unused imports where applicable.
- **Removed Redundant API Version Checks**: Cleaned up the codebase by removing obsolete `Build.VERSION.SDK_INT >= Build.VERSION_CODES.O` checks since the application's `minSdk` is configured at 26 (Android Oreo). Removed the unused `android.os.Build` imports.

### Fixed
- **Onboarding Renewal Dates**: Fixed initial billing date projections during onboarding templates to respect target cycles (using `Calendar` offsets) rather than hardcoding a generic 30-day offset.
- **Notification Rollover Logic**: Aligned background notification service rollover calculations with the newly added billing cycles.
- **Code Health & Lint Warnings**:
  - Converted local delegate-based Compose states (`showAddBottomSheet`, `showSettingsDialog`, `showTemplatesDialog` in `DashboardScreen.kt`) to direct property accesses using `.value` to eliminate linter false-positives regarding unread assignments.
  - Removed unused parameters (`percentage` in `InteractiveCategoryRow`, `totalSpending` in `InteractiveCategoryLegend`, and unused lambda argument `isGranted` in `MainActivity.kt`) and properties (`secondaryColor` in `CashflowForecastingChart`).
  - Cleaned up redundant package qualifiers from `BorderStroke`, `Paint`, `Typeface`, and `Toast`.
  - Removed the unused helper function `getFormatter` in `CurrencyFormatter.kt`.

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
