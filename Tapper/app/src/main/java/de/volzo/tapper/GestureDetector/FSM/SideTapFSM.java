package de.volzo.tapper.GestureDetector.FSM;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.volzo.tapper.GestureDetector.Detector;

import static de.volzo.tapper.GestureDetector.FSM.SideTapFSM.SideTapState.END;
import static de.volzo.tapper.GestureDetector.FSM.SideTapFSM.SideTapState.INIT;

/**
 * Created by tassilokarge on 25.11.16.
 */

public class SideTapFSM {

    public static long sideTapTimeout = 50;

    ScheduledExecutorService sideTapTimer;

    private SideTapState currentSideTapState;

    //transition event matrix
    //transition if exact mask (event ^ transition == 0)
    //1 = nothing, 2 = peak, 4 = strong peak, 8 = very strong peak
    //in hightest digit: 1 = timeout
    int[][] tapTransitionXNOR = new int[][]{
            /*INIT */{0x0000, 0x0111, 0x0000, 0x0000},
            /*START*/{0x0000, 0x0111, 0x0000, 0x0000},
            /*PEAK */{0x1000, 0x0000, 0x0000, 0x0111},
            /*END  */{0x0000, 0x0000, 0x0000, 0x0000}};

    //transition event matrix
    //transition if any overlap (event & transition != 0)
    //F = everything, E = more than nothing, C = more than peak, 8 = more than strong peak 6 = peak or strong peak
    int[][] tapTransitionAND = new int[][]{
            /*INIT */{0xFEEE, 0x0000, 0x0000, 0x0000},
            /*START*/{0xF88C, 0x0000, 0x0660, 0x0000},
            /*PEAK */{0xF88C, 0x0000, 0x0660, 0x0000},
            /*END  */{0xFFFF, 0x0000, 0x0000, 0x0000}};

    //Tasks executed on entering some state
    Runnable[] stateEnterAction = new Runnable[] {
            () -> {},
            () -> {},
            () -> {
                sideTapTimer = Executors.newSingleThreadScheduledExecutor();
                sideTapTimer.schedule(() -> this.stateTransition(
                        1 << Detector.Shift.TIMEOUT.getShift()),
                        sideTapTimeout, TimeUnit.MILLISECONDS);
            },
            () -> {}};

    //Tasks executed on exiting some state
    Runnable[] stateExitAction = new Runnable[] {
            () -> {},
            () -> {},
            () -> {
                sideTapTimer.shutdownNow();
                sideTapTimer = null;
            },
            () -> {}};

    public enum SideTapState {
        INIT(0), START(1), SIDETAP(2), END(3);

        private int state;

        SideTapState(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public static SideTapState fromValue(int value) {
            {
                switch (value) {
                    case 0:
                        return INIT;
                    case 1:
                        return START;
                    case 2:
                        return SIDETAP;
                    default:
                        return END;
                }
            }
        }
    }

    public final SideTapState finalState = END;

    public SideTapFSM() {
        this.currentSideTapState = INIT;
    }

    public void reset() {
        if (currentSideTapState != INIT) {
            stateExitAction[currentSideTapState.getState()].run();
        }
        this.currentSideTapState = INIT;
    }

    public boolean stateTransition(int event) {

        SideTapState formerTapState = null;
        SideTapState newTapState = null;

        for (int i = 0; i < 4; i++) {
            if ((event ^ tapTransitionXNOR[currentSideTapState.getState()][i]) == 0) {
                formerTapState = currentSideTapState;
                newTapState = SideTapState.fromValue(i);
                break;
            } else if ((event & tapTransitionAND[currentSideTapState.getState()][i]) != 0) {
                formerTapState = currentSideTapState;
                newTapState = SideTapState.fromValue(i);
                break;
            }
        }

        if (formerTapState != null && newTapState != currentSideTapState) {
            stateExitAction[formerTapState.getState()].run();
            currentSideTapState = newTapState;
            stateEnterAction[newTapState.getState()].run();
        }

        return currentSideTapState == finalState;
    }

    public SideTapState getCurrentSideTapState() {
        return currentSideTapState;
    }
}
