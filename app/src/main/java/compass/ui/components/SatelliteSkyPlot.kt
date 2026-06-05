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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compass.domain.SatelliteInfo
import compass.domain.SatelliteSkyPlotLayout
import dev.jonathan.compass.R

private val PlotBackground = Color(0xFF1C1C1E)
private val HorizonRing = Color(0x55FFFFFF)
private val CardinalText = Color(0x99EBEBF5)
private val InFixColor = Color(0xFF30D158)
private val VisibleColor = Color(0x66FFFFFF)

@Composable
fun SatelliteSkyPlot(
    satellites: List<SatelliteInfo>,
    deviceHeadingDegrees: Float,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .aspectRatio(1f)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            CardinalLabel("N", Alignment.TopCenter)
            CardinalLabel("E", Alignment.CenterEnd)
            CardinalLabel("S", Alignment.BottomCenter)
            CardinalLabel("W", Alignment.CenterStart)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f * 0.88f
                val center = Offset(size.width / 2f, size.height / 2f)

                drawCircle(color = PlotBackground, radius = radius, center = center)
                drawCircle(color = HorizonRing, radius = radius, center = center, style = Stroke(width = 2f))
                drawCircle(
                    color = HorizonRing.copy(alpha = 0.35f),
                    radius = radius * 0.5f,
                    center = center,
                    style = Stroke(width = 1f),
                )

                satellites.forEach { satellite ->
                    val position = SatelliteSkyPlotLayout.position(
                        azimuthDegrees = satellite.azimuthDegrees,
                        elevationDegrees = satellite.elevationDegrees,
                        deviceHeadingDegrees = deviceHeadingDegrees,
                        radius = radius,
                        centerX = center.x,
                        centerY = center.y,
                    )
                    drawCircle(
                        color = if (satellite.usedInFix) InFixColor else VisibleColor,
                        radius = if (satellite.usedInFix) 8f else 5f,
                        center = position,
                    )
                }

                drawCircle(color = Color.White, radius = 4f, center = center)
            }
        }

        Text(
            text = stringResource(R.string.satellite_sky_plot_legend),
            color = CardinalText,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun CardinalLabel(label: String, alignment: Alignment) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        contentAlignment = alignment,
    ) {
        Text(
            text = label,
            color = CardinalText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
