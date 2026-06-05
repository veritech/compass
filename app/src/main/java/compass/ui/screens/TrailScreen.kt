package compass.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.jonathan.compass.R
import compass.ui.CompassUiState
import compass.ui.components.map.TrailMap

@Composable
fun TrailScreen(
    state: CompassUiState,
    onBreadcrumbIntervalChange: (String) -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onLoadGpx: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val gpxPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()?.let(onLoadGpx)
    }

    Box(modifier = modifier.fillMaxSize()) {
        TrailMap(
            currentLatitude = state.latitude,
            currentLongitude = state.longitude,
            recordingPoints = if (state.breadcrumbsActive) state.breadcrumbPoints else emptyList(),
            loadedPoints = state.loadedTrailPoints,
            mapsApiKeyConfigured = state.mapsApiKeyConfigured,
            modifier = Modifier.fillMaxSize(),
        )

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            ColumnControls(
                state = state,
                onBreadcrumbIntervalChange = onBreadcrumbIntervalChange,
                onStartTracking = onStartTracking,
                onStopTracking = onStopTracking,
                onLoadTrail = { gpxPicker.launch(arrayOf("application/gpx+xml", "application/xml", "text/xml", "*/*")) },
            )
        }
    }
}

@Composable
private fun ColumnControls(
    state: CompassUiState,
    onBreadcrumbIntervalChange: (String) -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onLoadTrail: () -> Unit,
) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            value = state.breadcrumbIntervalSeconds,
            onValueChange = onBreadcrumbIntervalChange,
            label = { Text(stringResource(R.string.interval_seconds)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.breadcrumbsActive,
            singleLine = true,
        )
        Text(
            text = when {
                state.breadcrumbsActive -> stringResource(
                    R.string.recording_trail_points,
                    state.breadcrumbPoints.size,
                )
                state.loadedTrailPoints.isNotEmpty() -> stringResource(
                    R.string.loaded_trail_points,
                    state.loadedTrailPoints.size,
                )
                else -> stringResource(R.string.trail_idle_hint)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.breadcrumbsActive) {
                Button(
                    onClick = onStopTracking,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.stop_tracking))
                }
            } else {
                Button(
                    onClick = onStartTracking,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.start_tracking))
                }
            }
            Button(
                onClick = onLoadTrail,
                modifier = Modifier.weight(1f),
                enabled = !state.breadcrumbsActive,
            ) {
                Text(stringResource(R.string.load_trail))
            }
        }
    }
}
