package de.volzo.tapper.GestureDetector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.List;

import de.volzo.tapper.MainActivity;
import de.volzo.tapper.R;

/**
 * Created by volzotan on 11.11.16.
 */
public class DataCollector implements SensorEventListener {

    private static final String TAG = DataCollector.class.getName();

    private MainActivity main;

    private Detector gestureDetector;

    private SensorManager mSensorManager;
    private Sensor mSensor;

    // Number of available samples
    private static final int QUEUE_SIZE = 128;

    public CircularFifoQueue<Double> x = new CircularFifoQueue<Double>(QUEUE_SIZE);
    public CircularFifoQueue<Double> y = new CircularFifoQueue<Double>(QUEUE_SIZE);
    public CircularFifoQueue<Double> z = new CircularFifoQueue<Double>(QUEUE_SIZE);
    public CircularFifoQueue<Double> m = new CircularFifoQueue<Double>(QUEUE_SIZE);

    public Double[] ax = new Double[QUEUE_SIZE];
    public Double[] ay = new Double[QUEUE_SIZE];
    public Double[] az = new Double[QUEUE_SIZE];
    public Double[] am = new Double[QUEUE_SIZE];

    private Filter fx = new Filter();
    private Filter fy = new Filter();
    private Filter fz = new Filter();
    private Filter fm = new Filter();


    // in seconds
    private double UPDATE_FREQUENCY = 0.01;

    public DataCollector(MainActivity main, Detector gestureDetector) {
        this.main = main;
        this.gestureDetector = gestureDetector;

        // list all accelerometers and use the last one
        mSensorManager = (SensorManager) main.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            List<Sensor> accelSensors = mSensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION);
            for (int i = 0; i < accelSensors.size(); i++) {
                mSensor = accelSensors.get(i);
                Log.i(TAG, "Found sensor " + mSensor.getName().toString());
                break;
            }
        } else {
            Log.i(TAG, "No Accelerometer found.");
        }

        // register Listener and set update rate
        mSensorManager.registerListener(this, mSensor, (int) Math.round(this.UPDATE_FREQUENCY * 1000 * 1000));

        // configure the filter objects
        fz.lowpass_alpha    = 0.4;
        fz.cutoff_threshold = 0.3;

        fm.lowpass = false;
        fm.cutoff = false;
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        int filtered_x = fx.work((double) event.values[0], x.size() == 0 ? null : x.get(x.size()-1)); // ( new value, previous value or null)
        int filtered_y = fy.work((double) event.values[1], y.size() == 0 ? null : y.get(y.size()-1));
        int filtered_z = fz.work((double) event.values[2], z.size() == 0 ? null : z.get(z.size()-1));

        x.add(filtered_x);
        y.add(filtered_y);
        z.add(filtered_z);
        m.add(fm.work(Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2))));

        x.toArray(ax);
        y.toArray(ay);
        z.toArray(az);
        m.toArray(am);

        //gestureDetector.dataUpdated(this, ax, ay, az, am);

        gestureDetector.dataUpdated(Quantile.values()[Math.abs(filtered_x)], Quantile.values()[Math.abs(filtered_y)], Quantile.values()[Math.abs(filtered_z)]);

        // redraw the graph with new data by invalidating the View (Displayer)
        Displayer view = (Displayer) main.findViewById(R.id.displayView);
        view.invalidate();
    }

    public void discardAllData() {
        x.clear();
        y.clear();
        z.clear();
        m.clear();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "accelerometer accuracy has changed.");
    }

}
