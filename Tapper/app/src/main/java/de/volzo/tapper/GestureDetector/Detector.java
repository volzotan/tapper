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

    public void dataUpdated(Double[] ax, Double[] ay, Double[] az, Double[] am) {
        // TODO
        // detect gesture

        Intent intent = new Intent("GESTURE_DETECTED");
        intent.putExtra("GESTURE_TYPE", GestureType.DOUBLETAP.name());
        intent.putExtra("GESTURE_INTENSITY", 1023);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}

