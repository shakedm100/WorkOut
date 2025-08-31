# WorkOut

WorkOut is an Android application that helps users discover and join workouts offered by local sports businesses. It aims to give small sport businesses a fair chance to compete against large fitness companies by providing them with a simple platform for managing workouts and attracting new clients.

---

## Features

* **Authentication** – Firebase Authentication for secure login & registration
* **Workout Discovery** – Users can browse available courses/workouts from businesses
* **Location-Based Search** – Find businesses and workouts near you
* **Course Enrollment** – Sign up for available courses directly through the app
* **Auto Login** – Remember last login and log in automatically on startup
* **Business Tools** – Businesses can publish and manage their workouts
* **Client Tools** – Clients can view, enroll, and manage their workout history
* **Google Maps Integration** – View workouts on a map and navigate to businesses
* **Push Notifications** – Firebase Cloud Messaging (FCM) for reminders and updates
* **Responsive UI** – Material Design for a modern and intuitive experience

---

## Tech Stack

* **Language:** Java (Android Studio project)
* **UI/UX:** Android Views, Material Components
* **Architecture:** MVVM (Model–View–ViewModel)
* **Database:** Firebase Firestore (cloud-hosted, no local DB)
* **Auth:** Firebase Authentication
* **Notifications:** Firebase Cloud Messaging (FCM)
* **Testing:** Espresso, Mockito
* **Build Tools:** Gradle (Kotlin DSL)

---

## Project Structure

```
Documentation/                  # JavaDocs and generated documentation
Preparations/                   # Assignment docs and diagrams
│   └── diagrams/
│       ├── Before Project Finished   # Original assignment diagrams
│       └── After Project Finished    # Updated diagrams after completion
Firebase Tools/                 # Instructions & scripts for Firebase features (e.g., notifications)
Workout/
 └── app/
      └── src/
          └── main/
              ├── java/
              │    ├── Adapters/                 # RecyclerView & UI adapters
              │    ├── Model/                    # Data models (Client, Business, Course, etc.)
              │    │    └── Repositories/        # Firestore data access
              │    ├── ViewModel/                # ViewModels for MVVM
              │    └── com/example/workout/      # Activities (Login, Main, History, Map, etc.)
              └── res/
                   ├── layout/      # XML layouts
                   ├── drawable/    # Icons & images
                   ├── values/      # Strings, colors, styles
                   ├── color/       # App color definitions
                   ├── mipmap/      # App icons
                   └── raw/         # Map config files
```

---

## Getting Started

### Prerequisites

* [Android Studio](https://developer.android.com/studio) (latest version recommended)
* A Firebase project (Firestore + Authentication + Cloud Messaging enabled)

### Setup

1. Clone the repo:

   ```bash
   git clone https://github.com/shakedm100/WorkOut.git
   cd WorkOut
   ```

2. Open the project in **Android Studio**

3. Connect the app to your **Firebase project**:

   * Add `google-services.json` in `app/`
   * Enable Firebase Auth, Firestore & Cloud Messaging in your Firebase console

4. Sync Gradle and run the app on an emulator or device.

---

## Testing

The project includes **unit tests and UI tests**:

* **Mockito** – For mocking repositories and dependencies
* **Espresso** – For UI testing user flows (e.g., login, course enrollment)

You can run the tests directly with Android Studio's UI

