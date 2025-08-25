# WorkOut

WorkOut is an Android application that helps users discover and join workouts offered by local sports businesses.
It aims to give small sport businesses a fair chance to compete against large fitness companies by providing them with a simple platform for managing workouts and attracting new clients.

---

## Features

*  **Authentication** – Firebase Authentication for secure login & registration
*  **Workout Discovery** – Users can browse available courses/workouts from businesses
*  **Location-Based Search** – Find businesses and workouts near you
*  **Course Enrollment** – Sign up for available courses directly through the app
*  **Auto Login** – Remember last login and log in automatically on startup
*  **Business Tools** – Businesses can publish and manage their workouts
*  **Client Tools** – Clients can view, enroll, and manage their workout history

---

## Tech Stack

* **Language:** Java (Android Studio project)
* **UI/UX:** Android Views, Material Components
* **Architecture:** MVVM (Model–View–ViewModel)
* **Database:** Firebase Firestore (cloud-hosted, no local DB)
* **Auth:** Firebase Authentication
* **Testing:** Espresso, Mockito

---

## Project Structure

```
app/
 ├── java/
 │    ├── Model/          # Data models (Client, Business, Course, Schedule, etc.)
 │    ├── ViewModel/      # ViewModels for MVVM
 │    ├── Repository/     # Firestore data access
 │    └── Activities/     # Login, Main, History, Map, etc.
 └── res/
      ├── layout/         # XML layouts
      ├── drawable/       # Icons & images
      └── values/         # Strings, colors, styles
```

---

## Getting Started

### Prerequisites

* [Android Studio](https://developer.android.com/studio) (latest version recommended)
* A Firebase project (Firestore + Authentication enabled)

### Setup

1. Clone the repo:

   ```bash
   git clone https://github.com/shakedm100/WorkOut.git
   cd WorkOut
   ```
2. Open the project in **Android Studio**
3. Connect the app to your **Firebase project**:

   * Add `google-services.json` in `app/`
   * Enable Firebase Auth & Firestore in your Firebase console
4. Sync Gradle and run the app on an emulator or device.

---

## Running Tests

Unit tests & instrumentation tests use **JUnit**, **Mockito**, and **Espresso**.
Run them from Android Studio with:

```
./gradlew test
./gradlew connectedAndroidTest
```
