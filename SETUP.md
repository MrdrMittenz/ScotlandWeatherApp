# ScotlandWeatherApp - Setup Guide

## Project Structure
```
ScotlandWeatherApp/
├── app/
│   ├── build.gradle.kts          # Ktor, osmdroid, Room, Coil, Compose BOM
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/scotlandweather/app/
│       │   ├── MainActivity.kt
│       │   ├── MainApplication.kt
│       │   ├── ScotlandWeatherApp.kt    # Scaffold, search, bottom sheet, all controls
│       │   ├── ui/
│       │   │   ├── theme/               # Material 3 dark/light
│       │   │   ├── screen/MapScreen.kt  # osmdroid MapView + WeatherCircleOverlay
│       │   │   └── components/          # TimeSlider, Legend, BottomSheet, LayerToggle, etc.
│       │   ├── data/
│       │   │   ├── model/               # Location, WeatherData, MapState, TileLayer
│       │   │   ├── network/WeatherApiService.kt   # Ktor → Open-Meteo
│       │   │   ├── repository/          # Weather + Location repos
│       │   │   ├── database/WeatherDatabase.kt   # Room cache
│       │   │   └── tile/WeatherTileRenderer.kt   # On-device overlay renderer
│       │   └── viewmodel/WeatherViewModel.kt
│       └── res/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/wrapper/                 # gradle-wrapper.jar + .properties (Gradle 8.5)
└── local.properties                # Android SDK path

Backend: /home/build/weather_tile_server/  (FastAPI, optional)
```

## Setup Steps

### Build in Android Studio
1. Open Android Studio → "Open an existing project"
2. Select `C:\Users\ReviOSGaming\Projects\ScotlandWeatherApp`
3. Let Gradle sync (first time: download deps, ~2-5 min)
4. Connect Android device or start emulator
5. Click **Run**

**No API key, no billing, no account needed.** Uses OpenStreetMap tiles (osmdroid).

## Features
- OpenStreetMap base map with Scotland bounds + 22 fishing location markers
- Custom weather overlay (colored circles + values) — 8 layers: Temperature, Precipitation, Wind, Pressure, Cloud, UV, Solunar, Fishing Score
- 24-hour time slider with scrolling hourly forecast
- Layer selector + color legend
- Bottom sheet with detailed weather, solunar data, ranking
- Location search dialog & favorites
- Dark/light Material 3 theme
- Offline Room cache
- Ranking engine for fishing conditions
- Optional FastAPI tile server backend

## Running the Tile Server (Optional)
Better performance with server-side rendering:
```bash
cd /home/build/weather_tile_server
pip install -r requirements.txt
python main.py
# http://localhost:8000/health
# http://localhost:8000/tile/6/31/21.png?hour=14&layer=temperature
```

## Build Requirements
- Android Studio Hedgehog (2023.1.1+) or IntelliJ
- JDK 17 (already at `C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\`)
- Android SDK (already at `C:\Users\ReviOSGaming\Android\sdk`)
