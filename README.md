# Fiscus

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-blue.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)

A personal expense tracker built with Kotlin Multiplatform and Compose Multiplatform, targeting Android and iOS with a fully shared UI. Snap or share a receipt (mobile banking, e-wallet, or paper receipt), review the amount and merchant, and keep everything stored locally on your device.

<!-- TODO: add a demo GIF or screenshots here -->

## Project Structure
- `/shared` - Shared code for all platforms (logic and Compose UI)
  - `commonMain` - Code shared across all platforms
    - `domain` - Models and repository interfaces (pure Kotlin)
    - `data` - SQLDelight database and repository implementations
    - `di` - Koin modules
    - `presentation` - Screens, ScreenModels, theme, and shared components
    - `sqldelight` - Database schema (`.sq` files)
  - `androidMain` - Android-specific code
  - `iosMain` - iOS-specific code
  - `commonTest` - Shared tests, run on both the JVM and the iOS simulator
- `/androidApp` - Android application entry point
- `/iosApp` - iOS application entry point (Xcode project)

## Architecture
- **Clean Architecture (lightweight)**: `presentation → domain ← data`
- **MVVM with unidirectional data flow**: each screen has a Voyager `ScreenModel` exposing a single immutable state
- **expect/actual** for platform-specific pieces such as the SQLite driver

## Tech Stack
- **Kotlin Multiplatform** - Share code between platforms
- **Compose Multiplatform** - Shared UI framework with Material 3
- **SQLDelight** - Type-safe local database
- **Koin** - Dependency injection
- **Voyager** - Navigation, tabs, and ScreenModel
- **KotlinX Coroutines** - Asynchronous programming with Flow
- **KotlinX DateTime** - Date and time handling

## Getting Started

### Prerequisites
- Android Studio with the Kotlin Multiplatform plugin
- JDK 17 or higher (the JDK bundled with Android Studio works)
- Xcode with an iOS simulator runtime installed (for iOS development)
- Android device or emulator running Android 7.0 (API 24) or higher

### Building the Project
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files

### Android
```bash
./gradlew :androidApp:installDebug
```

### iOS
Select the `iosApp` run configuration in Android Studio and pick a simulator, or open `iosApp/iosApp.xcodeproj` in Xcode and run the project.

> Xcode builds the shared Kotlin framework through Gradle, so a JDK must be visible to Xcode. If the build fails with "Unable to locate a Java Runtime", install a system JDK or run from Android Studio instead.

### Running Tests
```bash
./gradlew :shared:testAndroidHostTest      # shared tests on the JVM
./gradlew :shared:iosSimulatorArm64Test    # shared tests on the iOS simulator
```
