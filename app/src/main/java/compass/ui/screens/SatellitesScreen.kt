package compass.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.jonathan.compass.R
import compass.ui.CompassUiState
import compass.ui.components.CameraPreview
import compass.ui.components.SatelliteArOverlay
import compass.ui.components.SatelliteArPermissionMessage
import compass.ui.components.SatelliteSkyPlot

private enum class SatelliteViewMode {
    AR,
    SKY_PLOT,
}

@Composable
fun SatellitesScreen(
    state: CompassUiState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var viewModeIndex by remember { mutableIntStateOf(SatelliteViewMode.AR.ordinal) }
    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        cameraGranted = granted
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            SegmentedButton(
                selected = viewModeIndex == SatelliteViewMode.AR.ordinal,
                onClick = {
                    viewModeIndex = SatelliteViewMode.AR.ordinal
                    if (!cameraGranted) {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text(stringResource(R.string.satellite_view_ar))
            }
            SegmentedButton(
                selected = viewModeIndex == SatelliteViewMode.SKY_PLOT.ordinal,
                onClick = { viewModeIndex = SatelliteViewMode.SKY_PLOT.ordinal },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text(stringResource(R.string.satellite_view_sky_plot))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            when (SatelliteViewMode.entries[viewModeIndex]) {
                SatelliteViewMode.AR -> {
                    if (cameraGranted) {
                        CameraPreview(modifier = Modifier.fillMaxSize())
                        SatelliteArOverlay(
                            satellites = state.satellites,
                            rotationMatrix = state.deviceRotationMatrix,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        SatelliteArPermissionMessage()
                    }
                }
                SatelliteViewMode.SKY_PLOT -> {
                    SatelliteSkyPlot(
                        satellites = state.satellites,
                        deviceHeadingDegrees = state.headingDegrees,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCC000000))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.satellite_visible_count_format,
                    state.satellites.size,
                    state.satelliteCount,
                ),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (SatelliteViewMode.entries[viewModeIndex] == SatelliteViewMode.AR) {
                Text(
                    text = stringResource(R.string.satellite_ar_hint),
                    color = Color(0x99EBEBF5),
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Text(
                    text = stringResource(R.string.satellite_sky_plot_legend),
                    color = Color(0x99EBEBF5),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
