# SL Downloader

SL Downloader is a local-first Android app for saving authorized direct video files such as MP4 and WebM. Only URLs the user owns or is permitted to download should be used.

## Features

- Material 3 Compose dashboard with Home, Downloads, History, and Settings
- Paste and share-to-app URL intake with HTTP/HTTPS validation
- Room-backed download history
- Streaming HTTP download to `Movies/SL Downloader` through MediaStore
- Progress state, completed file records, and safe filename handling
- Foreground-service notification channel for long-running download work
- Light Android 8.0+ foundation with English and Bangla resources

The app intentionally does not bypass DRM, paywalls, CAPTCHAs, authentication, private content, or platform restrictions. It does not collect URLs, downloaded files, cookies, or tokens.

## Technology and architecture

- Kotlin, Jetpack Compose, Material 3
- MVVM with StateFlow
- Room for local history
- MediaStore for scoped storage
- Media3 dependencies are included for the player surface
- `data/database`, `data/repository`, `domain`, `presentation`, `service`, and `util` boundaries

## Build

Open the project in Android Studio with its bundled JDK, allow Gradle sync, and run:

```text
./gradlew assembleDebug
./gradlew test
```

On Windows use `gradlew.bat assembleDebug` and `gradlew.bat test`. The debug APK is written to `app/build/outputs/apk/debug/`.

For a release build, configure a signing key in Android Studio and run the `bundleRelease` or `assembleRelease` task.

## Android Studio setup

1. Open the repository root, not the `app` directory.
2. Select the bundled JDK in Gradle settings.
3. Sync the project and install on an Android 8.0+ device or emulator.
4. Grant notifications on Android 13+ if download notifications are enabled.

## Testing and troubleshooting

Unit coverage currently includes URL policy and filename sanitization. Use a direct media URL that explicitly permits downloading when exercising the network path. A server may reject resume, redirects, or unknown content lengths; the app reports the resulting failure rather than attempting to bypass the server.

If Gradle cannot start, verify that Android Studio's embedded JDK is selected. If a download fails, check network access, URL permissions, server response status, and available device storage.

## Privacy

The app is designed to work locally. Download records are stored in the app's local Room database and media files are stored using MediaStore. No analytics or remote upload is included.
