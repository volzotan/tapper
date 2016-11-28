package de.volzo.tapper.GestureDetector.FSM;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.volzo.tapper.GestureDetector.Detector;

import static de.volzo.tapper.GestureDetector.FSM.TapFSM.TapState.END;
import static de.volzo.tapper.GestureDetector.FSM.TapFSM.TapState.INIT;

public class TapFSM {

    public static long tapTimeout = 100;

    ScheduledExecutorService tapTimer;

    private TapState currentTapState;

    //transition event matrix
    //transition if exact mask (event ^ transition == 0)
    //1 = nothing, 2 = peak, 4 = strong peak, 8 = very strong peak
    //in hightest digit: 1 = timeout
    int[][] tapTransitionXNOR = new int[][]{
            /*INIT */{0x0000, 0x0111, 0x0000, 0x0000},
            /*START*/{0x0000, 0x0111, 0x0112, 0x0000},
            /*PEAK */{0x1000, 0x0000, 0x0112, 0x0111},
            /*END  */{0x0000, 0x0000, 0x0000, 0x0000}};

    //transition event matrix
    //transition if any overlap (event & transition != 0)
    //F = everything, E = more than nothing, C = more than peak, 8 = more than strong peak
    int[][] tapTransitionAND = new int[][]{
            /*INIT */{0xFEEE, 0x0000, 0x0000, 0x0000},
            /*START*/{0xFEEC, 0x0000, 0x0000, 0x0000},
            /*PEAK */{0xFEEC, 0x0000, 0x0000, 0x0000},
            /*END  */{0xFFFF, 0x0000, 0x0000, 0x0000}};

    //Tasks executed on entering some state
    Runnable[] stateEnterAction = new Runnable[] {
            () -> {},
            () -> {},
            () -> {
                tapTimer = Executors.newSingleThreadScheduledExecutor();
                tapTimer.schedule(() -> this.stateTransition(
                        1 << Detector.Shift.TIMEOUT.getShift()),
                        tapTimeout, TimeUnit.MILLISECONDS);
            },
            () -> {}};

    //Tasks executed on exiting some state
    Runnable[] stateExitAction = new Runnable[] {
            () -> {},
            () -> {},
            () -> {
                tapTimer.shutdownNow();
                tapTimer = null;
            },
            () -> {}};

    public enum TapState {
        INIT(0), START(1), PEAK(2), END(3);

        private int state;

        TapState(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public static TapState fromValue(int value) {
            {
                switch (value) {
                    case 0:
                        return INIT;
                    case 1:
                        return START;
                    case 2:
                        return PEAK;
                    default:
                        return END;
                }
            }
        }
    }

    public final TapState finalState = END;

    public TapFSM() {
        this.currentTapState = TapState.INIT;
    }

    public void reset() {
        if (currentTapState != INIT) {
            stateExitAction[currentTapState.getState()].run();
        }
        this.currentTapState = INIT;
    }

    public boolean stateTransition(int event) {

        TapState formerTapState = null;
        TapState newTapState = null;

        for (int i = 0; i < tapTransitionAND.length; i++) {
            if ((event ^ tapTransitionXNOR[currentTapState.getState()][i]) == 0) {
                formerTapState = currentTapState;
                newTapState = TapState.fromValue(i);
                break;
            } else if ((event & tapTransitionAND[currentTapState.getState()][i]) != 0) {
                formerTapState = currentTapState;
                newTapState = TapState.fromValue(i);
                break;
            }
        }

        if (formerTapState != null && newTapState != currentTapState) {
            stateExitAction[formerTapState.getState()].run();
            currentTapState = newTapState;
            stateEnterAction[newTapState.getState()].run();
        }

        return currentTapState == finalState;
    }

    public TapState getCurrentTapState() {
        return currentTapState;
    }
}