package com.scotlandweather.app.data.tile

import android.graphics.*
import com.scotlandweather.app.data.model.OpenMeteoResponse
import com.scotlandweather.app.data.model.RankingData
import com.scotlandweather.app.data.model.SeaLochs
import com.scotlandweather.app.data.model.SolunarData
import com.scotlandweather.app.data.model.TileLayer

object WeatherTileRenderer {
    private val locationCoords = mapOf(
        "Aberdeen" to (57.148 to -2.094), "Ayr" to (55.458 to -4.629),
        "Dundee" to (56.462 to -2.971), "Edinburgh" to (55.953 to -3.189),
        "Glasgow" to (55.864 to -4.252), "Inverness" to (57.477 to -4.225),
        "Perth" to (56.395 to -3.434), "Stirling" to (56.117 to -3.940),
        "Oban" to (56.415 to -5.472), "Fort William" to (56.820 to -5.105),
        "Ullapool" to (57.899 to -5.159), "Thurso" to (58.593 to -3.522),
        "Wick" to (58.440 to -3.089), "Kirkwall" to (58.982 to -2.959),
        "Stornoway" to (58.209 to -6.386), "Portree" to (57.413 to -6.194),
        "Mallaig" to (57.006 to -5.831), "Campbeltown" to (55.426 to -5.606),
        "Dumfries" to (55.072 to -3.605), "Stranraer" to (54.904 to -5.027),
        "Lerwick" to (60.155 to -1.145), "Isle of Lewis" to (58.212 to -6.619)
    )

    fun getLayerValue(
        data: OpenMeteoResponse,
        layer: TileLayer,
        day: Int,
        hour: Int,
        solunarData: Map<String, SolunarData>? = null,
        rankings: List<RankingData>? = null,
        name: String? = null
    ): Double {
        val h = data.hourly ?: return 0.0
        val idx = (day * 24 + hour).coerceIn(0, h.temperature_2m.lastIndex)
        return when (layer) {
            TileLayer.TEMPERATURE -> h.temperature_2m.getOrElse(idx) { 10.0 }
            TileLayer.PRECIPITATION -> h.precipitation.getOrElse(idx) { 0.0 }
            TileLayer.RAIN_CHANCE -> h.precipitation_probability.getOrElse(idx) { 0 }.toDouble()
            TileLayer.WIND -> h.windspeed_10m.getOrElse(idx) { 10.0 } / 3.6
            TileLayer.PRESSURE -> h.pressure_msl.getOrElse(idx) { 1013.0 }
            TileLayer.CLOUD -> h.cloudcover.getOrElse(idx) { 50.0 }.toDouble()
            TileLayer.UV -> h.uv_index.getOrElse(idx) { 3.0 }
            TileLayer.SOLUNAR -> {
                if (name != null && solunarData != null) {
                    solunarData[name]?.solunarRating?.toDouble() ?: 50.0
                } else 50.0
            }
            TileLayer.RANKING -> {
                if (name != null && rankings != null) {
                    rankings.find { it.locationName == name }?.totalScore ?: 50.0
                } else 50.0
            }
        }
    }

    fun getColorForValue(value: Double, layer: TileLayer): Int {
        val normalized = when (layer) {
            TileLayer.TEMPERATURE -> ((value + 10) / 40).coerceIn(0.0, 1.0)
            TileLayer.PRECIPITATION -> (value / 20).coerceIn(0.0, 1.0)
            TileLayer.RAIN_CHANCE -> (value / 100).coerceIn(0.0, 1.0)
            TileLayer.WIND -> (value / 15).coerceIn(0.0, 1.0)
            TileLayer.PRESSURE -> ((value - 980) / 60).coerceIn(0.0, 1.0)
            TileLayer.CLOUD -> (value / 100).coerceIn(0.0, 1.0)
            TileLayer.UV -> (value / 11).coerceIn(0.0, 1.0)
            TileLayer.SOLUNAR -> (value / 100).coerceIn(0.0, 1.0)
            TileLayer.RANKING -> (value / 100).coerceIn(0.0, 1.0)
        }

        return when (layer) {
            TileLayer.TEMPERATURE -> Color.rgb(
                (255 * normalized).toInt(),
                (255 * (1 - kotlin.math.abs(normalized - 0.5) * 2)).toInt(),
                (255 * (1 - normalized)).toInt()
            )
            TileLayer.PRECIPITATION -> Color.rgb(
                30, (100 + 155 * (1 - normalized)).toInt(), 255
            )
            TileLayer.RAIN_CHANCE -> Color.rgb(
                50, (50 + 205 * (1 - normalized)).toInt(), 255
            )
            TileLayer.WIND -> Color.rgb(
                (180 + 75 * normalized).toInt(),
                (150 * (1 - normalized)).toInt(), 50
            )
            else -> Color.rgb(
                (255 * normalized).toInt(),
                (100 + 155 * (1 - normalized)).toInt(), 50
            )
        }
    }

    fun formatValue(value: Double, layer: TileLayer): String = when (layer) {
        TileLayer.TEMPERATURE -> "${value.toInt()}°C"
        TileLayer.PRECIPITATION -> "${"%.1f".format(value)}mm"
        TileLayer.RAIN_CHANCE -> "${value.toInt()}%"
        TileLayer.WIND -> "${"%.1f".format(value)}m/s"
        TileLayer.PRESSURE -> "${value.toInt()}hPa"
        TileLayer.CLOUD -> "${value.toInt()}%"
        TileLayer.UV -> "${"%.1f".format(value)}"
        TileLayer.SOLUNAR -> "${value.toInt()}"
        TileLayer.RANKING -> "${value.toInt()}"
    }

    fun getCoords(name: String): Pair<Double, Double> =
        locationCoords[name]
            ?: SeaLochs.all.find { it.name == name }?.let { it.lat to it.lon }
            ?: (56.0 to -4.0)
}
