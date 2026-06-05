package compass.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compass.domain.SatelliteArOverlayEngine
import compass.domain.SatelliteInfo
import dev.jonathan.compass.R

private val InFixColor = Color(0xFF30D158)
private val InFixGlowColor = Color(0x6630D158)
private val VisibleColor = Color(0x99FFFFFF)
private val TrackingLabelBackground = Color(0xCC1A3D22)
private val VisibleLabelBackground = Color(0x99000000)

@Composable
fun SatelliteArOverlay(
    satellites: List<SatelliteInfo>,
    rotationMatrix: FloatArray,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val engine = remember { SatelliteArOverlayEngine() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val markers = engine.markers(
            satellites = satellites,
            rotationMatrix = rotationMatrix,
            screenWidth = size.width,
            screenHeight = size.height,
        )

        markers.forEach { marker ->
            val tracking = marker.satellite.usedInFix
            val color = if (tracking) InFixColor else VisibleColor
            val radius = if (tracking) 16f else 8f
            val center = marker.position

            if (tracking) {
                drawCircle(color = InFixGlowColor, radius = radius + 10f, center = center)
            }

            drawCircle(color = color, radius = radius, center = center)
            drawCircle(
                color = if (tracking) InFixColor else Color.White,
                radius = radius,
                center = center,
                style = Stroke(width = if (tracking) 3f else 1.5f),
            )

            val label = marker.satellite.displayLabel()
            val textStyle = TextStyle(
                color = if (tracking) Color.White else color,
                fontSize = if (tracking) 12.sp else 11.sp,
                fontWeight = if (tracking) FontWeight.Bold else FontWeight.SemiBold,
            )
            val textLayout = textMeasurer.measure(label, style = textStyle)
            val paddingX = 8f
            val paddingY = 4f
            val labelWidth = textLayout.size.width + paddingX * 2
            val labelHeight = textLayout.size.height + paddingY * 2
            val labelTopLeft = Offset(
                x = center.x - labelWidth / 2f,
                y = center.y + radius + 8f,
            )

            drawRoundRect(
                color = if (tracking) TrackingLabelBackground else VisibleLabelBackground,
                topLeft = labelTopLeft,
                size = Size(labelWidth, labelHeight),
                cornerRadius = CornerRadius(6f, 6f),
            )
            if (tracking) {
                drawRoundRect(
                    color = InFixColor,
                    topLeft = labelTopLeft,
                    size = Size(labelWidth, labelHeight),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 2f),
                )
            }
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    x = labelTopLeft.x + paddingX,
                    y = labelTopLeft.y + paddingY,
                ),
            )
        }
    }
}

@Composable
fun SatelliteArPermissionMessage(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.camera_permission_required),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
    }
}
