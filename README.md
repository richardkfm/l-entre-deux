         __,    __,
        /. )   /. )
       (   |  (   |          l'entre-deux
       (   |  (   |
       (   |  (   |
        \_/    \_/


l’entre-deux (French for "the in-between") is the moment between reaching
for your phone and actually using it. This app lives in that moment.
Instead of blocking apps, it adds a short, respectful pause that asks you
to name your intention. Then it gets out of the way.

**Status:** v0.9.3 — Phases 0–7 code work complete. Onboarding, a guided
first run, app selection, a calm one-question pause flow with an organic
dot-field breathing animation and a rotating reflective line, intention
logging, on-device reflection, accessibility pass, French localization,
adaptive icon, R8, F-Droid metadata, and home-screen shortcut pinning all in
place. F-Droid submission as `1.0.0` is the only remaining item. See
[`roadmap.md`](roadmap.md).

**License:** [GPL-3.0](LICENSE).
**Distribution target:** F-Droid first.
**Languages:** English, French.

---

## What it does (target MVP)

1. You pick the apps that pull you in.
2. When you go to open one of them through l’entre-deux, you see a short
   pause screen.
3. You tap an intention: *I need this for one specific task* / *I am
   checking something briefly* / *I opened this automatically.*
4. You proceed.
5. Later, a private, on-device reflection screen shows your patterns. No
   scores, no streaks.

## What it is not

- Not a blocker. It does not lock you out of your phone.
- Not a parental-control app.
- Not a surveillance tool. It does not read other apps’ content,
  notifications, screen, or network.
- Not a streak / badge / points wellness app.
- Not commercial. No ads, no upsells, no accounts.

## How this differs from Google’s Pause Point

Android 17 ships *Pause Point* as part of Google’s Digital Wellbeing. It,
too, puts a moment of friction in front of a distracting app, so the
overlap is real. The difference is what that moment asks of you.

Google’s wellbeing tools are built to **measure and limit**: screen-time
dashboards, app timers that grey an app out, scheduled focus modes. They
answer the question *how long*.

l’entre-deux asks a different question: *why*. The pause has one job — to
let you **name your intention** before you go in (*I need this for one
specific task* / *I am checking something briefly* / *I opened this
automatically*). We never show a time-spent score, never lock you out, and
never measure you against a goal, a streak, or anyone else.

That single shift — from *how long* to *why* — is the whole product.
Everything else (local-only data, no accounts, F-Droid distribution, an
always-visible way to proceed) follows from treating you as an adult making
your own choices, not a usage statistic to be corrected.

## Privacy and trust

- **Local-only.** No accounts, no sync, no telemetry. The app does not
  request the `INTERNET` permission.
- **No proprietary SDKs.** No Google Play Services, no Firebase, no
  analytics, no crash reporting service.
- **Minimal permissions.** The app requests no runtime permissions at all
  and no special access. See
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

## Install

### Download a pre-built APK (easiest)

Each [GitHub release](https://github.com/richardkfm/l-entre-deux/releases)
includes a release APK you can sideload directly onto your device.

1. Go to the [Releases page](https://github.com/richardkfm/l-entre-deux/releases)
   and download the latest `l-entre-deux-*.apk`.
2. On your Android device, enable **Install unknown apps** for your file
   manager or browser (Settings → Apps → Special app access).
3. Open the APK file on your device and tap **Install**.

> **Note:** The production release will also be distributed via **F-Droid**
> once submission is complete, with automatic updates and no sideloading needed.

### F-Droid (coming soon)

F-Droid submission is the final remaining step before `1.0.0`. Once listed,
installation will be a single tap with automatic updates, no sideloading
needed.

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
