package com.scotlandweather.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scotlandweather.app.data.model.TileLayer
import com.scotlandweather.app.ui.theme.OceanBlue

@Composable
fun LayerToggle(
    selectedLayer: TileLayer,
    onLayerSelected: (TileLayer) -> Unit,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        // Current layer button
        SmallFloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = layerIcon(selectedLayer),
                contentDescription = "Change layer",
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Surface(
                modifier = Modifier.padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    TileLayer.entries.forEach { layer ->
                        val isSelected = layer == selectedLayer
                        FilledTonalButton(
                            onClick = { onLayerSelected(layer) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = layerIcon(layer),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = layer.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun layerIcon(layer: TileLayer) = when (layer) {
    TileLayer.TEMPERATURE -> Icons.Filled.Thermostat
    TileLayer.PRECIPITATION -> Icons.Filled.WaterDrop
    TileLayer.RAIN_CHANCE -> Icons.Filled.WaterDrop
    TileLayer.WIND -> Icons.Filled.Air
    TileLayer.PRESSURE -> Icons.Filled.Speed
    TileLayer.CLOUD -> Icons.Filled.Cloud
    TileLayer.UV -> Icons.Filled.Lightbulb
    TileLayer.SOLUNAR -> Icons.Filled.DarkMode
    TileLayer.RANKING -> Icons.Filled.EmojiEvents
}
