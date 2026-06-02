# Architecture proposal

The architecture is deliberately small. l’entre-deux is a single-user,
offline-first Android app with a few screens and a small amount of local
data. We want a structure that is easy for new contributors to read, not a
showcase of patterns.

## Principles

- **Plain Android.** Kotlin, Jetpack Compose, Room, DataStore, AndroidX
  ViewModel. Nothing exotic.
- **F-Droid-friendly.** No Google Play Services, no Firebase, no proprietary
  SDKs, no Crashlytics, no analytics, no ad libraries, no closed-source
  binaries. Fully reproducible Gradle build using the version catalog.
- **One module to start.** A single `:app` module. Split only when something
  concrete forces it.
- **Unidirectional data flow.** ViewModel owns state; the UI observes it;
  user events flow back via function references.
- **Domain logic in pure Kotlin.** Anything that can be unit-tested without
  the Android framework should live in `domain/` and not import
  `android.*`.
- **No DI framework yet.** Manual constructor injection is enough for MVP.
  Hilt is allowed only when manual wiring becomes a real burden, not
  preemptively.
- **No WorkManager yet.** Add only if a real background-scheduling need
  appears (e.g. nightly summary recompute). The pause flow itself is purely
  foreground.

## Module layout (current)

```
app/
  src/main/
    java/org/entredeux/app/
      EntreDeuxApplication.kt
      MainActivity.kt
      ui/
        theme/                Compose theme (colors, typography, shapes)
        home/                 Launcher grid screen (Phase 2)
        pause/                Pause flow screen      (Phase 2)
        selection/            App selection screen   (Phase 2)
        reflection/           Reflection screen      (Phase 4)
        settings/             Settings screen        (Phase 2+)
      domain/                 Pure-Kotlin model + use cases
        model/                Data classes (Intention, PauseEvent, ...)
        usecase/              One file per use case, small, testable
      data/                   Repositories + storage adapters
        local/                Room DAOs and entities
        prefs/                DataStore-backed settings
        apps/                 PackageManager-backed installed-app source
      util/                   Tiny shared helpers (formatters, time)
    res/
      values/
        strings.xml           All user-facing strings live here
        themes.xml
    AndroidManifest.xml
```

The folders above are placeholders in the skeleton; concrete files come in
the relevant phase. We do not pre-create empty packages just to "look
architectural."

## Tech choices

| Concern              | Choice                              | Why                                                  |
|----------------------|-------------------------------------|------------------------------------------------------|
| Language             | Kotlin                              | Standard for Android.                                |
| UI                   | Jetpack Compose + Material 3        | Modern, less boilerplate, good for calm UI.          |
| Min SDK              | 26 (Android 8.0)                    | Wide reach; supports `UsageStatsManager` if ever used. |
| Target SDK           | latest stable (35 at time of writing) | Required for store/F-Droid compliance.             |
| State                | `ViewModel` + `StateFlow`           | Lifecycle-aware, testable, no extra deps.            |
| Persistence          | Room                                | Pause-event log; queryable for reflection.           |
| Settings             | Jetpack DataStore (Preferences)     | Async, type-safe, no SharedPreferences quirks.       |
| Async                | Kotlin coroutines                   | Built into all the above.                            |
| DI                   | None (manual)                       | App is small; revisit only if pain emerges.          |
| Background work      | None (foreground only) for MVP      | Avoid policy and battery surprises.                  |
| Analytics / crash    | None                                | Trust principle. Errors logged locally only.         |
| Network              | None. `INTERNET` permission not requested. | Trust principle and F-Droid alignment.        |

## Data model (initial)

The MVP needs almost nothing.

- `SelectedApp(packageName: String, label: String)` — apps the user has
  chosen to route through the pause.
- `Intention` — fixed enum of three values (`SPECIFIC_TASK`,
  `BRIEF_CHECK`, `AUTOPILOT`). Stable wire string per value, so we can
  evolve UI labels without breaking stored data.
- `PauseEvent(id, timestamp, packageName, intention, outcome)` where
  `outcome ∈ {PROCEEDED, BACKED_OUT}`. (A `budgetMinutes` column existed
  through 0.6.0; the time-limit feature was removed in 0.7.0 and the column
  dropped via a Room v1→v2 migration.)
- `Settings` (DataStore): selected apps, hasCompletedOnboarding.

That is the entire schema for MVP. No user table, no sessions table, no
account.

## Navigation

A single-activity app (`MainActivity`) hosting Compose `NavHost` with
top-level routes:

- `home` (launcher grid)
- `selection` (pick apps)
- `pause/{packageName}` (pause flow; opened via in-app tap or pinned
  shortcut intent)
- `reflection`
- `settings`
- `onboarding`

The pause route can be entered from outside (a launcher shortcut intent)
because the pause is the whole product surface for many sessions.

## Threading and lifecycle

- All disk I/O on `Dispatchers.IO`.
- ViewModels expose `StateFlow<UiState>`; UI collects with
  `collectAsStateWithLifecycle`.
- No global singletons except a small `ServiceLocator` object created in
  `EntreDeuxApplication.onCreate`. Replaced with Hilt only if it earns its
  place.

## Testing strategy

- **Unit tests** for `domain/usecase/*` and any non-trivial mapper. Pure
  Kotlin, no Robolectric. JUnit 4 + Truth.
- **Repository tests** with in-memory Room where useful.
- **One Compose UI test** for the pause-flow happy path (Phase 2).
- **CI** runs `./gradlew lint test assembleDebug` on every push.

## Build and distribution

- Single Gradle build with the version catalog (`gradle/libs.versions.toml`).
- No product flavors at MVP. If we ever add an "advanced" build with
  sensitive capabilities, it lives in a clearly separate flavor and is
  excluded from the default F-Droid build.
- Versioning per `roadmap.md` and `CHANGELOG.md`.
- Distribution target: **F-Droid first.** Reproducible builds; an
  `fastlane/metadata/android/` tree will be added when first publishing.
  Any sensitive capability the app gains will be declared as an
  Anti-Feature in the F-Droid metadata.
- An APK build for direct download will also be produced, signed
  consistently across releases.

## Things we explicitly will not add yet

- Hilt / Dagger / Koin
- Retrofit / OkHttp / Ktor (no network)
- Coil / Glide (Compose has what we need for the launcher icons)
- Detekt / Spotless beyond AGP defaults — until the codebase grows
- Module split (`:domain`, `:data`, …) — until the single-module build
  becomes painful
- Any Google Play Services artifact — would break F-Droid build.

When in doubt, the answer is "not yet."
