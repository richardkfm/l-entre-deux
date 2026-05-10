# Changelog

All notable changes to l’entre-deux are recorded here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning is [Semantic Versioning](https://semver.org/), with the
project-specific rules described in [`CLAUDE.md`](CLAUDE.md).

## [Unreleased]

Nothing yet.

## [0.5.0] — 2026-05-10

Phase 5: accessibility, testing, polish, release prep.

### Added
- **Accessibility pass.**
  - `heading()` semantics on the pause-screen title, onboarding page
    headings, and the Settings "Default time limit" section header so
    TalkBack announces them as headings.
  - `Role.RadioButton` + `selectableGroup` on the default-budget chooser
    in Settings, replaced the bare `"✓"` glyph with a `Check` icon that
    has a localized "Selected" content description.
  - `Role.Button` on the home tile and Settings-row click targets;
    full-row `toggleable` (with `Role.Checkbox`) on the app-selection
    list so the whole row toggles state and reads correctly.
  - 48dp `heightIn` minimum on the app-selection rows.
  - Search field in app selection now has a real "Search" label and
    placeholder instead of reusing the screen title.
- **French translation** — full `values-fr/strings.xml` mirroring the
  English source. `resourceConfigurations += listOf("en", "fr")` pins
  the shipped locale set.
- **App icon** — adaptive icon (`mipmap-anydpi-v26/ic_launcher.xml` +
  `ic_launcher_round.xml`) with a vector foreground (two soft "pause"
  bars), a warm off-white background colour, and a monochrome variant
  for Android 13+ themed icons. No raster fallbacks needed — `minSdk`
  is 26.
- **F-Droid metadata** under `fastlane/metadata/android/{en-US,fr-FR}/`:
  `title.txt`, `short_description.txt`, `full_description.txt`, and
  `changelogs/5.txt`. A `fastlane/README.md` documents the layout.
- **Pause-flow instrumented test** rewired against the real
  `PauseViewModel` constructor (Phase 3 had silently outgrown the test;
  CI builds main only, so the regression slipped past). The test now
  spins up a Room in-memory database and reuses the production
  repositories.

### Changed
- `notification_budget_body` now uses positional placeholders
  (`%1$d` / `%2$s`) so French and other locales can reorder the args.
- `app/build.gradle.kts`: bumped `versionName` to `0.5.0` and
  `versionCode` to `5`. `buildFeatures.buildConfig = false` (no
  `BuildConfig` references in app code; turning it off keeps the APK
  lean and avoids a generated class).
- `proguard-rules.pro` documents that Compose / AndroidX / Room
  consumer rules are sufficient and adds a single
  `-assumenosideeffects` block to strip `Log.v/d/i` from release
  builds as future-proofing.

### Notes
- No new permissions. No new dependencies. No analytics. `INTERNET` still
  not declared.
- Adaptive icons require API 26+, which matches our `minSdk`. No
  legacy raster icons are shipped.
- F-Droid submission as `1.0.0` is deferred until the app is exercised
  on real devices.

## [0.4.0] — 2026-05-10

Phase 4: local reflection and weekly insights.

### Added
- **Reflection screen** — bottom-nav destination showing plain-language
  patterns computed from the local pause log: total pauses, back-out count,
  per-app breakdown (sorted by count), intention mix, time-of-day distribution
  (morning / afternoon / evening / night), and time-limit usage. All data
  is computed locally from Room; nothing leaves the device.
- **Bottom navigation bar** — three top-level destinations (Home / Reflection /
  Settings) are now accessible via a persistent bottom nav bar. Navigation
  between tabs preserves state.
- **`PauseEventDao.allEvents()`** — unbounded query returning all recorded
  pauses as a `Flow`; used by the reflection stats computation.
- **`getReflectionStats` use case** — pure Kotlin function in `domain/usecase/`;
  unit-tested (6 cases covering empty input, counts, app ranking, intentions,
  time-of-day buckets, and budget tracking).
- **Empty state** — "Nothing to look at yet." shown on the Reflection screen
  before any pauses are recorded, per `docs/ui-principles.md`.

### Changed
- Settings moved to a bottom-nav tab; the back button and the Settings icon
  in HomeScreen's top bar are removed (redundant with the new bottom nav).

### Notes
- No new permissions. No new dependencies. No analytics. `INTERNET` still
  not declared.
- Plural strings used for "X pause(s) recorded" and "You backed out X time(s)".

## [0.3.0] — 2026-05-10

Phase 3: intention logging, micro-session budgeting, and settings expansion.

### Added
- **Intention logging** — every pause (proceeded or backed out) is recorded
  locally in a Room database (`PauseEvent`: timestamp, package, intention
  key, budget, outcome). No data leaves the device.
- **Budget reminder notification** — when the user sets a time limit and
  proceeds, a single `AlarmManager.setAndAllowWhileIdle` fires after the
  chosen budget elapses and posts one calm notification ("Time's up —
  your X-minute limit for [App] has elapsed."). No sound, no badge, no
  follow-up. The notification channel is `IMPORTANCE_DEFAULT`.
- **`POST_NOTIFICATIONS` permission** — declared in the manifest; requested
  at runtime on Android 13+ only when the user picks a time budget.
  Documented in `docs/permissions-and-risks.md`. The app proceeds fully if
  the permission is denied — the reminder is optional.
- **Default budget setting** — users can pre-select their usual time limit
  (None / 3 / 5 / 10 min) in Settings. The pause flow pre-fills this on
  every open.
- **Wipe session log** — Settings → "Wipe session log" clears all
  `PauseEvent` rows after a confirmation dialog. App selection is kept.
- **`PauseEventRepository`** — wraps the DAO; instrumented tests cover
  insert, ordering, deleteAll, and field round-trip.
- Room 2.6.1 added (Apache 2.0). KSP 2.0.21-1.0.25 added as the
  annotation-processor plugin.

### Notes
- `AlarmManager.setAndAllowWhileIdle` requires no special permission and
  fires in the next Doze maintenance window — adequate for a soft reminder.
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` are deliberately not
  requested: exact timing is not needed for a soft reminder.
- `INTERNET` still not declared. No analytics, no crash reporters.

## [0.2.0] — 2026-05-09

Phase 2: app selection, intentional-launcher home screen, and pause flow.

### Added
- **Onboarding** — 3-screen flow shown on first launch; explains what the
  app does, what it does not do, and that all data stays on device.
- **App selection screen** — lists all installed launchable apps (via
  `PackageManager` + `<queries>` manifest entry; no `QUERY_ALL_PACKAGES`).
  Apps are toggled and persisted via DataStore (Preferences).
- **Home screen** — grid of selected apps; tapping an app tile enters the
  pause flow for that app. Empty state with a factual hint when no apps
  are selected.
- **Pause flow screen** — choose from three intentions ("I need this for
  one specific task", "I am checking something briefly", "I opened this
  automatically"), optionally choose a time limit (3 / 5 / 10 min or
  none), then proceed or back out. Proceeding launches the target app via
  an explicit `Intent`. The "Open the app" button is disabled until an
  intention is picked.
- **Settings screen** — stub with link to app-selection; further settings
  deferred to Phase 3.
- **Navigation** — single-activity Compose `NavHost` with routes:
  `onboarding`, `home`, `selection`, `pause/{packageName}`, `settings`.
- **Domain models**: `Intention` enum (stable wire keys), `SelectedApp`.
- **`ToggleAppSelectionUseCase`** — pure function; unit-tested.
- **Unit tests** for `ToggleAppSelectionUseCase` (5 cases).
- **Compose UI tests** for the pause-flow happy path (3 cases).
- Deps added (all Apache 2.0 / open-source): Navigation Compose 2.8.4,
  DataStore Preferences 1.1.1, Lifecycle Runtime Compose 2.8.7.

### Notes
- No new runtime permissions. The `<queries>` entry is a visibility
  declaration, not a runtime permission.
- No network access added. `INTERNET` is still not declared.
- No analytics, no crash reporting, no proprietary SDKs.

## [0.1.0] — 2026-05-09

Foundation release. No working app yet — documentation and skeleton only.

### Added
- Product brief, MVP scope, architecture proposal, permissions-and-risks
  memo, privacy principles, and UI principles in `docs/`.
- `README.md`, `CLAUDE.md`, `roadmap.md`, `CHANGELOG.md`.
- Single-module Android skeleton (`app/`) with Kotlin, Jetpack Compose,
  and Material 3; placeholder `MainActivity` confirming the build runs.
- Gradle version catalog at `gradle/libs.versions.toml`.
- `.gitignore` and basic ProGuard rules.
- GitHub Actions CI: lint, test, assembleDebug on every push and PR.

### Notes
- No runtime permissions, no special access, no `INTERNET` permission
  declared. No analytics, no third-party SDKs that talk off-device.
- Distribution target is F-Droid first; build is free of Google Play
  Services and other proprietary dependencies.
