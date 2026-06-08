package com.scotlandweather.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.scotlandweather.app.data.model.*
import com.scotlandweather.app.data.network.FishingReportScraper
import com.scotlandweather.app.data.repository.LocationRepository
import com.scotlandweather.app.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val weatherRepository = WeatherRepository()
    private val api = com.scotlandweather.app.data.network.WeatherApiService()
    private val scraper = FishingReportScraper()
    private val locationRepository = LocationRepository()
    private var loadJob: Job? = null

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    val allLocations: List<ScotlandLocation> = locationRepository.getAllLocations() + SeaLochs.all
    val locationMap: Map<String, ScotlandLocation> = allLocations.associateBy { it.name }

    init {
        loadAllData()
    }

    fun searchLocations(query: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSearching = true)
            val results = weatherRepository.searchLocations(query)
            _state.value = _state.value.copy(searchResults = results, isSearching = false)
        }
    }

    fun clearSearch() {
        _state.value = _state.value.copy(searchResults = emptyList())
    }

    fun selectCustomLocation(location: ScotlandLocation) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            try {
                val weather = api.fetchWeatherForLocation(location)
                val solunar = api.fetchSolunarForLocation(location)
                val customLocations = _state.value.customLocations + (location.name to location)
                val hourlyData = if (weather != null) {
                    _state.value.hourlyData + (location.name to weather)
                } else _state.value.hourlyData
                val solunarData = if (solunar != null) {
                    _state.value.solunarData + (location.name to solunar)
                } else _state.value.solunarData

                _state.value = _state.value.copy(
                    customLocations = customLocations,
                    hourlyData = hourlyData,
                    solunarData = solunarData,
                    isRefreshing = false,
                    searchResults = emptyList()
                )
            } catch (e: Exception) {
                Log.e("WeatherVM", "Custom location fetch failed: ${e.message}")
                _state.value = _state.value.copy(isRefreshing = false)
            }
        }
    }

    fun loadAllData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true, error = null)
            try {
                val weatherData = weatherRepository.fetchAllWeather(allLocations)
                Log.d("WeatherVM", "weatherData=${weatherData.size}/22")
                val solunarData = weatherRepository.fetchAllSolunar(allLocations)
                Log.d("WeatherVM", "solunarData=${solunarData.size}/22")
                val rankings = weatherRepository.calculateRankings(
                    weatherData, solunarData, _state.value.selectedDay, _state.value.selectedHour
                )
                _state.value = _state.value.copy(
                    hourlyData = weatherData,
                    solunarData = solunarData,
                    rankings = rankings,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                Log.e("WeatherVM", "loadAllData failed: ${e.message}", e)
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    fun selectDay(day: Int) {
        _state.value = _state.value.copy(selectedDay = day.coerceIn(0, 2))
    }

    fun selectHour(hour: Int) {
        _state.value = _state.value.copy(selectedHour = hour.coerceIn(0, 23))
        recalculateRankings()
    }

    fun selectLayer(layer: TileLayer) {
        _state.value = _state.value.copy(selectedLayer = layer)
    }

    fun selectLocation(location: ScotlandLocation?) {
        _state.value = _state.value.copy(selectedLocation = location)
    }

    fun toggleDarkMode() {
        _state.value = _state.value.copy(isDarkMode = !_state.value.isDarkMode)
    }

    fun toggleFavorite(name: String) {
        val current = _state.value.favoriteLocations.toMutableSet()
        if (current.contains(name)) current.remove(name) else current.add(name)
        _state.value = _state.value.copy(favoriteLocations = current)
    }

    fun toggleAllMarkers() {
        _state.value = _state.value.copy(showAllMarkers = !_state.value.showAllMarkers)
    }

    fun getWeatherForLocation(name: String): OpenMeteoResponse? =
        _state.value.hourlyData[name]

    fun getSolunarForLocation(name: String): SolunarData? =
        _state.value.solunarData[name]

    fun getRankingForLocation(name: String): RankingData? =
        _state.value.rankings.find { it.locationName == name }

    private fun recalculateRankings() {
        val rankings = weatherRepository.calculateRankings(
            _state.value.hourlyData, _state.value.solunarData,
            _state.value.selectedDay, _state.value.selectedHour
        )
        _state.value = _state.value.copy(rankings = rankings)
    }

    fun fetchCatchReports() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingReports = true)
            val reports = scraper.fetchCatchReports()
            _state.value = _state.value.copy(catchReports = reports, isLoadingReports = false)
        }
    }

    fun getActiveSpecies(weatherData: OpenMeteoResponse?): List<FishingSpecies> {
        val month = java.time.LocalDate.now().monthValue
        val temp = weatherData?.hourly?.temperature_2m?.getOrElse(
            _state.value.selectedDay * 24 + _state.value.selectedHour
        ) { 12.0 } ?: 12.0
        return ScottishSpecies.all
            .filter { it.isInSeason(month) }
            .sortedByDescending { it.tempScore(temp) }
    }
}
