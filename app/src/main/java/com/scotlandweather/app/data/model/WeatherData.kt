package com.scotlandweather.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val precipitation: List<Double>,
    val precipitation_probability: List<Int> = emptyList(),
    val weathercode: List<Int>,
    val windspeed_10m: List<Double>,
    val pressure_msl: List<Double>,
    val cloudcover: List<Int>,
    val relativehumidity_2m: List<Int> = emptyList(),
    val uv_index: List<Double> = emptyList()
)

@Serializable
data class DailyWeather(
    val time: List<String> = emptyList(),
    val temperature_2m_max: List<Double> = emptyList(),
    val temperature_2m_min: List<Double> = emptyList(),
    val sunrise: List<String> = emptyList(),
    val sunset: List<String> = emptyList(),
    val uv_index_max: List<Double> = emptyList(),
    val precipitation_hours: List<Double> = emptyList(),
    val windspeed_10m_max: List<Double> = emptyList(),
    val winddirection_10m_dominant: List<Int> = emptyList()
)

@Serializable
data class OpenMeteoResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val hourly: HourlyWeather? = null,
    val daily: DailyWeather? = null
)

@Serializable
data class SolunarData(
    val moonPhase: Double = 0.0,
    val moonIllumination: Double = 0.0,
    val moonRise: String? = null,
    val moonSet: String? = null,
    val sunRise: String? = null,
    val sunSet: String? = null,
    val solunarRating: Int = 0,
    val fishActivity: String = "Average",
    val bestTimes: List<TimeRange> = emptyList(),
    val daylightHours: Double = 12.0
)

@Serializable
data class TimeRange(
    val start: String,
    val end: String,
    val rating: String
)

@Serializable
data class RankingData(
    val locationName: String,
    val temperatureScore: Double = 0.0,
    val windScore: Double = 0.0,
    val precipitationScore: Double = 0.0,
    val cloudScore: Double = 0.0,
    val uvScore: Double = 0.0,
    val pressureScore: Double = 0.0,
    val solunarScore: Double = 0.0,
    val totalScore: Double = 0.0,
    val rank: Int = 0
)

data class WeatherTileRequest(
    val lat: Double,
    val lon: Double,
    val zoom: Int,
    val x: Int,
    val y: Int,
    val hour: Int,
    val layer: TileLayer
)

enum class TileLayer(val displayName: String) {
    TEMPERATURE("Temperature (°C)"),
    PRECIPITATION("Rain (mm)"),
    RAIN_CHANCE("Rain Chance (%)"),
    WIND("Wind (m/s)"),
    PRESSURE("Pressure (hPa)"),
    CLOUD("Cloud Cover (%)"),
    UV("UV Index"),
    SOLUNAR("Solunar"),
    RANKING("Fishing Score")
}

data class MapState(
    val selectedDay: Int = 0,
    val selectedHour: Int = 12,
    val selectedLayer: TileLayer = TileLayer.TEMPERATURE,
    val isRefreshing: Boolean = false,
    val selectedLocation: ScotlandLocation? = null,
    val hourlyData: Map<String, OpenMeteoResponse> = emptyMap(),
    val solunarData: Map<String, SolunarData> = emptyMap(),
    val rankings: List<RankingData> = emptyList(),
    val error: String? = null,
    val isDarkMode: Boolean = true,
    val favoriteLocations: Set<String> = emptySet(),
    val showAllMarkers: Boolean = true,
    val searchResults: List<ScotlandLocation> = emptyList(),
    val isSearching: Boolean = false,
    val customLocations: Map<String, ScotlandLocation> = emptyMap(),
    val catchReports: List<CatchReport> = emptyList(),
    val isLoadingReports: Boolean = false
)
