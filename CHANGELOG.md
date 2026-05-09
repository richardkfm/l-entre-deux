# Changelog

All notable changes to l’entre-deux are recorded here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning is [Semantic Versioning](https://semver.org/), with the
project-specific rules described in [`CLAUDE.md`](CLAUDE.md).

## [Unreleased]

Nothing yet.

## [0.1.0] — 2026-05-09

Foundation release. No working app yet — documentation and skeleton only.

### Added
- Product brief, MVP scope, architecture proposal, permissions-and-risks
  memo, privacy principles, and UI principles in `docs/`.
- `README.md`, `CLAUDE.md`, `roadmap.md`, `CHANGELOG.md`.
- Single-module Android skeleton (`app/`) with Kotlin, Jetpack Compose,
  and Material 3; placeholder `MainActivity` confirming the build runs.
- Gradle version catalog at `gradle/libs.versions.toml`.
- `.gitignore` and basic ProGuard rules.
- GitHub Actions CI: lint, test, assembleDebug on every push and PR.

### Notes
- No runtime permissions, no special access, no `INTERNET` permission
  declared. No analytics, no third-party SDKs that talk off-device.
- Distribution target is F-Droid first; build is free of Google Play
  Services and other proprietary dependencies.
