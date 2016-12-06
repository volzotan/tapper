package de.volzo.tapper.GestureDetector.DTW;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class Accellerometer extends StreamElement<double[]> implements SensorEventListener {

    /** the update frequency of the accelerometer in seconds */
    private static final double UPDATE_FREQUENCY = 0.01;

    public Accellerometer(Context context, Consumer<double[]> accellerometerConsumer) {

        super(accellerometerConsumer);

        // list all accelerometers and use the last one
        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor mSensor;
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            List<Sensor> accelSensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
            mSensor = accelSensors.get(accelSensors.size() - 1);
            Log.i(TAG, "Found sensor " + mSensor.getName());
        } else {
            mSensor = null;
            Log.i(TAG, "No Accelerometer found.");
        }

        // register Listener and set update rate
        mSensorManager.registerListener(this, mSensor, (int) (UPDATE_FREQUENCY * 1000 * 1000));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] data = event.values;
        super.passProcessedElement(new double[]{data[0], data[1], data[2]});
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "accelerometer accuracy has changed.");
    }
}
