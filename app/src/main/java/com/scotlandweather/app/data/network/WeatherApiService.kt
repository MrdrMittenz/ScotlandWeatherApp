package com.scotlandweather.app.data.network

import com.scotlandweather.app.data.model.OpenMeteoResponse
import com.scotlandweather.app.data.model.ScotlandLocation
import com.scotlandweather.app.data.model.SolunarData
import com.scotlandweather.app.data.model.TimeRange
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalTime

@Serializable
data class GeocodingResult(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String = "",
    val admin1: String = ""
)

@Serializable
data class GeocodingApiResponse(
    val results: List<GeocodingResult>? = null
)

class WeatherApiService {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchWeather(lat: Double, lon: Double): OpenMeteoResponse = withContext(Dispatchers.IO) {
        val urlStr = "https://api.open-meteo.com/v1/forecast?" +
            "latitude=$lat&longitude=$lon" +
            "&hourly=temperature_2m,precipitation,precipitation_probability,weathercode,windspeed_10m,pressure_msl,cloudcover,relativehumidity_2m,uv_index" +
            "&daily=temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_hours,windspeed_10m_max,winddirection_10m_dominant" +
            "&timezone=GMT&forecast_days=3"
        Log.d("WeatherAPI", "Fetching: $urlStr")
        val body = httpGet(URL(urlStr))
        Log.d("WeatherAPI", "Response ${body.take(100)}")
        json.decodeFromString<OpenMeteoResponse>(body)
    }

    suspend fun fetchSolunar(lat: Double, lon: Double): SolunarData = withContext(Dispatchers.IO) {
        val body = try {
            val url = URL("https://api.open-meteo.com/v1/forecast?" +
                "latitude=$lat&longitude=$lon" +
                "&daily=sunrise,sunset&timezone=GMT&forecast_days=2")
            httpGet(url)
        } catch (e: Exception) {
            Log.w("WeatherAPI", "Solunar HTTP failed: ${e.message}")
            "{}"
        }
        val openMeteo = try {
            json.decodeFromString<OpenMeteoResponse>(body)
        } catch (e: Exception) {
            Log.w("WeatherAPI", "Solunar JSON parse failed: ${e.message}")
            null
        }

        val today = LocalDate.now().toString()
        val sunRise = openMeteo?.daily?.sunrise?.firstOrNull()
        val sunSet = openMeteo?.daily?.sunset?.firstOrNull()
        val moonPhase = calculateMoonPhase(today)
        val illumination = (1.0 - kotlin.math.cos(moonPhase * 2 * kotlin.math.PI)) / 2.0

        val daylightHours = try {
            if (sunRise != null && sunSet != null) {
                val rise = LocalTime.parse(sunRise.substringAfter("T").take(5))
                val set = LocalTime.parse(sunSet.substringAfter("T").take(5))
                (set.toSecondOfDay() - rise.toSecondOfDay()) / 3600.0
            } else 12.0
        } catch (_: Exception) { 12.0 }

        SolunarData(
            moonPhase = moonPhase,
            moonIllumination = illumination,
            sunRise = sunRise,
            sunSet = sunSet,
            solunarRating = calculateSolunarRating(moonPhase),
            fishActivity = getFishActivity(moonPhase),
            bestTimes = generateBestTimes(sunRise, sunSet),
            daylightHours = daylightHours
        )
    }

    private fun httpGet(url: URL): String {
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 30000
        conn.requestMethod = "GET"
        return try {
            val code = conn.responseCode
            Log.d("WeatherAPI", "HTTP $code for ${url.host}")
            if (code in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            } else {
                val errorBody = BufferedReader(InputStreamReader(conn.errorStream)).use { it.readText() }
                Log.e("WeatherAPI", "HTTP error $code: $errorBody")
                throw RuntimeException("HTTP $code: $errorBody")
            }
        } catch (e: Exception) {
            Log.e("WeatherAPI", "HTTP failed: ${e.message}")
            throw e
        } finally {
            conn.disconnect()
        }
    }

    private fun calculateMoonPhase(dateStr: String): Double {
        val date = LocalDate.parse(dateStr)
        val knownNewMoon = LocalDate.of(2024, 1, 11)
        val daysDiff = date.toEpochDay() - knownNewMoon.toEpochDay()
        val lunarCycle = 29.53058867
        return ((daysDiff % lunarCycle) / lunarCycle).let { if (it < 0) it + 1.0 else it }
    }

    private fun calculateSolunarRating(phase: Double): Int {
        val proximityToNew = minOf(phase, 1.0 - phase)
        val proximityToFull = minOf(kotlin.math.abs(phase - 0.5), 1.0 - kotlin.math.abs(phase - 0.5))
        val score = ((1.0 - proximityToNew * 2) * 0.6 + (1.0 - proximityToFull * 2) * 0.4)
        return (score.coerceIn(0.0, 1.0) * 100).toInt()
    }

    private fun getFishActivity(phase: Double): String {
        val rating = calculateSolunarRating(phase)
        return when {
            rating >= 90 -> "Excellent"
            rating >= 75 -> "Good"
            rating >= 50 -> "Average"
            rating >= 25 -> "Fair"
            else -> "Poor"
        }
    }

    private fun generateBestTimes(sunRise: String?, sunSet: String?): List<TimeRange> {
        val majorStart = try {
            if (sunRise != null) LocalTime.parse(sunRise.substringAfter("T").take(5)) else LocalTime.of(6, 0)
        } catch (_: Exception) { LocalTime.of(6, 0) }
        val majorEnd = majorStart.plusHours(2)
        val minorStart = try {
            if (sunSet != null) LocalTime.parse(sunSet.substringAfter("T").take(5)) else LocalTime.of(18, 0)
        } catch (_: Exception) { LocalTime.of(18, 0) }
        val minorEnd = minorStart.plusHours(2)

        return listOf(
            TimeRange(start = majorStart.toString(), end = majorEnd.toString(), rating = "Major"),
            TimeRange(start = minorStart.toString(), end = minorEnd.toString(), rating = "Minor")
        )
    }

    suspend fun searchLocation(query: String): List<ScotlandLocation> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$query&count=8&language=en&format=json")
            val body = httpGet(url)
            val response = json.decodeFromString<GeocodingApiResponse>(body)
            response.results?.map {
                ScotlandLocation(
                    name = it.name,
                    lat = it.latitude,
                    lon = it.longitude,
                    country = it.country,
                    region = it.admin1,
                    isCustom = true
                )
            } ?: emptyList()
        } catch (e: Exception) {
            Log.w("WeatherAPI", "Geocoding failed: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchWeatherForLocation(location: ScotlandLocation): OpenMeteoResponse? {
        return try {
            fetchWeather(location.lat, location.lon)
        } catch (_: Exception) { null }
    }

    suspend fun fetchSolunarForLocation(location: ScotlandLocation): SolunarData? {
        return try {
            fetchSolunar(location.lat, location.lon)
        } catch (_: Exception) { null }
    }
}
