package compass.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import compass.domain.SatelliteArProjector
import compass.domain.SatelliteInfo
import dev.jonathan.compass.R

private val InFixColor = Color(0xFF30D158)
private val VisibleColor = Color(0xCCFFFFFF)

@Composable
fun SatelliteArOverlay(
    satellites: List<SatelliteInfo>,
    rotationMatrix: FloatArray,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        satellites.forEach { satellite ->
            val position = SatelliteArProjector.project(
                satelliteAzimuthDegrees = satellite.azimuthDegrees,
                satelliteElevationDegrees = satellite.elevationDegrees,
                rotationMatrix = rotationMatrix,
                screenWidth = size.width,
                screenHeight = size.height,
            ) ?: return@forEach

            val color = if (satellite.usedInFix) InFixColor else VisibleColor
            val radius = if (satellite.usedInFix) 14f else 9f
            drawCircle(color = color, radius = radius, center = position)
            drawCircle(
                color = Color.White,
                radius = radius,
                center = position,
                style = Stroke(width = 2f),
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
