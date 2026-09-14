# Contributing to SL Downloader

Thank you for helping improve SL Downloader. Contributions are welcome when they preserve the project's local-first privacy model, Android security boundaries, and authorized-download policy.

## Before you start

1. Read the [README](README.md) and [authorized-download policy](README.md#authorized-download-policy).
2. Search existing issues and pull requests before opening a new one.
3. For security problems, follow [SECURITY.md](SECURITY.md) instead of opening a public issue.

## Development setup

- Android Studio with the bundled JDK
- Android SDK 37
- Android 8.0+ emulator or device
- Git and the Gradle wrapper

```bash
git clone https://github.com/surjolive/SL-Downloader.git
cd SL-Downloader
```

Open the repository root in Android Studio and allow Gradle sync to complete.

## Build and test

Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat test
```

macOS/Linux:

```bash
./gradlew :app:assembleDebug
./gradlew test
```

Do not commit `local.properties`, keystores, generated build output, IDE state, or credentials.

## Making changes

- Keep changes focused and explain the user-visible behavior.
- Prefer existing Kotlin, AndroidX, Compose, WebView, Room, and repository patterns.
- Keep HTML, CSS, and JavaScript assets local; do not add untrusted remote scripts.
- Do not add DRM circumvention, CAPTCHA bypass, paywall bypass, private-content extraction, cookie theft, token theft, or authentication scraping.
- Validate direct media URLs and sanitize filenames.
- Add or update tests for validation, persistence, queue behavior, or other changed logic.
- Preserve accessibility, English/Bangla resource support, and Android 8.0 compatibility.

## Pull requests

1. Create a focused branch from `main`.
2. Make the smallest complete change.
3. Run the relevant Gradle build and tests.
4. Update documentation when behavior or setup changes.
5. Open a pull request using the repository template.

A good pull request includes:

- What changed and why
- Tests or build commands run
- Screenshots for UI changes
- Storage, permission, privacy, and compatibility considerations
- Any known limitations

## Commit messages

Use short imperative messages, for example:

```text
Add queue search filter
Fix direct media URL validation
Update release installation notes
```

## License

By contributing, you agree that your contribution is provided under the repository's [MIT License](LICENSE).
