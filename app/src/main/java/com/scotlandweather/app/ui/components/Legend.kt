package com.scotlandweather.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scotlandweather.app.data.model.TileLayer
import com.scotlandweather.app.ui.theme.*

@Composable
fun Legend(
    layer: TileLayer,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = layer.displayName,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        when (layer) {
            TileLayer.TEMPERATURE -> TemperatureLegend()
            TileLayer.PRECIPITATION -> PrecipitationLegend()
            TileLayer.RAIN_CHANCE -> RainChanceLegend()
            TileLayer.WIND -> WindLegend()
            TileLayer.PRESSURE -> PressureLegend()
            TileLayer.CLOUD -> CloudLegend()
            TileLayer.UV -> UVLegend()
            TileLayer.SOLUNAR -> SolunarLegend()
            TileLayer.RANKING -> RankingLegend()
        }
    }
}

@Composable
private fun GradientBar(colors: List<Color>, labels: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(4.dp))
    ) {
        colors.forEachIndexed { _, color ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.85f
            )
        }
    }
}

@Composable
private fun TemperatureLegend() = GradientBar(
    colors = listOf(TempCold, TempCool, TempMild, TempWarm, TempHot, TempExtreme),
    labels = listOf("-5°", "5°", "12°", "18°", "25°", "35°+")
)

@Composable
private fun PrecipitationLegend() = GradientBar(
    colors = listOf(
        Color(0xFFE3F2FD), Color(0xFF90CAF9), Color(0xFF42A5F5),
        Color(0xFF1565C0), Color(0xFF0D47A1)
    ),
    labels = listOf("0mm", "2mm", "5mm", "10mm", "20mm+")
)

@Composable
private fun RainChanceLegend() = GradientBar(
    colors = listOf(
        Color(0xFFE3F2FD), Color(0xFF90CAF9), Color(0xFF42A5F5),
        Color(0xFF1565C0), Color(0xFF0D47A1)
    ),
    labels = listOf("0%", "25%", "50%", "75%", "100%")
)

@Composable
private fun WindLegend() = GradientBar(
    colors = listOf(
        Color(0xFFE8F5E9), Color(0xFFA5D6A7), Color(0xFFFFF176),
        Color(0xFFFF7043), Color(0xFFD32F2F)
    ),
    labels = listOf("0", "3", "6", "9", "14+ m/s")
)

@Composable
private fun PressureLegend() = GradientBar(
    colors = listOf(
        Color(0xFFD32F2F), Color(0xFFFF7043), Color(0xFFFFF176),
        Color(0xFFA5D6A7), Color(0xFF42A5F5)
    ),
    labels = listOf("980", "1000", "1013", "1025", "1040 hPa")
)

@Composable
private fun CloudLegend() = GradientBar(
    colors = listOf(
        Color(0xFF90CAF9), Color(0xFFB0BEC5), Color(0xFF78909C),
        Color(0xFF546E7A), Color(0xFF37474F)
    ),
    labels = listOf("0%", "25%", "50%", "75%", "100%")
)

@Composable
private fun UVLegend() = GradientBar(
    colors = listOf(
        Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFFFEB3B),
        Color(0xFFFF9800), Color(0xFFF44336)
    ),
    labels = listOf("0-2", "3-5", "6-7", "8-10", "11+")
)

@Composable
private fun SolunarLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LegendItem(color = MajorPeriod, label = "Major")
        LegendItem(color = MinorPeriod, label = "Minor")
        LegendItem(color = ExcellentRating, label = "Best")
        LegendItem(color = PoorRating, label = "Poor")
    }
}

@Composable
private fun RankingLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LegendItem(color = ExcellentRating, label = "Top")
        LegendItem(color = GoodRating, label = "Good")
        LegendItem(color = AverageRating, label = "Avg")
        LegendItem(color = PoorRating, label = "Poor")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
