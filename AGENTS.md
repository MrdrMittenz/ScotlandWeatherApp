# Agent Log — Fair Weather Fishing Scotland App

## Project Overview
Android native weather/fishing app for Scotland with live webcams, polar/lunar data, species guide, fishing reports, and interactive map. Built with Jetpack Compose + osmdroid.

**GitHub:** https://github.com/MrdrMittenz/ScotlandWeatherApp  
**Latest Release:** https://github.com/MrdrMittenz/ScotlandWeatherApp/releases/tag/v1.0.0

---

## Session 1: Initial Setup & Features

### Webcams
- 5 live webcams using Windy/WorldCam static image URLs: Kirkwall, Aberdeen, Oban, Armadale, Brodick
- 5 ports removed (no working static feeds): Stornoway, Lerwick, Ullapool, Mallaig, Portree
- Webcam images update when the refresh button is tapped

### UI/Theme
- **Dark mode toggle** — wired through `MainActivity` → `ScotlandWeatherTheme(darkTheme = state.isDarkMode)`
- **Glassmorphism TopAppBar** — oceanic blue-to-teal gradient with white text/icons
- **Glassmorphism overlays** — translucent frosted-glass `Surface` with visible borders, 12dp shadow elevation
- **Weather circle glow** — translucent 1.6x-radius halo behind each value dot on the map
- **Bottom sheet styling** — custom drag handle, gradient accent bar (blue→teal), reduced elevation

### Keystore & Signing
- Generated: `app/release.jks` (alias `scotlandweather`, password `TempPass123`, 10000-day validity)
- Signing config added to `app/build.gradle.kts`
- Bundle config also added (language density ABI splits)
- **WARNING:** Replace passwords before Play Store release

### Git & GitHub
- Git init + initial commit (46 files, 4730 insertions)
- Public repo created: `MrdrMittenz/ScotlandWeatherApp`
- Source pushed to `master`

### In-App Update Feature
Files created:
- `data/update/UpdateModels.kt` — `VersionManifest` + `UpdateResult` data classes
- `data/update/UpdateChecker.kt` — fetches `version.json` from GitHub, compares versionCode, downloads APK, installs via FileProvider

Manifest changes:
- Added `REQUEST_INSTALL_PACKAGES` permission
- Added `FileProvider` with `file_paths.xml` (cache-path)

UI:
- Update button (download icon) in TopAppBar
- Update available dialog with changelog + install button
- Error shown via Snackbar

### Version Manifest
- Created `version.json` at project root (uploaded to GitHub release)
- App checks: `https://github.com/MrdrMittenz/ScotlandWeatherApp/releases/latest/download/version.json`
- APK downloads from: `https://github.com/MrdrMittenz/ScotlandWeatherApp/releases/latest/download/app-release.apk`

### GitHub Release
- Created `v1.0.0` release with `app-release.apk` + `version.json` attached

---

## Session 2: Landscape Layout Fix
- **Detected:** landscape mode had controls overlapping the map (z-order issue with AndroidView)
- **Old layout:** side rail (60dp) with LayerToggle, buttons, Legend, Favorites + separate TimeSlider strip
- **New layout:** `BoxWithConstraints` detects orientation
  - Portrait: unchanged
  - Landscape: controls in a bottom control bar (below map, renders on top) + compact single-row TimeSlider
- Layer selector uses `DropdownMenu` (popup window, guaranteed over map)
- Control bar: 32dp buttons, horizontally scrollable, contains Layer/Refresh/Reports/Legend/Favorites
- TimeSlider: single 40dp row with 3 day chips (Tdy/Tmrw/d/M) + hour slider + current time label
- Compact TopAppBar in landscape (single line, no subtitle)

### Cleanup
Removed unused files:
- `SETUP.md`
- `data/database/WeatherDatabase.kt` (Room DB class, never used)
- `TimeSliderCompact` function (dead code from refactor)
- Backed up old Python prototype build caches freed 10GB

---

## Key Commands

### Build
```bash
.\gradlew.bat assembleDebug        # debug APK
.\gradlew.bat assembleRelease       # signed release APK
```

### Install on Device
```bash
adb -s <DEVICE_ID> install -r app/build/outputs/apk/release/app-release.apk
```

### Release
```bash
# Update versionCode in app/build.gradle.kts
# Update version.json
# Build APK
gh release upload vX.Y.Z app/build/outputs/apk/release/app-release.apk#app-release.apk --clobber
gh release upload vX.Y.Z version.json#version.json --clobber
```

### Git
```bash
git add -A && git commit -m "msg"
git push origin master
```

---

## Configuration Reference

### Keystore (app/release.jsk)
- **Alias:** scotlandweather
- **Storepass / Keypass:** TempPass123
- **Validity:** 10000 days
- **Distinguished Name:** CN=ScotlandWeatherApp, OU=Development, O=ScotlandWeather, L=Scotland, ST=Scotland, C=GB

### build.gradle.kts signingConfigs
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("release.jks")
        storePassword = "TempPass123"
        keyAlias = "scotlandweather"
        keyPassword = "TempPass123"
    }
}
```

### version.json format
```json
{
  "versionCode": 1,
  "versionName": "1.0.0",
  "downloadUrl": "https://github.com/MrdrMittenz/ScotlandWeatherApp/releases/latest/download/app-release.apk",
  "changelog": "Description of changes",
  "minApiLevel": 26
}
```

---

## Key Files

| File | Purpose |
|------|---------|
| `app/build.gradle.kts` | Signing config, dependencies, bundle config |
| `app/release.jks` | Release keystore |
| `app/src/main/AndroidManifest.xml` | FileProvider, permissions |
| `app/src/main/java/.../ScotlandWeatherApp.kt` | Main UI (portrait + landscape layouts) |
| `app/src/main/java/.../data/update/UpdateChecker.kt` | In-app update logic |
| `app/src/main/java/.../data/update/UpdateModels.kt` | Version manifest models |
| `app/src/main/res/xml/file_paths.xml` | FileProvider paths |
| `version.json` | Release metadata for in-app update |
| `app/src/main/java/.../data/model/FishingReport.kt` | ScottishPortWebcams (5 entries) |
| `app/src/main/java/.../ui/screen/MapScreen.kt` | WeatherCircleOverlay, LochLabelOverlay |

---

## Current State
- **versionCode:** 1, **versionName:** 1.0.0
- **minSdk:** 26, **targetSdk:** 34, **compileSdk:** 34
- **Package:** com.scotlandweather.app
- **App name:** Fair Weather Fishing
