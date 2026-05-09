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

**Verdict:** This is the recommended MVP path.

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

**Verdict:** Do not implement. If ever revisited, only as a clearly-labeled
"Phase X" optional module, with explicit user consent, prominent in-app
disclosure, separate F-Droid build flavor, and never as a default.

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
- Can be combined with Option A.

**Cons**
- Users must opt in by pinning the shortcuts. Not all launchers expose
  pinning the same way.
- Doesn’t cover launches from notifications or deep links.

**Verdict:** Pair with Option A in the MVP. Together they form the
"intentional launcher" experience.

## Recommendation summary

- **MVP (Phase 2–4):** Option A + Option G. No sensitive permissions.
- **Phase 5+ (opt-in):** Optional Option B (UsageStats + overlay) for
  broader coverage. Off by default. Documented honestly.
- **Phase X (probably never):** Option C. Only if the project consciously
  decides the trust cost is acceptable, which it currently is not.

## Required disclosures (whatever path we ship)

For any sensitive capability we ever add, the app must, before requesting
it:

1. Show a plain-language screen explaining what the capability lets the app
   see and do.
2. Say what the app will and will not do with that capability.
3. Provide a non-capability path through the same feature wherever possible.
4. Make the capability revocable from inside the app, with the same one-tap
   ease as enabling it.

## Things we will never do

- Read screen content.
- Read other apps’ notifications.
- Track location.
- Send any usage data off-device.
- Use AccessibilityService for non-accessibility purposes.
- Add network permission unless and until a feature genuinely needs it. The
  default manifest should not request `INTERNET`.
