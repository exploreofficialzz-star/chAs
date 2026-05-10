# chAs — Identity Verification Wrapper

A lightweight Android app that wraps any KYC verification link and intercepts the **selfie step only** — giving users the option to upload a photo from their gallery instead of taking a live selfie. The video step is left completely untouched.

---

## How It Works

```
KYC page triggers file input
         │
         ▼
  ┌─────────────────────────┐
  │  Detect accept type     │
  └─────────────────────────┘
         │
    ┌────┴─────────────────┐
    │                      │
 image/*               video/*
    │                      │
    ▼                      ▼
Gallery or Camera    Straight to
  (your choice)      video recorder
```

---

## Project Structure

```
chAs/
├── .github/workflows/
│   └── build.yml              # GitHub Actions — debug + signed release
├── app/src/main/
│   ├── java/com/chas/verify/
│   │   ├── MainActivity.kt        # URL entry screen
│   │   └── KycWebViewActivity.kt  # WebView with smart interception
│   ├── res/
│   │   ├── drawable/
│   │   │   ├── ic_launcher_background.xml
│   │   │   └── ic_launcher_foreground.xml
│   │   ├── mipmap-anydpi-v26/
│   │   │   ├── ic_launcher.xml
│   │   │   └── ic_launcher_round.xml
│   │   ├── values/
│   │   │   ├── strings.xml
│   │   │   └── colors.xml
│   │   └── xml/
│   │       └── file_paths.xml
│   └── AndroidManifest.xml
├── app/build.gradle
├── build.gradle
├── settings.gradle
├── gradle.properties
└── gradle/wrapper/
    └── gradle-wrapper.properties
```

---

## Build Locally

```bash
git clone https://github.com/YOUR_USERNAME/chas.git
cd chas
./gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

---

## GitHub Actions Setup

### Debug builds (automatic)
Every push to `main` or `develop` automatically builds a debug APK available under **Actions → Artifacts**.

### Signed release builds
1. Generate a keystore (once):
   ```bash
   keytool -genkey -v -keystore chas.jks \
     -alias chas -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Base64-encode it:
   ```bash
   # Mac
   base64 -i chas.jks | pbcopy

   # Linux
   base64 chas.jks | xclip
   ```

3. Add these 4 secrets in **GitHub → Settings → Secrets & Variables → Actions**:

   | Secret name        | Value                          |
   |--------------------|-------------------------------|
   | `KEYSTORE_BASE64`  | The base64 string from step 2 |
   | `KEYSTORE_PASSWORD`| Your keystore password        |
   | `KEY_ALIAS`        | `chas` (or your alias)        |
   | `KEY_PASSWORD`     | Your key password             |

4. Create a release:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   # Then create a GitHub Release from that tag
   ```
   The signed APK will be automatically attached to the release.

---

## Customisation

| What to change | Where |
|---|---|
| Package name | `app/build.gradle` → `applicationId` + rename folder `com/chas/verify` |
| App name | `res/values/strings.xml` → `app_name` |
| Selfie dialog text | `KycWebViewActivity.kt` → `showSelfieDialog()` |
| Video recording limits | `KycWebViewActivity.kt` → `launchVideoCapture()` |

---

## Important Notes

- Gallery uploads are sent to the KYC provider's backend as-is. If they run **liveness detection ML**, a static photo will likely be rejected. This wrapper gives the user the option — whether it passes is up to the provider's backend.
- The best long-term fix is asking your KYC provider to enable **upload mode** in your account dashboard (Persona, Onfido, Jumio, Veriff all support this).

---

## License
MIT
