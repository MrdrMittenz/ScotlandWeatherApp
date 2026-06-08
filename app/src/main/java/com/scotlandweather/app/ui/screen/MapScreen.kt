package com.scotlandweather.app.ui.screen

import android.graphics.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.scotlandweather.app.data.model.MapState
import com.scotlandweather.app.data.model.RankingData
import com.scotlandweather.app.data.model.SeaLochs
import com.scotlandweather.app.data.model.ScotlandLocation
import com.scotlandweather.app.data.model.SolunarData
import com.scotlandweather.app.data.model.TileLayer
import com.scotlandweather.app.data.tile.WeatherTileRenderer
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.overlay.Overlay
import java.util.concurrent.atomic.AtomicReference

@Composable
fun MapScreen(
    state: MapState,
    onLocationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val customMarkers = remember { mutableListOf<Marker>() }

    // Init osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidTileCache = context.cacheDir
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                mapViewRef = this
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                isTilesScaledToDpi = true

                val scotlandCenter = GeoPoint(57.0, -4.0)
                controller.setCenter(scotlandCenter)
                controller.setZoom(6.5)
                minZoomLevel = 5.0
                maxZoomLevel = 12.0

                val wOverlay = WeatherCircleOverlay(
                    initialData = state.hourlyData,
                    initialLayer = state.selectedLayer,
                    initialDay = state.selectedDay,
                    initialHour = state.selectedHour,
                    initialSolunar = state.solunarData,
                    initialRankings = state.rankings
                )
                overlays.add(wOverlay)

                ScotlandLocation.ALL.forEach { location ->
                    val marker = Marker(this).apply {
                        position = GeoPoint(location.lat, location.lon)
                        title = location.name
                        snippet = ""
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setOnMarkerClickListener { _, _ ->
                            onLocationClick(location.name)
                            true
                        }
                    }
                    overlays.add(marker)
                }

                // Sea loch overlay (green bookmarks with names)
                val lochOverlay = LochLabelOverlay()
                overlays.add(lochOverlay)

                // Sea loch markers (click targets)
                SeaLochs.all.forEach { loch ->
                    val marker = Marker(this).apply {
                        position = GeoPoint(loch.lat, loch.lon)
                        title = loch.name
                        snippet = loch.region
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setOnMarkerClickListener { _, _ ->
                            onLocationClick(loch.name)
                            true
                        }
                    }
                    overlays.add(marker)
                }
            }
        },
        update = { mv ->
            // Update weather overlay
            val overlay = mv.overlays.filterIsInstance<WeatherCircleOverlay>().firstOrNull()
            if (overlay != null) {
                overlay.weatherData = state.hourlyData
                overlay.layer = state.selectedLayer
                overlay.day = state.selectedDay
                overlay.hour = state.selectedHour
                overlay.solunarData = state.solunarData
                overlay.rankings = state.rankings
            }
            // Remove old custom markers
            customMarkers.forEach { mv.overlays.remove(it) }
            customMarkers.clear()
            // Add custom location markers
            state.customLocations.forEach { (name, loc) ->
                val marker = Marker(mv).apply {
                    position = GeoPoint(loc.lat, loc.lon)
                    title = loc.name
                    snippet = buildString {
                        if (loc.region.isNotEmpty()) append(loc.region)
                        if (loc.country.isNotEmpty()) {
                            if (isNotEmpty()) append(", ")
                            append(loc.country)
                        }
                    }
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    setOnMarkerClickListener { _, _ ->
                        onLocationClick(name)
                        true
                    }
                }
                mv.overlays.add(marker)
                customMarkers.add(marker)
            }
            mv.invalidate()
        }
    )
}

class LochLabelOverlay : Overlay() {
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(220, 0, 128, 128) // Ocean teal
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.argb(255, 0, 180, 180)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(255, 0, 180, 180)
        textSize = 28f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val textBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(180, 0, 0, 0)
    }

    override fun draw(canvas: Canvas, mapView: org.osmdroid.views.MapView, shadow: Boolean) {
        if (shadow) return
        val projection = mapView.projection
        val point = android.graphics.Point()
        val density = mapView.resources.displayMetrics.density
        val r = 10f * density

        SeaLochs.all.forEach { loch ->
            projection.toPixels(GeoPoint(loch.lat, loch.lon), point)
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), r + 1f, strokePaint)
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), r, fillPaint)

            val label = loch.name
            val textSize = textPaint.textSize
            val labelWidth = textPaint.measureText(label)
            canvas.drawRoundRect(
                point.x.toFloat() - labelWidth / 2f - 6f,
                point.y.toFloat() + r + 2f,
                point.x.toFloat() + labelWidth / 2f + 6f,
                point.y.toFloat() + r + textSize + 6f,
                6f, 6f, textBgPaint
            )
            canvas.drawText(label, point.x.toFloat(), point.y.toFloat() + r + textSize, textPaint)
        }
    }
}

