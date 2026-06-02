# Permissions and platform-risks memo

This memo evaluates technical options for detecting or interrupting the launch
of a "distracting" app on Android, and the permissions, policy implications,
and trust costs of each. **Read this before adding any feature that needs
sensitive capabilities.**

This is the most important document in the repo for keeping the project
honest. l’entre-deux is privacy-first and offline-first. We will refuse
capabilities — even useful ones — when their cost to user trust exceeds the
product benefit.

## Goal

Show a short pause flow at the moment the user is about to open a chosen
distracting app, and only then.

## Constraints

- Do not silently spy on app usage.
- Do not request capabilities the app does not strictly need to deliver its
  current feature.
- Prefer the least invasive technical path that delivers the experience.
- Be honest with the user about what each capability lets the app see or do.
- Stay compatible with F-Droid distribution.
- Stay within Google Play policy if we ever distribute there. In particular,
  Play restricts AccessibilityServices to apps that primarily help users with
  disabilities (see "Accessibility API" policy). Using Accessibility purely
  for UX features risks removal.

## Option A — Intentional in-app launcher (no special permissions)

The user opens distracting apps **through l’entre-deux**, not through the
home screen. The app shows a grid of chosen apps; tapping one runs the pause
flow, then launches the target via a normal `Intent`. Optional: user creates
a home-screen shortcut to the l’entre-deux launcher screen, or replaces a few
home-screen icons with l’entre-deux shortcuts that route through the pause.

**Permissions needed:** `QUERY_ALL_PACKAGES` (or, preferably, the more
restrictive `<queries>` manifest entries) to enumerate user-installed apps
for selection. Nothing else.

**Pros**
- Zero sensitive runtime permissions.
- Immediately F-Droid friendly.
- Maximum user trust: the app cannot see anything the user did not click in
  the app.
- Aligns with the product thesis: a deliberate, opt-in pause.

**Cons**
- Only catches launches initiated through l’entre-deux. App opens from
  notifications, recents, deep links, or untouched home-screen icons are not
  intercepted.
- Requires the user to change a small habit (use our launcher screen, or
  replace a few icons). This is friction. We argue that friction is exactly
  the point and is acceptable for this product.

**Verdict:** This is the MVP path. Pair with Option G (pinned shortcuts) for
the actual interception mechanic.

## Option B — UsageStatsManager + System Alert Window (overlay)

Detect that a target app has come to the foreground via `UsageStatsManager`
or `UsageEvents` and draw an overlay on top of it with the pause flow. The
overlay can finish-and-go-back if the user dismisses it.

**Permissions needed**
- `PACKAGE_USAGE_STATS` (special access; granted via Settings, not a normal
  runtime grant).
- `SYSTEM_ALERT_WINDOW` (special access; granted via Settings; on newer
  Android versions, increasingly restricted UX).
- A foreground service to keep polling/detecting reliably, plus the
  associated foreground-service notification.

**Pros**
- Catches launches no matter how the user opened the app.
- Familiar pattern from existing wellbeing tools.

**Cons**
- Two special-access permissions, each of which can read or affect things
  far beyond what we need.
- Polling-based detection is inherently reactive: pause appears *after* the
  target app is already visible. That undermines the "intentional pause
  before" idea.
- Foreground service is always-on battery and notification cost.
- `SYSTEM_ALERT_WINDOW` overlays are increasingly flagged by users and OEMs
  as suspicious.
- Drawing over another app to coerce a choice resembles patterns that Play
  policy treats as deceptive overlays. Care needed.

**Verdict:** Possible Phase 5+ enhancement, **opt-in**, with a clear
explanation screen and an off-switch in Settings. Not part of the MVP.

## Option C — AccessibilityService

An `AccessibilityService` can listen for `TYPE_WINDOW_STATE_CHANGED` events
and react when a target package’s window appears, then open our pause
activity over it.

**Pros**
- Most reliable real-time detection of foreground app changes.
- Lower latency than `UsageStatsManager` polling.

**Cons**
- AccessibilityService is the most privileged user-grantable permission on
  Android. It can read screen content, intercept gestures, and observe input
  across all apps.
- Google Play restricts AccessibilityService to apps that "primarily help
  users with disabilities." Using it for habit/UX purposes risks removal
  from Play and undermines our trust story.
