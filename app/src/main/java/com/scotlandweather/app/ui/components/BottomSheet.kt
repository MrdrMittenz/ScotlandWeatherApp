package com.scotlandweather.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scotlandweather.app.data.model.*
import com.scotlandweather.app.ui.icons.FishingAppIcons
import com.scotlandweather.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherBottomSheet(
    selectedLocation: ScotlandLocation?,
    weather: OpenMeteoResponse?,
    solunar: SolunarData?,
    ranking: RankingData?,
    day: Int,
    hour: Int,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeIndex = day * 24 + hour
    if (selectedLocation == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        dragHandle = { Surface(
            modifier = Modifier.padding(top = 8.dp),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            shadowElevation = 0.dp
        ) { Box(modifier = Modifier.width(36.dp).height(4.dp)) } }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Gradient header bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.horizontalGradient(listOf(OceanBlue, OceanTeal)))
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedLocation.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (ranking != null) {
                        RankingBadge(rank = ranking.rank)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = if (isFavorite) GoldYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weather metrics
            if (weather != null) {
                HourlyForecastRow(weather = weather, timeIndex = timeIndex)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Solunar card
            if (solunar != null) {
                SolunarDetailCard(solunar = solunar)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Fishing conditions
            if (weather != null && solunar != null) {
                FishingConditionsCard(
                    weather = weather,
                    solunar = solunar,
                    timeIndex = timeIndex
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Hourly preview
            if (weather != null) {
                Text(
                    text = "Hourly Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                HourlyPreview(weather = weather, timeIndex = timeIndex, day = day)
            }
        }
    }
}

@Composable
private fun HourlyForecastRow(weather: OpenMeteoResponse, timeIndex: Int) {
    val h = weather.hourly ?: return
    if (timeIndex >= h.temperature_2m.size) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DetailMetric(
            icon = { FishingAppIcons.Temperature(modifier = Modifier.size(22.dp)) },
            value = "${h.temperature_2m[timeIndex].toInt()}°C",
            label = "Temperature"
        )
        DetailMetric(
            icon = { FishingAppIcons.Rain(modifier = Modifier.size(22.dp)) },
            value = "${h.precipitation[timeIndex]}mm",
            label = "Precipitation"
        )
        DetailMetric(
            icon = { FishingAppIcons.Wind(modifier = Modifier.size(22.dp)) },
            value = "${h.windspeed_10m[timeIndex].toInt()} km/h",
            label = "Wind"
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DetailMetric(
            icon = { FishingAppIcons.CloudCover(modifier = Modifier.size(22.dp)) },
            value = "${h.cloudcover[timeIndex]}%",
            label = "Cloud"
        )
        DetailMetric(
            icon = { FishingAppIcons.Pressure(modifier = Modifier.size(22.dp)) },
            value = "${h.pressure_msl[timeIndex].toInt()} hPa",
            label = "Pressure"
        )
        DetailMetric(
            icon = { FishingAppIcons.UvIndex(modifier = Modifier.size(22.dp)) },
            value = "${h.uv_index.getOrElse(timeIndex) { 0.0 }}",
            label = "UV Index"
        )
    }
}

@Composable
private fun DetailMetric(
    icon: @Composable () -> Unit,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        icon()
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SolunarDetailCard(solunar: SolunarData) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Solunar Data",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OceanTeal
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FishingAppIcons.MoonPhase(modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${"%.0f".format(solunar.moonIllumination * 100)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FishingAppIcons.FishActivity(modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${solunar.solunarRating}/100",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            solunar.solunarRating >= 90 -> ExcellentRating
                            solunar.solunarRating >= 75 -> GoodRating
                            else -> AverageRating
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            if (solunar.bestTimes.isNotEmpty()) {
                Text(
                    text = "Best Times:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                solunar.bestTimes.forEach { time ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FishingAppIcons.BestTimes(
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${time.start} - ${time.end} (${time.rating})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FishingConditionsCard(
    weather: OpenMeteoResponse,
    solunar: SolunarData,
    timeIndex: Int
) {
    val h = weather.hourly ?: return
    if (timeIndex >= h.temperature_2m.size) return

    val temp = h.temperature_2m[timeIndex]
    val wind = h.windspeed_10m[timeIndex]
    val prep = h.precipitation[timeIndex]
    val cloud = h.cloudcover.getOrElse(timeIndex) { 50 }
    val pressure = h.pressure_msl.getOrElse(timeIndex) { 1013.0 }

    val conditions = buildList {
        when {
            prep > 5 -> add("Heavy rain - fish may seek cover")
            prep > 1 -> add("Light rain - good surface feeding")
            cloud > 70 -> add("Overcast - fish are less cautious")
            cloud < 30 -> add("Bright sun - fish may be deep")
            else -> add("Fair conditions - good for fishing")
        }
        when {
            wind > 30 -> add("Strong wind - difficult casting")
            wind in 15.0..30.0 -> add("Moderate wind - good for drift fishing")
            wind < 5 -> add("Calm - ideal for still water")
        }
        if (pressure > 1015) add("High pressure - stable, good fishing")
        if (pressure < 1005) add("Low pressure - active feeding")
        if (temp in 12.0..20.0) add("Optimal temperature range")
        if (solunar.solunarRating >= 75) add("High solunar activity")
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FishingAppIcons.FishingScore(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Fishing Conditions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OceanTeal
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            conditions.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "\u2022",
                        style = MaterialTheme.typography.bodySmall,
                        color = OceanBlue,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HourlyPreview(weather: OpenMeteoResponse, timeIndex: Int, day: Int) {
    val wh = weather.hourly ?: return
    val startHour = (timeIndex - 3).coerceAtLeast(0)
    val endHour = (timeIndex + 4).coerceAtMost(wh.time.lastIndex)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (hi in startHour..endHour) {
            val isSelected = hi == timeIndex
            val timeLabel = try {
                wh.time[hi].substringAfter("T").take(5)
            } catch (_: Exception) {
                hi.toString().padStart(2, '0') + ":00"
            }
            val currentDay = hi / 24
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) OceanBlue else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = if (currentDay != day) 9.sp else 11.sp
                )
                if (currentDay != day) {
                    Text(
                        text = listOf("Today","Tom","+2").getOrElse(currentDay) { "D$currentDay" },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
                Text(
                    text = "${wh.temperature_2m.getOrElse(hi) { 0.0 }.toInt()}°",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FishingAppIcons.Rain(
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "${wh.precipitation.getOrElse(hi) { 0.0 }.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
