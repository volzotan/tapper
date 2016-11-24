package de.volzo.tapper.GestureDetector;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import static de.volzo.tapper.GestureDetector.Detector.Shift.TAP;
import static de.volzo.tapper.GestureDetector.Detector.Shift.X;
import static de.volzo.tapper.GestureDetector.Detector.Shift.Y;
import static de.volzo.tapper.GestureDetector.Detector.Shift.Z;

/**
 * Created by volzotan on 11.11.16.
 */
public class Detector {

    public enum Shift {
        TAP (13), TIMEOUT (12), X (8), Y (4), Z (0);

        private int shift;

        Shift(int shift) {
            this.shift = shift;
        }

        public int getShift() {
            return shift;
        }
    }

    enum SideTapState {
        INIT, SIDETAP, END
    }

    enum PickUpDropState {
        INIT, MOVING, DROP, END
    }

    Context context;

    final TapFSM tapFSM = new TapFSM();
    final DoubleTapFSM doubleTapFSM = new DoubleTapFSM();

    public Detector() {}

    public Detector(Context context) {
        this();
        this.context = context;
    }

    public void dataUpdated(Quantile newX, Quantile newY, Quantile newZ) {

        int event = (newX.getMask() << X.getShift())
                & (newY.getMask() << Y.getShift())
                & (newZ.getMask() << Z.getShift());

        boolean tapRecognized = tapFSM.stateTransition(event);
        boolean doubleTapRecognized;

        if (tapRecognized) {
            doubleTapRecognized = doubleTapFSM.stateTransition(1 << TAP.getShift());
        } else {
            doubleTapRecognized = doubleTapFSM.stateTransition(event);
        }

        boolean sideTapRecognized = false;// sideTapFSM.stateTransition(event);
        boolean pickUpDropRecognized = false;// pickUpDropFSM.stateTransition(event);

        handleRecognizedGestures(doubleTapRecognized, sideTapRecognized, pickUpDropRecognized);
    }

    public void handleRecognizedGestures(boolean doubleTapRecognized, boolean sideTapRecognized, boolean pickUpDropRecognized) {
        if (doubleTapRecognized || sideTapRecognized || pickUpDropRecognized) {
            Intent intent = new Intent("GESTURE_DETECTED");
            if (doubleTapRecognized) {
                intent.putExtra("GESTURE_TYPE", GestureType.DOUBLETAP.name());
            } else if (sideTapRecognized) {
                intent.putExtra("GESTURE_TYPE", GestureType.SIDETAP.name());
            } else if (pickUpDropRecognized) {
                intent.putExtra("GESTURE_TYPE", GestureType.PICKUPDROP.name());
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public Context getContext() {
        return context;
    }
}

