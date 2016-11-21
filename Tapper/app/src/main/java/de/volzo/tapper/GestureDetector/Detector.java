package de.volzo.tapper.GestureDetector;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by volzotan on 11.11.16.
 */
public class Detector {

    Context context;

    public Detector(Context context) {
        this.context = context;
    }

    public void dataUpdated(DataCollector dataCollector, Double[] ax, Double[] ay, Double[] az, Double[] am) {

        // TODO
        // check if a gesture can be detected. If yes, call dataCollector.discardAllData()
        // to avoid multiple detections of the same gesture on the same data

        // run a low pass filter



//        Intent intent = new Intent("GESTURE_DETECTED");
//        intent.putExtra("GESTURE_TYPE", GestureType.DOUBLETAP.name());
//        intent.putExtra("GESTURE_INTENSITY", 1023);
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

}

