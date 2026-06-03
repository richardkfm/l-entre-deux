# Changelog

All notable changes to l’entre-deux are recorded here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning is [Semantic Versioning](https://semver.org/), with the
project-specific rules described in [`CLAUDE.md`](CLAUDE.md).

## [Unreleased]

Nothing yet.

## [0.9.3] — 2026-06-03

Pause-screen micro-polish.

### Changed
- **Shorter button copy.** Dropped the repetitive leading "I" from every
  choice — "I opened this automatically" → "Opened this automatically", and
  likewise for the others and the get-out button ("Leave it for now").
- **More breathing room** between the reflective line and the animation.
- **Calmer dot field.** Smaller dots, and each now follows two superimposed
  (counter-rotating) epicycles instead of one, so the motion flows more
  naturally and organically. Still fully seamless.

## [0.9.2] — 2026-06-03

Pause-screen bug fix and polish.

### Fixed
- **Buttons could be clipped / the proceed button went missing.** The screen
  content (large aura + four buttons + a separate Open button) overflowed on
  some devices, cutting off the last option and pushing "Open" off-screen.
  The breathing aura now flexes to fill whatever vertical space is left after
  the fixed elements (phrase, heading, the four buttons), so the whole screen
  always fits in a single view — no scrolling, nothing clipped.

### Changed
- **Tapping an intention now opens the app directly** — the separate "Open"
  button is gone. Naming the intention *is* the act of proceeding (one tap),
  which is simpler and removes the overflow.
- **Intention subtitles removed.** Each intention is a single calm line; the
  one-line hints are gone, leaving less to read and a quieter screen.
- All four choices are now uniform pill buttons (three intentions + the
  get-out button), still shuffled in order each pause.
- **Richer, seamless breathing animation.** The dot field now reads as one
  slowly turning whole — the structure rotates like a galaxy, each dot rides
  its own small epicycle, and a brightness wave ripples outward. All motion
  runs on whole-number cycles, so the loop no longer visibly jumps back.

### Notes
- A back-out carries no intention; it's logged with an empty intention key
  (no schema change), so the reflection back-out and per-app totals still
  count it while the intention mix correctly ignores it.

## [0.9.1] — 2026-06-03

Three UX fixes.

### Added
- **"Done" button on the app-selection screen.** Selections already persist
  as you toggle them, but the only way out was the Back arrow — easy to miss.
  A clear bottom "Done" button now carries the user forward.

### Changed
- **Onboarding intro reworded.** "A small pause" → "A moment in between",
  with body copy that leans into the in-between / intention idea rather than
  the word "pause".
- **Pause screen: stable layout, shuffled buttons.** Reverted 0.9.0's
  whole-layout shuffle — every element (phrase, breathing aura, heading,
  prompt) now stays put. The anti-autopilot effect comes from shuffling just
  the four action buttons instead: the three intention choices and the
  get-out button appear in a random order each pause. The Open button stays
  in its fixed spot. Both proceeding and leaving remain clearly labelled.
- **New breathing animation.** The aura is now a living field of ~76 small
  dots arranged in a sunflower (phyllotaxis) spread. They breathe outward
  and back together while each drifts on its own gentle orbit and softly
  twinkles, so no two frames look quite alike — calming to rest your eyes
  on rather than a flat pulse.

## [0.9.0] — 2026-06-02

The pause is a little different each time — on purpose, to defeat autopilot.

### Added
- **Rotating reflective line.** The fixed "A small pause" kicker is replaced
  by one of several short, calm phrases about time, focus, and the present
  moment, chosen at random for each pause (`pause_phrases` string-array, en
  and fr). Kept deliberately gentle — no urgency, scarcity, or guilt, per
  `docs/ui-principles.md`.
- **Shuffled layout.** Each pause randomly picks one of three arrangements,
  moving the breathing aura, the intention cards, and — crucially — the
  **Open** and **leave** buttons (including their order) to different spots.
  You can't dismiss the pause from muscle memory; you have to actually look.
  Both actions stay clearly labelled, so this disrupts autopilot without
  becoming a dark pattern.

### Notes
- The chosen phrase and layout are fixed for the duration of a single pause
  (they survive rotation) and re-roll the next time you open one.
- No new permissions, no new dependencies.

## [0.8.1] — 2026-06-02

Pause-screen refinements.

### Changed
- **Intention cards are now borderless**, in line with Material's filled
  style: selection is shown by the container colour and the selection dot,
  not an outline.
- **The back-out action is clearer and more prominent.** "Not now" (easily
  misread as "don't ask me now") becomes "I'll leave it for now" — a full
  width tonal button that's always present. Tapping it now sends the app to
  the background so the user lands back on their home screen, rather than
  dropping them onto l'entre-deux's own grid.

### Notes
- The app still requests no special permissions. Actually powering the
  screen off would require device-admin access, which we deliberately don't
  use (see `docs/permissions-and-risks.md`); `moveTaskToBack` is the
  least-invasive equivalent.

## [0.8.0] — 2026-06-02

Design polish: a richer pause, a more expressive breathing animation, and a
first-run guide that takes new users by the hand.

### Added
- **Guided start.** A one-time, two-step coach on the Home screen walks new
  users through the core flow: ① add an app (the **+** button pulses), then
  ② pin it to your home screen (the pin button on the first tile pulses). A
  bottom card explains each step; the guide is dismissible and shown once,
  tracked by a new local `home_coach_done` preference. It advances on its own
  once an app is added.
- New `CoachStep` state on `HomeViewModel`, derived from whether the guide is
  still pending and whether any app is selected, plus `dismissCoach()`.

### Changed
- **Breathing animation.** The flat two-circle pulse is replaced by a layered
  aura drawn on a `Canvas`: a soft radial glow that swells with the breath,
  three phase-shifted ripple rings rising and fading, and a gradient core orb
  — a calmer, more premium focal point.
- **Pause screen redesign.** A soft gradient backdrop, a small kicker label, a
  larger heading, and the three intentions presented as animated selectable
  cards (each with a one-line hint and an animated selection dot). The Open
  button now names the app and animates in once an intention is chosen.

### Notes
- No new permissions, no new dependencies. The pin glyph and animations use
  only stock Compose APIs.

## [0.7.0] — 2026-06-02

Design pass: a calmer pause and a discoverable killer feature. Removes the
time-limit/budget feature — and with it the `POST_NOTIFICATIONS` permission.

### Added
- **Visible "pin to home screen" button on every Home tile.** Pinning an app
  through the pause is the core of the product, so it no longer hides behind a
  long-press. Each tile now shows a pin icon (a local vector drawable, no new
  icon dependency) that calls the existing `ShortcutRepository`. The long-press
  menu is kept as a secondary affordance.
- **Breathing animation on the pause screen.** A slow expand/contract circle
  (`rememberInfiniteTransition`, ~4s reverse loop) gives the moment a calming
  focal point. Purely decorative — the screen works the same with motion
  reduced or ignored.

### Changed
- **The pause screen now asks one question, not two.** It shows the breathing
  circle, the intention prompt, the three intention choices, and Open / Not
  now. The per-pause time-limit chooser is gone.
- `PauseScreen` / `PauseViewModel` no longer depend on the budget scheduler,
  the default-budget preference, or the notification permission launcher.
- Home tiles cap the label at two lines to make room for the pin button.

### Removed
- **Time limits / micro-session budgeting, end to end.** Deleted
  `BudgetNotificationScheduler`, `BudgetAlarmReceiver`, and `NotificationHelper`;
  removed the "Default time limit" setting, the Reflection "Time limits"
  section, the `default_budget_minutes` preference, and the `budgetMinutes`
  column from the `PauseEvent` log.
- **`POST_NOTIFICATIONS` permission** and the budget-alarm `<receiver>` are no
  longer declared. The app now requests no runtime permissions at all. See the
  updated `docs/permissions-and-risks.md` and `docs/privacy-principles.md`.

### Migration
- Room database bumped to version 2 with `MIGRATION_1_2`, which recreates the
  `pause_events` table without `budgetMinutes`. Existing reflection history is
  preserved across the upgrade.

### Notes
- No new dependencies. No analytics. `INTERNET` still not declared, and the
  manifest now declares zero runtime permissions.

## [0.6.0] — 2026-05-11

Phase 6: home-screen shortcut pinning.

### Added
- **Home-screen shortcuts.** Long-pressing any app tile on the Home screen
  now shows an "Add to home screen" option. Tapping it calls
  `ShortcutManager.requestPinShortcut()`, which asks the launcher to pin
  a shortcut that looks like the target app (same icon, same label) but
  routes every tap through l'entre-deux's pause flow first. No new
  permissions are required — pinned shortcuts are a normal Android API
  available since API 26 (our `minSdk`).
- `ShortcutRepository` (`data/shortcuts/`) wraps ShortcutManager: checks
  support, fetches the target app's icon via PackageManager, builds the
  ShortcutInfo, and fires the pin request.
- `ShortcutRequest` data class in `ui/AppNavHost.kt` carries a unique
  timestamp-based id so repeated shortcut taps each trigger a navigation
  even when the package name is unchanged.
- `MainActivity` now handles `onNewIntent` (activity is `singleTop`) and
  parses the `org.entredeux.app.action.PAUSE_LAUNCH` intent action to
  route directly to the pause flow for the shortcut's target app.
- Snackbar feedback on the Home screen after a pin request: success
  message ("Shortcut requested — check your home screen") or an
  unsupported-launcher message.
- English and French strings for all new UI copy.

### Changed
- `MainActivity` launch mode set to `singleTop` in the manifest so
  tapping a shortcut while the app is already in the foreground calls
  `onNewIntent` rather than creating a second instance.
- `HomeViewModel` now takes `ShortcutRepository` and exposes
  `requestPinShortcut()` / `clearShortcutResult()`.
- `HomeScreen` adds a `SnackbarHost` and uses `combinedClickable` on
  each tile for the long-press context menu.
- `AppNavHost` accepts `shortcutRequest` / `onShortcutHandled` and uses
  `LaunchedEffect` to navigate to the pause screen on shortcut launch.

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
