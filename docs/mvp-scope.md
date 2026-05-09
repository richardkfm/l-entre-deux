# MVP scope

This document defines what is, and is not, in the first usable version of
l’entre-deux (target version `0.4.0`, end of Phase 4). Everything outside
this scope is explicitly deferred.

The MVP is the smallest version of the app that delivers the core promise:
**a calm, intentional pause before a chosen distracting app, with private
local reflection.**

## In scope

### 1. App selection (Phase 2)
- A screen listing the user’s installed launchable apps.
- The user picks which apps should trigger the pause flow.
- Selection is stored locally.

### 2. Intentional launcher (Phase 2)
- A grid screen showing the user’s selected apps as tiles.
- Tapping a tile starts the pause flow for that app, then launches it.
- Optional: home-screen pinned shortcuts per selected app, each routing
  through the pause flow.
- No detection of launches initiated outside l’entre-deux. This is
  documented honestly in onboarding.

### 3. Pause flow (Phase 2)
- A short, full-screen, calm screen.
- One tap to pick an intention from a small fixed set:
  - "I need this for one specific task"
  - "I am checking something briefly"
  - "I opened this automatically"
- One tap to optionally pick a session budget: 3, 5, or 10 minutes, or
  "no budget".
- One tap to proceed; one tap to back out.
- Total flow targets ≤ 2 taps to proceed.

### 4. Intention logging (Phase 3)
- Each pause flow records (locally only): timestamp, target package,
  selected intention, selected budget (if any), and whether the user
  proceeded or backed out.
- No content of any other app is recorded. Ever.

### 5. Micro-session budgeting (Phase 3)
- After proceeding with a budget, a gentle local notification fires when
  the budget elapses. The notification is a soft reminder, not an
  enforcement action. It does not lock anything.
- Going over budget is recorded as a data point, not a failure.

### 6. Reflection screen (Phase 4)
- A read-only local view summarizing the user’s recent pauses:
  - which apps trigger the most autopilot pauses
  - which times of day
  - how often a budget was set vs not
  - how often the user backed out at the pause
- No streaks. No scores. No comparative shaming.

### 7. Onboarding (Phase 2 / 4)
- 2–4 short screens explaining what the app does, what it does not do,
  and what permissions it asks for.
- A clear statement that data never leaves the device.

### 8. Settings (Phase 2 onward)
- Manage selected apps.
- Manage default budget.
- Wipe local data with a single confirmation.
- Theme follows system.

### 9. Foundations
- Kotlin + Jetpack Compose.
- Room for structured logs.
- DataStore (Preferences) for settings.
- MVVM-ish unidirectional state, no DI framework unless clearly justified.
- Unit tests for domain logic.
- One UI test for the pause flow happy path.
- CI runs lint + tests + assembleDebug.

## Out of scope for MVP

These are not bad ideas; they are deferred to keep MVP small and trusted.

- **AccessibilityService-based detection.** See
  [permissions-and-risks.md](permissions-and-risks.md). Likely never.
- **UsageStats + overlay detection.** Possible Phase 5+, opt-in only.
- **Always-on foreground service.**
- **Cloud sync, accounts, sign-in.**
- **Notifications beyond a single optional budget reminder.**
- **Streaks, points, badges, rewards, leaderboards.**
- **Social features, sharing, friends.**
- **Coach, advice, tips, AI suggestions.**
- **Schedules, modes, focus profiles.** (May come in Phase 5.)
- **Per-app whitelists, network-level blocking, VPN.**
- **Widgets.** (Phase 5+.)
- **Wear OS, tablets, foldables-specific UI.** Layouts should be sensible
  but not specially tuned.
- **Localization beyond English.** French should follow soon after, given
  the name; not blocking MVP.

## What "done" looks like for MVP

A user can:

1. Install the APK from F-Droid (or sideload).
2. Pick three apps in under a minute.
3. Open one of them through l’entre-deux, see a calm pause, pick an
   intention, optionally set a budget, and arrive in the target app.
4. Open the reflection screen a few days later and see plain-language
   patterns about their own usage.
5. Read the privacy section in-app and feel confident no data left their
   device — and verify that claim by reading the source.

If all five hold, the MVP has shipped.
