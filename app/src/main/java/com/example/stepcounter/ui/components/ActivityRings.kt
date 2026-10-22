package com.example.stepcounter.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class RingSpec(
    val progress: Float,
    val color: Color,
    val trackColor: Color
)

/**
 * Concentric activity rings (Apple-Watch style) drawn on a Canvas — no chart
 * library needed. Rings are ordered outer → inner. Progress animates in.
 */
@Composable
fun ActivityRings(
    rings: List<RingSpec>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 16.dp,
    ringGap: Dp = 6.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val animated = rings.map { ring ->
            animateFloatAsState(
                targetValue = ring.progress.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 900),
                label = "ring"
            ).value
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val gapPx = ringGap.toPx()
            rings.forEachIndexed { index, ring ->
                val inset = strokePx / 2f + index * (strokePx + gapPx)
                val diameter = size.minDimension - inset * 2f
                if (diameter <= 0f) return@forEachIndexed
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)

                drawArc(
                    color = ring.trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
                drawArc(
                    color = ring.color,
                    startAngle = -90f,
                    sweepAngle = 360f * animated[index],
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
            }
        }
        content()
    }
}
