# l’entre-deux

> A small, calm Android app that helps you stay off your phone — by
> interrupting autopilot **before** a distracting app is opened.

l’entre-deux (French for "the in-between") is the moment between reaching
for your phone and actually using it. This app lives in that moment.
Instead of blocking apps, it adds a short, respectful pause that asks you
to name your intention. Then it gets out of the way.

**Status:** v0.3.0 — Phases 0–3 complete. Onboarding, app selection,
pause flow, intention logging, and budget reminders all work.
Reflection screen coming in Phase 4. See [`roadmap.md`](roadmap.md).

**License:** [GPL-3.0](LICENSE).
**Distribution target:** F-Droid first.

---

## What it does (target MVP)

1. You pick the apps that pull you in.
2. When you go to open one of them through l’entre-deux, you see a short
   pause screen.
3. You tap an intention: *I need this for one specific task* / *I am
   checking something briefly* / *I opened this automatically.*
4. Optionally, pick a tiny session budget (3 / 5 / 10 minutes).
5. You proceed.
6. Later, a private, on-device reflection screen shows your patterns. No
   scores, no streaks.

## What it is not

- Not a blocker. It does not lock you out of your phone.
- Not a parental-control app.
- Not a surveillance tool. It does not read other apps’ content,
  notifications, screen, or network.
- Not a streak / badge / points wellness app.
- Not commercial. No ads, no upsells, no accounts.

## Privacy and trust

- **Local-only.** No accounts, no sync, no telemetry. The app does not
  request the `INTERNET` permission.
- **No proprietary SDKs.** No Google Play Services, no Firebase, no
  analytics, no crash reporting service.
- **Minimal permissions.** `POST_NOTIFICATIONS` is the only runtime
  permission — requested on Android 13+ only when you set a time budget,
  and fully optional. No special access. See
  [`docs/permissions-and-risks.md`](docs/permissions-and-risks.md).
- **Open source under GPL-3.0**, so you can verify the claims above
  yourself.

Read the full commitments in [`docs/privacy-principles.md`](docs/privacy-principles.md).

## How it intercepts app opens

Honestly, mostly: by being the thing you tap. The MVP uses an in-app
launcher grid plus optional pinned home-screen shortcuts. It does **not**
use AccessibilityService, an overlay, or `UsageStatsManager`. We chose the
least invasive path even though it covers fewer launch paths, because
trust matters more than coverage. Trade-offs are documented in
[`docs/permissions-and-risks.md`](docs/permissions-and-risks.md).

## Repository map

```
README.md                        — you are here
CLAUDE.md                        — working notes for AI assistants and contributors
CHANGELOG.md                     — semver changes
roadmap.md                       — phased delivery plan and current status
LICENSE                          — GPL-3.0
docs/
  product-brief.md               — what the app is and is not
  mvp-scope.md                   — sharply scoped MVP definition
  architecture.md                — single-module Android architecture
  permissions-and-risks.md       — evaluation of detection/interception options
  privacy-principles.md          — hard commitments
  ui-principles.md               — calm-by-default design rules
app/                             — Android app module (Phase 1+)
gradle/                          — Gradle wrapper and version catalog
```

## Build

Requirements: JDK 17+, Android SDK with platform 35.

```sh
./gradlew assembleDebug   # build a debug APK
./gradlew test            # run unit tests
./gradlew lint            # run Android lint
```

CI runs the same three commands.

## Contributing

This project is small on purpose. Before opening a PR, please skim:

1. [`docs/product-brief.md`](docs/product-brief.md) — what we are building.
2. [`docs/mvp-scope.md`](docs/mvp-scope.md) — what is and isn’t in scope now.
3. [`docs/privacy-principles.md`](docs/privacy-principles.md) — the
   non-negotiables.
4. [`CLAUDE.md`](CLAUDE.md) — coding principles, versioning rules, and the
   session workflow we follow when iterating.

Issues and PRs that materially expand the app’s data collection,
permissions, or off-device communication will be held to a high bar and
must update the privacy and permissions docs in the same change.

## Versioning

Semantic versioning. See [`CHANGELOG.md`](CHANGELOG.md) and [`roadmap.md`](roadmap.md).
