package compass.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompassDial(
    headingDegrees: Float,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val northColor = Color(0xFFE74C3C)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            CardinalLabel("N", Alignment.TopCenter, northColor)
            CardinalLabel("E", Alignment.CenterEnd, onSurface)
            CardinalLabel("S", Alignment.BottomCenter, onSurface)
            CardinalLabel("W", Alignment.CenterStart, onSurface)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                drawCircle(
                    color = onSurface.copy(alpha = 0.15f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 4f),
                )

                rotate(degrees = headingDegrees, pivot = center) {
                    val needleTop = Offset(center.x, center.y - radius * 0.7f)
                    val needleBottom = Offset(center.x, center.y + radius * 0.25f)
                    drawLine(
                        color = northColor,
                        start = center,
                        end = needleTop,
                        strokeWidth = 10f,
                    )
                    drawLine(
                        color = onSurface.copy(alpha = 0.5f),
                        start = center,
                        end = needleBottom,
                        strokeWidth = 8f,
                    )
                    drawCircle(color = primary, radius = 12f, center = center)
                }
            }
        }

        Text(
            text = "${headingDegrees.toInt()}°",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 8.dp),
        )
    }
}

@Composable
private fun CardinalLabel(
    label: String,
    alignment: Alignment,
    color: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        contentAlignment = alignment,
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