class WeatherCircleOverlay(
    initialData: Map<String, com.scotlandweather.app.data.model.OpenMeteoResponse>,
    initialLayer: TileLayer,
    initialDay: Int,
    initialHour: Int,
    initialSolunar: Map<String, SolunarData> = emptyMap(),
    initialRankings: List<RankingData> = emptyList()
) : Overlay() {
    private val dataRef = AtomicReference(initialData)
    private val layerRef = AtomicReference(initialLayer)
    private val dayRef = AtomicReference(initialDay)
    private val hourRef = AtomicReference(initialHour)
    private val solunarRef = AtomicReference(initialSolunar)
    private val rankingsRef = AtomicReference(initialRankings)

    var weatherData: Map<String, com.scotlandweather.app.data.model.OpenMeteoResponse>
        get() = dataRef.get()
        set(v) { dataRef.set(v) }
    var layer: TileLayer
        get() = layerRef.get()
        set(v) { layerRef.set(v) }
    var day: Int
        get() = dayRef.get()
        set(v) { dayRef.set(v) }
    var hour: Int
        get() = hourRef.get()
        set(v) { hourRef.set(v) }
    var solunarData: Map<String, SolunarData>
        get() = solunarRef.get()
        set(v) { solunarRef.set(v) }
    var rankings: List<RankingData>
        get() = rankingsRef.get()
        set(v) { rankingsRef.set(v) }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        alpha = 220
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        color = Color.argb(220, 255, 255, 255)
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        alpha = 40
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(80, 0, 0, 0)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(255, 255, 255, 255)
        textSize = 32f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val textBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(150, 0, 0, 0)
    }

    override fun draw(canvas: Canvas, mapView: org.osmdroid.views.MapView, shadow: Boolean) {
        if (shadow) return
        val snapshotData = dataRef.get()
        if (snapshotData.isEmpty()) return
        val snapshotLayer = layerRef.get()
        val snapshotDay = dayRef.get()
        val snapshotHour = hourRef.get()
        val projection = mapView.projection
        val point = android.graphics.Point()
        val density = mapView.resources.displayMetrics.density

        val snapshotSolunar = solunarRef.get()
        val snapshotRankings = rankingsRef.get()

        snapshotData.forEach { (name, data) ->
            val (lat, lon) = WeatherTileRenderer.getCoords(name)
            projection.toPixels(GeoPoint(lat, lon), point)
            val value = WeatherTileRenderer.getLayerValue(data, snapshotLayer, snapshotDay, snapshotHour, snapshotSolunar, snapshotRankings, name)
            val color = WeatherTileRenderer.getColorForValue(value, snapshotLayer)

            val baseRadius = 18f * density
            val zoomFactor = (mapView.zoomLevelDouble.toFloat() / 8f).coerceIn(0.6f, 2.0f)
            val r = (baseRadius * zoomFactor).coerceIn(12f * density, 50f * density)

            fillPaint.color = color
            glowPaint.color = Color.argb(60, Color.red(color).toFloat().toInt().coerceIn(0, 255), Color.green(color).toFloat().toInt().coerceIn(0, 255), Color.blue(color).toFloat().toInt().coerceIn(0, 255))
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), r * 1.6f, glowPaint)
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), r + 2f, shadowPaint)
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), r, fillPaint)
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), r, strokePaint)

            val label = WeatherTileRenderer.formatValue(value, snapshotLayer)
            val textSize = textPaint.textSize
            val labelWidth = textPaint.measureText(label)
            canvas.drawRoundRect(
                point.x.toFloat() - labelWidth / 2f - 6f,
                point.y.toFloat() + r + 2f,
                point.x.toFloat() + labelWidth / 2f + 6f,
                point.y.toFloat() + r + textSize + 6f,
                6f, 6f, textBgPaint
            )
            canvas.drawText(label, point.x.toFloat(), point.y.toFloat() + r + textSize, textPaint)
        }
    }
}


