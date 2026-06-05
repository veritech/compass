package compass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonathan.compass.R
import compass.domain.Formatters
import compass.ui.CompassUiState
import compass.ui.components.CompassDial

private val IosBackground = Color(0xFF000000)
private val IosPrimaryText = Color(0xFFFFFFFF)
private val IosSecondaryText = Color(0x99EBEBF5)

@Composable
fun CompassHomeScreen(
    state: CompassUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IosBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!state.hasLocationPermission) {
            Text(
                text = stringResource(R.string.location_permission_required),
                color = Color(0xFFFF453A),
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        CompassDial(
            headingDegrees = state.headingDegrees,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = Formatters.formatHeading(state.headingDegrees),
            color = IosPrimaryText,
            fontSize = 48.sp,
            fontWeight = FontWeight.Thin,
        )
        Text(
            text = Formatters.cardinalDirection(state.headingDegrees),
            color = IosSecondaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (!state.hasGpsFix) {
                Text(
                    text = stringResource(R.string.waiting_for_gps),
                    color = IosSecondaryText,
                    fontSize = 14.sp,
                )
            }
            CoordinateLine(
                label = stringResource(R.string.latitude_label),
                value = state.latitude?.let(Formatters::formatCoordinate) ?: "—",
            )
            CoordinateLine(
                label = stringResource(R.string.longitude_label),
                value = state.longitude?.let(Formatters::formatCoordinate) ?: "—",
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        CoordinateLine(
            label = stringResource(R.string.altitude_label),
            value = state.altitude?.let(Formatters::formatAltitude) ?: "—",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CoordinateLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(text = label, color = IosSecondaryText, fontSize = 12.sp)
        Text(text = value, color = IosPrimaryText, fontSize = 17.sp, fontWeight = FontWeight.Normal)
    }
}
