# Sub Lazy 📱

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-SDK%2026%2B-green.svg?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg?style=for-the-badge&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room%20DB-SQLite-orange.svg?style=for-the-badge)](https://developer.android.com/training/data-storage/room)

Have you ever looked at your bank statement and gone, *"Wait, when did I sign up for a premium cheese-of-the-month club?!"* only to realize you\'ve been paying for it for the last two years? Or did you forget to cancel that "free trial" and paid the ultimate price? 

Yeah, we\'ve all been there. 

**Sub Lazy** was born to save your wallet from your own forgetfulness! It is a premium, modern subscription tracker and manager for Android that helps you easily track recurring services, visualizes the monthly damage, and yells at you (nicely, via local notifications) 2 days before a bill renews so you actually have enough funds.

---

## ✨ Features

- **Quick Setup / Onboarding**: Start instantly by selecting from popular pre-defined subscription templates (Netflix, Spotify, YouTube Premium, Net/Wifi, etc.).
- **Interactive Dashboard**:
  - **Dynamic Spending Card**: Visualizes your total monthly subscription costs in a premium gradient card.
  - **Animated Pie Chart**: Custom Canvas-drawn animated distribution chart showing category spending dynamically.
  - **Language Switcher**: In-app dropdown switcher to toggle between **English** and **Vietnamese** seamlessly using standard Android AppCompat locale APIs.
- **Subscription List**:
  - **Active Services Summary**: Shows monthly equivalent costs, renewal schedules, and color-coded countdown indicators.
  - **Swipe-to-Delete**: Quick deletion of tracked subscriptions using fluid gesture animations.
- **Add / Edit Subscriptions**: Fully customizable forms for tracking custom services. Includes pricing in VND, renewal date picker, categories, and billing cycles (Monthly vs Yearly).
- **Billing Alerts (Notifications)**: Schedules background reminders using **Android WorkManager** exactly 2 days before renewal at 9:00 AM.

---

## 🛠️ Architecture & Tech Stack

The project follows standard Android **MVVM (Model-View-ViewModel)** architecture and modern clean-code practices:

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) for fully declarative, responsive, and state-of-the-art UI elements.
- **Data Persistence**: [Room SQLite Database](https://developer.android.com/training/data-storage/room) for reactive storage and flow streams.
- **Background Tasks**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for reliable background notification scheduling that survives reboot and app termination.
- **Asynchronous Execution**: Kotlin **Coroutines** and **StateFlow** for lifecycle-aware reactive UI updates.
- **Design System**: Material Design 3 (M3) with custom HSL-based styling, subtle micro-animations, and edge-to-edge screens.

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
Tất cả các thay đổi và cập nhật tính năng của dự án được lưu trữ đầy đủ trong file [CHANGELOG.md](./CHANGELOG.md).

---

## 👥 Tác giả (Author)
*   **Trịnh Gia Bảo**
*   📧 Email: [tiktokthu10@gmail.com](mailto:tiktokthu10@gmail.com)

---

## 📄 Bản quyền (License)
Dự án được phân phối dưới bản quyền **GNU General Public License Version 3 (GPLv3)**. Chi tiết vui lòng xem file [LICENSE](./LICENSE).
