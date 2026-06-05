package compass.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import compass.provider.OrientationListener
import compass.provider.OrientationProvider

class AndroidOrientationProvider(
    context: Context,
    private val sensorManager: SensorManager = context.getSystemService(SensorManager::class.java),
) : OrientationProvider {

    private var listener: OrientationListener? = null
    private val rotationMatrix = FloatArray(9)
    private val remappedMatrix = FloatArray(9)

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!CompassSensorTypes.isRotationVectorType(event.sensor.type)) return
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.remapCoordinateSystem(
                rotationMatrix,
                SensorManager.AXIS_X,
                SensorManager.AXIS_MINUS_Z,
                remappedMatrix,
            )
            listener?.onRotationMatrix(remappedMatrix.copyOf())
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    override fun start(listener: OrientationListener) {
        this.listener = listener
        val sensor = CompassSensorTypes.rotationVectorTypes
            .asSequence()
            .map { type -> sensorManager.getDefaultSensor(type) }
            .firstOrNull { it != null }
            ?: return

        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    override fun stop() {
        sensorManager.unregisterListener(sensorListener)
        listener = null
    }
}
