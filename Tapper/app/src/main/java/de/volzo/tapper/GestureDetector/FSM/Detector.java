package de.volzo.tapper.GestureDetector.FSM;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import de.volzo.tapper.GestureDetector.GestureType;

import static de.volzo.tapper.GestureDetector.FSM.Detector.Shift.TAP;
import static de.volzo.tapper.GestureDetector.FSM.Detector.Shift.X;
import static de.volzo.tapper.GestureDetector.FSM.Detector.Shift.Y;
import static de.volzo.tapper.GestureDetector.FSM.Detector.Shift.Z;

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

    final FSMTap tapFSM = new FSMTap();
    final FSMDoubleTap fSMDoubleTap = new FSMDoubleTap();
    final FSMSideTap sideTapFSM = new FSMSideTap();
    final FSMPickUpDrop pickUpDropFSM = new FSMPickUpDrop();

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
            doubleTapRecognized = fSMDoubleTap.stateTransition(1 << TAP.getShift());
        } else {
            doubleTapRecognized = fSMDoubleTap.stateTransition(event);
        }

        boolean sideTapRecognized = sideTapFSM.stateTransition(event);
        if (sideTapRecognized) {
            sideTapFSM.reset();
        }

        boolean pickUpDropRecognized = pickUpDropFSM.stateTransition(event);

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

    public FSMTap getTapFSM() {
        return tapFSM;
    }

    public FSMDoubleTap getfSMDoubleTap() {
        return fSMDoubleTap;
    }

    public FSMSideTap getSideTapFSM() {
        return sideTapFSM;
    }
}

