# Changelog

All notable changes to l’entre-deux are recorded here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning is [Semantic Versioning](https://semver.org/), with the
project-specific rules described in [`CLAUDE.md`](CLAUDE.md).

## [Unreleased]

Nothing yet.

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
