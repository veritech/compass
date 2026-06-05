package compass.domain

import androidx.compose.ui.geometry.Offset

/** Low-pass filter for projected satellite screen positions. */
class SatelliteArPositionSmoother(
    private val alpha: Float = DEFAULT_ALPHA,
) {
    private val smoothed = mutableMapOf<String, Offset>()

    fun update(
        satellites: List<SatelliteInfo>,
        project: (SatelliteInfo) -> Offset?,
    ): List<SatelliteArMarker> {
        val activeKeys = mutableSetOf<String>()
        val markers = mutableListOf<SatelliteArMarker>()

        for (satellite in satellites) {
            val projected = project(satellite) ?: continue
            val key = satellite.trackKey()
            activeKeys.add(key)
            val position = smoothPosition(key, projected)
            markers.add(SatelliteArMarker(satellite = satellite, position = position))
        }

        smoothed.keys.retainAll(activeKeys)
        return markers
    }

    fun reset() {
        smoothed.clear()
    }

    private fun smoothPosition(key: String, target: Offset): Offset {
        val previous = smoothed[key]
        if (previous == null) {
            smoothed[key] = target
            return target
        }

        val blended = Offset(
            x = previous.x + alpha * (target.x - previous.x),
            y = previous.y + alpha * (target.y - previous.y),
        )
        smoothed[key] = blended
        return blended
    }

    companion object {
        const val DEFAULT_ALPHA = 0.2f
    }
}
