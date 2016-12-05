package de.volzo.tapper.GestureDetector.FSM;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.volzo.tapper.GestureDetector.FSM.FSMDoubleTap.DoubleTapState.INIT;

public class FSMDoubleTap {

    public static long doubleTapTimeout = 1000;

    ScheduledExecutorService doubleTapTimer;

    private DoubleTapState currentDoubleTapState;

    //transition event matrix
    //transition if exact mask (event ^ transition == 0)
    //1 = nothing, 2 = peak, 4 = strong peak, 8 = very strong peak
    //in highest digit: 1 = timeout, 2 = tap, 3 = timeout or tap
    int[][] tapTransitionXNOR = new int[][]{
            /*INIT */{0x0000, 0x2000, 0x0000},
            /*TAP1 */{0x1000, 0x0111, 0x2000},
            /*TAP2 */{0x0000, 0x0000, 0x0000}};

    //transition event matrix
    //transition if any overlap (event & transition != 0)
    //F = everything, E = more than nothing, C = more than peak, 8 = more than strong peak
    //in highest digit: F = everything, E = everything despite timeout, D = everything despite tap,
    //C = everything despite timeout or tap
    int[][] tapTransitionAND = new int[][]{
            /*INIT */{0xDFFF, 0x0000, 0x0000},
            /*TAP1 */{0xCEEC, 0x0000, 0x0000},
            /*TAP2 */{0xFFFF, 0x0000, 0x0000}};

    //Tasks executed on entering some state
    Runnable[] stateEnterAction = new Runnable[] {
            () -> {},
            () -> {
                doubleTapTimer = Executors.newSingleThreadScheduledExecutor();
                doubleTapTimer.schedule(() -> this.stateTransition(
                        1 << FSMDetector.Shift.TIMEOUT.getShift()),
                        doubleTapTimeout, TimeUnit.MILLISECONDS);
            },
            () -> {}};

    //Tasks executed on exiting some state
    Runnable[] stateExitAction = new Runnable[] {
            () -> {},
            () -> {
                doubleTapTimer.shutdownNow();
                doubleTapTimer = null;
            },
            () -> {}};


    public enum DoubleTapState {
        INIT(0), TAP1(1), TAP2(2);

        private int state;

        DoubleTapState(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public static DoubleTapState fromValue(int value) {
            switch (value) {
                case 0:
                    return INIT;
                case 1:
                    return TAP1;
                default:
                    return TAP2;
            }
        }
    }

    public final DoubleTapState finalState = DoubleTapState.TAP2;

    public FSMDoubleTap() {
        this.currentDoubleTapState = INIT;
    }

    public void reset() {
        if (currentDoubleTapState != INIT) {
            stateExitAction[currentDoubleTapState.getState()].run();
        }
        this.currentDoubleTapState = INIT;
    }

    public boolean stateTransition(int event) {

        DoubleTapState formerDoubleTapState = null;
        DoubleTapState newDoubleTapState = null;

        for (int i = 0; i < tapTransitionAND.length; i++) {
            if ((event ^ tapTransitionXNOR[currentDoubleTapState.getState()][i]) == 0) {
                formerDoubleTapState = currentDoubleTapState;
                newDoubleTapState = DoubleTapState.fromValue(i);
                break;
            } else if ((event & tapTransitionAND[currentDoubleTapState.getState()][i]) != 0) {
                formerDoubleTapState = currentDoubleTapState;
                newDoubleTapState = DoubleTapState.fromValue(i);
                break;
            }
        }

        if (formerDoubleTapState != null && newDoubleTapState != currentDoubleTapState) {
            stateExitAction[formerDoubleTapState.getState()].run();
            currentDoubleTapState = newDoubleTapState;
            stateEnterAction[newDoubleTapState.getState()].run();
        }

        return currentDoubleTapState == finalState;
    }

    public DoubleTapState getCurrentDoubleTapState() {
        return currentDoubleTapState;
    }
}