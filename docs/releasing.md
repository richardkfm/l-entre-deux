# Releasing

GitHub releases carry a release APK signed with one stable key. Android
only installs an update over an existing app when both are signed with
the same key, so this key must never change and never be lost.

F-Droid builds from source and signs with its own key. An install from
F-Droid and an install from GitHub cannot update each other; switching
source means uninstalling once.

## One-time setup: the signing key

Run on your own machine, never in CI:

```sh
keytool -genkeypair -v \
  -keystore entredeux-release.jks \
  -alias entredeux \
  -keyalg RSA -keysize 4096 -validity 10000
```

`keytool` asks for a password and a name (any name works; it is visible
in the certificate). The keystore is PKCS12, so the key password is the
same as the keystore password.

Then add four repository secrets under GitHub → Settings → Secrets and
variables → Actions:

| Secret                       | Value                                   |
| ---------------------------- | --------------------------------------- |
| `RELEASE_KEYSTORE_BASE64`    | output of `base64 -w0 entredeux-release.jks` (macOS: `base64 -i entredeux-release.jks`) |
| `RELEASE_KEYSTORE_PASSWORD`  | the password                            |
| `RELEASE_KEY_ALIAS`          | `entredeux`                             |
| `RELEASE_KEY_PASSWORD`       | the same password                       |

Keep `entredeux-release.jks` and its password backed up somewhere safe
and offline. If the key is lost, every user has to uninstall (and lose
their pause history) to install the next release. `*.jks` is in
`.gitignore`; never commit it.

## Cutting a release

1. Bump `versionName` / `versionCode`, update `CHANGELOG.md` and the
   fastlane changelog for the new `versionCode`.
2. Tag and push: `git tag v1.3.1 && git push origin v1.3.1`.
3. The `Release` workflow builds `assembleRelease` with the key and
   creates a **draft** GitHub release with `l-entre-deux-<version>.apk`
   and the certificate's SHA-256 fingerprint in the notes. Review it and
   publish.

The workflow fails if `RELEASE_KEYSTORE_BASE64` is missing or the APK
comes out unsigned, rather than publishing an APK that cannot update an
existing install.

To check a downloaded APK locally:

```sh
apksigner verify --print-certs l-entre-deux-1.3.1.apk
```
