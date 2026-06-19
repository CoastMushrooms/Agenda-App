# Agenda App

An Android app for tracking homework/assignments. You can create assignments with a title, description, subject, due date, and priority, attach a photo (taken with the camera or from storage), mark them complete, and view everything in a list or calendar view. Data is synced through Firebase Realtime Database, and completed/old assignments are cleaned up periodically with a background worker.

## Features

- Create, edit, and delete assignments
- Set due dates, subjects, and priority levels (High/Medium/Low)
- Attach a photo to an assignment via the camera or device storage
- List view and full-screen calendar view
- Automatic cleanup of old/completed assignments (via `WorkManager`)
- Cloud sync with Firebase Realtime Database

## Tech stack

- Java, Android SDK (min SDK 29, target/compile SDK 36)
- Firebase Realtime Database, Firebase Storage, Firebase Auth
- AndroidX (AppCompat, RecyclerView, ConstraintLayout, WorkManager)
- Picasso (image loading)
- Gradle (Kotlin DSL)

## Setup

This project does **not** ship with any Firebase configuration — you'll need to connect it to your own free Firebase project. This keeps your data on your own account and avoids putting any private credentials in source control.

### 1. Create your own Firebase project

1. Go to the [Firebase Console](https://console.firebase.google.com/) and click **Add project** (it's free on the default Spark plan).
2. Give it any name (e.g. "Agenda App") and finish project creation.
3. In your new project, click the **Android icon** to add an Android app.
   - Package name: `com.example.homeworkapp` (must match exactly, or update `applicationId`/`namespace` in `app/build.gradle.kts` to whatever you choose)
   - Nickname and SHA-1 are optional for this app.
4. Download the generated **`google-services.json`** file.
5. Place it at `app/google-services.json` in this project (same folder as `app/build.gradle.kts`). It's already excluded in `.gitignore` so it won't accidentally get committed.

### 2. Enable the Firebase products the app uses

In the Firebase Console for your project:

- **Realtime Database** → Create a database (test mode is fine to start, but tighten the security rules before any real/public use).
- **Storage** → Get started (used if you store assignment images in Firebase Storage rather than as Base64).
- **Authentication** → Enable a sign-in method if/when you want to add user accounts (the app currently works without requiring sign-in).

### 3. Build and run

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and run it on an emulator or device as usual.

## Notes

- The original copy of this project had a personal Firebase project's credentials (`google-services.json`) and a hardcoded database URL baked in. Both have been removed so this repo is safe to share/use without affecting anyone else's Firebase usage or quota — see **Setup** above to wire up your own.
- If you change the app's package name away from `com.example.homeworkapp`, make sure it matches the package name you registered in the Firebase Console, and update `applicationId`/`namespace` in `app/build.gradle.kts` accordingly.
