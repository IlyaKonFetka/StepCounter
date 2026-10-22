package com.example.stepcounter.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Text with a solid outline drawn in [outlineColor]. Rendered as two stacked
 * layers: a thick stroked copy underneath and a normal filled copy on top, so
 * the glyphs read as a distinct element even on top of the activity rings.
 */
@Composable
fun OutlinedText(
    text: String,
    style: TextStyle,
    color: Color,
    outlineColor: Color,
    modifier: Modifier = Modifier,
    outlineWidth: Dp = 3.dp,
    textAlign: TextAlign? = null
) {
    val outlinePx = with(LocalDensity.current) { outlineWidth.toPx() }
    Box(modifier = modifier) {
        Text(
            text = text,
            style = style.merge(
                TextStyle(
                    color = outlineColor,
                    drawStyle = Stroke(width = outlinePx, join = StrokeJoin.Round),
                    textAlign = textAlign ?: TextAlign.Unspecified
                )
            )
        )
        Text(
            text = text,
            style = style,
            color = if (color == Color.Unspecified) LocalContentColor.current else color,
            textAlign = textAlign
        )
    }
}
