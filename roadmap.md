# Roadmap

l’entre-deux is built in deliberate phases. Each phase ends in a usable
artifact, even if small. We do not start Phase N+1 until Phase N is
shippable. Versioning rules are in [`CLAUDE.md`](CLAUDE.md).

Status legend: `[x]` done · `[~]` in progress · `[ ]` not started.

---

## Phase 0 — Planning and documentation
**Target version:** 0.1.0 (foundation tag, shared with Phase 1).

- [x] Product brief (`docs/product-brief.md`)
- [x] MVP scope (`docs/mvp-scope.md`)
- [x] Architecture proposal (`docs/architecture.md`)
- [x] Permissions and risks memo (`docs/permissions-and-risks.md`)
- [x] Privacy principles (`docs/privacy-principles.md`)
- [x] UI principles (`docs/ui-principles.md`)
- [x] README, CLAUDE.md, CHANGELOG, this roadmap.

## Phase 1 — Project skeleton and architecture setup
**Target version:** 0.1.0.

- [x] Single Android module `:app`.
- [x] Kotlin + Jetpack Compose + Material 3.
- [x] Gradle version catalog (`gradle/libs.versions.toml`).
- [x] `EntreDeuxApplication`, `MainActivity`, theme files.
- [x] Placeholder home screen confirming the build runs.
- [x] `.gitignore`, ProGuard rules placeholder.
- [x] CI workflow: lint + test + assembleDebug.
- [x] No sensitive permissions in the manifest.

## Phase 2 — App selection and pause-flow prototype
**Target version:** 0.2.0.

- [x] App selection screen using `PackageManager` to enumerate launchable
      apps (no `QUERY_ALL_PACKAGES`; rely on `<queries>` where possible).
- [x] Persist the selection (DataStore for the set; Room introduced if
      richer data is needed).
- [x] Launcher grid screen on Home showing selected apps as tiles.
- [x] Pause flow screen: choose intention, optionally choose budget,
      proceed or back out.
- [x] Launch the target app via `Intent` after the pause completes.
- [x] Onboarding: 2–4 screens explaining what the app does.
- [x] One Compose UI test for the pause-flow happy path.
- [x] No new sensitive permissions.

Definition of done: a user can select three apps and open one of them
through l’entre-deux with a calm pause in the middle.

## Phase 3 — Intention logging and micro-session budgeting
**Target version:** 0.3.0.

- [x] Room schema for `PauseEvent` (id, ts, package, intention, budget,
      outcome).
- [x] DAO and repository; unit tests for queries.
- [x] Pause flow records each event locally.
- [x] Optional budget reminder via a one-shot local notification when the
      chosen budget elapses. Single channel, no badges, no follow-ups.
- [x] Settings: default budget, wipe local data.

Definition of done: pause events are persisted privately; setting a
budget produces a single calm reminder.

## Phase 4 — Local reflection and weekly insights
**Target version:** 0.4.0.

- [x] Reflection screen: per-app counts, intention mix, time-of-day
      heatmap, budget-set vs not, back-out rate.
- [x] All summaries computed locally from Room. No streaks, no scores.
- [x] Empty state copy that matches `docs/ui-principles.md`.

Definition of done: a user with a week of data sees plain-language
patterns about themselves.

## Phase 5 — Accessibility, testing, polish, release prep
**Target version:** 0.5.0 → 1.0.0 (when stable).

- [x] Accessibility audit: 48dp targets, font scaling, screen-reader
      flow on the pause screen.
- [x] Localization scaffolding; French translation as first target.
- [x] App icon and visual identity (adaptive icon with monochrome
      themed-icon variant).
- [x] R8 / shrinker configured; reproducible Gradle build verified.
- [x] F-Droid metadata under `fastlane/metadata/android/`.
- [ ] First F-Droid submission as v1.0.0.

## Phase 6 — Home-screen shortcut pinning
**Target version:** 0.6.0.

- [x] `ShortcutRepository` wraps `ShortcutManager.requestPinShortcut()`.
      Fetches the target app's icon from PackageManager and builds a
      `ShortcutInfo` whose intent routes through our pause flow. No new
      permissions needed.
- [x] Long-press on any Home tile reveals an "Add to home screen" context
      menu item. Snackbar confirms success or reports that the launcher
      does not support pinned shortcuts.
- [x] `MainActivity` set to `singleTop`; `onNewIntent` parses the custom
      action and passes a `ShortcutRequest` (with a unique timestamp id)
      to `AppNavHost` via Compose state.
- [x] `AppNavHost` navigates directly to `pause/{packageName}` on each
      incoming `ShortcutRequest` via `LaunchedEffect`.
- [x] English and French strings for all new copy.

Definition of done: a user can long-press an app tile, pin the shortcut
to their home screen, then tap that shortcut and land on the pause flow
for that app — no new permissions, no launcher replacement.

## Phase X — Optional advanced or sensitive capabilities
**Default: not pursued. Every item here requires a public decision first.**

### Why this phase might happen

The shortcut-based interception (Option A + G) only catches taps on
home-screen icons the user has explicitly replaced. Launches from
notifications, deep links, or the recents switcher are not caught. If real
users tell us this misses too many autopilot reaches, we may pursue one of
the options below.

### Under active consideration (if shortcut approach proves insufficient)

- [ ] **Opt-in AccessibilityService path** — listens for foreground app
      changes and shows the pause flow before the target app is usable.
      Most reliable real-time coverage. Highest trust cost. Requires a
      separate opt-in build flavor, prominent plain-language disclosure,
      a hard off-switch, and an F-Droid Anti-Feature declaration. See
      `docs/permissions-and-risks.md` Option C for full conditions.

### Lower priority

- [ ] Opt-in `UsageStatsManager` + `SYSTEM_ALERT_WINDOW` overlay path —
      technically weaker than AccessibilityService (reactive, not
      proactive); included only as a lighter-weight intermediate option.
      Same disclosure requirements apply.

### Excluded

- NotificationListenerService — reads all notifications; not relevant to
  launch interception.
- Device admin / Device Policy Controller — parental control territory.
- Any cloud sync, account, or telemetry — ever.

Adding any item from this phase requires updating the privacy and
permissions docs, and a corresponding F-Droid Anti-Feature declaration.

---

## Current status

Phase 0–5 code work shipped at `0.5.0`. The only remaining Phase 5
item is the actual F-Droid submission, which will be cut as `1.0.0`
once the app is exercised on real devices.
