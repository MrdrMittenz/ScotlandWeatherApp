package com.scotlandweather.app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scotlandweather.app.data.model.*
import com.scotlandweather.app.ui.icons.FishingAppIcons
import com.scotlandweather.app.ui.theme.*

@Composable
fun LocationCard(
    name: String,
    weather: OpenMeteoResponse?,
    solunar: SolunarData?,
    ranking: RankingData?,
    hour: Int,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .animateContentSize()
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (ranking != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        RankingBadge(rank = ranking.rank)
                    }
                }

                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) GoldYellow else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weather data if available
            val wh = weather?.hourly
            if (wh != null && wh.temperature_2m.size > hour) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                    WeatherMetric(
                        icon = { FishingAppIcons.Temperature(modifier = Modifier.size(20.dp)) },
                        value = "${wh.temperature_2m[hour].toInt()}°",
                        label = "Temp"
                    )
                    WeatherMetric(
                        icon = { FishingAppIcons.Rain(modifier = Modifier.size(20.dp)) },
                        value = "${wh.precipitation[hour].toInt()}mm",
                        label = "Rain"
                    )
                    WeatherMetric(
                        icon = { FishingAppIcons.Wind(modifier = Modifier.size(20.dp)) },
                        value = "${wh.windspeed_10m[hour].toInt()}km",
                        label = "Wind"
                        )
                }
            }

            // Solunar data

            if (solunar != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FishingAppIcons.MoonPhase(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${"%.0f".format(solunar.moonIllumination * 100)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FishingAppIcons.FishActivity(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${solunar.fishActivity}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = when (solunar.fishActivity) {
                                "Excellent" -> ExcellentRating
                                "Good" -> GoodRating
                                "Average" -> AverageRating
                                else -> PoorRating
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetric(
    icon: @Composable () -> Unit,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        icon()
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
fun RankingBadge(rank: Int) {
    val color = when {
        rank == 1 -> GoldYellow
        rank <= 3 -> ExcellentRating
        rank <= 5 -> GoodRating
        rank <= 10 -> AverageRating
        else -> PoorRating
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
