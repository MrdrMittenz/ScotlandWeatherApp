package com.scotlandweather.app.data.repository

import com.scotlandweather.app.data.model.ScotlandLocation

class LocationRepository {
    fun getAllLocations(): List<ScotlandLocation> = ScotlandLocation.ALL

    fun getLocationByName(name: String): ScotlandLocation? =
        ScotlandLocation.ALL.find { it.name == name }

    fun searchLocations(query: String): List<ScotlandLocation> =
        ScotlandLocation.ALL.filter { it.name.contains(query, ignoreCase = true) }
}
