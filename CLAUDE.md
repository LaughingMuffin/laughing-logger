# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Laughing Logger is an Android logcat viewer (Java, minSdk 21, targetSdk 34, AGP 7.4.2). Forked from Matlog/CatLog. Package `org.laughing.logger`.

## Build flavors

Two `store` flavors × two build types give four variants. Always specify a variant — `assembleDebug` alone is ambiguous.

- **fdroid** — open-source variant. No Firebase, no AdMob, no signing required. Use this for local development.
- **play** — Play Store variant. Pulls in Firebase Crashlytics/Analytics and AdMob. Requires `google-services.json` at `app/src/play/` (note: the README incorrectly says `app/src/main/play/`) and AdMob IDs in `local.properties` (`AD_APP_ID`, `AD_ON_CLICK_ID`) — these are injected at build time via the `secrets-gradle-plugin`.

Release signing is optional and gated on `RELEASE_STORE_FILE` being present in `local.properties` (with `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS_LL`, `RELEASE_KEY_PASSWORD_LL`); without it, release builds fall back to the debug signing config.

## Common commands

```
./gradlew assembleFdroidDebug         # fast local build, no setup needed
./gradlew installFdroidDebug          # build + install to attached device
./gradlew assemblePlayDebug           # needs google-services.json + AdMob IDs
./gradlew lint                        # lint.xml lives in app/
./gradlew dependencyUpdates           # ben-manes versions plugin
```

There are no unit or instrumentation tests in this repo despite `AndroidJUnitRunner` being declared in `app/build.gradle`.

## Flavor-isolation pattern (important)

Code that depends on Google Play Services must NOT be referenced from `app/src/main/`. The pattern used here is a wrapper class with two implementations:

- `app/src/play/java/.../util/CrashlyticsWrapper.java` — real implementation, imports Firebase.
- `app/src/fdroid/java/.../util/CrashlyticsWrapper.java` — no-op stub, same package + class name.

`App.java` calls `CrashlyticsWrapper.initCrashlytics(this)` and Gradle picks the right one per flavor. Follow this same pattern for any new Google-services-dependent code (AdMob, Analytics, etc.) — otherwise the fdroid build will break.

## Architecture

**Entry point:** `LogcatActivity` (single-top launcher) — the only real screen, plus three dialog-themed activities (`AboutDialogActivity`, `RecordLogDialogActivity`, `SettingsActivity`).

**Log reading pipeline (`reader/` package):**
- `LogcatReader` interface, backed by `SingleLogcatReader` (one buffer) or `MultipleLogcatReader` (interleaves several).
- `LogcatReaderLoader` is `Parcelable` so the reader config can survive process death / be handed to the recording service. It picks single vs. multiple based on which buffers are enabled in preferences.
- Readers shell out to `logcat` as a subprocess (`LogcatHelper`). `dmesg` is read separately via `DmesgHelper` + `SuperUserHelper` (requires root).
- `ScrubberUtils` redacts PII from log lines when scrubber mode is on (toggled via `LogLine.isScrubberEnabled`).

**Recording (`LogcatRecordingService`):** foreground service (`foregroundServiceType="specialUse"`) that uses a `LogcatReaderLoader` to keep tailing after the activity is gone and writes to disk via `SaveLogHelper`. `RecordingWidgetProvider` is the home-screen widget toggle for it.

**Storage path differs by Android version** (`SaveLogHelper`):
- Android ≤ 10: `/<internal>/Laughing Logger/saved_logs/` (uses legacy storage + `WRITE_EXTERNAL_STORAGE` permission, capped at SDK 32 in the manifest).
- Android ≥ 11: `/<internal>/Android/media/org.laughing.logger/Laughing Logger/saved_logs/` (scoped storage).

When touching file I/O, check the API-level branch — bugs here typically only manifest on one side.

**Persistence:** `CatlogDBHelper` (SQLite) stores saved filters (`FilterItem`); preferences via `PreferenceHelper` wrapping `SharedPreferences`. No Room, no DI framework.

**Custom preference widgets** live in `widget/` (`SweetSwitchPreference`, `MultipleChoicePreference`, `NonnegativeIntegerEditTextPreference`) — they're referenced from `res/xml/settings.xml` and used by `SettingsActivity`.

## Constraints to respect

`app/build.gradle` pins three dependencies with "do not upgrade" comments — `androidx.appcompat:1.4.2`, `material:1.6.1`, and (implicitly) the recyclerview/fastscroll combo. Upgrading them breaks the fast-scroll behavior. Leave them alone unless explicitly asked.

`READ_LOGS` is a `signature|privileged` permission on modern Android; the app relies on the user granting it via ADB (`pm grant org.laughing.logger android.permission.READ_LOGS`) or on rooted devices. Missing-logs bugs are usually permission issues, not code bugs.
