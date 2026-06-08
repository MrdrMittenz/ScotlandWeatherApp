package com.scotlandweather.app.data.repository

import com.scotlandweather.app.data.model.*
import android.util.Log
import com.scotlandweather.app.data.network.WeatherApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class WeatherRepository(
    private val api: WeatherApiService = WeatherApiService()
) {
    private val concurrencyLimit = Semaphore(5)

    suspend fun fetchAllWeather(locations: List<ScotlandLocation>): Map<String, OpenMeteoResponse> = coroutineScope {
        val results = mutableMapOf<String, OpenMeteoResponse>()
        locations.map { location ->
            async {
                concurrencyLimit.withPermit {
                    try {
                        location.name to api.fetchWeather(location.lat, location.lon)
                    } catch (e: Exception) {
                        Log.e("WeatherRepo", "${location.name}: ${e.message}", e)
                        null
                    }
                }
            }
        }.forEach { deferred ->
            deferred.await()?.let { (name, data) -> results[name] = data }
        }
        Log.d("WeatherRepo", "fetchAllWeather: ${results.size}/${locations.size} locations loaded")
        results
    }

    suspend fun fetchWeatherForLocation(location: ScotlandLocation): OpenMeteoResponse? {
        return try {
            api.fetchWeather(location.lat, location.lon)
        } catch (_: Exception) { null }
    }

    suspend fun fetchSolunarForLocation(location: ScotlandLocation): SolunarData? {
        return try {
            api.fetchSolunar(location.lat, location.lon)
        } catch (_: Exception) { null }
    }

    suspend fun searchLocations(query: String): List<ScotlandLocation> {
        return try {
            api.searchLocation(query)
        } catch (_: Exception) { emptyList() }
    }

    suspend fun fetchAllSolunar(locations: List<ScotlandLocation>): Map<String, SolunarData> = coroutineScope {
        val results = mutableMapOf<String, SolunarData>()
        locations.map { location ->
            async {
                concurrencyLimit.withPermit {
                    try {
                        location.name to api.fetchSolunar(location.lat, location.lon)
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        }.forEach { deferred ->
            deferred.await()?.let { (name, data) -> results[name] = data }
        }
        Log.d("WeatherRepo", "fetchAllSolunar: ${results.size}/${locations.size} locations loaded")
        results
    }

    fun calculateRankings(
        weatherData: Map<String, OpenMeteoResponse>,
        solunarData: Map<String, SolunarData>,
        day: Int,
        hour: Int
    ): List<RankingData> {
        val timeIndex = day * 24 + hour
        val rankings = mutableListOf<RankingData>()

        val winds = mutableListOf<Double>()
        val preps = mutableListOf<Double>()

        weatherData.forEach { (_, data) ->
            val h = data.hourly ?: return@forEach
            if (h.temperature_2m.size > timeIndex) {
                winds.add(h.windspeed_10m[timeIndex])
                preps.add(h.precipitation[timeIndex])
            }
        }

        val windMax = winds.maxOrNull() ?: 20.0
        val prepMax = preps.maxOrNull() ?: 10.0

        weatherData.forEach { (name, data) ->
            val h = data.hourly ?: return@forEach
            if (h.temperature_2m.size > timeIndex) {
                val temp = h.temperature_2m[timeIndex]
                val wind = h.windspeed_10m[timeIndex]
                val prep = h.precipitation[timeIndex]
                val cloud = h.cloudcover.getOrElse(timeIndex) { 50 }
                val uv = h.uv_index.getOrElse(timeIndex) { 3.0 }
                val pressure = h.pressure_msl.getOrElse(timeIndex) { 1013.0 }

                // Ideal temp 12-20°C, normalize
                val tempScore = 1.0 - kotlin.math.abs(temp - 16.0) / 20.0
                val windScore = 1.0 - (wind / (windMax + 1))
                val prepScore = 1.0 - (prep / (prepMax + 1))
                val cloudScore = 1.0 - cloud / 100.0
                val uvScore = uv.coerceIn(0.0, 8.0) / 8.0
                val pressureScore = 1.0 - kotlin.math.abs(pressure - 1013.0) / 40.0
                val sol = solunarData[name]
                val baseSolunar = (sol?.solunarRating ?: 50) / 100.0
                val daylightBonus = ((sol?.daylightHours ?: 12.0) - 12.0) / 24.0
                val solunarScore = (baseSolunar + daylightBonus).coerceIn(0.0, 1.0)

                val totalScore = tempScore * 0.20 + windScore * 0.15 +
                        prepScore * 0.15 + cloudScore * 0.05 +
                        uvScore * 0.05 + pressureScore * 0.05 + solunarScore * 0.35

                rankings.add(
                    RankingData(
                        locationName = name,
                        temperatureScore = tempScore,
                        windScore = windScore,
                        precipitationScore = prepScore,
                        cloudScore = cloudScore,
                        uvScore = uvScore,
                        pressureScore = pressureScore,
                        solunarScore = solunarScore,
                        totalScore = totalScore,
                        rank = 0
                    )
                )
            }
        }

        // Sort and assign ranks
        val sorted = rankings.sortedByDescending { it.totalScore }
        return sorted.mapIndexed { index, r -> r.copy(rank = index + 1) }
    }
}
