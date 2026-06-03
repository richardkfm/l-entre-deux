# CLAUDE.md

Working notes for Claude (and humans) when iterating on this repo.
Keep this file practical and compact. If it grows past a couple of screens,
trim it.

---

## Project mission

l’entre-deux is an open-source, offline-first Android app that helps people
stay off their phone by adding a short, respectful pause before a chosen
distracting app is opened. The pause asks the user to name their intention.
That is the whole product.

It is not a blocker. It is not a tracker. It is not a wellness gamification
app. See [`docs/product-brief.md`](docs/product-brief.md).

## Guardrails (non-negotiable)

- **Privacy-first, local-only.** No telemetry, no analytics, no accounts,
  no cloud sync, no `INTERNET` permission unless a feature is documented
  to need it. See [`docs/privacy-principles.md`](docs/privacy-principles.md).
- **F-Droid-first distribution.** No Google Play Services, no Firebase,
  no proprietary SDKs, no closed-source binaries. Build must remain
  reproducible and free of non-free dependencies. Anti-features must be
  declared honestly.
- **Minimal permissions.** Default manifest requests no runtime permissions
  and no special access. New permissions require updating
  [`docs/permissions-and-risks.md`](docs/permissions-and-risks.md) and
  [`docs/privacy-principles.md`](docs/privacy-principles.md) in the same
  PR.
- **No dark patterns.** No streaks, points, badges, comparisons, urgency
  language, or notification spam. See
  [`docs/ui-principles.md`](docs/ui-principles.md).
- **Adults treating adults as adults.** Friction, not coercion. The user
  can always proceed.

## Sensitive capability rules

These capabilities require explicit, deliberate decision in an issue or PR
discussion **before** any code:

- AccessibilityService
- `SYSTEM_ALERT_WINDOW` (overlays)
- `PACKAGE_USAGE_STATS`
- `NotificationListenerService`
- Device admin / DPC
- VPN service
- Any always-on foreground service
- Any new runtime permission

For each, see [`docs/permissions-and-risks.md`](docs/permissions-and-risks.md).
The current default is "no" for all of the above. AccessibilityService and
overlays are "not yet" — deferred to Phase X and only if the shortcut-based
interception proves insufficient for real users; all others remain Phase X
at the earliest, opt-in only, with plain-language disclosure and a hard
off-switch.

## Roadmap discipline

Roadmap lives in [`roadmap.md`](roadmap.md). Phases:

- **Phase 0** — planning and documentation.
- **Phase 1** — project skeleton and architecture setup.
- **Phase 2** — app selection + pause-flow prototype.
- **Phase 3** — intention logging and micro-session budgeting.
- **Phase 4** — local reflection and weekly insights.
- **Phase 5** — accessibility, testing, polish, release prep.
- **Phase X** — optional advanced enforcement / sensitive capabilities.

Rules:
- Build phases in order. Do not start Phase N+1 until Phase N is shippable.
- Each phase ends in a usable, testable artifact, not a half-implementation.
- Skip Phase X by default.

## Versioning rules

Semantic versioning, starting at `0.1.0` after this Phase 0/1 commit.

- **patch** (`0.1.x`): fixes, refactors, docs-only changes, internal cleanup.
- **minor** (`0.x.0`): every new user-visible feature.
- **major** (`x.0.0`): breaking architecture or product changes; reserved
  for `1.0.0` (first F-Droid release with full MVP) and beyond.

Whenever a feature lands:

1. Bump the version in `app/build.gradle.kts` (`versionName`, `versionCode`).
2. Add an entry to [`CHANGELOG.md`](CHANGELOG.md) under a dated section.
3. Update [`roadmap.md`](roadmap.md) progress checkboxes.
4. Update the **Current status** section of this file.

These four steps are part of the same PR. Reviewers should refuse PRs that
add user-visible behavior without doing them.

## Repository map

```
README.md                  public-facing intro
CLAUDE.md                  this file
CHANGELOG.md               semver changelog
roadmap.md                 phased delivery + status
LICENSE                    GPL-3.0
docs/
  product-brief.md
  mvp-scope.md
  architecture.md
  permissions-and-risks.md
  privacy-principles.md
  ui-principles.md
app/                       single Android module (Phase 1+)
gradle/                    wrapper + libs.versions.toml
.github/workflows/         CI
```

## Coding principles

- **Plain Android.** Kotlin, Compose, Room, DataStore, AndroidX ViewModel.
  Nothing exotic.
- **Single module** (`:app`) until splitting solves a real problem.
- **No DI framework yet.** Manual constructor injection. Hilt only when
  manual wiring is genuinely painful.
- **No WorkManager yet.** Foreground only.
- **Domain logic is pure Kotlin.** Anything in `domain/` must not import
  `android.*`. Use cases are small, testable, one per file.
- **Strings live in `strings.xml`** from the first screen. No literal UI
  strings in composables.
- **No comments explaining what code does.** Comments only for non-obvious
  *why* (a hidden constraint, a workaround, a subtle invariant).
- **No premature abstraction.** Three similar lines beat a clever helper.
- **No dead code, no TODO graveyards.** Delete or file an issue.
- **Tests where they earn their keep:** unit tests for domain logic,
  one Compose UI test for the pause-flow happy path. Don’t test getters.
- **Dependencies are visible.** Add to `gradle/libs.versions.toml` with a
  one-line justification in the PR description. No surprise transitive
  brand-new SDKs.

