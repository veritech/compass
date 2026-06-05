package compass.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.jonathan.compass.R
import compass.domain.Formatters
import compass.ui.CompassUiState
import compass.ui.components.map.BearingMap

@Composable
fun BearingScreen(
    state: CompassUiState,
    onTargetLatitudeChange: (String) -> Unit,
    onTargetLongitudeChange: (String) -> Unit,
    onMapTargetPicked: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        BearingMap(
            currentLatitude = state.latitude,
            currentLongitude = state.longitude,
            targetLatitude = state.targetLatitude,
            targetLongitude = state.targetLongitude,
            onMapClick = onMapTargetPicked,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.tap_map_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                    text = stringResource(
                        R.string.bearing_value,
                        state.bearingToTarget?.let { Formatters.formatHeading(it.toFloat()) } ?: "—",
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
    }
}
