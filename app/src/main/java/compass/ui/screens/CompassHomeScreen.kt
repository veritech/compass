package compass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonathan.compass.R
import compass.domain.Formatters
import compass.domain.VelocityDisplay
import compass.ui.CompassUiState
import compass.ui.components.CompassDial

private val IosBackground = Color(0xFF000000)
private val IosPrimaryText = Color(0xFFFFFFFF)
private val IosSecondaryText = Color(0x99EBEBF5)
private const val METRIC_VALUE_FONT_SIZE_SP = 21.25f
private const val METRIC_LABEL_FONT_SIZE_SP = 15f

@Composable
fun CompassHomeScreen(
    state: CompassUiState,
    modifier: Modifier = Modifier,
) {
    var showMetersPerSecond by remember { mutableStateOf(false) }
    val showVelocity = VelocityDisplay.shouldShow(state.speedMetersPerSecond)

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
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        CompassDial(
            headingDegrees = state.headingDegrees,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (showVelocity) 20.dp else 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = Formatters.formatHeading(state.headingDegrees),
                color = IosPrimaryText,
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = Formatters.cardinalDirection(state.headingDegrees),
                color = IosSecondaryText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(if (showVelocity) 12.dp else 20.dp))

            if (!state.hasGpsFix) {
                Text(
                    text = stringResource(R.string.waiting_for_gps),
                    color = IosSecondaryText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(if (showVelocity) 4.dp else 8.dp))
            }

            Text(
                text = Formatters.formatLatLngLine(state.latitude, state.longitude),
                color = IosPrimaryText,
                fontSize = 18.75.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 360.dp),
            )

            Spacer(modifier = Modifier.height(if (showVelocity) 10.dp else 16.dp))

            Text(
                text = stringResource(R.string.altitude_label),
                color = IosSecondaryText,
                fontSize = METRIC_LABEL_FONT_SIZE_SP.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = state.altitude?.let(Formatters::formatAltitude) ?: "—",
                color = IosPrimaryText,
                fontSize = METRIC_VALUE_FONT_SIZE_SP.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
            )

            if (showVelocity) {
                val speed = state.speedMetersPerSecond!!
                val velocityText = if (showMetersPerSecond) {
                    Formatters.formatVelocityMps(speed)
                } else {
                    Formatters.formatVelocityKmh(speed)
                }
                val velocityDescription = stringResource(
                    R.string.velocity_toggle_content_description,
                    velocityText,
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = velocityText,
                    color = IosPrimaryText,
                    fontSize = METRIC_VALUE_FONT_SIZE_SP.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .semantics {
                            role = Role.Button
                            contentDescription = velocityDescription
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            showMetersPerSecond = !showMetersPerSecond
                        },
                )
            }
        }
    }
}