## Session workflow

When working in this repo (whether human or Claude):

1. **Re-read the relevant doc first.**
   - Touching the pause flow or selection? Re-skim
     [`docs/ui-principles.md`](docs/ui-principles.md) and
     [`docs/mvp-scope.md`](docs/mvp-scope.md).
   - Adding any permission, manifest entry, dependency that talks to a
     network, or anything in `app/src/main/java/.../data/`? Re-skim
     [`docs/privacy-principles.md`](docs/privacy-principles.md) and
     [`docs/permissions-and-risks.md`](docs/permissions-and-risks.md).
2. **Plan in the PR description**, not in the code. State what changes,
   what doesn’t, and why this is the smallest version of the change.
3. **Stay inside the current phase.** If a desire pulls toward a future
   phase, write it down in `roadmap.md` instead of building it.
4. **Update the four files** when shipping a feature: version,
   CHANGELOG, roadmap, this file’s Current status.
5. **Run before declaring done:** `./gradlew lint test assembleDebug`.

## Build

Requirements: JDK 17+, Android SDK platform 35.

```sh
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

## Current status

**Version:** 0.9.3
**Phase:** Phase 0–7 (code work shipped; F-Droid submission as `1.0.0`
is the only remaining item).
**Last updated:** 2026-06-03.

What exists:
- Full documentation set in `docs/`.
- README, CLAUDE.md, roadmap, CHANGELOG.
- Single-module Android app (`app/`) with Compose, Material 3, Navigation
  Compose, DataStore Preferences, Room.
- Onboarding (3 screens, shown once).
- App selection screen: lists installed launchable apps, persists
  selection via DataStore as toggled; a bottom "Done" button carries the
  user forward (Back also works).
- Home screen: grid of selected apps; tapping opens the pause flow. A
  one-time guided coach (`CoachStep`) walks new users through ① adding an
  app and ② pinning it to the home screen, with a pulsing highlight on the
  relevant control and a dismissible bottom card. Tracked by the
  `home_coach_done` preference.
- Pause flow: one calm single-view screen (never scrolls — the aura flexes
  to fill the space left by the fixed elements, so everything always fits) —
  a randomly chosen reflective line (`pause_phrases`), a breathing aura of
  ~84 dots in a phyllotaxis spread that together form a slowly rotating whole
  (global spin + per-dot epicycles + an outward brightness wave, all on
  whole-number cycles so the loop is seamless), the heading, and the four
  action buttons.
  The buttons are uniform single-line pills — the three intentions plus the
  "I'll leave it for now" get-out button — shuffled into a random order each
  pause to resist autopilot (both kinds stay clearly labelled, so it's not a
  dark pattern). Tapping an intention is the act of proceeding: it logs the
  choice and launches the target app via explicit Intent (no separate Open
  button). Leaving sends the app to the background (`moveTaskToBack`) so the
  user returns to their launcher. Every pause (proceeded or backed out) is
  logged to Room. No time-limit question, no notifications.
- Settings screen: manage apps, wipe session log.
- Reflection screen: per-app counts, intention mix, time-of-day
  distribution, back-out count. All computed locally from Room. No
  scores, no streaks. Empty state when no data.
- Bottom navigation bar: Home / Reflection / Settings.
- Accessibility pass: `heading()` semantics on titles, `Role.Button` on
  clickable list rows, full-row `toggleable` on the app-selection list,
  48dp minimum touch targets, proper content descriptions on icon-only
  actions (including the per-tile pin button).
- Localization: English (default) and French (`values-fr/strings.xml`).
  `resourceConfigurations` pins the shipped locale set.
- Adaptive app icon (vector foreground + background color + monochrome
  themed-icon variant). No raster icons; `minSdk = 26` covers adaptive.
- R8 + resource shrinker enabled for `release`; consumer rules from
  Compose / AndroidX / Room are sufficient. `BuildConfig` disabled.
- F-Droid metadata under `fastlane/metadata/android/{en-US,fr-FR}/`
  with title, short and full descriptions, and per-version changelogs.
- Home-screen shortcut pinning (Phase 6): every home tile shows a visible
  pin button (long-press still opens the same action as a fallback) →
  `ShortcutManager.requestPinShortcut()` pins an icon that looks like the
  target app but routes every tap through the pause flow. No new
  permissions. Works on any launcher that supports pinned shortcuts (all
  major ones do). `MainActivity` is `singleTop` and handles `onNewIntent`
  so re-tapping a shortcut while the app is already open navigates
  correctly.
- Domain models (`Intention`, `SelectedApp`, `PauseEvent`, `PauseOutcome`,
  `ReflectionStats` and helpers), pure use cases (`toggleAppSelection`,
  `getReflectionStats`), repositories (`InstalledAppsRepository`,
  `AppSelectionRepository`, `PauseEventRepository`, `ShortcutRepository`).
- Unit tests for use cases; instrumented tests for Room DAO and pause flow UI.
- Gradle version catalog (`gradle/libs.versions.toml`).
- GitHub Actions workflow running lint + test + assembleDebug.

What is intentionally missing:
- Any sensitive capability — deferred indefinitely; see
  [`docs/permissions-and-risks.md`](docs/permissions-and-risks.md).
- F-Droid submission. Listed as the final Phase 5 item; will land
  alongside the `1.0.0` tag once the app is exercised on real devices.

Next: **F-Droid submission as `1.0.0`**.
