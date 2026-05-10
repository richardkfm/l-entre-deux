# F-Droid metadata

Layout follows the Fastlane structured-metadata convention that
F-Droid’s build server picks up automatically:

```
fastlane/metadata/android/
  en-US/
    title.txt              app name (≤ 50 chars)
    short_description.txt  one-line summary (≤ 80 chars)
    full_description.txt   long description (≤ 4000 chars)
    changelogs/<versionCode>.txt   release notes for that version
  fr-FR/
    ...
```

Add screenshots later under
`fastlane/metadata/android/<locale>/images/phoneScreenshots/` once we
have a stable visual identity.

The `<versionCode>` filename must match the `versionCode` in
`app/build.gradle.kts` for the corresponding release.
