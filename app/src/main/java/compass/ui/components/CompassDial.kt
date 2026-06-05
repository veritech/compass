package compass.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

private val CompassWhite = Color(0xFFF2F2F2)
private val CompassRed = Color(0xFFFF3B30)
private val TickGray = Color(0x99FFFFFF)

@Composable
fun CompassDial(
    headingDegrees: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = -headingDegrees },
        ) {
            CardinalLabel("N", Alignment.TopCenter, CompassRed)
            CardinalLabel("E", Alignment.CenterEnd, CompassWhite)
            CardinalLabel("S", Alignment.BottomCenter, CompassWhite)
            CardinalLabel("W", Alignment.CenterStart, CompassWhite)
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f * 0.92f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                color = TickGray,
                radius = radius,
                center = center,
                style = Stroke(width = 2f),
            )

            rotate(degrees = -headingDegrees, pivot = center) {
                for (degree in 0 until 360 step 30) {
                    val isMajor = degree % 90 == 0
                    val tickLength = if (isMajor) radius * 0.12f else radius * 0.06f
                    val radians = Math.toRadians(degree.toDouble() - 90.0)
                    val outerX = center.x + radius * cos(radians).toFloat()
                    val outerY = center.y + radius * sin(radians).toFloat()
                    val innerX = center.x + (radius - tickLength) * cos(radians).toFloat()
                    val innerY = center.y + (radius - tickLength) * sin(radians).toFloat()
                    drawLine(
                        color = if (isMajor) CompassWhite else TickGray,
                        start = Offset(innerX, innerY),
                        end = Offset(outerX, outerY),
                        strokeWidth = if (isMajor) 3f else 1.5f,
                    )
                }

                val northRadians = Math.toRadians(-90.0)
                val northX = center.x + (radius - 28f) * cos(northRadians).toFloat()
                val northY = center.y + (radius - 28f) * sin(northRadians).toFloat()
                drawCircle(color = CompassRed, radius = 10f, center = Offset(northX, northY))
            }

            val indicator = Path().apply {
                moveTo(center.x, center.y - radius - 6f)
                lineTo(center.x - 12f, center.y - radius + 18f)
                lineTo(center.x + 12f, center.y - radius + 18f)
                close()
            }
            drawPath(indicator, CompassRed)
        }
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
            .padding(6.dp),
        contentAlignment = alignment,
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
