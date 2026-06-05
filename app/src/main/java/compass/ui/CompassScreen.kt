package compass.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.jonathan.compass.R
import compass.domain.Formatters
import compass.ui.components.CompassDial

@Composable
fun CompassScreen(
    state: CompassUiState,
    onTargetLatitudeChange: (String) -> Unit,
    onTargetLongitudeChange: (String) -> Unit,
    onBreadcrumbIntervalChange: (String) -> Unit,
    onStartBreadcrumbs: () -> Unit,
    onStopBreadcrumbs: () -> Unit,
    onExportGpx: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!state.hasLocationPermission) {
            Text(
                text = stringResource(R.string.location_permission_required),
                color = MaterialTheme.colorScheme.error,
            )
        }

        CompassDial(headingDegrees = state.headingDegrees)

        LocationCard(state)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.bearing_label), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = state.targetLatitudeInput,
                    onValueChange = onTargetLatitudeChange,
                    label = { Text(stringResource(R.string.target_latitude)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.targetLongitudeInput,
                    onValueChange = onTargetLongitudeChange,
                    label = { Text(stringResource(R.string.target_longitude)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = state.bearingToTarget?.let { Formatters.formatHeading(it.toFloat()) } ?: "—",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.breadcrumbs_label), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = state.breadcrumbIntervalSeconds,
                    onValueChange = onBreadcrumbIntervalChange,
                    label = { Text(stringResource(R.string.interval_seconds)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.breadcrumbsActive,
                )
                Text(text = stringResource(R.string.breadcrumb_count_format, state.breadcrumbCount))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onStartBreadcrumbs,
                        enabled = !state.breadcrumbsActive,
                    ) {
                        Text(stringResource(R.string.start_tracking))
                    }
                    Button(
                        onClick = onStopBreadcrumbs,
                        enabled = state.breadcrumbsActive,
                    ) {
                        Text(stringResource(R.string.stop_tracking))
                    }
                    Button(onClick = onExportGpx, enabled = state.breadcrumbCount > 0) {
                        Text(stringResource(R.string.export_gpx))
                    }
                }
            }
        }

        state.gpxExport?.let { gpx ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "GPX", style = MaterialTheme.typography.titleMedium)
                    Text(text = gpx, style = MaterialTheme.typography.bodySmall)
                    Button(
                        onClick = {
                            copyToClipboard(context, gpx)
                        },
                    ) {
                        Text("Copy GPX")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LocationCard(state: CompassUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (!state.hasGpsFix) {
                Text(text = stringResource(R.string.waiting_for_gps))
            }
            MetricRow(
                label = stringResource(R.string.latitude_label),
                value = state.latitude?.let(Formatters::formatCoordinate) ?: "—",
            )
            MetricRow(
                label = stringResource(R.string.longitude_label),
                value = state.longitude?.let(Formatters::formatCoordinate) ?: "—",
            )
            MetricRow(
                label = stringResource(R.string.altitude_label),
                value = state.altitude?.let(Formatters::formatAltitude) ?: "—",
            )
            MetricRow(
                label = stringResource(R.string.velocity_label),
                value = state.velocityKmh,
            )
            MetricRow(
                label = stringResource(R.string.satellites_label),
                value = stringResource(R.string.satellite_count_format, state.satelliteCount),
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("gpx", text))
}