- Many security tools, MDMs, and banking apps disable themselves or warn
  the user when an AccessibilityService is enabled. We would be asking for
  a capability that other apps actively flag.
- Requesting it conflicts directly with our "minimal permissions" and
  "trust by construction" principles.

**Verdict:** Not in the current scope. This option is kept open as a potential
future path if the shortcut approach (Option A + G) proves insufficient for
enough real users. If ever pursued, it must be:

- A separate, explicitly labeled opt-in module (e.g. a dedicated build flavor).
- Preceded by a plain-language disclosure screen explaining exactly what the
  service can see across all apps.
- Off by default; revocable in one tap from Settings.
- Accompanied by an F-Droid Anti-Feature declaration.
- Only initiated after an explicit decision in a public issue, not a quiet PR.

Play Store distribution with AccessibilityService for non-accessibility
purposes is high-risk (policy violation). Any build that includes this path
would be F-Droid only.

## Option D — Device admin / Device Policy Controller

Out of scope. This is parental-control / MDM territory and contradicts the
"adult treating adults as adults" stance.

## Option E — VPN service

Routes network traffic through a local VPN to block or shape it. Does not
help us, because we want to interrupt the *launch*, not the network. Also
costs the user a permanent VPN slot.

**Verdict:** Not relevant.

## Option F — Notification listener

`NotificationListenerService` can read all notifications across the device.
Way over-broad for our use case and not aligned with the launch-interruption
idea.

**Verdict:** Do not use.

## Option G — App shortcuts and home-screen replacement

Use `ShortcutManager` to ship a single "Pause then open X" shortcut per
configured target app. The user pins these to the home screen, optionally
replacing the original app icon. Tap → pause flow → real app.

**Pros**
- Zero sensitive permissions. Trivially F-Droid friendly.
- Catches every tap on the replaced icon.
- Combined with Option A, this is the complete interception story for the MVP.

**Cons**
- Users must opt in by pinning the shortcuts. Not all launchers expose
  pinning the same way.
- Doesn’t cover launches from notifications, the recents switcher, or deep
  links from other apps.

**Verdict:** This is the current implementation. Together with Option A it
forms the "intentional launcher" experience. The coverage gap is real and
documented honestly in `docs/product-brief.md`.

## Required disclosures (whatever path we ship)

For any sensitive capability we ever add, the app must, before requesting
it:

1. Show a plain-language screen explaining what the capability lets the app
   see and do.
2. Say what the app will and will not do with that capability.
3. Provide a non-capability path through the same feature wherever possible.
4. Make the capability revocable from inside the app, with the same one-tap
   ease as enabling it.

## Permissions in use

**None.** As of 0.7.0 the app requests no runtime permissions and no
special access. The manifest declares only the `<queries>` visibility entry
needed to enumerate launchable apps for selection (a declaration, not a
runtime permission). `INTERNET` is not declared.

### Removed: `POST_NOTIFICATIONS` (was Phase 3)

The time-limit/budget feature posted a single local notification when a
chosen budget elapsed, which required `POST_NOTIFICATIONS` on Android 13+.
That feature was removed in 0.7.0 along with its alarm receiver and the
permission. The pause is now a single, calmer "name your intention" step
with no time limits and no notifications, so the app no longer needs this
(or any) runtime permission. This is a strict reduction in what the app can
do and a win for the trust story.

## Things we will never do

- Read screen content without explicit user consent and a clear product reason.
- Read other apps’ notifications.
- Track location.
- Send any usage data off-device.
- Add network permission unless and until a feature genuinely needs it. The
  default manifest should not request `INTERNET`.

## Things we will not do yet (but keep open)

- **AccessibilityService** — the highest-trust capability on Android. We may
  revisit as an opt-in module if the shortcut approach is demonstrably
  insufficient. Any decision to proceed requires a public discussion (issue or
  PR), prominent in-app disclosure, a hard off-switch, and a separate build
  flavor. See Option C above.

## Recommendation summary

- **Current implementation (MVP, Phase 2–5):** Option A + Option G. No
  sensitive permissions. Coverage is limited to taps on home-screen shortcuts
  the user has pinned. This limitation is documented honestly.
- **Future opt-in, if needed (Phase X):** Option C (AccessibilityService) for
  full-coverage interception. Requires deliberate decision. Off by default.
- **Not pursued:** Option B (UsageStats + overlay) — reactive, invasive, and
  technically weaker than Option C for our use case.
