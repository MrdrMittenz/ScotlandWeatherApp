package com.scotlandweather.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scotlandweather.app.ui.theme.OceanBlue
import com.scotlandweather.app.ui.theme.OceanTeal
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TimeSlider(
    selectedDay: Int,
    selectedHour: Int,
    onDaySelected: (Int) -> Unit,
    onHourSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = (0..23).toList()
    val dayLabels = (0..2).map { offset ->
        val date = LocalDate.now().plusDays(offset.toLong())
        when (offset) {
            0 -> "Today"
            1 -> "Tomorrow"
            else -> date.format(DateTimeFormatter.ofPattern("EEE\nd/M"))
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${selectedHour.toString().padStart(2, '0')}:00 - " +
                            LocalTime.of(selectedHour, 0)
                                .format(DateTimeFormatter.ofPattern("ha"))
                                .lowercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = OceanTeal,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val isSelected = index == selectedDay
                    val bgColor by animateColorAsState(
                        if (isSelected) OceanBlue else MaterialTheme.colorScheme.surface
                    )
                    val textColor by animateColorAsState(
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { onDaySelected(index) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        label.split("\n").forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val sliderPosition = selectedHour.toFloat() / 23f

            Slider(
                value = sliderPosition,
                onValueChange = { onHourSelected((it * 23f).toInt().coerceIn(0, 23)) },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = OceanBlue,
                    activeTrackColor = OceanBlue,
                    inactiveTrackColor = MaterialTheme.colorScheme.surface
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "00:00",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "12:00",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "23:00",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                hours.chunked(6).forEach { chunk ->
                    chunk.forEach { hour ->
                        val isSelected = hour == selectedHour
                        val isDawn = hour in 4..7
                        val isDay = hour in 8..17
                        val isDusk = hour in 18..20
                        val bgColor = when {
                            isSelected -> OceanBlue
                            isDay -> OceanBlue.copy(alpha = 0.1f)
                            isDawn || isDusk -> OceanTeal.copy(alpha = 0.08f)
                            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        }
                        val textColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(bgColor)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = hour.toString().padStart(2, '0'),
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
