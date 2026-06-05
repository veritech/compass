package compass.domain

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

object SatelliteSkyPlotLayout {
    fun position(
        azimuthDegrees: Float,
        elevationDegrees: Float,
        deviceHeadingDegrees: Float,
        radius: Float,
        centerX: Float,
        centerY: Float,
    ): Offset {
        val relativeAzimuth = normalizeDegrees(azimuthDegrees - deviceHeadingDegrees)
        val distance = ((90f - elevationDegrees.coerceIn(0f, 90f)) / 90f) * radius
        val radians = Math.toRadians((relativeAzimuth - 90f).toDouble())
        return Offset(
            x = centerX + distance * cos(radians).toFloat(),
            y = centerY + distance * sin(radians).toFloat(),
        )
    }

    fun normalizeDegrees(degrees: Float): Float = ((degrees % 360f) + 360f) % 360f
}
