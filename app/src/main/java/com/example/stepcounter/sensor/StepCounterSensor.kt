package com.example.stepcounter.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * Thin wrapper over the hardware TYPE_STEP_COUNTER sensor. This sensor keeps
 * counting steps since the last device reboot in hardware, independent of
 * whether any app is currently listening — so we only need to listen while
 * the app is visible, and the first reading after (re)registering already
 * reflects every step taken while we weren't listening.
 */
class StepCounterSensor(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var listener: SensorEventListener? = null

    fun isAvailable(): Boolean = stepCounterSensor != null

    fun start(onRawSteps: (Int) -> Unit) {
        val sensor = stepCounterSensor ?: return
        stop()
        val newListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                onRawSteps(event.values[0].toInt())
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        listener = newListener
        sensorManager.registerListener(newListener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
    }
}
