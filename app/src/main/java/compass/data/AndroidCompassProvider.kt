package compass.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import compass.provider.CompassProvider
import compass.provider.HeadingListener
import kotlin.math.roundToInt

class AndroidCompassProvider(
    context: Context,
    private val sensorManager: SensorManager = context.getSystemService(SensorManager::class.java),
) : CompassProvider {

    private var listener: HeadingListener? = null
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
            val heading = ((azimuthDegrees + 360f) % 360f).roundToInt().toFloat()
            listener?.onHeading(heading)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    override fun start(listener: HeadingListener) {
        this.listener = listener
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        if (sensor != null) {
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun stop() {
        sensorManager.unregisterListener(sensorListener)
        listener = null
    }
}
