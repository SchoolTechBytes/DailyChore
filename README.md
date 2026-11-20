# Overview

As a software engineer I wanted to learn native Android development with Kotlin and Jetpack Compose, and how a real app is layered: a local database, a repository, a ViewModel, and multiple screens connected with navigation.

Daily Chore is a daily routine and reward tracker for kids. A parent sets up chores (each worth points) and rewards (each with a point cost). The kid ticks off chores to earn points and spends them on rewards.

How to use it:
* **Parent tab (PIN protected):** the first time, create a 4-digit PIN. Then add chores (name + points) and rewards (name + cost), tap the bin icon to delete one, or change the PIN. It locks again when you leave the tab. **Forgot PIN?** erases the PIN and all data so you can start over.
* **Today tab:** tap a chore to mark it done and earn its points; tap again to undo. Ticks reset each new day.
* **Rewards tab:** tap **Redeem** on a reward you can afford; the points are deducted and a message confirms it.

All data is saved on the device, so it is still there after the app is closed or the phone restarts.

My purpose was to build a small but complete app that is useful at home, while practising Compose UI, state management, and persistent storage.

[Software Demo Video](https://youtu.be/EixlpOgyXIk)

# Development Environment

* Android Studio, Android Emulator (API 37), Gradle 9.6 with Android Gradle Plugin 9.5.1
* Language: Kotlin 2.2.10
* Libraries: Jetpack Compose with Material 3 (UI) and the Nunito Google Font, Navigation Compose 2.10.2 with Kotlin Serialization (type-safe routes), Lifecycle ViewModel 2.11.0, Room 2.8.5 with KSP (local SQLite database)
* Testing: JUnit, AndroidX Test / Espresso, Compose UI Test

# Useful Websites

* [Android Developers: Save data in a local database using Room](https://developer.android.com/training/data-storage/room)
* [Android Developers: Create ViewModels with dependencies](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories)
* [Android Developers: Navigation with Compose](https://developer.android.com/develop/ui/compose/navigation)
* [Android Developers: Type safety in Kotlin DSL and Navigation Compose](https://developer.android.com/guide/navigation/design/type-safety)
* [Android Developers: AndroidX Test releases](https://developer.android.com/jetpack/androidx/releases/test)

# Future Work

* Support multiple children on the same app, with a way to switch between them
* Add more selectable themes (green and blue is the current default)
* Let each child pick their own theme
* Allow editing existing chores and rewards, and confirm before deleting
* Show a points history (earned and spent)
