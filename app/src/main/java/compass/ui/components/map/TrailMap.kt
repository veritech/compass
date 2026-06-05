package compass.ui.components.map

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import compass.domain.BreadcrumbPoint

@Composable
fun TrailMap(
    currentLatitude: Double?,
    currentLongitude: Double?,
    recordingPoints: List<BreadcrumbPoint>,
    loadedPoints: List<BreadcrumbPoint>,
    mapsApiKeyConfigured: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!mapsApiKeyConfigured) {
        MapsMissingKeyMessage(modifier)
        return
    }

    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(51.5, -0.12), 14f)
    }

    val allPoints = remember(recordingPoints, loadedPoints) {
        recordingPoints + loadedPoints
    }

    LaunchedEffect(currentLatitude, currentLongitude, allPoints) {
        val positions = buildList {
            if (currentLatitude != null && currentLongitude != null) {
                add(LatLng(currentLatitude, currentLongitude))
            }
            allPoints.forEach { point ->
                add(LatLng(point.latitude, point.longitude))
            }
        }
        when {
            positions.isEmpty() -> Unit
            positions.size == 1 -> cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(positions.first(), 15f),
            )
            else -> cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(
                    positions.fold(LatLngBounds.builder()) { builder, latLng ->
                        builder.include(latLng)
                    }.build(),
                    120,
                ),
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
        ),
    ) {
        if (loadedPoints.size >= 2) {
            Polyline(
                points = loadedPoints.map { LatLng(it.latitude, it.longitude) },
                color = Color(0xFF34C759),
                width = 8f,
            )
        }
        if (recordingPoints.size >= 2) {
            Polyline(
                points = recordingPoints.map { LatLng(it.latitude, it.longitude) },
                color = Color(0xFF0A84FF),
                width = 8f,
            )
        }

        loadedPoints.forEachIndexed { index, point ->
            NumberedMarker(
                context = context,
                index = index + 1,
                point = point,
                color = Color(0xFF34C759),
            )
        }
        recordingPoints.forEachIndexed { index, point ->
            NumberedMarker(
                context = context,
                index = index + 1,
                point = point,
                color = Color(0xFF0A84FF),
            )
        }
    }
}

@Composable
fun BearingMap(
    currentLatitude: Double?,
    currentLongitude: Double?,
    targetLatitude: Double?,
    targetLongitude: Double?,
    mapsApiKeyConfigured: Boolean,
    onMapClick: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!mapsApiKeyConfigured) {
        MapsMissingKeyMessage(modifier)
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(51.5, -0.12), 14f)
    }

    LaunchedEffect(currentLatitude, currentLongitude, targetLatitude, targetLongitude) {
        val positions = buildList {
            if (currentLatitude != null && currentLongitude != null) {
                add(LatLng(currentLatitude, currentLongitude))
            }
            if (targetLatitude != null && targetLongitude != null) {
                add(LatLng(targetLatitude, targetLongitude))
            }
        }
        when {
            positions.isEmpty() -> Unit
            positions.size == 1 -> cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(positions.first(), 14f),
            )
            else -> cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(
                    positions.fold(LatLngBounds.builder()) { builder, latLng ->
                        builder.include(latLng)
                    }.build(),
                    140,
                ),
            )
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = true),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = true,
        ),
        onMapClick = { latLng -> onMapClick(latLng.latitude, latLng.longitude) },
    ) {
        if (targetLatitude != null && targetLongitude != null) {
            Marker(
                state = MarkerState(LatLng(targetLatitude, targetLongitude)),
                title = "Target",
            )
        }
        if (currentLatitude != null && currentLongitude != null &&
            targetLatitude != null && targetLongitude != null
        ) {
            Polyline(
                points = listOf(
                    LatLng(currentLatitude, currentLongitude),
                    LatLng(targetLatitude, targetLongitude),
                ),
                color = Color(0xFFFF9500),
                width = 6f,
            )
        }
    }
}

@Composable
private fun NumberedMarker(
    context: Context,
    index: Int,
    point: BreadcrumbPoint,
    color: Color,
) {
    val icon = remember(index, point, color) {
        NumberedMarkerIcon.create(context, index, color)
    }
    Marker(
        state = MarkerState(LatLng(point.latitude, point.longitude)),
        icon = icon,
        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
    )
}

@Composable
private fun MapsMissingKeyMessage(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Add MAPS_API_KEY to local.properties to enable maps.",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
