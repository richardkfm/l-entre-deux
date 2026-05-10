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
The current default is "no" for all of the above. AccessibilityService is
effectively never; others are Phase 5+ at the earliest, opt-in only, with
plain-language disclosure and a hard off-switch.

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

**Version:** 0.2.0
**Phase:** Phase 0, 1, and 2 complete.
**Last updated:** 2026-05-09.

What exists:
- Full documentation set in `docs/`.
- README, CLAUDE.md, roadmap, CHANGELOG.
- Single-module Android app (`app/`) with Compose, Material 3, Navigation
  Compose, DataStore Preferences.
- Onboarding (3 screens, shown once).
- App selection screen: lists installed launchable apps, persists
  selection via DataStore.
- Home screen: grid of selected apps; tapping opens the pause flow.
- Pause flow: choose intention → optionally choose budget → proceed or
  back out → target app launches via explicit Intent.
- Settings screen stub.
- Domain models (`Intention`, `SelectedApp`), pure use case
  (`toggleAppSelection`), repositories (`InstalledAppsRepository`,
  `AppSelectionRepository`).
- Unit tests for the toggle use case; Compose UI tests for pause flow.
- Gradle version catalog (`gradle/libs.versions.toml`).
- GitHub Actions workflow running lint + test + assembleDebug.

What is intentionally missing:
- Intention logging (Room + PauseEvent) — Phase 3.
- Budget reminder notification — Phase 3.
- Reflection screen — Phase 4.
- Any sensitive capability — deferred indefinitely; see
  [`docs/permissions-and-risks.md`](docs/permissions-and-risks.md).

Next: **Phase 3 — Intention logging and micro-session budgeting**.
