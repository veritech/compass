package compass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.jonathan.compass.R
import compass.ui.CompassUiState

private val ScreenBackground = Color(0xFF000000)
private val PrimaryText = Color(0xFFFFFFFF)
private val SecondaryText = Color(0x99EBEBF5)
private val Accent = Color(0xFFFF3B30)

@Composable
fun CalibrateCompassScreen(
    state: CompassUiState,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = state.calibrationProgress.coerceIn(0f, 1f)
    val isComplete = progress >= 1f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isComplete) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(96.dp),
                    color = Accent,
                    strokeWidth = 6.dp,
                    trackColor = Color(0x33FFFFFF),
                    strokeCap = StrokeCap.Round,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(96.dp),
                    color = Accent,
                    strokeWidth = 6.dp,
                    trackColor = Color(0x33FFFFFF),
                    strokeCap = StrokeCap.Round,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isComplete) {
                    stringResource(R.string.calibrate_compass_complete_title)
                } else {
                    stringResource(R.string.calibrate_compass_title)
                },
                color = PrimaryText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isComplete) {
                    stringResource(R.string.calibrate_compass_complete_body)
                } else {
                    stringResource(R.string.calibrate_compass_instructions)
                },
                color = SecondaryText,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = Accent,
                trackColor = Color(0x33FFFFFF),
                strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.calibrate_compass_progress_percent, (progress * 100).toInt()),
                color = SecondaryText,
                fontSize = 14.sp,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = onDone,
                enabled = isComplete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = PrimaryText,
                    disabledContainerColor = Color(0x33FFFFFF),
                    disabledContentColor = Color(0x66FFFFFF),
                ),
            ) {
                Text(stringResource(R.string.calibrate_compass_done))
            }

            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(R.string.calibrate_compass_cancel),
                    color = SecondaryText,
                )
            }
        }
    }
}
