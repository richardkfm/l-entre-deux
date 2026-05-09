# Privacy principles

These are commitments, not aspirations. They constrain what the app and the
project may do.

## 1. Local-only by default

All data the app collects about the user lives on the user’s device. No
account, no sync, no telemetry, no crash reporting service, no analytics.
The app does not request the `INTERNET` permission until and unless a
feature genuinely needs it, at which point that feature must be explicitly
documented in this file and in the README.

## 2. Minimal data

Record the smallest amount of information that delivers the feature.

What we record:
- Which apps the user has selected to route through the pause.
- For each pause: timestamp, target package name, chosen intention, optional
  budget minutes, whether the user proceeded.
- Settings (default budget, onboarding status, theme override).

What we never record:
- Content of any other app.
- Notifications or messages.
- Keystrokes, gestures, or screen content.
- Location.
- Device identifiers, advertising IDs.
- Network traffic.

## 3. Minimal permissions

The default manifest at MVP requests no runtime permissions and no special
access. App enumeration uses Android’s `<queries>` mechanism rather than
`QUERY_ALL_PACKAGES` where possible.

Any future capability that requires a sensitive permission must:
- be opt-in,
- show a plain-language explanation before the system prompt,
- be revocable from inside the app,
- be documented in `permissions-and-risks.md`,
- be declared in the F-Droid Anti-Features metadata if applicable.

## 4. No dark patterns, no engagement bait

- No streaks, points, badges, leaderboards, daily-goal nags.
- No "you’re doing worse than X% of users" comparisons.
- No notifications other than (optionally) a single soft budget reminder.
- No urgency, scarcity, or guilt language.
- No upsells, paid tiers, or "pro" features.

## 5. No third-party SDKs that talk off-device

The build must remain free of any SDK that calls home: no Crashlytics, no
Firebase, no analytics, no ad libraries, no Play Services dependencies.
This is enforced by review and by keeping the dependency list tiny and
visible in `gradle/libs.versions.toml`.

This is also a requirement for F-Droid distribution.

## 6. Verifiable trust

The whole point of a privacy claim is that the user can check it. We help
the user check it by:
- being open source under GPL-3.0,
- keeping the dependency list short and human-readable,
- avoiding obfuscation in release builds beyond what R8/ProGuard does for
  size reasons (and committing the rules),
- targeting reproducible builds for F-Droid,
- writing this document as a public commitment rather than private intent.

## 7. User-controlled data lifecycle

- One-tap "wipe local data" in Settings, with a confirmation. After the
  wipe, the app is in the same state as a fresh install.
- Uninstalling the app removes all data. There is no off-device residue
  because there is no off-device storage.

## 8. Honest disclosures

- Onboarding states plainly what the app does and does not do.
- The Settings screen has a short "What this app sees" section linking to
  this document.
- The README and the F-Droid description match this document; if they
  drift, this document wins.

## 9. Change discipline

Any PR that affects what the app collects, what permissions it requests,
or what it sends off-device must update this file in the same change.
Reviewers should refuse changes that quietly broaden data collection.
