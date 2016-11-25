package de.volzo.tapper.GestureDetector;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import de.volzo.tapper.GestureDetector.FSM.DoubleTapFSM;
import de.volzo.tapper.GestureDetector.FSM.SideTapFSM;
import de.volzo.tapper.GestureDetector.FSM.TapFSM;

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

    enum PickUpDropState {
        INIT, MOVING, DROP, END
    }

    Context context;

    final TapFSM tapFSM = new TapFSM();
    final DoubleTapFSM doubleTapFSM = new DoubleTapFSM();
    private SideTapFSM sideTapFSM = new SideTapFSM();

    public Detector() {}

    public Detector(Context context) {
        this();
        this.context = context;
    }

    public void dataUpdated(Quantile newX, Quantile newY, Quantile newZ) {

        int event = (newX.getMask() << X.getShift())
                | (newY.getMask() << Y.getShift())
                | (newZ.getMask() << Z.getShift());

        boolean tapRecognized = tapFSM.stateTransition(event);
        boolean doubleTapRecognized;

        if (tapRecognized) {
            tapFSM.reset();
            doubleTapRecognized = doubleTapFSM.stateTransition(1 << TAP.getShift());
        } else {
            doubleTapRecognized = doubleTapFSM.stateTransition(event);
        }

        boolean sideTapRecognized = sideTapFSM.stateTransition(event);
        if (sideTapRecognized) {
            sideTapFSM.reset();
        }

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

    public TapFSM getTapFSM() {
        return tapFSM;
    }

    public DoubleTapFSM getDoubleTapFSM() {
        return doubleTapFSM;
    }

    public SideTapFSM getSideTapFSM() {
        return sideTapFSM;
    }
}

