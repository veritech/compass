package compass.ui.components.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import compass.domain.BreadcrumbPoint
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun TrailMap(
    currentLatitude: Double?,
    currentLongitude: Double?,
    recordingPoints: List<BreadcrumbPoint>,
    loadedPoints: List<BreadcrumbPoint>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val persistentOverlays = remember { mutableStateOf<List<Overlay>>(emptyList()) }

    OsmdroidMapView(
        modifier = modifier,
        onMapViewCreated = { mapView ->
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            if (persistentOverlays.value.isEmpty()) {
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
                locationOverlay.enableMyLocation()
                mapView.overlays.add(locationOverlay)
                persistentOverlays.value = listOf(locationOverlay)
            }
        },
        onMapViewUpdate = { mapView ->
            val keep = persistentOverlays.value.toSet()
            mapView.overlays.removeAll { overlay -> overlay !in keep }

            if (loadedPoints.size >= 2) {
                mapView.overlays.add(
                    createPolyline(
                        mapView = mapView,
                        points = loadedPoints,
                        color = android.graphics.Color.parseColor("#34C759"),
                    ),
                )
            }
            if (recordingPoints.size >= 2) {
                mapView.overlays.add(
                    createPolyline(
                        mapView = mapView,
                        points = recordingPoints,
                        color = android.graphics.Color.parseColor("#0A84FF"),
                    ),
                )
            }

            loadedPoints.forEachIndexed { index, point ->
                NumberedMarkerIcon.addNumberedMarker(
                    mapView = mapView,
                    context = context,
                    index = index + 1,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    color = Color(0xFF34C759),
                )
            }
            recordingPoints.forEachIndexed { index, point ->
                NumberedMarkerIcon.addNumberedMarker(
                    mapView = mapView,
                    context = context,
                    index = index + 1,
                    latitude = point.latitude,
                    longitude = point.longitude,
                    color = Color(0xFF0A84FF),
                )
            }

            val positions = buildList {
                if (currentLatitude != null && currentLongitude != null) {
                    add(GeoPoint(currentLatitude, currentLongitude))
                }
                recordingPoints.forEach { add(GeoPoint(it.latitude, it.longitude)) }
                loadedPoints.forEach { add(GeoPoint(it.latitude, it.longitude)) }
            }
            mapView.zoomToPositions(positions)
            mapView.invalidate()
        },
    )
}

@Composable
fun BearingMap(
    currentLatitude: Double?,
    currentLongitude: Double?,
    targetLatitude: Double?,
    targetLongitude: Double?,
    onMapClick: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val persistentOverlays = remember { mutableStateOf<List<Overlay>>(emptyList()) }
    val clickHandler = remember { mutableStateOf<(Double, Double) -> Unit>({ _, _ -> }) }
    clickHandler.value = onMapClick

    OsmdroidMapView(
        modifier = modifier,
        onMapViewCreated = { mapView ->
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            if (persistentOverlays.value.isEmpty()) {
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
                locationOverlay.enableMyLocation()
                val mapEventsOverlay = MapEventsOverlay(
                    object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(geoPoint: GeoPoint?): Boolean {
                            geoPoint?.let { clickHandler.value(it.latitude, it.longitude) }
                            return true
                        }

                        override fun longPressHelper(geoPoint: GeoPoint?): Boolean = false
                    },
                )
                mapView.overlays.add(locationOverlay)
                mapView.overlays.add(0, mapEventsOverlay)
                persistentOverlays.value = listOf(locationOverlay, mapEventsOverlay)
            }
        },
        onMapViewUpdate = { mapView ->
            val keep = persistentOverlays.value.toSet()
            mapView.overlays.removeAll { overlay -> overlay !in keep }

            if (targetLatitude != null && targetLongitude != null) {
                addTargetMarker(
                    mapView = mapView,
                    context = context,
                    latitude = targetLatitude,
                    longitude = targetLongitude,
                )
            }

            if (currentLatitude != null && currentLongitude != null &&
                targetLatitude != null && targetLongitude != null
            ) {
                mapView.overlays.add(
                    createPolyline(
                        mapView = mapView,
                        points = listOf(
                            BreadcrumbPoint(currentLatitude, currentLongitude, 0.0, 0L),
                            BreadcrumbPoint(targetLatitude, targetLongitude, 0.0, 0L),
                        ),
                        color = android.graphics.Color.parseColor("#FF9500"),
                    ),
                )
            }

            val positions = buildList {
                if (currentLatitude != null && currentLongitude != null) {
                    add(GeoPoint(currentLatitude, currentLongitude))
                }
                if (targetLatitude != null && targetLongitude != null) {
                    add(GeoPoint(targetLatitude, targetLongitude))
                }
            }
            mapView.zoomToPositions(positions, padding = 140)
            mapView.invalidate()
        },
    )
}

private fun addTargetMarker(
    mapView: org.osmdroid.views.MapView,
    context: android.content.Context,
    latitude: Double,
    longitude: Double,
) {
    val marker = org.osmdroid.views.overlay.Marker(mapView)
    marker.position = GeoPoint(latitude, longitude)
    marker.icon = android.graphics.drawable.BitmapDrawable(
        context.resources,
        NumberedMarkerIcon.createBitmap(context, "T", Color(0xFFFF9500)),
    )
    marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_CENTER)
    mapView.overlays.add(marker)
}

private fun createPolyline(
    mapView: org.osmdroid.views.MapView,
    points: List<BreadcrumbPoint>,
    color: Int,
): Polyline {
    val polyline = Polyline(mapView)
    polyline.setPoints(points.map { GeoPoint(it.latitude, it.longitude) })
    polyline.outlinePaint.color = color
    polyline.outlinePaint.strokeWidth = 8f
    return polyline
}
