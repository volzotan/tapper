package de.volzo.tapper.GestureDetector.DTW;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import de.volzo.tapper.GestureDetector.GestureType;

/**
 * Created by tassilokarge on 05.12.16.
 */

public class DTWDetector {

    private final Accellerometer accel;

    private Double[] windowX;
    private Double[] windowY;

    private Context context;

    public DTWDetector(Context context) {

        this.context = context;

        //Pipeline: accelerometer -> filter -> windower -> gestureAnalyzer -> foundGesture

        //init pipeline in reverse order
        GestureAnalyzer gestureAnalyzer = new GestureAnalyzer(this::foundGesture);

        Windower windowerX = new Windower((Double[] values) -> this.windowX = values);
        Windower windowerY = new Windower((Double[] values) -> this.windowY = values);
        Windower windowerZ = new Windower((Double[] values) -> gestureAnalyzer.updateData(windowX, windowY, values));

        Filter filterX = new Filter(windowerX::updateData);
        Filter filterY = new Filter(windowerY::updateData);
        Filter filterZ = new Filter(windowerZ::updateData);

        this.accel = new Accellerometer(context, (double[] reading) -> {
            filterX.dataUpdate(reading[0]);
            filterY.dataUpdate(reading[1]);
            filterZ.dataUpdate(reading[2]);
        });
    }

    void foundGesture(GestureType type) {

        //output everything but the "NOTHING" gesture
        if (type == GestureType.NOTHING) { return; }

        Intent intent = new Intent("GESTURE_DETECTED");
        intent.putExtra("GESTURE_TYPE", GestureType.DOUBLETAP.name());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
