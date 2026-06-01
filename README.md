# Sub Lazy 📱

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-SDK%2026%2B-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room%20DB-SQLite-orange.svg?style=for-the-badge)](https://developer.android.com/training/data-storage/room)

Have you ever looked at your bank statement and gone, *"Wait, when did I sign up for a premium cheese-of-the-month club?!"* only to realize you've been paying for it for the last two years? Or did you forget to cancel that "free trial" and paid the ultimate price? 

Yeah, we've all been there. 

**Sub Lazy** was born to save your wallet from your own forgetfulness! It is a premium, modern subscription tracker and manager for Android that helps you easily track recurring services, visualizes the monthly damage, and yells at you (nicely, via local notifications) 2 days before a bill renews so you actually have enough funds.

<p align="center">
  <img src="app/src/main/res/drawable/cat_onboarding.png" width="220" alt="Lazy Onboarding Cat"/>
  <br>
  <i>"I watch your lazy bills so you can sleep all day!" — Onboarding Cat 🐱</i>
</p>

---

## ✨ Features

- **Quick Setup / Onboarding**: Start instantly by selecting from popular pre-defined subscription templates (Netflix, Spotify, YouTube Premium, Net/Wifi, etc.).
- **Funny Cat Memes & Humor UX**:
  - **Smug Onboarding Cat**: Welcomes you with black sunglasses and a pile of money, encouraging you to select templates.
  - **Crying Empty State Cat**: Stares dramatically at an empty food bowl when your list is clean and empty.
- **Interactive Dashboard**:
  - **Dynamic Spending Card**: Visualizes your total monthly subscription costs in a premium gradient card.
  - **Interactive Donut Chart**: Touch-responsive category spending donut chart. Tapping segments or the legend explodes (offsets) the segment and updates center details (category, price, percentage).
  - **Expanded Legends**: Tapping a category legend item expands a list showing all subscriptions tracked under that category with smooth slide animations.
  - **Billing Cycle Comparison**: Dynamic vertical bar charts comparing Monthly vs. Yearly spending impacts.
  - **Upcoming Renewals Timeline**: A scrollable horizontal timeline linking renewal dates. Urgently renewing items (due in <= 3 days) display a pulsing animated halo outer ring.
- **Subscription List Screen**:
  - **Active Services Summary**: Shows monthly equivalent costs, renewal schedules, and color-coded countdown indicators.
  - **Swipe-to-Delete Confirmation**: Prevent accidental deletions with an Android `AlertDialog` confirmation showing the service name. Cancel slides the item back smoothly.
- **Add / Edit Subscriptions**: Fully customizable forms for tracking custom services. Includes pricing, renewal date picker, categories, billing cycles, and dynamic currency prefix selection (₫/$) based on app language.
- **Dynamic Localization**: Instantly switch the app language between **English** and **Vietnamese** dynamically using the Top Bar selector.
- **Billing Alerts (Notifications)**: Schedules background reminders using **Android WorkManager** exactly 2 days before renewal at 9:00 AM.

<p align="center">
  <img src="app/src/main/res/drawable/cat_empty_state.png" width="200" alt="Empty Bowl Crying Cat"/>
  <br>
  <i>"Empty food bowl, empty wallet, empty list... A truly perfect lazy lifestyle!" 😿</i>
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

This project is licensed under the **GNU General Public License Version 3 (GPLv3)**. See the [LICENSE](./LICENSE) file for details.
