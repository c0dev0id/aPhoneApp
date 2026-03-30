# Development Journal

## Software Stack

- **Language**: Java 17
- **Platform**: Android (minSdk 34, targetSdk 35, compileSdk 35)
- **Build**: Gradle 8.7, AGP 8.5.2
- **UI**: Android Views (XML layouts), Material3
- **Font**: Apotek (medium 400, bold 700)
- **Dependencies**: AndroidX AppCompat 1.7.0, RecyclerView 1.3.2, Material 1.12.0, Core 1.13.1
- **CI/CD**: GitHub Actions — lint → build → sign → nightly pre-release on every push to main

## Key Decisions

### Java over Kotlin
More training data and community examples exist for Java in the Android space, leading to better AI-assisted development experience.

### InCallService as default dialer
The app requests to be set as the default dialer on first launch. This is mandatory — without it, `InCallService` cannot intercept calls and display the calling card overlay. Users who decline cannot proceed.

### TYPE_APPLICATION_OVERLAY for all UI
Both the calling card and the sidecar are `WindowManager` overlays (`TYPE_APPLICATION_OVERLAY`), not Activities. This avoids back-stack management and task history, fitting the single-purpose motorcycle use case. The app quits when the last overlay is closed.

### Permission gate before any UI
All permissions (READ_CONTACTS, READ_CALL_LOG, CALL_PHONE, SYSTEM_ALERT_WINDOW, default dialer) are requested in sequence via `PermissionActivity` before any app UI is shown. No partial functionality — all or nothing.

### Overlay interaction model
The calling card and sidecar are treated as independent overlays. An incoming call while the sidecar is open dims the sidecar and locks focus on the calling card. Focus returns to the sidecar when the call ends. The app quits only when the last overlay is dismissed.

### DMD Remote 2 input
Key Down events with `repeat=0` only (no key repeat). BUTTON 1 / BUTTON 2 map to accept/initiate and decline/end/back respectively. The Dialpad tab uses a vim-like modal focus: BUTTON 1 enters dialpad mode, BUTTON 2 exits back to the tab bar.

## Core Features

- **Incoming calls**: overlay with accept/decline, call duration, end call
- **Sidecar**: History tab (call log), Contacts tab (favorites first), Dialpad tab (modal input)
- **Outgoing calls**: overlay with "Calling…" state, duration when connected, end call
- **Hardware navigation**: full DMD Remote 2 button support throughout
