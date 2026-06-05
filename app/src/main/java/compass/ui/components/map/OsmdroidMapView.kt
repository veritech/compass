package compass.ui.components.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import compass.data.OsmdroidConfig
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun OsmdroidMapView(
    modifier: Modifier = Modifier,
    onMapViewCreated: (MapView) -> Unit,
    onMapViewUpdate: (MapView) -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = rememberMapView(context)

    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onMapViewCreated(mapView)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = onMapViewUpdate,
    )
}

@Composable
private fun rememberMapView(context: Context): MapView {
    return remember {
        OsmdroidConfig.initialize(context)
        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(14.0)
        }
    }
}

internal fun MapView.zoomToPositions(positions: List<GeoPoint>, padding: Int = 120) {
    if (positions.isEmpty()) return
    if (positions.size == 1) {
        controller.setCenter(positions.first())
        controller.setZoom(15.0)
        return
    }
    val boundingBox = BoundingBox.fromGeoPoints(positions)
    post { zoomToBoundingBox(boundingBox, true, padding) }
}
