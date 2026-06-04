# Sub Lazy 📱

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-SDK%2026%2B-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room%20DB-SQLite-orange.svg?style=for-the-badge)](https://developer.android.com/training/data-storage/room)

Have you ever looked at your bank statement and gone, *"Wait, when did I sign up for a premium cheese-of-the-month club?!"* only to realize you've been paying for it for the last two years? Or did you forget to cancel that "free trial" and paid the ultimate price? 

Yeah, we've all been there. 

**Sub Lazy** was born to save your wallet from your own forgetfulness! It is a premium, modern subscription tracker and manager for Android that helps you easily track recurring services, visualizes the monthly damage, and yells at you (nicely, via local notifications) 2 days before a bill renews so you actually have enough funds.

<p align="center">
  <img src="assets/cat_coding.png" width="220" alt="Lazy Cat coding"/>
  <br>
  <i>Me trying to track all my subscriptions... 🐱</i>
</p>

---

## ✨ Features

- **Quick Setup / Onboarding**: Start instantly by selecting from popular pre-defined subscription templates (Netflix, Spotify, YouTube Premium, Net/Wifi, etc.).
- **Smart Bill Ingestion**:
  - **Offline OCR Scanning**: Parse bill details (prices, billing cycles, renewal dates) locally and privately using **Google ML Kit Text Recognition** (no API key required).
  - **Notification Parsing**: Integrates a `BillNotificationListener` to capture SMS or banking app alerts (like MoMo, Vietcombank) and auto-prefill new subscriptions.
- **Interactive Dashboard**:
  - **Dynamic Spending Card**: Visualizes your total monthly subscription costs in a premium gradient card.
  - **Interactive Donut Chart**: Touch-responsive category spending donut chart. Tapping segments explodes (offsets) the segment and updates center details.
  - **Expanded Legends**: Tapping a category legend item expands a list showing all subscriptions tracked under that category with smooth slide animations.
  - **Billing Cycle Comparison**: Dynamic vertical bar charts comparing Weekly, Monthly, and Yearly spending impacts.
  - **Upcoming Renewals Timeline**: A scrollable horizontal timeline showing renewal dates. Urgently renewing items display a pulsing animated halo outer ring.
  - **Cashflow & Runway Forecasting**: A bespoke Canvas-drawn curve Line Chart projecting total subscription spending over the next 6 months, automatically highlighting and detailing the month with peak spending.
  - **Payment History feed**: Displays the last 5 manually/automatically completed billing events in a list on the Dashboard.
- **Manual Payment Tracking & VietQR Quick Pay**: Users can click "Mark as Paid" directly from the timeline card details. This records the payment in the database, rolls over the renewal date, and decrements remaining limits. For subscriptions with bank transfer details, the app automatically generates a standard Napas **VietQR** transfer code so users can scan and pay instantly.
- **Subscription List Screen**: Shows monthly equivalent costs, renewal schedules, color-coded countdown indicators, and a **remaining count badge** (e.g. `Còn 3 lần`) for limited subscriptions. Swift swipe-to-delete has a double-confirm dialog.
- **Add / Edit Subscriptions**: Fully customizable forms. Includes pricing, renewal date picker, category dropdown, billing cycle selection (**Daily**, **Weekly**, **Monthly**, **Every 3 Months**, **Every 6 Months**, **Yearly**, **One-time**), and a **currency toggle (VND/USD)** using Material 3 card controls.
- **Auto-Delete Limit (1-time / N-times count)**: Users can configure subscriptions to delete automatically after a set number of payments (1 time, custom N times, or unlimited). The app automatically rolls over renewal dates, decrements remaining cycles, or deletes expired subscriptions on startup.
- **Dynamic Localization & Multi-Currency**:
  - Instantly switch the app language between **English** and **Vietnamese** dynamically.
  - **Real-Time Currency Conversions**: All amounts are dynamically formatted and converted based on the active language (VND displayed in Vietnamese, USD displayed in English) using a real-time exchange rate of `1 USD = 25,400 VND` to prevent raw numerical discrepancies.
- **Billing Alerts (Notifications)**: Schedules background reminders using **Android WorkManager** exactly 2 days before renewal.

<p align="center">
  <img src="assets/cat_saving.png" width="220" alt="Lazy Cat with savings"/>
  <br>
  <i>Current state of my bank account after subscription renewals... 💰</i>
</p>

---

## 🛠️ Architecture & Tech Stack

The project follows standard Android **MVVM (Model-View-ViewModel)** architecture and modern clean-code practices:

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for fully declarative, responsive, and state-of-the-art UI elements.
- **Data Persistence**: [Room SQLite Database](https://developer.android.com/training/data-storage/room) for reactive storage and flow streams.
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for reliable background notification scheduling that survives reboot and app termination.
- **Asynchronous Execution**: Kotlin **Coroutines** and **StateFlow** for lifecycle-aware reactive UI updates.
- **Design System**: Material Design 3 (M3) with custom HSL-based styling, system-adaptive theme colors (light/dark mode toggle), and edge-to-edge screens.

---

## 📂 Project Structure

```
lazy_sub/
├── app/
│   ├── build.gradle.kts                # Subproject Gradle build file
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml      # App configuration & permissions
│           ├── java/com/gbao86/sub_lazy/
│           │   ├── MainActivity.kt     # App entry point (AppCompatActivity)
│           │   ├── data/               # Room Database entity, DAOs, & repository
│           │   ├── ui/                 # Composable screens & theme definitions
│           │   ├── viewmodel/          # State & business logic handlers
│           │   └── worker/             # Background workers for notifications
│           └── res/                    # UI resources (Drawables, Mipmaps, Values)
│               ├── drawable/           # Custom cat drawables (onboarding & empty state)
│               ├── values/strings.xml  # English string resource dictionary
│               └── values-vi/strings.xml # Vietnamese translation resources
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug or newer recommended)
- **Java SE Development Kit (JDK) 17 or 21**
- Android Device or Emulator running **API Level 26 (Android 8.0)** or higher

### Installation & Run

1. Clone or copy the project to your local directory.
2. Open the project in Android Studio.
3. Let Gradle sync project dependencies.
4. Run the app on an emulator or a physical device:
   ```bash
   ./gradlew.bat installDebug
   ```
---

## 🌍 Localization
To switch languages programmatically, use the Globe/Language icon on the top right corner of the Dashboard screen. Selecting a language automatically updates the context locale via the `AppCompatDelegate` API.

---

## 📜 Changelog

All notable changes and updates to this project are documented in the [CHANGELOG.md](./CHANGELOG.md) file.

---

## 👥 Author

*   **Trịnh Gia Bảo (gbao86)**
*   📧 Email: [tiktokthu10@gmail.com](mailto:tiktokthu10@gmail.com)

---

## 📄 License

This project is licensed under a Non-Commercial License. You are free to copy and modify the code for personal use, but commercial exploitation or monetization is strictly prohibited. See the [LICENSE](./LICENSE) file for details.
