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
      proceed or back out. *(The budget question was later removed in the
      0.7.0 design pass — see Phase 7. The pause now asks intention only.)*
- [x] Launch the target app via `Intent` after the pause completes.
- [x] Onboarding: 2–4 screens explaining what the app does.
- [x] One Compose UI test for the pause-flow happy path.
- [x] No new sensitive permissions.

Definition of done: a user can select three apps and open one of them
through l’entre-deux with a calm pause in the middle.

## Phase 3 — Intention logging and micro-session budgeting
**Target version:** 0.3.0.

> **Superseded in 0.7.0:** the micro-session budgeting half of this phase
> (time limits, the budget reminder notification, and the `POST_NOTIFICATIONS`
> permission) was removed in the Phase 7 design pass. Intention logging
> stays. See Phase 7.

- [x] Room schema for `PauseEvent` (id, ts, package, intention, budget,
      outcome). *(The `budget` column was dropped in 0.7.0 via a migration.)*
- [x] DAO and repository; unit tests for queries.
- [x] Pause flow records each event locally.
- [x] ~~Optional budget reminder via a one-shot local notification.~~
      *Removed in 0.7.0.*
- [x] Settings: ~~default budget,~~ wipe local data.

Definition of done: pause events are persisted privately.

## Phase 4 — Local reflection and weekly insights
**Target version:** 0.4.0.

- [x] Reflection screen: per-app counts, intention mix, time-of-day
      heatmap, ~~budget-set vs not,~~ back-out rate. *(Budget section
      removed in 0.7.0.)*
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

## Phase 7 — Design pass: calmer pause, discoverable pinning
**Target version:** 0.7.0 → 0.8.0.

A focused design pass after dogfooding revealed two problems: the pause
screen asked too much, and the home-screen pinning — the product's core
mechanic — was hidden behind a long-press.

- [x] Pause screen reduced to one question (intention) plus a breathing
      animation; proceed / back-out unchanged. (0.7.0)
- [x] Time limits / micro-session budgeting removed end to end (UI, the
      budget reminder, the alarm receiver, the default-budget setting, the
      Reflection time-limits section, and the `budgetMinutes` column via a
      Room v1→v2 migration that preserves existing history). (0.7.0)
- [x] `POST_NOTIFICATIONS` and the budget-alarm receiver dropped from the
      manifest. The app now requests zero runtime permissions. (0.7.0)
- [x] Visible "pin to home screen" button on every Home tile (local vector
      drawable, no new icon dependency); long-press menu kept as a fallback.
      (0.7.0)
- [x] Higher-fidelity pause visuals: layered breathing aura (glow + ripple
      rings + gradient core), gradient backdrop, kicker label, and the
      intentions as animated selectable cards with hints. (0.8.0)
- [x] One-time guided start that hand-holds new users through ① add an app
      and ② pin it to the home screen, with a pulsing highlight and a
      dismissible bottom card. (0.8.0)
- [x] Borderless intention cards; the back-out action reworded to "I'll
      leave it for now" and made a prominent button that backgrounds the app
      (returns to the launcher). (0.8.1)
- [x] Anti-autopilot variation: a random reflective phrase per pause, and a
      shuffled order for the four action buttons (the three intentions + the
      get-out button) so the screen can't be dismissed from muscle memory.
      The rest of the layout stays put. (0.9.0, refined in 0.9.1)
- [x] App-selection "Done" button so the user moves forward without reaching
      for Back; onboarding intro reworded around the in-between. (0.9.1)
- [x] English and French strings updated; privacy and permissions docs
      updated to reflect the reduced footprint.

Definition of done: the pause is a single calm step, and a first-time user
can see how to pin an app to their real home screen without discovering a
hidden gesture.

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

Phase 0–7 code work shipped, most recently the `0.9.x` anti-autopilot
variation (random reflective phrase + shuffled button order) with UX fixes
(app-selection "Done" button, reworded intro) and the `0.8.x`
polish (layered breathing animation, redesigned/ borderless pause cards, a
clearer "leave" action, and a one-time guided start) on top of the `0.7.0`
design pass. The only remaining item is the actual F-Droid submission,
which will be cut as `1.0.0` once the app is exercised on real devices.
