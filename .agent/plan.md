# Project Plan

Build a native Android subscription tracker app named 'sub_lazy' using Kotlin and Jetpack Compose. The app should help users track their subscriptions, provide a dashboard with spending insights, and send notifications 2 days before renewal. It should follow Material Design 3 and a premium dark mode theme.

## Project Brief

# Project Brief: sub_lazy

`sub_lazy` is a minimalist, high-performance native Android subscription tracker tailored for "lazy" users who want to manage their recurring expenses with zero friction. It features a premium Dark Mode aesthetic, automated reminders, and quick-entry templates for popular services.

## Features

* **Quick-Onboarding Templates:** Instantly add popular subscriptions (Netflix, Spotify, YouTube Premium, etc.) with pre-filled regional costs and billing cycles via multi-select.
* **Intelligent Home Dashboard:** A visual overview of your financial health featuring total estimated monthly costs and a spending distribution pie chart.
* **Active Subscription Management:** A clean list of active services showing monthly costs and a live countdown to the next renewal date (e.g., "3 days left") with intuitive swipe-to-delete actions.
* **Proactive Billing Alerts:** Automated background notifications powered by WorkManager that alert you 2 days before a billing event at 9:00 AM.
* **Manual Subscription Entry:** A streamlined manual entry flow via a Material 3 Floating Action Button for custom or local services.

## High-Level Technical Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Local Database:** Room (using **KSP** for efficient code generation)
* **Background Processing:** WorkManager (for scheduled billing notifications)
* **Concurrency:** Kotlin Coroutines & Flow (for reactive data streams)

## Visual Direction (Premium Dark Mode)

* **Primary Background:** `#0F172A` (Deep Navy)
* **Accent Colors:** `#6366F1` (Indigo) / `#06B6D4` (Cyan)
* **Experience:** Full Edge-to-Edge display with vibrant Material 3 components and fluid animations.

## Implementation Steps
**Total Duration:** 47m 45s

### Task_1_Setup_Data_Theme_Navigation: Initialize the Room database, Material 3 Premium Dark Theme, and Navigation structure.
- **Status:** COMPLETED
- **Updates:** Room database with Subscription entity and DAO is set up. Material 3 Dark Mode theme (Deep Navy background: #0F172A) is configured. Navigation graph for Dashboard, List, and Entry screens is defined. Project builds successfully. Adaptive app icon generated. Architecture set up with ViewModel and Repository.
- **Acceptance Criteria:**
  - Room database with Subscription entity and DAO is set up
  - Material 3 Dark Mode theme (Deep Navy background) is configured
  - Navigation graph for Dashboard, List, and Entry screens is defined
  - Project builds successfully
- **Duration:** 26m 40s

### Task_2_Subscription_Management_UI: Implement the main subscription list with swipe-to-delete, manual entry flow, and template-based onboarding.
- **Status:** COMPLETED
- **Updates:** Implemented Subscription List with countdowns and swipe-to-delete. Created Add/Edit screen with DatePicker and cycle selection. Implemented Onboarding screen with popular service templates and multi-select. Dashboard shows total monthly spending. All CRUD operations are integrated with Room and ViewModel. Premium Dark Mode and Edge-to-Edge display applied.
- **Acceptance Criteria:**
  - Subscription list displays active services with countdowns
  - Manual entry form with M3 FAB works correctly
  - Template-based onboarding allows multi-select of popular services
  - Full CRUD operations on subscriptions are functional
- **Duration:** 11m 53s

### Task_3_Dashboard_and_Notifications: Implement the spending dashboard with a pie chart and WorkManager-based notifications.
- **Status:** COMPLETED
- **Updates:** Implemented a custom Canvas-based Donut Chart for spending distribution with subtle animations. Integrated WorkManager for scheduling notifications 2 days before renewal at 9:00 AM. Implemented notification rescheduling/cancellation logic in the repository. Added POST_NOTIFICATIONS permission handling. Dashboard now shows a detailed category-wise spending breakdown. UI is fully reactive using Room Flows.
- **Acceptance Criteria:**
  - Dashboard shows total monthly cost and spending distribution pie chart
  - WorkManager schedules notifications 2 days before renewal at 9:00 AM
  - Notifications are correctly triggered in background
  - Reactive UI updates via Flow/Coroutines
- **Duration:** 9m 12s

### Task_4_Refinement_and_Final_Verify: Apply final UI polish including edge-to-edge display, adaptive app icon, and verify application stability.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Full edge-to-edge display is implemented
  - Adaptive app icon matching the 'sub_lazy' theme is created
  - App builds and runs without crashes
  - All existing tests pass
  - Critic agent verifies alignment with requirements and stability
- **StartTime:** 2026-06-01 15:18:01 ICT

