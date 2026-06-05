package compass.ui

import androidx.lifecycle.ViewModel
import compass.data.BreadcrumbTracker
import compass.domain.BearingCalculator
import compass.domain.Formatters
import compass.domain.GpxParser
import compass.domain.LocationSnapshot
import compass.provider.CompassProvider
import compass.provider.LocationProvider
import compass.provider.OrientationProvider
import compass.provider.SatelliteProvider

class CompassViewModel(
    private val locationProvider: LocationProvider,
    private val compassProvider: CompassProvider,
    private val satelliteProvider: SatelliteProvider,
    private val orientationProvider: OrientationProvider,
    private val breadcrumbTracker: BreadcrumbTracker,
) : ViewModel() {

    var uiState: CompassUiState = CompassUiState()
        private set

    private var latestLocation: LocationSnapshot? = null
    private var onStateChanged: ((CompassUiState) -> Unit)? = null

    fun setOnStateChanged(listener: (CompassUiState) -> Unit) {
        onStateChanged = listener
        listener(uiState)
    }

    fun onPermissionResult(granted: Boolean) {
        updateState { copy(hasLocationPermission = granted) }
        if (granted) {
            startSensors()
        } else {
            stopSensors()
        }
    }

    fun startSensors() {
        compassProvider.start { heading ->
            updateState { copy(headingDegrees = heading) }
        }

        orientationProvider.start { rotationMatrix ->
            updateState { copy(deviceRotationMatrix = rotationMatrix) }
        }

        locationProvider.start { snapshot ->
            latestLocation = snapshot
            breadcrumbTracker.onLocation(snapshot)
            updateState {
                copy(
                    latitude = snapshot.latitude.takeIf { snapshot.hasFix },
                    longitude = snapshot.longitude.takeIf { snapshot.hasFix },
                    altitude = snapshot.altitude.takeIf { snapshot.hasFix },
                    velocityKmh = Formatters.formatVelocityKmh(snapshot.speedMetersPerSecond),
                    hasGpsFix = snapshot.hasFix,
                    breadcrumbPoints = breadcrumbTracker.points(),
                    bearingToTarget = computeBearing(snapshot),
                )
            }
        }

        satelliteProvider.start { status ->
            updateState {
                copy(
                    satelliteCount = status.satellitesInFix,
                    satellites = status.satellites,
                )
            }
        }
    }

    fun stopSensors() {
        locationProvider.stop()
        compassProvider.stop()
        satelliteProvider.stop()
        orientationProvider.stop()
    }

    fun setTargetLocation(latitude: Double, longitude: Double) {
        updateState {
            copy(
                targetLatitude = latitude,
                targetLongitude = longitude,
                targetLatitudeInput = latitude.toString(),
                targetLongitudeInput = longitude.toString(),
                bearingToTarget = computeBearing(
                    latestLocation,
                    latitude.toString(),
                    longitude.toString(),
                ),
            )
        }
    }

    fun updateTargetLatitude(value: String) {
        val lat = value.toDoubleOrNull()
        updateState {
            copy(
                targetLatitudeInput = value,
                targetLatitude = lat,
                bearingToTarget = computeBearing(latestLocation, value, targetLongitudeInput),
            )
        }
    }

    fun updateTargetLongitude(value: String) {
        val lng = value.toDoubleOrNull()
        updateState {
            copy(
                targetLongitudeInput = value,
                targetLongitude = lng,
                bearingToTarget = computeBearing(latestLocation, targetLatitudeInput, value),
            )
        }
    }

    fun updateBreadcrumbInterval(value: String) {
        updateState { copy(breadcrumbIntervalSeconds = value) }
        value.toIntOrNull()?.let { breadcrumbTracker.configure(it) }
    }

    fun startBreadcrumbs() {
        val interval = uiState.breadcrumbIntervalSeconds.toIntOrNull() ?: return
        breadcrumbTracker.configure(interval)
        breadcrumbTracker.clear()
        breadcrumbTracker.start()
        updateState {
            copy(
                breadcrumbsActive = true,
                breadcrumbPoints = breadcrumbTracker.points(),
                loadedTrailPoints = emptyList(),
            )
        }
    }

    fun stopBreadcrumbs() {
        breadcrumbTracker.stop()
        updateState { copy(breadcrumbsActive = false) }
    }

    fun loadGpxTrail(content: String) {
        val points = GpxParser.parse(content)
        updateState { copy(loadedTrailPoints = points) }
    }

    fun clearLoadedTrail() {
        updateState { copy(loadedTrailPoints = emptyList()) }
    }

    override fun onCleared() {
        stopSensors()
        super.onCleared()
    }

    private fun updateState(transform: CompassUiState.() -> CompassUiState) {
        uiState = uiState.transform()
        onStateChanged?.invoke(uiState)
    }

    private fun computeBearing(snapshot: LocationSnapshot?): Double? =
        computeBearing(snapshot, uiState.targetLatitudeInput, uiState.targetLongitudeInput)

    private fun computeBearing(
        snapshot: LocationSnapshot?,
        latitudeInput: String,
        longitudeInput: String,
    ): Double? {
        if (snapshot == null || !snapshot.hasFix) return null
        val targetLat = latitudeInput.toDoubleOrNull() ?: return null
        val targetLng = longitudeInput.toDoubleOrNull() ?: return null
        return BearingCalculator.bearingDegrees(
            fromLatitude = snapshot.latitude,
            fromLongitude = snapshot.longitude,
            toLatitude = targetLat,
            toLongitude = targetLng,
        )
    }
}
