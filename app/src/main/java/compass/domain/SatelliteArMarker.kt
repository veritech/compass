package compass.domain

import androidx.compose.ui.geometry.Offset

data class SatelliteArMarker(
    val satellite: SatelliteInfo,
    val position: Offset,
)
