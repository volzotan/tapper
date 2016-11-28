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


    //training data
    double maxXY = 0;
    double maxZ = 0;
    int dataCount = 0;

    // Number of available samples
    private static final int QUEUE_SIZE = 128;

    public CircularFifoQueue<Double> x = new CircularFifoQueue<>(QUEUE_SIZE);
    public CircularFifoQueue<Double> y = new CircularFifoQueue<>(QUEUE_SIZE);
    public CircularFifoQueue<Double> z = new CircularFifoQueue<>(QUEUE_SIZE);

    public CircularFifoQueue<Double> rawx = new CircularFifoQueue<>(QUEUE_SIZE);
    public CircularFifoQueue<Double> rawy = new CircularFifoQueue<>(QUEUE_SIZE);
    public CircularFifoQueue<Double> rawz = new CircularFifoQueue<>(QUEUE_SIZE);
    public Double[] rawax = new Double[QUEUE_SIZE];
    public Double[] raway = new Double[QUEUE_SIZE];
    public Double[] rawaz = new Double[QUEUE_SIZE];

    public Double[] ax = new Double[QUEUE_SIZE];
    public Double[] ay = new Double[QUEUE_SIZE];
    public Double[] az = new Double[QUEUE_SIZE];

    private Filter fx = new Filter();
    private Filter fy = new Filter();
    private Filter fz = new Filter();


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
        fz.cutoffThreshold  = 0.3;
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {

        //TODO: implement calibration method and delete this hack
        /*
        if (dataCount == 1000) {
            System.out.println("MaxXY: " + maxXY + ", MaxZ: " + maxZ);
            maxXY = 0;
            maxZ = 0;
            dataCount = 0;
        }

        maxXY = Math.max(maxXY, Math.max((double) event.values[0], (double) event.values[1]));
        maxZ = Math.max(maxZ, (double) event.values[2]);
        dataCount++;
        */
        rawx.add((double) event.values[0]);
        rawy.add((double) event.values[1]);
        rawz.add((double) event.values[2]);
        rawx.toArray(rawax);
        rawy.toArray(raway);
        rawz.toArray(rawaz);

        Double filtered_x = fx.workXY((double) event.values[0], rawax); // ( new value, previous values or null)
        Double filtered_y = fy.workXY((double) event.values[1], raway);
        Double filtered_z = fz.workZ((double) event.values[2], rawaz);

        x.add(filtered_x);
        y.add(filtered_y);
        z.add(filtered_z);

        x.toArray(ax);
        y.toArray(ay);
        z.toArray(az);

        gestureDetector.dataUpdated(
                Quantile.values()[Math.abs(filtered_x.intValue())],
                Quantile.values()[Math.abs(filtered_y.intValue())],
                Quantile.values()[Math.abs(filtered_z.intValue())]);

        // redraw the graph with new data by invalidating the View (Displayer)
        Displayer view = (Displayer) main.findViewById(R.id.displayView);
        view.invalidate();
    }

    public void discardAllData() {
        x.clear();
        y.clear();
        z.clear();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "accelerometer accuracy has changed.");
    }

}
