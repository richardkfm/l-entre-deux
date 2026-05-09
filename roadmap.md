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

- [ ] App selection screen using `PackageManager` to enumerate launchable
      apps (no `QUERY_ALL_PACKAGES`; rely on `<queries>` where possible).
- [ ] Persist the selection (DataStore for the set; Room introduced if
      richer data is needed).
- [ ] Launcher grid screen on Home showing selected apps as tiles.
- [ ] Pause flow screen: choose intention, optionally choose budget,
      proceed or back out.
- [ ] Launch the target app via `Intent` after the pause completes.
- [ ] Onboarding: 2–4 screens explaining what the app does.
- [ ] One Compose UI test for the pause-flow happy path.
- [ ] No new sensitive permissions.

Definition of done: a user can select three apps and open one of them
through l’entre-deux with a calm pause in the middle.

## Phase 3 — Intention logging and micro-session budgeting
**Target version:** 0.3.0.

- [ ] Room schema for `PauseEvent` (id, ts, package, intention, budget,
      outcome).
- [ ] DAO and repository; unit tests for queries.
- [ ] Pause flow records each event locally.
- [ ] Optional budget reminder via a one-shot local notification when the
      chosen budget elapses. Single channel, no badges, no follow-ups.
- [ ] Settings: default budget, wipe local data.

Definition of done: pause events are persisted privately; setting a
budget produces a single calm reminder.

## Phase 4 — Local reflection and weekly insights
**Target version:** 0.4.0.

- [ ] Reflection screen: per-app counts, intention mix, time-of-day
      heatmap, budget-set vs not, back-out rate.
- [ ] All summaries computed locally from Room. No streaks, no scores.
- [ ] Empty state copy that matches `docs/ui-principles.md`.

Definition of done: a user with a week of data sees plain-language
patterns about themselves.

## Phase 5 — Accessibility, testing, polish, release prep
**Target version:** 0.5.0 → 1.0.0 (when stable).

- [ ] Accessibility audit: 48dp targets, font scaling, screen-reader
      flow on the pause screen.
- [ ] Localization scaffolding; French translation as first target.
- [ ] App icon and visual identity.
- [ ] R8 / shrinker configured; reproducible Gradle build verified.
- [ ] F-Droid metadata under `fastlane/metadata/android/`.
- [ ] First F-Droid submission as v1.0.0.

## Phase X — Optional advanced or sensitive capabilities
**Default: not pursued.**

If, and only if, real users tell us the in-app launcher pattern doesn’t
catch enough autopilot reaches, we may evaluate:

- [ ] Opt-in `UsageStatsManager` + `SYSTEM_ALERT_WINDOW` overlay path,
      with clear in-app disclosure and an off-switch. See
      `docs/permissions-and-risks.md`.

Capabilities explicitly excluded indefinitely:

- AccessibilityService for non-accessibility purposes.
- NotificationListenerService.
- Device admin.
- Any cloud sync, account, or telemetry.

Adding any item from this phase requires updating the privacy and
permissions docs, and a corresponding F-Droid Anti-Feature declaration.

---

## Current status

Phase 0 and Phase 1 complete at `0.1.0`. Next prompt should kick off
Phase 2.
