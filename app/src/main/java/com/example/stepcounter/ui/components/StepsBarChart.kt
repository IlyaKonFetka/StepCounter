package com.example.stepcounter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class BarChartEntry(val label: String, val value: Int)

/**
 * Minimal bar chart drawn on a Canvas, with a dashed goal line. Bars that meet
 * the goal are drawn in the accent color, the rest in a soft tint.
 */
@Composable
fun StepsBarChart(
    entries: List<BarChartEntry>,
    goal: Int,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    barHeight: androidx.compose.ui.unit.Dp = 150.dp
) {
    val softColor = accent.copy(alpha = 0.28f)
    val goalLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    val maxValue = (entries.maxOfOrNull { it.value } ?: 0)
        .coerceAtLeast(goal)
        .coerceAtLeast(1)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
        ) {
            val barCount = entries.size.coerceAtLeast(1)
            val gap = size.width * 0.025f
            val barWidth = (size.width - gap * (barCount + 1)) / barCount
            val radius = CornerRadius(barWidth / 2.4f, barWidth / 2.4f)

            if (goal in 1..maxValue) {
                val goalY = size.height * (1f - goal.toFloat() / maxValue)
                drawLine(
                    color = goalLineColor,
                    start = Offset(0f, goalY),
                    end = Offset(size.width, goalY),
                    strokeWidth = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f))
                )
            }

            entries.forEachIndexed { index, entry ->
                val h = size.height * (entry.value.toFloat() / maxValue)
                val left = gap + index * (barWidth + gap)
                val top = size.height - h
                drawRoundRect(
                    color = if (entry.value >= goal && goal > 0) accent else softColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, h.coerceAtLeast(6f)),
                    cornerRadius = radius
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            entries.forEach { entry ->
                Text(
                    text = entry.label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
